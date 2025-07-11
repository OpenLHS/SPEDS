package ca.griis.speds.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;

import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.integration.util.DomainProviderUtil;
import ca.griis.speds.integration.util.KeyVar;
import ca.griis.speds.integration.util.PgaServiceMock;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.SyncNetworkFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.SyncSessionFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.sync.SyncTransportFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ServerSocket;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.Mockito;

public class FullSpedsIT {
  private PgaService pgaServiceMock;
  private PgaServiceMock serviceMock;

  private SyncApplicationFactory originAppFactory;
  private SyncApplicationFactory targetAppFactory;

  private MutablePresentationFactory originPresFactory;
  private MutablePresentationFactory targetPresFactory;

  private SyncSessionFactory originSesFactory;
  private SyncSessionFactory targetSesFactory;

  private SyncTransportFactory originTransFactory;
  private SyncTransportFactory targetTransFactory;

  private SyncNetworkFactory networkFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;

  @BeforeProperty
  public void setup() {
    pgaServiceMock = Mockito.mock(PgaService.class);

    serviceMock = new PgaServiceMock();

    networkFactory = new SyncNetworkFactory();

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }


  @Property(tries = 1)
  @Domain(DomainProviderUtil.class)
  public void fullSpedsExchangeTest(@ForAll InterfaceDataUnit01Dto idu01Dto) throws Exception {

    try (ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0)) {
      // todo
      testUtil.freePorts(originSocket, targetSocket, null);

      String originIri = "https://localhost:%1$s".formatted(originSocket.getLocalPort());

      String targetIri = "https://localhost:%1$s".formatted(targetSocket.getLocalPort());

      serviceMock.mock(pgaServiceMock, idu01Dto.getContext().getPga(),
          idu01Dto.getContext().getSourceCode(), idu01Dto.getContext().getDestinationCode(),
          originIri, targetIri);

      final String originParams =
          """
              {
                "options": {
                  "speds.app.version":"2.0.0",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.ses.version":"3.0.0",
                  "speds.ses.reference": "https://reference.iri/speds",
                  "speds.ses.cert": "%1$s",
                  "speds.ses.private.key": "%2$s",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "%1$s",
                  "speds.net.private.key": "%2$s",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIIDlTCCAn2gAwIBAgIUDylZ/ORta2ZLQhFD6PJF0U0oKpcwDQYJKoZIhvcNAQELBQAwWjELMAkGA1UEBhMCQ0ExCzAJBgNVBAgMAlFDMRMwEQYDVQQHDApTaGVyYnJvb2tlMQwwCgYDVQQKDANkZXYxDDAKBgNVBAsMA2RldjENMAsGA1UEAwwEdGVzdDAeFw0yNTA0MTAxNjA3NTlaFw0yNjA0MTAxNjA3NTlaMFoxCzAJBgNVBAYTAkNBMQswCQYDVQQIDAJRQzETMBEGA1UEBwwKU2hlcmJyb29rZTEMMAoGA1UECgwDZGV2MQwwCgYDVQQLDANkZXYxDTALBgNVBAMMBHRlc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCzz5a0f9uCxzRWGQB8hRaDECfyy97l5My4HipU+KNw0DMwrS4d72WMI2l3/5kqw3uallbNeUil8TcMEbC/UCUJjL4yP2EHz549GI+LAHa2h39+d8RD939UHpXhtvc/ZTO6z7m/60yViNUpYaS0Q3RHP3WOY+YUzh9cIb2XWAIavxgBscCeyWsBZlrUMqB2oJfTzKxhKzX9mdbnLDaVy3FTF6B1Qd0RF+bHMwpxlQdJoY7kiske+ydK08lCP0Ax4Me3YqrqxvMAf8WJqzzCwtUbmFWYCt+xpoN71n2POKO57114cIpbQ3nH3cvHaltdkk6G2C936JYlMRS7bhqIi9L5AgMBAAGjUzBRMB0GA1UdDgQWBBSWO0fG5GK81qQ7d+9RwgkB51YtrDAfBgNVHSMEGDAWgBSWO0fG5GK81qQ7d+9RwgkB51YtrDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBeY10jaqrEGol/5MxVUWm9xJe/61GLTHYaiCiZWGiL9fPY4fotBf5CJYYf2k6c2WkNkrQjPoopXwtgPTg7+Vxw4pS2rnURB2Uy5LY9a4HjMMSYmz1uYQQYcLaX6s2I5KULovXZ4lrPyGMjsYCk68+haUS4JXMxKh8wM1FegPKzI2qQM6lJ0AwxZwyVkStUYivOcLDX/xNKULf0bVyfFI+twawxg8+w61WNmuZ4eRH/Kzf9FO3WMrctzMYb/KVp6GciPbOsRm86m7mrSVqUnIDaOZv8+W6/ZdimnCo4nEzTvrEYL5iLDXacEyufUt1lkz4syegDEYt4dIHH5nmyqfOQ",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCzz5a0f9uCxzRWGQB8hRaDECfyy97l5My4HipU+KNw0DMwrS4d72WMI2l3/5kqw3uallbNeUil8TcMEbC/UCUJjL4yP2EHz549GI+LAHa2h39+d8RD939UHpXhtvc/ZTO6z7m/60yViNUpYaS0Q3RHP3WOY+YUzh9cIb2XWAIavxgBscCeyWsBZlrUMqB2oJfTzKxhKzX9mdbnLDaVy3FTF6B1Qd0RF+bHMwpxlQdJoY7kiske+ydK08lCP0Ax4Me3YqrqxvMAf8WJqzzCwtUbmFWYCt+xpoN71n2POKO57114cIpbQ3nH3cvHaltdkk6G2C936JYlMRS7bhqIi9L5AgMBAAECggEAJvGx1BeyIR3Ih9JD8PQ5RzsvT86YkQWwUjtUU1GDwqoRQDxt9dVVEthTnkYZdDHhGj7v/3JCihBXqhFFzPXMg8g8JHFmMFUnElf4tPQtggSQWEGT3F2lMgCq2TdxzwT6An69niPWEzgO5PbNP66xZ0IHEccwvLZVA/UZ1UbeM9DtLu3P+KMFj4pp5IBo6pczgcS0QtMdsrHUE8X4RrtgTuOp7tn2SV6wAyKMHW8oacutxZZzbOLUMNfrOyemtQnXZ4Gz6tWa8+3tCUkrXvAQaTDCFkj/zn380Ik4s7kIVEyzjkhQ9jnO4j2BnaVN/HVvbfNROFC0vVWNqbknPmru9QKBgQDYhm4nehseMQDlaLpeJ8EWRRs8msWvSbwK0i2/fApkX3LC4DWellvdCaHfmGmFTDeR0NvXkuOhsEcR45lKJcb8Tx72/3b62yaagiQX+3tIMd1dCL5Zp8T5gAEL75VlnKVQYGxJ8Yyxvx+zWr5KEDt7JQ0FA2sly0aVu5R9kvoyPQKBgQDUl6W/whXn4J18sItvzE2bOz+JnopYXk4+U2xF9+Tucu8yjAumof+K3TkGBS0UYVHiCXLgTJtguiHfcE6geFSsIcsed7M47JaevJY5FsWieEZxeSnQCvU0pgG+YjDrWke5APOYkKge/u3BuamZTq8pNLNTCp5clEFmaqVZ9xgbbQKBgQCaGGnyvGrqPLPHkJX6Bk7be4kbw4Zm7pHeHaCjQzLeJjO1Tv26BIYSNBW43G5UiF6P7tVWgVpxKtQZfiIM6//GdsSxwjO56hd6JJ5tVvNw+NPyrxNRGR4M9rVH+lUXgLkCD+1hXn/jzAJSkYUVjqHWTRML+1fZCOcODvZpvB1FfQKBgFYzv4PH4TYKwBElTQTiJL3DAnp9DL/UTYm8LfUZFX0SoacvXjINEh9uoIauZp8S7y7mgewtY/uOvdlqIpey8zJw6XnLM6LrXA+1jHxNnYnJl1a/uJKhPthAUAiwrAFitB5yIlREo8cdu66H6Bs/6oqc0fHkJl6HxxUOPUoDhYTpAoGAf3BUWlafVOOEx/Yc85bT8e8vn7IPqyUxFNe5/J3FDgj9HWP54Lle+y5G1VBeb0IVQPy6YxWB5jhshvpmryQULeXVxwYC6essEXBUXdeqfRRcAkO/aDyjlSZIiAQWAmXp0TLS8+JAXWOdySMbO4rJHHluZ1d/iGcTzMyh2QP8on0=",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %3$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  originSocket.getLocalPort());


      final String targetParams =
          """
              {
                "options": {
                  "speds.app.version":"2.0.0",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.ses.version":"3.0.0",
                  "speds.ses.reference": "https://reference.iri/speds",
                  "speds.ses.cert": "%1$s",
                  "speds.ses.private.key": "%2$s",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "%1$s",
                  "speds.net.private.key": "%2$s",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIIDhTCCAm2gAwIBAgIUBAFWukNh1P1hIceIcRb9NvR//HUwDQYJKoZIhvcNAQELBQAwUjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDTALBgNVBAMMBFRlc3QwHhcNMjUwMjIwMTYwOTIzWhcNMjYwMjIwMTYwOTIzWjBSMQswCQYDVQQGEwJDQTEPMA0GA1UECAwGUXVlYmVjMRMwEQYDVQQHDApTaGVyYnJvb2tlMQ4wDAYDVQQKDAVHUklJUzENMAsGA1UEAwwEVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKnNlcoNfZDVuC8slHdjd1Wac3Dt2ccLpUoCJzbmultKuW+z95iCMnaCvoNvLTNc4oaVl/iJ4427s1+XWcvrwuD1Zelj6kc7IxqsX+wGOGPb1XNc+6cjDynJwE91FLBSvlxF1QSPQwP4aUgp/sZL4eJKjdojrOHkAjqZkKsdHhsMiQIAJMhS8VodaxihryWV9XuQBwxAnGPGeG+iGLfOpnKQOth1Dva8EK94x05il+JeZiyw7P5/9MlkSpKyFmXzZ2x0rleLLlblTeLrnbuK1otC/iDvpHADoWcEV9keW4eH975CjcLQhc5TKx5LSUDrFYV0MpVE2iqLFVwqqB4dUoECAwEAAaNTMFEwHQYDVR0OBBYEFD8KOlGdKZr17kb0Zv5CdWOz5cQdMB8GA1UdIwQYMBaAFD8KOlGdKZr17kb0Zv5CdWOz5cQdMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH5iKkUnKGOMODV4I9lYaXFVLl1MA23ceuhIpvmV/Hqjoeig+/FrbuAsq8iRsiibGtBGuCcrjuqGaXEkAX2B+nA6iY1/Z3T0u+7aKHxvbxlaHbUVXc1P45Ps7Ef46zSMc+uRQWcysLdKubh14eh/lubI8uqq8TSYLgMziOW5OBwV5Im6J0a2XrETMfbvbZse7U+lvp1FWwTmjGbNZMMR3uGziPyEVAIZ6+S644VQWhA+DS492MWLOZx9mwxhG8faVtXKNL+SJrgmzCdJUmRT2ypTYqrZnCoy1QI5DX02WdQ2hS1pyg69/HoIWD03Wv/XRdOon11gpVWspzprLDy5KoU=",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCpzZXKDX2Q1bgvLJR3Y3dVmnNw7dnHC6VKAic25rpbSrlvs/eYgjJ2gr6Dby0zXOKGlZf4ieONu7Nfl1nL68Lg9WXpY+pHOyMarF/sBjhj29VzXPunIw8pycBPdRSwUr5cRdUEj0MD+GlIKf7GS+HiSo3aI6zh5AI6mZCrHR4bDIkCACTIUvFaHWsYoa8llfV7kAcMQJxjxnhvohi3zqZykDrYdQ72vBCveMdOYpfiXmYssOz+f/TJZEqSshZl82dsdK5Xiy5W5U3i6527itaLQv4g76RwA6FnBFfZHluHh/e+Qo3C0IXOUyseS0lA6xWFdDKVRNoqixVcKqgeHVKBAgMBAAECggEABo9/yPgFd0Bl2d8mK1WBL+8EPjaj+LNKYxYhnFbrMbzGdmDkx5RimiT9P5T4XgKQNzTCoHxfYBBo/Hmbo0VyjABSb2NU+Wr5AiS30T9nE8Cghuqh4UUsmefTUZ6YVCNJeviXVt8PVUqjP4HT3kB8Gglyx2m9pvEJMjXQ55n75rD6Rd4eqZCorGC/JYU/gurmmnnpyW3Nmii4cvDGvFwr8jC4CZN0jwrkwWj9X+Fv4dNNDLH4LxTvj03NBRU0ThyWLas/EE/NzA1mv713VMPyZwx7UvMxxto91GWv/nbh4sTNcBfKO+ZK+UrNhGWRkFG0kwjo0wzs4vYXN7RSjTpPuQKBgQDYI0VtMRN4WRreeRNntiPTBIA4w8Z4b4aCoeNY2WIVkNACSZgBNo9rn7SqM0yw0beaWNqSBIHSX5Ch5QMBW6QRRt6ZK6Tq3uNjui7J2uv+d/j+838R+2OoSqChYLDy/vdxUzCpVpkaisonIarKfcoqqc7SLRjsu845HQPt6erG3QKBgQDJHqq626GOzDuWp+hG1Qq3JdW3ZkXY8LqtQcbzhKKAz3w8u+zh++E+QscJfrr5sRoVzdmkr0lcMGeo1O3gtVWqnzWT4vzzLZi/l0+pKKCgbeiBzEI+mNFIQnZ1O/iKJQfs+fblNl3ub1Mlxm17sKFMFWWi0I3hnXgkv1BlWBJ19QKBgQC98TFAJlLP/q6IOKr/B6gv99KfEB3JFWmGP7LGEDQMc7j5aad12Xbsw+tHb9HDymmp8NAUZnWYZXd7bwDXHqvuqvNQdHR4G+yFZcdciVG/zbs6gs53BQ+tg/fqGkknIz5djxhCmOHv22yQOxwW27jhCV3CgvNWiC1RL9iWKm2y2QKBgGVtRNbliq10TBznYtnN+RByUTyjpFgK12onAQmwey+Q8+vBLm6tU2PN04jzU6I28ZvLa5aFG+8VLkHT2H95k9FvZ1rEn6KX/S+qRG9f4Nnnc9l5xHLDKNBTTGBFNUud70hQq3XfHDHyDLHBR1eYtU+kftREbzk36+5EWWwypWS9AoGAYdu5nDPqRAbHgCMknPzpZHoaPYdRBA4Z8jfbfKkSRscQEmkjRtyUumvju4lsdGzUdbIXmE1Or+cKF409Gu1hPPdA+H1LKx56ozwyUyGfM3XYPIcmtLu9GnR3d6tjoC2AsXqR2TX9gkdAIQjdlC5wsBz6HzOVZbLNtfJb3m/p4lA=",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %3$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(KeyVar.localhostCertRsa, KeyVar.localhostPrikeyRsa,
                  targetSocket.getLocalPort());

      // ********************************
      // Client Initialisation
      // ********************************

      // network-client-init-success
      NetworkHost originNetHostSpy = Mockito.spy(networkFactory.initHost(originParams));

      doNothing().when(originNetHostSpy).confirm();

      // trans-client-init-success
      originTransFactory =
          new SyncTransportFactory() {
            @Override
            public NetworkHost initNetworkHost(String parameters) {
              return originNetHostSpy;
            }
          };

      TransportHost originTransHost = originTransFactory.init(originParams);

      // session-client-init-success
      originSesFactory = new SyncSessionFactory(pgaServiceMock) {
        @Override
        public TransportHost initTransportHost(String parameters) {
          return originTransHost;
        }
      };

      SessionHost originSesHost = originSesFactory.init(originParams);


      // presentation-client-init-success
      originPresFactory = new MutablePresentationFactory(pgaServiceMock) {
        @Override
        public SessionHost initSessionHost(String parameters) {
          return originSesHost;
        }
      };

      PresentationHost originPresHost = originPresFactory.init(originParams);


      // app-client-init-success
      originAppFactory = new SyncApplicationFactory(pgaServiceMock) {
        @Override
        public PresentationHost initPresentationHost(String parameters) {
          return originPresHost;
        }
      };

      ApplicationHost originAppHost = originAppFactory.init(originParams);


      // ********************************
      // Server Initialisation
      // ********************************

      // network-server-init-success
      NetworkHost targetNetHostSpy = Mockito.spy(networkFactory.initHost(targetParams));

      doNothing().when(targetNetHostSpy).confirm();

      // trans-server-init-success
      targetTransFactory =
          new SyncTransportFactory() {
            @Override
            public NetworkHost initNetworkHost(String parameters) {
              return targetNetHostSpy;
            }
          };

      TransportHost targetTransHost = targetTransFactory.init(targetParams);

      // session-server-init-success
      targetSesFactory = new SyncSessionFactory(pgaServiceMock) {
        @Override
        public TransportHost initTransportHost(String parameters) {
          return targetTransHost;
        }
      };

      SessionHost targetSesHost = targetSesFactory.init(targetParams);


      // presentation-server-init-success
      targetPresFactory = new MutablePresentationFactory(pgaServiceMock) {
        @Override
        public SessionHost initSessionHost(String parameters) {
          return targetSesHost;
        }
      };

      PresentationHost targetPresHost = targetPresFactory.init(targetParams);


      // app-server-init-success
      targetAppFactory = new SyncApplicationFactory(pgaServiceMock) {
        @Override
        public PresentationHost initPresentationHost(String parameters) {
          return targetPresHost;
        }
      };

      ApplicationHost targetAppHost = targetAppFactory.init(targetParams);


      // ********************************
      // Exchange Processus
      // ********************************

      // app-client-request-success
      originAppHost.request(idu01Dto);


      // app-server-indication-success
      InterfaceDataUnit01Dto receivedIdu01Dto = targetAppHost.indication();


      // ********************************
      // Verifications idu reçu
      // ********************************

      assertNotNull(receivedIdu01Dto);

      assertEquals(idu01Dto.getMessage(), receivedIdu01Dto.getMessage());
      assertEquals(idu01Dto.getContext().getPga(), receivedIdu01Dto.getContext().getPga());
      assertEquals(idu01Dto.getContext().getSourceCode(),
          receivedIdu01Dto.getContext().getSourceCode());
      assertEquals(idu01Dto.getContext().getDestinationCode(),
          receivedIdu01Dto.getContext().getDestinationCode());
    }

  }

}
