package ca.griis.speds.integration.parallel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InArgDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ParamDto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheEnvoiDto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.net.ServerSocket;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
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
public class ApplicationTest {
  private String pgaId = UUID.randomUUID().toString();
  private String sourceIri = "https://localhost:4000";
  private String destinationIri = "https://localhost:4001";

  @Mock
  private PgaService pgaService;

  @Test
  public void sendReply() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    TestUtil testUtil = new TestUtil(new ObjectMapper());

    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    Certificate certificate = cf
        .generateCertificate(
            new ByteArrayInputStream(Base64.getDecoder().decode(KeyVar.localhostCertRsa)));
    byte[] bytes = certificate.getPublicKey().getEncoded();
    String key = Base64.getEncoder().encodeToString(bytes);

    String sourceCode = "source";
    String destinationCode = "destination";

    when(pgaService.getIri(pgaId, sourceCode)).thenReturn(sourceIri);
    when(pgaService.getPublicKey(pgaId, sourceCode)).thenReturn(key);
    when(pgaService.getIri(pgaId, destinationCode)).thenReturn(destinationIri);
    when(pgaService.getPublicKey(pgaId, destinationCode)).thenReturn(key);
    when(pgaService.verifyLegitimacy(pgaId, sourceCode, key)).thenReturn(true);
    when(pgaService.verifyLegitimacy(pgaId, destinationCode, key)).thenReturn(true);

    try (ServerSocket originSocket = new ServerSocket(4000);
        ServerSocket targetSocket = new ServerSocket(4001)) {
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

      SyncApplicationFactory factory = new SyncApplicationFactory(pgaService);
      ApplicationHost client = factory.init(originParams);
      assertNotNull(client);

      ApplicationHost server = factory.init(targetParams);
      assertNotNull(server);

      for (int i = 0; i < 1; ++i) {
        UUID fstId = UUID.randomUUID();
        UUID scdId = UUID.randomUUID();

        request(fstId, sourceCode, destinationCode, client, executor).get();
        request(scdId, destinationCode, sourceCode, server, executor).get();

        // client.close();
        // server.close();

        Callable<Void> indication = () -> {
          assertNotNull(server.indication());
          assertNotNull(client.indication());
          return null;
        };
        executor.submit(indication).get();

        response(fstId, sourceCode, destinationCode, server, executor).get();
        response(scdId, destinationCode, sourceCode, client, executor).get();

        Callable<InterfaceDataUnit01Dto> confirm = () -> {
          assertNotNull(client.confirm());
          assertNotNull(server.confirm());
          return null;
        };

        executor.submit(confirm).get();
      }

      client.close();
      server.close();
    }

    executor.shutdownNow();
  }

  private Future<Void> request(UUID msgID, String sourceCode, String destinationCode,
      ApplicationHost host, ExecutorService executor)
      throws JsonProcessingException {
    List<InArgDto> inArgs = Arrays.asList(
        new InArgDto("ABCDEFGHIJKLMNOPQ", "ABCDEFGHIJKLMNOPQ"),
        new InArgDto("ABCDEFGHIJKLMNOP", "ABCDEFGHIJKLMNOPQRSTUVWXYZABC"));
    TacheEnvoiDto taskEnvDto =
        new TacheEnvoiDto(
            "26ad7aef-16c2-4ef8-b196-f652026d513c",
            "11666f67-9680-4903-a91c-9b8ed459962c",
            inArgs,
            new ArrayList<ParamDto>(),
            TacheEnvoiDto.Command.START);
    String taskEnvContent =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(taskEnvDto);
    ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            HeaderDto.Msgtype.TACHE_ENVOI,
            msgID,
            false,
            new SPEDSDto("2.0.0", "a reference")),
        taskEnvContent);
    final String message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);
    final ContextDto context = new ContextDto(
        pgaId,
        sourceCode,
        destinationCode,
        UUID.randomUUID(),
        Boolean.FALSE);
    InterfaceDataUnit01Dto idu = new InterfaceDataUnit01Dto(context, message);
    Callable<Void> request = () -> {
      host.request(idu);

      return null;
    };
    return executor.submit(request);
  }

  private Future<Void> response(UUID msgID, String sourceCode, String destinationCode,
      ApplicationHost host, ExecutorService executor)
      throws JsonProcessingException {
    List<InArgDto> inArgs = Arrays.asList(
        new InArgDto("ABCDEFGHIJKLMNOPQ", "ABCDEFGHIJKLMNOPQ"),
        new InArgDto("ABCDEFGHIJKLMNOP", "ABCDEFGHIJKLMNOPQRSTUVWXYZABC"));
    TacheEnvoiDto taskEnvDto =
        new TacheEnvoiDto(
            "26ad7aef-16c2-4ef8-b196-f652026d513c",
            "11666f67-9680-4903-a91c-9b8ed459962c",
            inArgs,
            new ArrayList<ParamDto>(),
            TacheEnvoiDto.Command.START);
    String taskEnvContent =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(taskEnvDto);
    ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            HeaderDto.Msgtype.TACHE_ENVOI,
            msgID,
            false,
            new SPEDSDto("2.0.0", "a reference")),
        taskEnvContent);
    final String message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);
    final ContextDto context = new ContextDto(
        pgaId,
        sourceCode,
        destinationCode,
        UUID.randomUUID(),
        Boolean.FALSE);
    InterfaceDataUnit01Dto idu = new InterfaceDataUnit01Dto(context, message);
    Callable<Void> request = () -> {
      host.response(idu);

      return null;
    };
    return executor.submit(request);
  }
}
