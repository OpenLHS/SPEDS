package ca.griis.speds.integration.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.SyncSessionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.net.ServerSocket;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SessionTest {
  @Mock
  private PgaService pgaService;

  @Test
  public void sendReply() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);

    TestUtil testUtil = new TestUtil(new ObjectMapper());

    String pgaId = UUID.randomUUID().toString();
    String sourceCode = "source";
    String destinationCode = "destination";
    String sourceIri = "https://localhost:4000";
    String destinationIri = "https://localhost:4001";

    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    Certificate certificate = cf
        .generateCertificate(
            new ByteArrayInputStream(Base64.getDecoder().decode(KeyVar.localhostCertRsa)));
    byte[] bytes = certificate.getPublicKey().getEncoded();
    String key = Base64.getEncoder().encodeToString(bytes);

    when(pgaService.getIri(pgaId, sourceCode)).thenReturn(sourceIri);
    when(pgaService.getPublicKey(pgaId, sourceCode)).thenReturn(key);
    when(pgaService.getIri(pgaId, destinationCode)).thenReturn(destinationIri);
    when(pgaService.getPublicKey(pgaId, destinationCode)).thenReturn(key);
    when(pgaService.verifyLegitimacy(pgaId, sourceCode, key)).thenReturn(true);

    // Given
    try (ServerSocket originSocket = new ServerSocket(4000);
        ServerSocket targetSocket = new ServerSocket(4001)) {
      // todo
      testUtil.freePorts(originSocket, targetSocket, null);

      final String originParams =
          """
              {
                "options": {
                  "speds.ses.cert": "%s",
                  "speds.ses.private.key": "%s",
                  "speds.ses.version":"x.x.x",
                  "speds.ses.reference": "https://reference.iri/speds",
                  "speds.tra.version":"x.x.x",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"x.x.x",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "%s",
                  "speds.net.private.key": "%s",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "%s",
                  "speds.dl.https.server.private.key": "%s",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %7$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  originSocket.getLocalPort());

      final String targetParams =
          """
              {
                "options": {
                  "speds.ses.cert": "%s",
                  "speds.ses.private.key": "%s",
                  "speds.ses.version":"x.x.x",
                  "speds.ses.reference": "https://reference.iri/speds",
                  "speds.tra.version":"x.x.x",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"x.x.x",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "%s",
                  "speds.net.private.key": "%s",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "%s",
                  "speds.dl.https.server.private.key": "%s",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %7$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  targetSocket.getLocalPort());

      SyncSessionFactory factory = new SyncSessionFactory(pgaService);
      SessionHost client = factory.init(originParams);
      assertNotNull(client);

      SessionHost server = factory.init(targetParams);
      assertNotNull(server);

      for (int i = 0; i < 1; ++i) {
        UUID trackingNumber = UUID.randomUUID();

        SecretKey secretKey =
            SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
        byte[] keyBytes = secretKey.getEncoded();
        String serialKey = Base64.getEncoder().encodeToString(keyBytes);

        Context23Dto pici =
            new Context23Dto(pgaId, sourceCode, destinationCode, serialKey, trackingNumber, false);
        InterfaceDataUnit23Dto pidu = new InterfaceDataUnit23Dto(pici, "Salut");

        ObjectMapper mapper = new ObjectMapper();
        String serialPidu = mapper.writeValueAsString(pidu);

        Callable<String> indication = () -> {
          String result = server.indication();
          return result;
        };

        Future<String> futureI = executor.submit(indication);

        CompletableFuture<Void> futureRequest = client.requestFuture(serialPidu);

        String data = futureI.get();
        InterfaceDataUnit23Dto pidu_rec = mapper.readValue(data, InterfaceDataUnit23Dto.class);
        assertEquals(pidu_rec.getMessage(), "Salut");

        Callable<String> confirm = () -> {
          String result = client.confirm();
          return result;
        };

        Future<String> futureConfirm = executor.submit(confirm);

        Context23Dto pici_res =
            new Context23Dto(pgaId, sourceCode, destinationCode, serialKey,
                pidu_rec.getContext().getTrackingNumber(), false);
        InterfaceDataUnit23Dto response = new InterfaceDataUnit23Dto(pici_res, "Ben bye");
        String serialResPidu = mapper.writeValueAsString(response);
        server.response(serialResPidu);

        String resData = futureConfirm.get();
        InterfaceDataUnit23Dto resIdu = mapper.readValue(resData, InterfaceDataUnit23Dto.class);

        assertEquals(resIdu.getMessage(), "Ben bye");

        futureRequest.get();
      }

      client.close();
      server.close();
    }

    executor.shutdownNow();
  }
}
