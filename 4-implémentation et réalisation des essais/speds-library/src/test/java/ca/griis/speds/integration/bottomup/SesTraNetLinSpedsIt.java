package ca.griis.speds.integration.bottomup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.PgaServiceMock;
import ca.griis.speds.integration.util.SessionDomainProviderUtil;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.link.serializer.SharedObjectMapper;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.SyncSessionFactory;
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
public class SesTraNetLinSpedsIt {

  private SyncSessionFactory sessionFactory;
  private ObjectMapper objectMapper;
  private TestUtil testUtil;
  private PgaService pgaService;

  @BeforeProperty
  public void setup() {
    pgaService = Mockito.mock(PgaService.class);
    sessionFactory = new SyncSessionFactory(pgaService);
    objectMapper = new ObjectMapper();
    testUtil = new TestUtil(objectMapper);
  }

  @Property(tries = 1)
  @Domain(SessionDomainProviderUtil.class)
  public void SesNetLinkExchangeTest(@ForAll InterfaceDataUnit23Dto idu23Dto)
      throws IOException, CertificateException {

    new PgaServiceMock().mock(pgaService, 4030, 4031);

    try (ServerSocket originSocket = new ServerSocket(4030);
        ServerSocket targetSocket = new ServerSocket(4031)) {
      testUtil.freePorts(originSocket, targetSocket, null);

      final String originParams =
          """
              {
                "options": {
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

      // session-server-init-success
      SessionHost targetSesHost = sessionFactory.init(targetParams);

      // session-client-init-success
      SessionHost originSesHost = sessionFactory.init(originParams);
      String idu23 = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu23Dto);

      // session-client-request-success
      originSesHost.requestFuture(idu23);

      // session-server-indication-success
      String result = targetSesHost.indication();
      InterfaceDataUnit23Dto resultDto;
      resultDto = objectMapper.readValue(result, InterfaceDataUnit23Dto.class);
      assertEquals(idu23Dto.getMessage(), resultDto.getMessage());
      assertEquals(idu23Dto.getContext().getSdek(),
          resultDto.getContext().getSdek());
      assertEquals(idu23Dto.getContext().getPga(),
          resultDto.getContext().getPga());
      assertNotEquals(idu23Dto.getContext().getTrackingNumber(),
          resultDto.getContext().getTrackingNumber());
      assertEquals(Boolean.FALSE, resultDto.getContext().getOptions());

      // session-server-response-success
      Context23Dto responseContext = new Context23Dto(resultDto.getContext().getPga(),
          resultDto.getContext().getDestinationCode(), resultDto.getContext().getSourceCode(),
          resultDto.getContext().getTrackingNumber(), false);
      String responseContent = "anwser";
      InterfaceDataUnit23Dto response = new InterfaceDataUnit23Dto(responseContext,
          responseContent);
      String serialResponse = objectMapper.writeValueAsString(response);
      targetSesHost.response(serialResponse);

      // session-client-confirm-success
      String actualResponse = originSesHost.confirm();
      assertNotNull(actualResponse);
      InterfaceDataUnit23Dto actualIdu = objectMapper.readValue(actualResponse,
          InterfaceDataUnit23Dto.class);
      assertEquals(responseContent, actualIdu.getMessage());

      originSesHost.close();
      targetSesHost.close();
    }
  }
}
