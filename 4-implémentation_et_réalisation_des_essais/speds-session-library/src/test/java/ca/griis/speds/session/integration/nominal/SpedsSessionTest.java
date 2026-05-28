package ca.griis.speds.session.integration.nominal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.api.SessionHostFactory;
import ca.griis.speds.session.integration.SecurityUtils;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpedsSessionTest implements SessionHostEvent {
  private static final IRIFactory iriFactory = IRIFactory.iriImplementation();

  private static final String pgaId = UUID.randomUUID().toString();
  private static final String sourceCode = "source";
  private static final String destinationCode = "destination";
  private static final IRI sourceIri = iriFactory.create("https://localhost:8081");
  private static final IRI destinationIri = iriFactory.create("https://localhost:8082");

  private String parametersSrc;
  private String parametersDest;
  private ObjectMapper mapper;
  private SessionHost initiatorHost;
  private SessionHost peerHost;
  private CryptographyService cryptographyService;
  private ProjectService projectService;
  private ExecutorService executor;

  private final LinkedBlockingQueue<String> idus = new LinkedBlockingQueue<>();
  private final LinkedBlockingQueue<SessionId> initiatorSessions = new LinkedBlockingQueue<>();
  private final LinkedBlockingQueue<SessionId> peerSessions = new LinkedBlockingQueue<>();

  @BeforeEach
  public void setUp() throws Exception {
    executor = Executors.newFixedThreadPool(50);
    cryptographyService = SecurityUtils.createCryptographyService();
    mapper = new ObjectMapper();

    InitInParamsDto params = new InitInParamsDto(SessionParamsCreator.createSourceParams());
    parametersSrc = mapper.writeValueAsString(params);

    InitInParamsDto params2 = new InitInParamsDto(SessionParamsCreator.createDestinationParams());
    parametersDest = mapper.writeValueAsString(params2);

    projectService = SessionParamsCreator.createProjectService(pgaId, sourceCode, destinationCode,
        sourceIri, destinationIri);

    var factory = new SessionHostFactory(projectService, cryptographyService);

    initiatorHost = factory.initHost(parametersSrc, this);
    peerHost = factory.initHost(parametersDest, this);
  }

  @AfterEach
  public void afterEach() throws Exception {
    initiatorHost.close();
    peerHost.close();

    executor.shutdown();
  }

  @Test
  public void nominal() throws Exception {
    final var symKey = cryptographyService.generateSymmetricKey(SpedsLayer.SESSION);
    final var sdek = Base64.getEncoder().encodeToString(symKey.getEncoded());

    var context = new Context23Dto(pgaId, "source", "destination", sdek,
        Context23Dto.Service.DELEGATE, Context23Dto.ServicePrimitive.REQUEST, false);
    var idu45 = new InterfaceDataUnit23Dto(context, "Some data to exchange");
    var idu = mapper.writeValueAsString(idu45);

    var submitResult = initiatorHost.submitIdu(idu);

    var confirmIsPresent = submitResult.get();
    assertTrue(confirmIsPresent.isPresent());

    var confirm = mapper.readValue(confirmIsPresent.get(), InterfaceDataUnit23Dto.class);
    assertTrue(confirm.getContext().getServicePrimitive() == ServicePrimitive.CONFIRM);
    assertTrue(confirm.getMessage().equals("SUCCEED"));

    String peerReceivedIdu = idus.poll(200, TimeUnit.SECONDS);
    var indicationIdu = mapper.readValue(peerReceivedIdu, InterfaceDataUnit23Dto.class);
    assertTrue(indicationIdu.getMessage().equals("Some data to exchange"));

    final var params = mapper.convertValue(
        indicationIdu.getContext().getOptions(),
        new TypeReference<Map<String, String>>() {});
    String id = params.get("TN");
    var options = Map.of("TN", id);

    var context2 = new Context23Dto(pgaId, "source", "destination", sdek,
        Context23Dto.Service.TRANSFER, Context23Dto.ServicePrimitive.RESPONSE, options);
    var idu452 = new InterfaceDataUnit23Dto(context2, "YES tout marche");
    var idu2 = mapper.writeValueAsString(idu452);

    var responseResult = peerHost.submitIdu(idu2);
    assertTrue(responseResult.get().isEmpty());

    assertTrue(submitResult.get().isPresent());

    peerSessions.poll(200, TimeUnit.SECONDS);
    initiatorSessions.poll(200, TimeUnit.SECONDS);
  }

  @Override
  public void notifyIdu(String idu) {
    idus.add(idu);
  }

  @Override
  public void notifyInitiatorSessionTerminatedSuccessfully(SessionId sessionId) {
    initiatorSessions.add(sessionId);
  }

  @Override
  public void notifyPeerSessionTerminatedSuccessfully(SessionId sessionId) {
    peerSessions.add(sessionId);
  }

  @Override
  public void notifyException(Exception exception) {}
}
