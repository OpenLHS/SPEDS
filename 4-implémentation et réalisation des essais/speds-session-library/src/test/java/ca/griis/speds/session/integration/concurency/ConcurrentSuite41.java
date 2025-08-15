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
public class ConcurrentSuite41 {
  @Mock
  private TransportHost initiatrice1TransportHost;

  @Mock
  private TransportHost initiatrice2TransportHost;

  @Mock
  private TransportHost partenaireTransportHost;

  @Mock
  private PgaService pgaService;

  private final BlockingQueue<String> initiatrice1Request = new LinkedBlockingQueue<>();
  private final BlockingQueue<String> initiatrice1Response = new LinkedBlockingQueue<>();
  private final BlockingQueue<String> initiatrice2Request = new LinkedBlockingQueue<>();
  private final BlockingQueue<String> initiatrice2Response = new LinkedBlockingQueue<>();
  private final BlockingQueue<Map.Entry<String, SessionInformation>> outMsgSupplier =
      new LinkedBlockingDeque<>();
  private final Random random = new Random();

  private final KeyUtil keyUtil = new KeyUtil();
  private SessionHost initiatrice1;
  private SessionHost initiatrice2;
  private SessionHost partenaire;

  KeyPair ini1Keys;
  KeyPair ini2Keys;
  KeyPair partKeys;

  String payload1;
  String payload2;

  @BeforeEach
  public void setup() throws Exception {
    SyncSessionFactory factory = new SyncSessionFactory(pgaService) {
      @Override
      public TransportHost initTransportHost(String parameters) {
        if (parameters.contains("test_identity_ini1"))
          return initiatrice1TransportHost;
        if (parameters.contains("test_identity_ini2"))
          return initiatrice2TransportHost;
        if (parameters.contains("test_identity_part"))
          return partenaireTransportHost;
        return null;
      }
    };

    // Pas de requis fort sur la version, mais on m'a indiquer qu'on voulait fortement cette forme.
    String version = random.nextInt() + "." + random.nextInt() + "." + random.nextInt();
    // Iri légèrement aléatoire
    String randomIri = "https://example.com/resource/" + UUID.randomUUID();

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
    initiatrice1 = factory.init(ini1Params);

    ini2Keys = keyUtil.generateRSAKeyPair();
    X509Certificate ini2Cert = keyUtil.generateSelfSignedCertificate(ini2Keys);
    String ini2Params = String.format(parameterFormat, "test_identity_ini2", version, randomIri,
        keyUtil.certificateToString(ini2Cert), keyUtil.privateKeyToString(ini2Keys.getPrivate()));
    initiatrice2 = factory.init(ini2Params);

    partKeys = keyUtil.generateRSAKeyPair();
    X509Certificate partCert = keyUtil.generateSelfSignedCertificate(partKeys);
    String partenaireParams = String.format(parameterFormat, "test_identity_part", version,
        randomIri,
        keyUtil.certificateToString(partCert), keyUtil.privateKeyToString(partKeys.getPrivate()));
    partenaire = factory.init(partenaireParams);
  }

  @Test
  public void e_41_concurrency_2_senders() throws NoSuchFieldException, IllegalAccessException {

    // Préconfig
    String pgaId = UUID.randomUUID().toString();
    payload1 = "a message1 : " + UUID.randomUUID();
    payload2 = "a message2 : " + UUID.randomUUID();

    PiduContext piduContext1 = new PiduContext(pgaId, "sourceCode1", "destinationCode",
        keyUtil.secretKeyToString(keyUtil.generateSdek()), UUID.randomUUID(), false);
    Pidu triggerMsg1 = new Pidu(piduContext1, "payload");

    PiduContext piduContext2 = new PiduContext(pgaId, "sourceCode2", "destinationCode",
        keyUtil.secretKeyToString(keyUtil.generateSdek()), UUID.randomUUID(), false);
    Pidu triggerMsg2 = new Pidu(piduContext2, "payload");

    when(pgaService.getIri(pgaId, piduContext1.getSourceCode()))
        .thenReturn("https://localhost/source1");
    when(pgaService.getIri(pgaId, piduContext2.getSourceCode()))
        .thenReturn("https://localhost/source2");
    when(pgaService.getIri(pgaId, piduContext1.getDestinationCode()))
        .thenReturn("https://localhost/destination");
    when(pgaService.getPublicKey(pgaId, piduContext2.getDestinationCode()))
        .thenReturn(keyUtil.publicKeyToString(this.partKeys.getPublic()));
    when(pgaService.verifyLegitimacy(eq(pgaId), eq(piduContext1.getSourceCode()), any()))
        .thenReturn(true);
    when(pgaService.verifyLegitimacy(eq(pgaId), eq(piduContext2.getSourceCode()), any()))
        .thenReturn(true);

    MultipleTaskTrigger trigger = new MultipleTaskTrigger();

    // mécanisme de transfert initiatrice 1
    doAnswer(invocation -> {
      String arg = invocation.getArgument(0);
      initiatrice1Request.put(arg);
      return null;
    }).when(initiatrice1TransportHost).dataRequest(any());
    doAnswer(invocation -> initiatrice1Response.poll()).when(initiatrice1TransportHost).dataReply();

    // mécanisme de transfert initiatrice 2
    doAnswer(invocation -> {
      String arg = invocation.getArgument(0);
      initiatrice2Request.put(arg);
      return null;
    }).when(initiatrice2TransportHost).dataRequest(any());
    doAnswer(invocation -> initiatrice2Response.poll()).when(initiatrice2TransportHost).dataReply();

    // mécanisme de transfert partenaire
    doAnswer(invocation -> {
      String serialMsg = invocation.getArgument(0);
      Sidu sidu = SharedObjectMapper.getInstance().getMapper().readValue(serialMsg, Sidu.class);

      if (sidu.getContext().getDestinationCode().equals("sourceCode1")) {
        initiatrice1Response.add(serialMsg);
      } else {
        initiatrice2Response.add(serialMsg);
      }
      return null;
    }).when(partenaireTransportHost).dataRequest(any());

    doAnswer(invocation -> {
      // Faire varier pour simuler une latence de communication
      if (random.nextBoolean()) {
        return initiatrice1Request.poll();
      } else {
        return initiatrice2Request.poll();
      }
    }).when(partenaireTransportHost).dataReply();

    AtomicBoolean testCompleted = new AtomicBoolean(false);
    registerPartnerResponseLoop(testCompleted, triggerMsg1);

    // messages pour la validation, le payload est important
    Pidu responseMsg1 = new Pidu(new PiduContext("", "", "", "", UUID.randomUUID(), false),
        payload1);
    Pidu responseMsg2 = new Pidu(new PiduContext("", "", "", "", UUID.randomUUID(), false),
        payload2);

    // 100 fois, le cas normal
    IntStream.range(0, 100)
        .forEach(i -> Cases.ct_13(initiatrice1, initiatrice2, triggerMsg1, triggerMsg2,
            trigger, outMsgSupplier, responseMsg1, responseMsg2));
    testCompleted.set(true);
  }

  private void registerPartnerResponseLoop(AtomicBoolean testCompleted, Pidu triggerMsg) {
    // Transféré la réponse lorsque c'est prêt
    // tq on a pas finit le test
    CompletableFuture.supplyAsync(() -> {
      String result;
      do {
        try {
          // prendre le message
          result = partenaire.indication();
          // fetch session au plus vite
          Map<SessionId, SessionInformation> sessionInformations =
              Util.getSesInfo(partenaire, "server");
          // Faire la réponse
          Pidu message = SharedObjectMapper.getInstance().getMapper().readValue(result, Pidu.class);
          PiduContext piduContext = new PiduContext(message.getContext().getPga(),
              message.getContext().getSourceCode(), message.getContext().getDestinationCode(),
              message.getContext().getSdek(), message.getContext().getTrackingNumber(), false);
          String payload;
          if (result.contains(triggerMsg.getContext().getSourceCode())) {
            payload = payload1;
          } else {
            payload = payload2;
          }
          Pidu response = new Pidu(piduContext, payload);
          String serialResponse =
              SharedObjectMapper.getInstance().getMapper().writeValueAsString(response);
          // Enregistrer pour la validation subséquente
          SessionInformation sessionInformation = sessionInformations.values().stream()
              .filter(x -> x.initiatorId.equals(message.getContext().getSourceCode()))
              .findFirst()
              .orElseThrow();
          outMsgSupplier.put(Map.entry(result, sessionInformation));
          // envoyé la réponse
          partenaire.response(serialResponse);
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
