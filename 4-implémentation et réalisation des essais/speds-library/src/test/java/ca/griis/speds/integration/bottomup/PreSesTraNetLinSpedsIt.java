package ca.griis.speds.integration.bottomup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.PgaServiceMock;
import ca.griis.speds.integration.util.PresentationDomainProviderUtil;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.link.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
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
public class PreSesTraNetLinSpedsIt {

  private MutablePresentationFactory presentationFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;

  private PgaService pgaService;

  @BeforeProperty
  public void setup() {
    pgaService = Mockito.mock(PgaService.class);

    presentationFactory = new MutablePresentationFactory(pgaService);

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }

  @Property(tries = 1)
  @Domain(PresentationDomainProviderUtil.class)
  public void PreSesNetLinkExchangeTest(@ForAll InterfaceDataUnit12Dto idu12Dto)
      throws IOException, InterruptedException, CertificateException {

    new PgaServiceMock().mock(pgaService, 4020, 4021);
    try (ServerSocket originSocket = new ServerSocket(4020);
        ServerSocket targetSocket = new ServerSocket(4021)) {
      // todo
      testUtil.freePorts(originSocket, targetSocket, null);

      final String originParams =
          """
              {
                "options": {
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

      // presentation-server-init-success
      PresentationHost targetPreHost = presentationFactory.init(targetParams);

      // presentation-client-request-success
      PresentationHost originPreHost = presentationFactory.init(originParams);
      String idu12 = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu12Dto);

      originPreHost.request(idu12);

      String result = targetPreHost.indication();
      InterfaceDataUnit12Dto resultDto;

      resultDto = objectMapper.readValue(result, InterfaceDataUnit12Dto.class);

      assertEquals(idu12Dto.getMessage(), resultDto.getMessage());
      assertEquals(idu12Dto.getContext().getPga(),
          resultDto.getContext().getPga());
      assertNotEquals(idu12Dto.getContext().getTrackingNumber(),
          resultDto.getContext().getTrackingNumber());
      assertEquals(Boolean.FALSE, resultDto.getContext().getOptions());
    }
  }

}
