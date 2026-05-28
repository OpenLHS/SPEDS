package ca.griis.speds.integration.presentation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.speds.integration.security.CryptographyServiceCreator;
import ca.griis.speds.network.internal.serialization.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.PresentationHostFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpedsPresentationTest {
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
  private PresentationHost initiatorHost;
  private PresentationHost peerHost;
  private PreEvent initiatorConsumer = new PreEvent();
  private PreEvent peerConsumer = new PreEvent();
  private ExecutorService executor;

  @BeforeEach
  public void setUp() throws Exception {
    executor = Executors.newFixedThreadPool(50);
    cryptographyService = CryptographyServiceCreator.createCryptographyService();
    mapper = new ObjectMapper();

    InitInParamsDto params = new InitInParamsDto(PresentationParamsCreator.createSourceParams());
    parametersSrc = mapper.writeValueAsString(params);

    InitInParamsDto params2 =
        new InitInParamsDto(PresentationParamsCreator.createDestinationParams());
    parametersDest = mapper.writeValueAsString(params2);

    projectService = PresentationParamsCreator.createProjectService(pgaId, sourceCode,
        destinationCode, sourceIri, destinationIri);

    PresentationHostFactory factory =
        new PresentationHostFactory(projectService, cryptographyService);
    initiatorHost = factory.initHost(parametersSrc, initiatorConsumer);
    peerHost = factory.initHost(parametersDest, peerConsumer);
  }

  @AfterEach
  public void afterEach() throws Exception {
    initiatorHost.close();
    peerHost.close();

    executor.shutdown();
  }

  @Test
  public void nominal() throws Exception {
    var context = new Context12Dto(pgaId, sourceCode, destinationCode, "delegate",
        ServicePrimitive.REQUEST, false);
    var idu45 = new InterfaceDataUnit12Dto(context, "Some data to exchange");
    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);

    var submitResult = initiatorHost.submitIdu(idu);

    var confirmIsPresent = submitResult.get();
    assertTrue(confirmIsPresent.isPresent());

    var confirm = mapper.readValue(confirmIsPresent.get(), InterfaceDataUnit12Dto.class);
    assertTrue(confirm.getContext().getServicePrimitive() == ServicePrimitive.CONFIRM);
    assertTrue(confirm.getMessage().equals("SUCCEED"));

    String peerReceivedIdu = peerConsumer.poll(10L);

    var indicationIdu = mapper.readValue(peerReceivedIdu, InterfaceDataUnit12Dto.class);
    assertTrue(indicationIdu.getContext().getServicePrimitive() == ServicePrimitive.INDICATION);
    assertTrue(indicationIdu.getMessage().equals("Some data to exchange"));

    final var params = mapper.convertValue(indicationIdu.getContext().getOptions(),
        new TypeReference<Map<String, String>>() {});
    String id = params.get("TN");
    var options = Map.of("TN", id);

    var responseContext = new Context12Dto(pgaId, sourceCode, destinationCode, "transfer",
        ServicePrimitive.RESPONSE, options);
    var responseIdu = new InterfaceDataUnit12Dto(responseContext, "Thank you");
    var serializedResponseIdu = mapper.writeValueAsString(responseIdu);

    var responseResult = peerHost.submitIdu(serializedResponseIdu);
    assertTrue(responseResult.get().isEmpty());

    assertTrue(initiatorConsumer.isEmpty());

    // Laisser le temps les sessions de se fermer
    Thread.sleep(1000);
  }
}
