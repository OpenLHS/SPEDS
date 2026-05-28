package ca.griis.speds.integration.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.application.api.dto.MsgType;
import ca.griis.js2p.gen.speds.application.api.dto.Service;
import ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.internal.ApplicationHostFactory;
import ca.griis.speds.application.internal.domain.ApplicationInterface;
import ca.griis.speds.application.internal.verification.DefaultInterfaceChecker;
import ca.griis.speds.integration.security.CryptographyServiceCreator;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpedsApplicationTest {
  private static final IRIFactory iriFactory = IRIFactory.iriImplementation();

  private static final String pgaId = UUID.randomUUID().toString();
  private static final String sourceCode = "source";
  private static final String destinationCode = "destination";
  private static final IRI sourceIri = iriFactory.create("https://localhost:8081");
  private static final IRI destinationIri = iriFactory.create("https://localhost:8082");

  private String parametersSrc;
  private String parametersDest;
  private CryptographyService cryptographyService;
  private ProjectService projectService;
  private ObjectMapper mapper;
  private ApplicationHost initiatorHost;
  private ApplicationHost peerHost;
  private AppHostEvent initiatorIdus;
  private AppHostEvent peerIdus;
  private ExecutorService executor;
  private DefaultInterfaceChecker checker;

  @BeforeEach
  public void setUp() throws Exception {
    checker = new DefaultInterfaceChecker();
    executor = Executors.newFixedThreadPool(50);
    cryptographyService = CryptographyServiceCreator.createCryptographyService();
    mapper = new ObjectMapper();

    InitInParamsDto params = new InitInParamsDto(ApplicationParamsCreator.createSourceParams());
    parametersSrc = mapper.writeValueAsString(params);

    InitInParamsDto params2 =
        new InitInParamsDto(ApplicationParamsCreator.createDestinationParams());
    parametersDest = mapper.writeValueAsString(params2);

    projectService = ApplicationParamsCreator.createProjectService(pgaId, sourceCode,
        destinationCode, sourceIri, destinationIri);

    this.initiatorIdus = new AppHostEvent(mapper);
    this.peerIdus = new AppHostEvent(mapper);

    ApplicationHostFactory factory =
        new ApplicationHostFactory(cryptographyService, projectService);
    initiatorHost = factory.init(parametersSrc, initiatorIdus, checker);
    peerHost = factory.init(parametersDest, peerIdus, checker);
  }

  @AfterEach
  public void afterEach() throws Exception {
    initiatorHost.close();
    peerHost.close();

    executor.shutdown();
  }

  @Test
  public void nominal() throws Exception {
    String msgId = UUID.randomUUID().toString();
    final Map<String, Object> requestMap = Map.of("request", "content");
    final String requestContent = mapper.writeValueAsString(requestMap);

    ApplicationInterface appInterface =
        new ApplicationInterface(
            Service.DELEGATE,
            ServicePrimitive.REQUEST,
            sourceCode,
            destinationCode,
            pgaId,
            msgId,
            MsgType.ADMINISTRATION,
            requestContent);
    var submitResult = initiatorHost.submit(appInterface);

    var confirmAppInterface = submitResult.get();
    assertTrue(confirmAppInterface.servicePrimitive() == ServicePrimitive.CONFIRM);
    assertTrue(confirmAppInterface.content().equals("SUCCEED"));

    var indication = peerIdus.poll(1000L);
    assertTrue(indication.servicePrimitive() == ServicePrimitive.INDICATION);
    assertTrue(indication.content().equals(requestContent));

    assertTrue(initiatorIdus.isEmpty());

    // Laisser le temps les sessions de se fermer
    Thread.sleep(1000);
  }

  @Test
  public void stressTest_async_source_destination() throws Exception {
    int messageCount = 30;

    CountDownLatch responsesReceived = new CountDownLatch(messageCount);

    executor.submit(() -> {
      try {

        while (responsesReceived.getCount() > 0) {
          var indication = peerIdus.poll(30L);
          if (indication != null) {
            assertEquals(ServicePrimitive.INDICATION, indication.servicePrimitive());

            responsesReceived.countDown();
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    List<Future<?>> futures = new ArrayList<>();

    for (int i = 0; i < messageCount; i++) {
      futures.add(executor.submit(() -> {
        try {

          String msgId = UUID.randomUUID().toString();

          final Map<String, Object> requestMap = Map.of("request", "content");
          final String requestContent = mapper.writeValueAsString(requestMap);

          ApplicationInterface request =
              new ApplicationInterface(
                  Service.DELEGATE,
                  ServicePrimitive.REQUEST,
                  sourceCode,
                  destinationCode,
                  pgaId,
                  msgId,
                  MsgType.ADMINISTRATION,
                  requestContent);

          var confirm = initiatorHost.submit(request).get();

          assertEquals(ServicePrimitive.CONFIRM, confirm.servicePrimitive());
          assertEquals("SUCCEED", confirm.content());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }));
    }

    for (Future<?> f : futures) {
      f.get();
    }

    // attendre toutes les réponses
    Boolean await = responsesReceived.await(60, TimeUnit.SECONDS);
    assertTrue(await);

    Thread.sleep(2000);
  }
}
