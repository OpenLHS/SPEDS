package ca.griis.speds.integration.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.network.api.NetworkFactory;
import ca.griis.speds.network.api.sync.SyncNetworkFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ServerSocket;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkTest {
  @Test
  public void sendReply() throws Exception {
    // Given
    NetworkFactory factory = new SyncNetworkFactory(() -> UUID.randomUUID().toString());
    ExecutorService executor = Executors.newFixedThreadPool(10);
    TestUtil testUtil = new TestUtil(new ObjectMapper());

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

      var sourceHost = factory.initHost(originParams);
      var destHost = factory.initHost(targetParams);

      String sourceIri = "https://localhost:4000";
      String destinationIri = "https://localhost:4001";

      for (int i = 0; i < 1; ++i) {
        Callable<String> callableTask = () -> {
          String result = destHost.indication();
          return result;
        };
        Future<String> future = executor.submit(callableTask);

        UUID trackingNumber = UUID.randomUUID();

        String message = "Allo mon beau!";

        InterfaceDataUnit45Dto idu45Dto_e1 = new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, trackingNumber, false), message);

        ObjectMapper objectMapper = new ObjectMapper();
        String idu45_e1 = objectMapper.writeValueAsString(idu45Dto_e1);

        sourceHost.request(idu45_e1);

        String req = future.get();
        InterfaceDataUnit45Dto iduReq = objectMapper.readValue(req, InterfaceDataUnit45Dto.class);
        assertEquals(iduReq.getMessage(), "Allo mon beau!");

        sourceHost.confirm();
      }

      sourceHost.close();
      destHost.close();
    }

    executor.shutdownNow();
  }
}
