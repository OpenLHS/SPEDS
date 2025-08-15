package ca.griis.speds.integration.bottomup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.integration.util.ApplicationDomainProviderUtil;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.PgaServiceMock;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.cert.CertificateException;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AppPreSesTraNetLinSpedsIt {

  private SyncApplicationFactory applicationFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;
  PgaService pgaService;

  @BeforeProperty
  public void setup() {
    pgaService = Mockito.mock(PgaService.class);

    applicationFactory = new SyncApplicationFactory(pgaService);

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }

  @Property(tries = 1)
  @Domain(ApplicationDomainProviderUtil.class)
  public void AppPreSesNetLinkExchangeTest(@ForAll InterfaceDataUnit01Dto idu01Dto)
      throws IOException, InterruptedException, CertificateException {
    new PgaServiceMock().mock(pgaService, 4010, 4011);
    try (ServerSocket originSocket = new ServerSocket(4010);
        ServerSocket targetSocket = new ServerSocket(4011)) {
      testUtil.freePorts(originSocket, targetSocket, null);

      final String originParams =
          """
              {
                "options": {
                  "speds.app.version":"3.0.0",
                  "speds.app.reference": "https://reference.iri/speds",
                  "speds.pre.version":"3.0.0",
                  "speds.pre.reference": "https://reference.iri/speds",
                  "speds.ses.cert": "%s",
                  "speds.ses.private.key": "%s",
                  "speds.ses.version":"3.0.0",
                  "speds.ses.reference": "https://reference.iri/speds",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
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
              .formatted(KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  originSocket.getLocalPort());


      final String targetParams =
          """
              {
                "options": {
                  "speds.app.version":"3.0.0",
                  "speds.app.reference": "https://reference.iri/speds",
                  "speds.pre.version":"3.0.0",
                  "speds.pre.reference": "https://reference.iri/speds",
                  "speds.ses.cert": "%s",
                  "speds.ses.private.key": "%s",
                  "speds.ses.version":"3.0.0",
                  "speds.ses.reference": "https://reference.iri/speds",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
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
              .formatted(KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  targetSocket.getLocalPort());

      // application-server-init-success
      ApplicationHost targetAppHost = applicationFactory.init(targetParams);

      // application-client-request-success
      ApplicationHost originAppHost = applicationFactory.init(originParams);

      originAppHost.request(idu01Dto);

      InterfaceDataUnit01Dto resultDto = targetAppHost.indication();
      assertEquals(idu01Dto.getMessage(), resultDto.getMessage());
      assertEquals(idu01Dto.getContext().getPga(),
          resultDto.getContext().getPga());
      assertNotEquals(idu01Dto.getContext().getTrackingNumber(),
          resultDto.getContext().getTrackingNumber());
      assertEquals(Boolean.FALSE, resultDto.getContext().getOptions());
      ProtocolDataUnit1APPDto actualResultPdu = objectMapper.readValue(resultDto.getMessage(),
          ProtocolDataUnit1APPDto.class);

      HeaderDto headerDto = new HeaderDto(HeaderDto.Msgtype.TACHE_RECEPTION,
          actualResultPdu.getHeader().getId(), false, actualResultPdu.getHeader().getSpeds());
      ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(headerDto, "any");
      String serialPdu = objectMapper.writeValueAsString(pdu);
      InterfaceDataUnit01Dto response = new InterfaceDataUnit01Dto(resultDto.getContext(),
          serialPdu);
      targetAppHost.response(response);

      InterfaceDataUnit01Dto actualResponse = originAppHost.confirm();
      ProtocolDataUnit1APPDto actualResponsePdu =
          objectMapper.readValue(actualResponse.getMessage(),
              ProtocolDataUnit1APPDto.class);
      assertEquals(pdu.getContent(), actualResponsePdu.getContent());
    }
  }
}
