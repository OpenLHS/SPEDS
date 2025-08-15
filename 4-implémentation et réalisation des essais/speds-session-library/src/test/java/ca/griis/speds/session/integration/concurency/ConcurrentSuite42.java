package ca.griis.speds.session.integration.concurency;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.SyncSessionFactory;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.contract.PiduContext;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConcurrentSuite42 {
  @Mock
  private TransportHost initiatrice1TransportHost;

  @Mock
  private TransportHost partenaire1TransportHost;

  @Mock
  private TransportHost partenaire2TransportHost;

  @Mock
  private PgaService pgaService;

  private final BlockingQueue<String> initiatrice1Request = new LinkedBlockingQueue<>();
  private final BlockingQueue<String> initiatrice2Request = new LinkedBlockingQueue<>();
  private final BlockingQueue<String> initiatriceResponse = new LinkedBlockingQueue<>();
  private final BlockingQueue<Map.Entry<String, SessionInformation>> outMsgSupplier1 =
      new LinkedBlockingDeque<>();
  private final BlockingQueue<Map.Entry<String, SessionInformation>> outMsgSupplier2 =
      new LinkedBlockingDeque<>();
  private final Random random = new Random();

  private final KeyUtil keyUtil = new KeyUtil();
  private SessionHost initiatrice;
  private SessionHost partenaire1;
  private SessionHost partenaire2;

  KeyPair ini1Keys;
  KeyPair part1Keys;
  KeyPair part2Keys;

  String pgaId;

  @BeforeEach
  public void setup() throws Exception {
    SyncSessionFactory factory = new SyncSessionFactory(pgaService) {
      @Override
      public TransportHost initTransportHost(String parameters) {
        if (parameters.contains("test_identity_ini1"))
          return initiatrice1TransportHost;
        if (parameters.contains("test_identity_part1"))
          return partenaire1TransportHost;
        if (parameters.contains("test_identity_part2"))
          return partenaire2TransportHost;
        return null;
      }
    };

    // Pas de requis fort sur la version, mais on m'a indiquer qu'on voulait fortement cette forme.
    String version = random.nextInt() + "." + random.nextInt() + "." + random.nextInt();
    // Iri légèrement aléatoire
    String randomIri = "https://example.com/resource/" + UUID.randomUUID();
    pgaId = UUID.randomUUID().toString();

    String parameterFormat = """
        {
          "options": {
            "test_identity": "%s",
            "speds.ses.version":"%s",
            "speds.ses.reference":"%s",
            "speds.ses.cert": "%s",
            "speds.ses.private.key": "%s"
          }
        }
        """;

    ini1Keys = keyUtil.generateRSAKeyPair();
    X509Certificate ini1Cert = keyUtil.generateSelfSignedCertificate(ini1Keys);
    String ini1Params = String.format(parameterFormat, "test_identity_ini1", version, randomIri,
        keyUtil.certificateToString(ini1Cert), keyUtil.privateKeyToString(ini1Keys.getPrivate()));
    initiatrice = factory.init(ini1Params);

    part1Keys = keyUtil.generateRSAKeyPair();
    X509Certificate part1Cert = keyUtil.generateSelfSignedCertificate(part1Keys);
    String partenaire1Params = String.format(parameterFormat, "test_identity_part1", version,
        randomIri,
        keyUtil.certificateToString(part1Cert), keyUtil.privateKeyToString(part1Keys.getPrivate()));
    partenaire1 = factory.init(partenaire1Params);

    part2Keys = keyUtil.generateRSAKeyPair();
    X509Certificate part2Cert = keyUtil.generateSelfSignedCertificate(part2Keys);
    String partenaire2Params = String.format(parameterFormat, "test_identity_part2", version,
        randomIri,
        keyUtil.certificateToString(part2Cert), keyUtil.privateKeyToString(part2Keys.getPrivate()));
    partenaire2 = factory.init(partenaire2Params);
  }

  @Test
  public void e_42_concurrency_2_receivers() {
    // Pre configuration
    AtomicBoolean testCompleted = new AtomicBoolean(false);
    // Création des messages de réponses
    String payload1 = "a message1 : " + UUID.randomUUID();
    String payload2 = "a message2 : " + UUID.randomUUID();
    Pidu responseMsg1 = new Pidu(new PiduContext("", "", "", "", UUID.randomUUID(), false),
        payload1);
    Pidu responseMsg2 = new Pidu(new PiduContext("", "", "", "", UUID.randomUUID(), false),
        payload2);

    // Création des triggers messages
    PiduContext piduContext1 = new PiduContext(pgaId, "sourceCode", "destinationCode1",
        keyUtil.secretKeyToString(keyUtil.generateSdek()), UUID.randomUUID(), false);
    Pidu triggerMsg1 = new Pidu(piduContext1, "payload1");

    PiduContext piduContext2 = new PiduContext(pgaId, "sourceCode", "destinationCode2",
        keyUtil.secretKeyToString(keyUtil.generateSdek()), UUID.randomUUID(), false);
    Pidu triggerMsg2 = new Pidu(piduContext2, "payload2");
    MultipleTaskTrigger trigger = new MultipleTaskTrigger();

    // Enregistrer les appels mocked
    when(pgaService.getIri(pgaId, piduContext1.getSourceCode()))
        .thenReturn("https://localhost/source1");
    when(pgaService.getIri(pgaId, piduContext1.getDestinationCode()))
        .thenReturn("https://localhost/destination1");
    when(pgaService.getIri(pgaId, piduContext2.getDestinationCode()))
        .thenReturn("https://localhost/destination2");
    when(pgaService.getPublicKey(pgaId, piduContext1.getDestinationCode()))
        .thenReturn(keyUtil.publicKeyToString(this.part1Keys.getPublic()));
    when(pgaService.getPublicKey(pgaId, piduContext2.getDestinationCode()))
        .thenReturn(keyUtil.publicKeyToString(this.part2Keys.getPublic()));
    when(pgaService.verifyLegitimacy(eq(pgaId), eq(piduContext1.getSourceCode()), any()))
        .thenReturn(true);

    // mécanisme de transfert initiatrice
    doAnswer(invocation -> {
      String serialMsg = invocation.getArgument(0);
      Sidu sidu = SharedObjectMapper.getInstance().getMapper().readValue(serialMsg, Sidu.class);
      // Mettre le message dans une queue pour le partenaire associé
      if (sidu.getContext().getDestinationCode()
          .equals(triggerMsg1.getContext().getDestinationCode())) {
        initiatrice1Request.put(serialMsg);
      } else {
        initiatrice2Request.put(serialMsg);
      }
      return null;
    }).when(initiatrice1TransportHost).dataRequest(any());
    doAnswer(invocation -> initiatriceResponse.poll()).when(initiatrice1TransportHost).dataReply();

    // mécanisme de transfert partenaire1
    doAnswer(invocation -> {
      String serialMsg = invocation.getArgument(0);
      initiatriceResponse.add(serialMsg);
      return null;
    }).when(partenaire1TransportHost).dataRequest(any());
    doAnswer(invocation -> initiatrice1Request.poll()).when(partenaire1TransportHost).dataReply();
    registerPartnerResponseLoop(partenaire1, payload1, testCompleted,
        outMsgSupplier1);

    // mécanisme de transfert partenaire2
    doAnswer(invocation -> {
      String serialMsg = invocation.getArgument(0);
      initiatriceResponse.add(serialMsg);
      return null;
    }).when(partenaire2TransportHost).dataRequest(any());
    doAnswer(invocation -> initiatrice2Request.poll()).when(partenaire2TransportHost).dataReply();
    registerPartnerResponseLoop(partenaire2, payload2, testCompleted,
        outMsgSupplier2);

    // le cas 14 normal
    IntStream.range(0, 100)
        .forEach(i -> Cases.ct_14(initiatrice, triggerMsg1, triggerMsg2, trigger,
            outMsgSupplier1, outMsgSupplier2, responseMsg1, responseMsg2));
    testCompleted.set(true);
  }

  private void registerPartnerResponseLoop(SessionHost host, String payload,
      AtomicBoolean testCompleted,
      BlockingQueue<Map.Entry<String, SessionInformation>> outMessageQueue) {
    // Transféré la réponse lorsque c'est prêt
    // tq on a pas finit le test
    CompletableFuture.supplyAsync(() -> {
      String result;
      do {
        try {
          // prendre le message
          result = host.indication();
          // Idéalement, on aurait sessionInfo en même temps que result pour éviter la possibilité
          // que l'état change en cours de route.
          Map<SessionId, SessionInformation> sessionInformations = Util.getSesInfo(host, "server");
          // Faire la réponse
          Pidu message = SharedObjectMapper.getInstance().getMapper().readValue(result, Pidu.class);
          PiduContext piduContext = new PiduContext(message.getContext().getPga(),
              message.getContext().getSourceCode(), message.getContext().getDestinationCode(),
              message.getContext().getSdek(), message.getContext().getTrackingNumber(), false);
          Pidu response = new Pidu(piduContext, payload);
          String serialResponse =
              SharedObjectMapper.getInstance().getMapper().writeValueAsString(response);

          // Enregistrer pour la validation subséquente
          SessionInformation sessionInformation = sessionInformations.values().stream()
              .filter(x -> x.initiatorId.equals(message.getContext().getSourceCode()))
              .findFirst()
              .orElseThrow();
          outMessageQueue.put(Map.entry(result, sessionInformation));
          // envoyé la réponse
          host.response(serialResponse);
        } catch (JsonProcessingException | InterruptedException e) {
          throw new RuntimeException(e);
        }

        // busy wait pour pas spammer
        if (!testCompleted.get()) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      } while (!testCompleted.get());
      return result;
    });
  }
}
