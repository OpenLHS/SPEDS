package ca.griis.speds.integration.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.presentation.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.net.ServerSocket;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PresentationTest {
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

    try (ServerSocket originSocket = new ServerSocket(4000);
        ServerSocket targetSocket = new ServerSocket(4001)) {
      // todo
      testUtil.freePorts(originSocket, targetSocket, null);

      // Certificat RSA au lieu de Curve.
      final String originParams =
          """
              {
                "options": {
                  "speds.app.version":"x.x.x",
                  "speds.app.reference": "https://reference.iri/speds",
                  "speds.pre.version":"x.x.x",
                  "speds.pre.reference": "https://reference.iri/speds",
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
                  "speds.app.version":"x.x.x",
                  "speds.app.reference": "https://reference.iri/speds",
                  "speds.pre.version":"x.x.x",
                  "speds.pre.reference": "https://reference.iri/speds",
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

      MutablePresentationFactory factory = new MutablePresentationFactory(pgaService);
      PresentationHost client = factory.init(originParams);
      assertNotNull(client);

      PresentationHost server = factory.init(targetParams);
      assertNotNull(server);

      for (int i = 0; i < 1; ++i) {
        UUID trackingNumber = UUID.randomUUID();

        ContextDto pici = new ContextDto(pgaId, sourceCode, destinationCode, trackingNumber, false);
        InterfaceDataUnit12Dto aidu = new InterfaceDataUnit12Dto(pici, "Allo mon beau");

        ObjectMapper mapper = new ObjectMapper();
        String serialPidu = mapper.writeValueAsString(aidu);

        Callable<Void> request = () -> {
          client.request(serialPidu);
          return null;
        };

        Callable<String> indication = () -> {
          String result = server.indication();
          return result;
        };

        Future<String> futureI = executor.submit(indication);
        Future<Void> futureR = executor.submit(request);

        String data = futureI.get();
        InterfaceDataUnit12Dto aidu_rec = mapper.readValue(data, InterfaceDataUnit12Dto.class);
        assertEquals(aidu_rec.getMessage(), "Allo mon beau");

        ContextDto pici_res =
            new ContextDto(pgaId, sourceCode, destinationCode,
                aidu_rec.getContext().getTrackingNumber(), false);
        InterfaceDataUnit12Dto response = new InterfaceDataUnit12Dto(pici_res, "Merci bo-t");
        String serialResPidu = mapper.writeValueAsString(response);
        server.response(serialResPidu);

        Callable<String> confirm = () -> {
          String result = client.confirm();
          return result;
        };

        Future<String> futurC = executor.submit(confirm);

        String result = futurC.get();
        InterfaceDataUnit12Dto res = mapper.readValue(result, InterfaceDataUnit12Dto.class);
        assertEquals(res.getMessage(), "Merci bo-t");

        futureR.get();
      }

      client.close();
      server.close();
    }

    executor.shutdownNow();
  }
}
