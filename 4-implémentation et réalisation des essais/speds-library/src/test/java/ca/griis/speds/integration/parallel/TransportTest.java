package ca.griis.speds.integration.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.sync.SyncTransportFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

public class TransportTest {
  @Test
  public void sendReply() throws InterruptedException, ExecutionException, IOException {
    TransportFactory factory = new SyncTransportFactory(() -> UUID.randomUUID().toString());

    TestUtil testUtil = new TestUtil(new ObjectMapper());
    ExecutorService executor = Executors.newFixedThreadPool(10);

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

      var sourceHost = factory.init(originParams);
      var destHost = factory.init(targetParams);

      sourceHost.listen();
      destHost.listen();

      String sourceCode = "localhost";
      String destinationCode = "localhost";
      String sourceIri = "https://localhost:4000";
      String destinationIri = "https://localhost:4001";

      for (int i = 0; i < 1; ++i) {
        UUID trackingNumber = UUID.randomUUID();
        Context34Dto context =
            new Context34Dto(sourceCode, destinationCode, sourceIri, trackingNumber,
                destinationIri, false);
        String message = "Aye toiiii!!";

        InterfaceDataUnit34Dto x = new InterfaceDataUnit34Dto(context, message);
        ObjectMapper objectMapper = new ObjectMapper();
        String y = objectMapper.writeValueAsString(x);

        Callable<String> callableTask = () -> {
          String result = destHost.indication();
          return result;
        };

        Callable<Void> callableTask3 = () -> {
          sourceHost.request(y);
          return null;
        };

        Future<String> future = executor.submit(callableTask);
        executor.submit(callableTask3);

        String result = future.get();

        InterfaceDataUnit34Dto iduReq =
            objectMapper.readValue(result, InterfaceDataUnit34Dto.class);
        assertEquals(iduReq.getMessage(), "Aye toiiii!!");
      }

      sourceHost.close();
      destHost.close();
    }

    executor.shutdownNow();
  }
}
