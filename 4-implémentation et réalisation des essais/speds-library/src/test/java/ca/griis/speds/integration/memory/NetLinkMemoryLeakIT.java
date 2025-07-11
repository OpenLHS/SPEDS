package ca.griis.speds.integration.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ContextDto__1;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.link.api.sync.ImmutableDataLinkFactory;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.SyncNetworkFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@Disabled
public class NetLinkMemoryLeakIT {
  private PgaService pgaServiceMock;
  private SessionHost sessionHostMock;

  private SyncApplicationFactory appFactory;
  private MutablePresentationFactory presentationFactory;
  private SyncNetworkFactory networkFactory;
  private ImmutableDataLinkFactory linkFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;

  @BeforeEach
  public void setup() {
    pgaServiceMock = Mockito.mock(PgaService.class);
    sessionHostMock = Mockito.mock(SessionHost.class);

    presentationFactory = new MutablePresentationFactory(pgaServiceMock) {
      @Override
      public SessionHost initSessionHost(String parameters) {
        return sessionHostMock;
      }
    };

    linkFactory = new ImmutableDataLinkFactory();

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }

  @Test
  public void netLinkMemoryLeakTest() throws IOException {
    // INITIALISATIONS SPEDS
    try (ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0);
        ServerSocket proxySocket = new ServerSocket(0)) {
      // todo
      testUtil.freePorts(originSocket, targetSocket, proxySocket);

      final String originParams =
          """
              {
                "options": {
                  "speds.app.version":"2.0.0",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "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",
                  "speds.net.private.key": "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",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIIDlTCCAn2gAwIBAgIUDylZ/ORta2ZLQhFD6PJF0U0oKpcwDQYJKoZIhvcNAQELBQAwWjELMAkGA1UEBhMCQ0ExCzAJBgNVBAgMAlFDMRMwEQYDVQQHDApTaGVyYnJvb2tlMQwwCgYDVQQKDANkZXYxDDAKBgNVBAsMA2RldjENMAsGA1UEAwwEdGVzdDAeFw0yNTA0MTAxNjA3NTlaFw0yNjA0MTAxNjA3NTlaMFoxCzAJBgNVBAYTAkNBMQswCQYDVQQIDAJRQzETMBEGA1UEBwwKU2hlcmJyb29rZTEMMAoGA1UECgwDZGV2MQwwCgYDVQQLDANkZXYxDTALBgNVBAMMBHRlc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCzz5a0f9uCxzRWGQB8hRaDECfyy97l5My4HipU+KNw0DMwrS4d72WMI2l3/5kqw3uallbNeUil8TcMEbC/UCUJjL4yP2EHz549GI+LAHa2h39+d8RD939UHpXhtvc/ZTO6z7m/60yViNUpYaS0Q3RHP3WOY+YUzh9cIb2XWAIavxgBscCeyWsBZlrUMqB2oJfTzKxhKzX9mdbnLDaVy3FTF6B1Qd0RF+bHMwpxlQdJoY7kiske+ydK08lCP0Ax4Me3YqrqxvMAf8WJqzzCwtUbmFWYCt+xpoN71n2POKO57114cIpbQ3nH3cvHaltdkk6G2C936JYlMRS7bhqIi9L5AgMBAAGjUzBRMB0GA1UdDgQWBBSWO0fG5GK81qQ7d+9RwgkB51YtrDAfBgNVHSMEGDAWgBSWO0fG5GK81qQ7d+9RwgkB51YtrDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBeY10jaqrEGol/5MxVUWm9xJe/61GLTHYaiCiZWGiL9fPY4fotBf5CJYYf2k6c2WkNkrQjPoopXwtgPTg7+Vxw4pS2rnURB2Uy5LY9a4HjMMSYmz1uYQQYcLaX6s2I5KULovXZ4lrPyGMjsYCk68+haUS4JXMxKh8wM1FegPKzI2qQM6lJ0AwxZwyVkStUYivOcLDX/xNKULf0bVyfFI+twawxg8+w61WNmuZ4eRH/Kzf9FO3WMrctzMYb/KVp6GciPbOsRm86m7mrSVqUnIDaOZv8+W6/ZdimnCo4nEzTvrEYL5iLDXacEyufUt1lkz4syegDEYt4dIHH5nmyqfOQ",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCzz5a0f9uCxzRWGQB8hRaDECfyy97l5My4HipU+KNw0DMwrS4d72WMI2l3/5kqw3uallbNeUil8TcMEbC/UCUJjL4yP2EHz549GI+LAHa2h39+d8RD939UHpXhtvc/ZTO6z7m/60yViNUpYaS0Q3RHP3WOY+YUzh9cIb2XWAIavxgBscCeyWsBZlrUMqB2oJfTzKxhKzX9mdbnLDaVy3FTF6B1Qd0RF+bHMwpxlQdJoY7kiske+ydK08lCP0Ax4Me3YqrqxvMAf8WJqzzCwtUbmFWYCt+xpoN71n2POKO57114cIpbQ3nH3cvHaltdkk6G2C936JYlMRS7bhqIi9L5AgMBAAECggEAJvGx1BeyIR3Ih9JD8PQ5RzsvT86YkQWwUjtUU1GDwqoRQDxt9dVVEthTnkYZdDHhGj7v/3JCihBXqhFFzPXMg8g8JHFmMFUnElf4tPQtggSQWEGT3F2lMgCq2TdxzwT6An69niPWEzgO5PbNP66xZ0IHEccwvLZVA/UZ1UbeM9DtLu3P+KMFj4pp5IBo6pczgcS0QtMdsrHUE8X4RrtgTuOp7tn2SV6wAyKMHW8oacutxZZzbOLUMNfrOyemtQnXZ4Gz6tWa8+3tCUkrXvAQaTDCFkj/zn380Ik4s7kIVEyzjkhQ9jnO4j2BnaVN/HVvbfNROFC0vVWNqbknPmru9QKBgQDYhm4nehseMQDlaLpeJ8EWRRs8msWvSbwK0i2/fApkX3LC4DWellvdCaHfmGmFTDeR0NvXkuOhsEcR45lKJcb8Tx72/3b62yaagiQX+3tIMd1dCL5Zp8T5gAEL75VlnKVQYGxJ8Yyxvx+zWr5KEDt7JQ0FA2sly0aVu5R9kvoyPQKBgQDUl6W/whXn4J18sItvzE2bOz+JnopYXk4+U2xF9+Tucu8yjAumof+K3TkGBS0UYVHiCXLgTJtguiHfcE6geFSsIcsed7M47JaevJY5FsWieEZxeSnQCvU0pgG+YjDrWke5APOYkKge/u3BuamZTq8pNLNTCp5clEFmaqVZ9xgbbQKBgQCaGGnyvGrqPLPHkJX6Bk7be4kbw4Zm7pHeHaCjQzLeJjO1Tv26BIYSNBW43G5UiF6P7tVWgVpxKtQZfiIM6//GdsSxwjO56hd6JJ5tVvNw+NPyrxNRGR4M9rVH+lUXgLkCD+1hXn/jzAJSkYUVjqHWTRML+1fZCOcODvZpvB1FfQKBgFYzv4PH4TYKwBElTQTiJL3DAnp9DL/UTYm8LfUZFX0SoacvXjINEh9uoIauZp8S7y7mgewtY/uOvdlqIpey8zJw6XnLM6LrXA+1jHxNnYnJl1a/uJKhPthAUAiwrAFitB5yIlREo8cdu66H6Bs/6oqc0fHkJl6HxxUOPUoDhYTpAoGAf3BUWlafVOOEx/Yc85bT8e8vn7IPqyUxFNe5/J3FDgj9HWP54Lle+y5G1VBeb0IVQPy6YxWB5jhshvpmryQULeXVxwYC6essEXBUXdeqfRRcAkO/aDyjlSZIiAQWAmXp0TLS8+JAXWOdySMbO4rJHHluZ1d/iGcTzMyh2QP8on0=",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %1$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(originSocket.getLocalPort());


      final String targetParams =
          """
              {
                "options": {
                  "speds.app.version":"2.0.0",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "MIIGATCCA+mgAwIBAgIUAPRNG4LGNLFzLNSG+cIrpwQtZjcwDQYJKoZIhvcNAQELBQAwgY4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxFDASBgNVBAoMC3VzaGVyYnJvb2tlMQ4wDAYDVQQLDAVncmlpczEOMAwGA1UEAwwFYWRtaW4xIzAhBgkqhkiG9w0BCQEWFGFkbWluQHVzaGVyYnJvb2tlLmNhMCAXDTI1MDMxNDEzMzUzOFoYDzIxMjUwMjE4MTMzNTM4WjCBjjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEUMBIGA1UECgwLdXNoZXJicm9va2UxDjAMBgNVBAsMBWdyaWlzMQ4wDAYDVQQDDAVhZG1pbjEjMCEGCSqGSIb3DQEJARYUYWRtaW5AdXNoZXJicm9va2UuY2EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQDiHZz5bxw0qPREQLyLzUAgbYj5HD2aT+ao/AuDpMbE1U1CasUkrfhGVR5OCnYgYh+LGQISMoijSQSEA5jX1AyBs0uC0PSwhcTbFIZK0afeY9KC9iMgWW7UON8UItYAlgZ4XA3ZvS3eryVmFKL8ROM1oFEk0E5MK9njKoia9A8uPFnNiMCctNb6LjyJ1jbxi631IjrCH9B/1vZPFVi0+yEJNeKMXwdrl2fRvHzPInbFkU2ntu22N83Fd/aXJd1nnko23XoNyrXeSrMJsKri9DGz2/Yc0V4fgT5AE/lwAfCK5XK8ELdazD0utmW+VwvOo1PkgfAsBxzgffK5iI2wWYfoBtHyfgvnTrHg8CwmM6ctpT4o+BAkuoYnf/HjsD2U9Vr/2dGCT6G23gjGvlmKrUvPsZ0fbxxAlBOPudyhFAp6EoQIDjQot0Fjjc54lORX5Po56yxY1UpCWIhgcjiL4YZyIBGSU4MKcxE0QntcSiORCLAXVCi3lmUO84uGkm5aw4LHXDOZ4fN8cPi0fH6WpeCnoCv0MeyopxGmJ25pA8ZNWwmIAUYM+ylq5B6OGb3RCr3iBBTJ0qKljYTW5xsFeatc4o4e5DezNPENl1wLYbrvrXv2k+J5FyjQq+fyn9sRU0cFRq+cZdbj1twdyjhsWV53rRhQeNmbEcqCz3QO566u+QIDAQABo1MwUTAdBgNVHQ4EFgQUS6jWDr4SY28wiUfdhDvxDePDzUQwHwYDVR0jBBgwFoAUS6jWDr4SY28wiUfdhDvxDePDzUQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAEYPIefKIitzYR6wmjCRG5gcW1Awy86XE5Cy63Hs1z5hGGiMeslee9czEQkwqp/iACfPSs9yWF0wvoIq03WN4H0SpLTNnWwa/GbW/NJHh7hOsfQGAyR/jAleE3jNa8z2MWSidLiFop5ryJpztq4X1ftKYrgJ2QhHDGqwMNNgxb473GLa9fRZf8GFpDgzX3wbbJsprEV9FU3dXT1sQogJTloFMqdOKbepLzD491/NaOojaMnL3P0qG/TXQ0wgO3yyw1CGrSVEFw9ceZVYZK3xK4YHo5i3Gc1ejNVC7FXUdkSTuAD/guGSCLVuMX7z3NDSvA1Uj0W33Maa/hIvnpPp/mLR9bIGH267/JDhKihewUzxKeHraa5a/RznxJ7zn0sZ7Yq7Q9eUH8U5vJztPG0XZqVQzp3IiMsi9fia5B63+kJWh5Q+jV4QcUx/kUUxf0XnXMmnFqGtaU5lc9broVnV1ReTnto/Wa+/SwtO64Lsfqigu0Lv4EO6uuG+m+TNq373tB5VuC7mnyhZPL4HHdWDlv/JWZLNYpGRr77JBgjTZe48X8aLxmIk3Pstpe98RKOGpI0I7uOXRbgoxCPNd3sqKz4bM0fJ6bCIYU59aYh5qbhvsywxmKuDXeBDBp5zNQS7sk7uDP6GTiJp8t+CiEskhR1X//zxYKmEG+yYC5MeQKPY=",
                  "speds.net.private.key": "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",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIIDhTCCAm2gAwIBAgIUBAFWukNh1P1hIceIcRb9NvR//HUwDQYJKoZIhvcNAQELBQAwUjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDTALBgNVBAMMBFRlc3QwHhcNMjUwMjIwMTYwOTIzWhcNMjYwMjIwMTYwOTIzWjBSMQswCQYDVQQGEwJDQTEPMA0GA1UECAwGUXVlYmVjMRMwEQYDVQQHDApTaGVyYnJvb2tlMQ4wDAYDVQQKDAVHUklJUzENMAsGA1UEAwwEVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKnNlcoNfZDVuC8slHdjd1Wac3Dt2ccLpUoCJzbmultKuW+z95iCMnaCvoNvLTNc4oaVl/iJ4427s1+XWcvrwuD1Zelj6kc7IxqsX+wGOGPb1XNc+6cjDynJwE91FLBSvlxF1QSPQwP4aUgp/sZL4eJKjdojrOHkAjqZkKsdHhsMiQIAJMhS8VodaxihryWV9XuQBwxAnGPGeG+iGLfOpnKQOth1Dva8EK94x05il+JeZiyw7P5/9MlkSpKyFmXzZ2x0rleLLlblTeLrnbuK1otC/iDvpHADoWcEV9keW4eH975CjcLQhc5TKx5LSUDrFYV0MpVE2iqLFVwqqB4dUoECAwEAAaNTMFEwHQYDVR0OBBYEFD8KOlGdKZr17kb0Zv5CdWOz5cQdMB8GA1UdIwQYMBaAFD8KOlGdKZr17kb0Zv5CdWOz5cQdMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH5iKkUnKGOMODV4I9lYaXFVLl1MA23ceuhIpvmV/Hqjoeig+/FrbuAsq8iRsiibGtBGuCcrjuqGaXEkAX2B+nA6iY1/Z3T0u+7aKHxvbxlaHbUVXc1P45Ps7Ef46zSMc+uRQWcysLdKubh14eh/lubI8uqq8TSYLgMziOW5OBwV5Im6J0a2XrETMfbvbZse7U+lvp1FWwTmjGbNZMMR3uGziPyEVAIZ6+S644VQWhA+DS492MWLOZx9mwxhG8faVtXKNL+SJrgmzCdJUmRT2ypTYqrZnCoy1QI5DX02WdQ2hS1pyg69/HoIWD03Wv/XRdOon11gpVWspzprLDy5KoU=",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCpzZXKDX2Q1bgvLJR3Y3dVmnNw7dnHC6VKAic25rpbSrlvs/eYgjJ2gr6Dby0zXOKGlZf4ieONu7Nfl1nL68Lg9WXpY+pHOyMarF/sBjhj29VzXPunIw8pycBPdRSwUr5cRdUEj0MD+GlIKf7GS+HiSo3aI6zh5AI6mZCrHR4bDIkCACTIUvFaHWsYoa8llfV7kAcMQJxjxnhvohi3zqZykDrYdQ72vBCveMdOYpfiXmYssOz+f/TJZEqSshZl82dsdK5Xiy5W5U3i6527itaLQv4g76RwA6FnBFfZHluHh/e+Qo3C0IXOUyseS0lA6xWFdDKVRNoqixVcKqgeHVKBAgMBAAECggEABo9/yPgFd0Bl2d8mK1WBL+8EPjaj+LNKYxYhnFbrMbzGdmDkx5RimiT9P5T4XgKQNzTCoHxfYBBo/Hmbo0VyjABSb2NU+Wr5AiS30T9nE8Cghuqh4UUsmefTUZ6YVCNJeviXVt8PVUqjP4HT3kB8Gglyx2m9pvEJMjXQ55n75rD6Rd4eqZCorGC/JYU/gurmmnnpyW3Nmii4cvDGvFwr8jC4CZN0jwrkwWj9X+Fv4dNNDLH4LxTvj03NBRU0ThyWLas/EE/NzA1mv713VMPyZwx7UvMxxto91GWv/nbh4sTNcBfKO+ZK+UrNhGWRkFG0kwjo0wzs4vYXN7RSjTpPuQKBgQDYI0VtMRN4WRreeRNntiPTBIA4w8Z4b4aCoeNY2WIVkNACSZgBNo9rn7SqM0yw0beaWNqSBIHSX5Ch5QMBW6QRRt6ZK6Tq3uNjui7J2uv+d/j+838R+2OoSqChYLDy/vdxUzCpVpkaisonIarKfcoqqc7SLRjsu845HQPt6erG3QKBgQDJHqq626GOzDuWp+hG1Qq3JdW3ZkXY8LqtQcbzhKKAz3w8u+zh++E+QscJfrr5sRoVzdmkr0lcMGeo1O3gtVWqnzWT4vzzLZi/l0+pKKCgbeiBzEI+mNFIQnZ1O/iKJQfs+fblNl3ub1Mlxm17sKFMFWWi0I3hnXgkv1BlWBJ19QKBgQC98TFAJlLP/q6IOKr/B6gv99KfEB3JFWmGP7LGEDQMc7j5aad12Xbsw+tHb9HDymmp8NAUZnWYZXd7bwDXHqvuqvNQdHR4G+yFZcdciVG/zbs6gs53BQ+tg/fqGkknIz5djxhCmOHv22yQOxwW27jhCV3CgvNWiC1RL9iWKm2y2QKBgGVtRNbliq10TBznYtnN+RByUTyjpFgK12onAQmwey+Q8+vBLm6tU2PN04jzU6I28ZvLa5aFG+8VLkHT2H95k9FvZ1rEn6KX/S+qRG9f4Nnnc9l5xHLDKNBTTGBFNUud70hQq3XfHDHyDLHBR1eYtU+kftREbzk36+5EWWwypWS9AoGAYdu5nDPqRAbHgCMknPzpZHoaPYdRBA4Z8jfbfKkSRscQEmkjRtyUumvju4lsdGzUdbIXmE1Or+cKF409Gu1hPPdA+H1LKx56ozwyUyGfM3XYPIcmtLu9GnR3d6tjoC2AsXqR2TX9gkdAIQjdlC5wsBz6HzOVZbLNtfJb3m/p4lA=",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %1$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(targetSocket.getLocalPort());

      final String proxyParams =
          """
              {
                "options": {
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "MIIGATCCA+mgAwIBAgIUfqNQuNUyXJ3fXMmMg5dFoJ44s7EwDQYJKoZIhvcNAQELBQAwgY4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxFDASBgNVBAoMC3VzaGVyYnJvb2tlMQ4wDAYDVQQLDAVncmlpczEOMAwGA1UEAwwFYWRtaW4xIzAhBgkqhkiG9w0BCQEWFGFkbWluQHVzaGVyYnJvb2tlLmNhMCAXDTI1MDMxNDEzMzUxM1oYDzIxMjUwMjE4MTMzNTEzWjCBjjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEUMBIGA1UECgwLdXNoZXJicm9va2UxDjAMBgNVBAsMBWdyaWlzMQ4wDAYDVQQDDAVhZG1pbjEjMCEGCSqGSIb3DQEJARYUYWRtaW5AdXNoZXJicm9va2UuY2EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQC1MkppRVrXl4zy8z5f18FPxFv5a1LO0pv2DcqZg8ns0ZgHq4Vsu6OW+y8C2zrdPe1//3Fg3uXKDY02do2weIL/CNeS80CgIq7y+Q6XbWKQXrg8iF2FRTjXSYH4KQKfzZ3hlOj4b7Q4b7cASYyizLxtkxZK0iKQzloH4Hr4gxPdPg47k/PklVyZq6mx3WJbqoOu+n1I6gk8zNu36CMOV7dJlHVnyWjbJ6fLl7pmMMAJzbCisiNcooFl4IGOqmGFG91jSbsw5CSshGfHhTWgLj1MRakdyMNlRC6eHqGOCEkOy4QjvavF1RsYyg5k4ImSLK95kgRvR1sqZGt4U/3dp20aAOT9/CmRRpB7AIoz8/lC3KDDoKuV708tcydKkFDNVDj6KerEI3DP4n7eU+n/z1kPzGUx6SBgqLrDuRVDdB8Mk8F2BCzB3D56oD2ih70aGlx3mjPyJTuDpsuzaS77c42tPjT90FNFEWbfXcSm4COwmlP6luScMDjy+owIoy7JSgBIc/zdEPeJ83/wlCjumQyDyvZpjdYw1cqDSTiTa2TbIvakBEANt3HlIgIaVHtoNLnVzWim2np7bu/1sWOh+S/am1K1wg5Pnv0Z+/K//ebxMpXznl3yb3vG1SbHhtfQivVvp4WerRVVhW4ki2mLOuJI+VgP0fxupwm2Jtf4QDe+swIDAQABo1MwUTAdBgNVHQ4EFgQURGUiK3ofgoZoQGI2lXa9s7TZiHcwHwYDVR0jBBgwFoAURGUiK3ofgoZoQGI2lXa9s7TZiHcwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAQLr/w4Pmi3viyLgrO6kCNGFEdatO3p1h6sn2yHDNFJlBcPE6Aoy1+ejs1vhSWBff8a4GuI11ES6S8mQxvgl5hUIFpX41CmhAv6xMvOVj/1qyftiWpbG3VDSElv3J0tP+pskHwlc/cqbFVOwInj3R3lFRpbn1y5IxJfpmIIew7lvJYYA7mm/bLTWJImtogYnkPFwjkVZiHZ2e8+LBgU7R2bRVwWATvH9w7OWbz62PLSLwrZWEryL4wIKGyzgt8arW2P3jmn+OVYJNCXorv35L017d49uGeDu3eCChYtoirY6p/T0UgnXXP33TQYRy3P9iMmXy2RQhKkzRxaguNJuSg79cGoiu3UloSOrzhTW+ZAF5iERn+xZoOSu+/SkU9G1BQ1KcrWufG2mpNzTkAX5GGn+UqHgvWyD5HnTRVHLSc9+YFrcpePUcJrhpEyFSeRKVpicU3MpFO+UO71PhEE6xyG4IZe0Hn42Kk948P+kKZypWrnR0h3j8do0FMyXtOEMg8ZYt6juolbpIR9GIV/Wm2INV6Vv8D+0zoHT5pE34djUsjoHYQ4mOei09MU+zBqpu19Nsi0DS8DUXxS/YbxLeTIgqAUJdbe1GcSCfLSju7NyHJUfqrfhCdLCHFC5vPAuI5RY0XsJzUD3w9kIvSoK6ap3+DGC0iOyhfx5mUpLyP34=",
                  "speds.net.private.key": "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",
                  "speds.net.entities": {
                    "host1": "https://localhost:%1$s",
                    "host2": "https://localhost:%2$s"
                  },
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIIDoTCCAomgAwIBAgIUd8MRLCgCZsfo2a0KJU8846WtdpEwDQYJKoZIhvcNAQELBQAwYDELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDDAKBgNVBAsMA0RFVjENMAsGA1UEAwwEVGVzdDAeFw0yNTA0MTAxNTQ5MjBaFw0yNjA0MTAxNTQ5MjBaMGAxCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZRdWViZWMxEzARBgNVBAcMClNoZXJicm9va2UxDjAMBgNVBAoMBUdSSUlTMQwwCgYDVQQLDANERVYxDTALBgNVBAMMBFRlc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDib7ZhJub6j/arqwb72iM1wWWDF+iy027fXkWDQIMZOspUmkDoOOitiGTTFlO0ZyLAWGTyTxFrdyoMaTEU3FbPmy4TsDkbW17z4GYHivnUZh6yJR3ys9pUhKWP5V93WZfzbp4eTk1jbaUdvwEjTMu1LN10kMv+oDxM1j437HKtbMTKxQ25DCQ47CbdXvh8xTAx7Lx/6Fiok0MRG/52nxvpuPFHjwY7w0ChBgk/UCkmfgXTpv/A01wKJmVfmr+Ny+GbsOsFcRmWANttmBjwvVteXuRd2zRVyyLmvY6ECOd4toeQPTZfSBcqlROZ8ImjcH6aspb/7bZdFaf0B7DGGIQNAgMBAAGjUzBRMB0GA1UdDgQWBBToCdiqB+tOmeE2KyFHSg6ElrdBGDAfBgNVHSMEGDAWgBToCdiqB+tOmeE2KyFHSg6ElrdBGDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQAJ6iIm6mxJN3lQJ6qXbQNL3qlTe++bcRajl7ArR9NVhqUQ4DezHlcLHlnfZhIp6CgCOTi28zTynx4ivxxMaSpTl2KAwb8De0mNR+vpxE+1AlyNWFKap9Qm5OJD6xpKIrPa3kwAygHDDba4o6PfA7yp6iGW9tXGAIbaoX2C3SIEFcDqWaq7kjhGa9RXCUhJD5Q4Wx0c6HnM99mff+yUxSYL140FUoIIHqJ2M1wAAmZOSdX558C5UpEUsYrySGeWzqexu0UTmpMF50qlwcyytDMufxg8xWtR4DqbpUOwGolDnxUuvX82oxQRUN2rhEPAhKinV1ahc68GPl39wgG0AvSJ",
                  "speds.dl.https.server.private.key": "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDib7ZhJub6j/arqwb72iM1wWWDF+iy027fXkWDQIMZOspUmkDoOOitiGTTFlO0ZyLAWGTyTxFrdyoMaTEU3FbPmy4TsDkbW17z4GYHivnUZh6yJR3ys9pUhKWP5V93WZfzbp4eTk1jbaUdvwEjTMu1LN10kMv+oDxM1j437HKtbMTKxQ25DCQ47CbdXvh8xTAx7Lx/6Fiok0MRG/52nxvpuPFHjwY7w0ChBgk/UCkmfgXTpv/A01wKJmVfmr+Ny+GbsOsFcRmWANttmBjwvVteXuRd2zRVyyLmvY6ECOd4toeQPTZfSBcqlROZ8ImjcH6aspb/7bZdFaf0B7DGGIQNAgMBAAECggEAD5J1LWs6eULLSPUDDvDqePjq3UAYrauitVPLXgJiwapsXbMaAZ5gSgbWQDoJ2Z86IZYyowNTBFv2lVHqoS+h/LcETTBSTRJUjlsBSUWCdtvrAn9Q9u7tdeCFbkbs8M1FiOUzlIv4AH163HfVQTM30ybZt/l5PfKREwuszbXCETEHna/EYGnSJMs0vS9ZQgP71h9y73Ylqk074d9atFdDu9nQe6USNpwnlbzYuaTj7cCx0yQlpB3hW5/Ju7mVS/vWWpfEqMhczS2XJcpVwzKvhrRtv2EZ8+rZnpM9dJuIYfD7vk+ArsiEwCUsiLDeXozUHa4uMavevZsos3ckfcE8YQKBgQDy8cKKwAdOHi/WQ56u4uJTopQCmoZgJiWXewiXNAVXrVlqkSc4uzodL55Jt1C62zioY1XJ0rutntdEIaf8cmYAuQ943lxPVf1NBJQPauSuXCTW4eSwAYHVQUxg7NKWNSwsk3eR4VYZpWDJDHfeYPQiYGk8GbkqR0YGGUSaNlxvCQKBgQDumtkoQIUA5an+rAkxuWWRaCNUkaduO0NX6brDAqUfVltN1EBHEsay++rRSC0CViQpIoCJsiNpfdFaaTZagUpeKa5h0dKHBjm5ujaO6sFnPEeLpiyhAy+2a0lNaFo3R3Bb0pywkwHAI1da+cRhc7h5ZO1J3hRC157ELvMHvgrp5QKBgQCwrLg5hyTI3pMTUuGXdU/6WoUgz5dhXNyBqOu6ag5okJwyL5VCYIYGq5aFLVb8TGiLrJG60BHEVXtfBMrRf7BGXk6uK6cd2v72f5ndDHN5iXERA/33MWFl4LLW2eLbcZfKYwPVhvXCeM5F51dU1VU1mr9tAZfy8xotXTrSmy3lYQKBgQCddLI5Tdp3bRdxaDXpCl9sRTyLYlMK+zDFowgMCILO2z4Fbju8qzdUhcm/sdGB6TaTrqEEPPoBc3XkI+oqb4eT/A2Hn98+G22ckrXOOjzG05CjJ1XsKx4hU0tcObDPKeWLssCUBW7yTGApOQnPBX6HHYM5a8QTGBc9f34BbfflwQKBgQDFrDUGOpb4aAoiov3wCv6rT1xoDgeEAQ2Dl1DrE18pz8xexZUfjL6DRXhp//H1so5XoTJdI9yKOTFHhAPGGlDMcZIKo6WhKohIGqSpjwZqXxJpBar6j0N2vO/KCvN16VRi8tnqpWzNTS1kwBrTyrxGLtXTknoL7HMUeJ7D6EZLOQ==",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %3$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }
              """
              .formatted(originSocket.getLocalPort(), targetSocket.getLocalPort(),
                  proxySocket.getLocalPort());

      // pre-client-init-success
      PresentationHost originPresHost = presentationFactory.init(originParams);

      // app-client-init-success
      appFactory = new SyncApplicationFactory(pgaServiceMock) {
        @Override
        public PresentationHost initPresentationHost(String parameters) {
          return originPresHost;
        }
      };

      ApplicationHost originAppHost = appFactory.init(originParams);

      // network-client-init-success
      networkFactory =
          new SyncNetworkFactory();

      NetworkHost originNetHost = networkFactory.initHost(originParams);

      // proxy-init-success
      // NetworkProxy proxy = networkFactory.initProxy(proxyParams);

      // pre-server-init-success
      PresentationHost targetPresHost = presentationFactory.init(targetParams);

      // app-server-init-success
      appFactory = new SyncApplicationFactory(pgaServiceMock) {
        @Override
        public PresentationHost initPresentationHost(String parameters) {
          return targetPresHost;
        }
      };

      ApplicationHost targetAppHost = appFactory.init(targetParams);

      NetworkHost targetNetHost = networkFactory.initHost(targetParams);

      // INITIAL MEMORY
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      long initialMemory = memoryBean.getHeapMemoryUsage().getUsed();

      // EXCHANGES

      // Variables pour stocker les mesures de mémoire
      List<Long> memoryDifferences = new ArrayList<>();

      // BOUCLE TEST (10 itérations)
      final int ITERATIONS = 10;

      final int MESSAGES_PER_ITERATION = 300;

      for (int i = 0; i < ITERATIONS; i++) {

        // Envoyer 5000 messages différents
        for (int j = 0; j < MESSAGES_PER_ITERATION; j++) {

          // app-client-request-success
          InterfaceDataUnit01Dto idu01Dto = testUtil.buildIdu01();
          originAppHost.request(idu01Dto);

          ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
          verify(sessionHostMock, atLeastOnce()).request(captor.capture());

          String idu23 = captor.getValue();
          assertNotNull(idu23);

          InterfaceDataUnit23Dto idu23Dto =
              objectMapper.readValue(idu23, InterfaceDataUnit23Dto.class);

          // transform-idu
          InterfaceDataUnit45Dto idu45Dto =
              tranformIdu23ToIdu45(idu23Dto, originSocket.getLocalPort(),
                  proxySocket.getLocalPort()); // proxySocket

          String idu45 = objectMapper.writeValueAsString(idu45Dto);

          // network-client-request-success
          originNetHost.request(idu45);

          // proxy-forward-success
          // proxy.forward();

          // network-client-confirm
          originNetHost.confirm();

          // network-server-indication-success
          String receivedIdu45 = targetNetHost.indication();

          // transform-inverse-idu
          InterfaceDataUnit45Dto receivedIdu45Dto =
              objectMapper.readValue(receivedIdu45, InterfaceDataUnit45Dto.class);

          InterfaceDataUnit23Dto receivedIdu23Dto = tranformIdu45ToIdu23(receivedIdu45Dto,
              idu23Dto.getContext().getPga(), idu23Dto.getContext().getSourceCode(),
              idu23Dto.getContext().getDestinationCode(), idu23Dto.getContext().getSdek());


          // app-server-indication-success
          String receivedIdu23 = objectMapper.writeValueAsString(receivedIdu23Dto);

          doReturn(receivedIdu23).when(sessionHostMock).indicateDataExchange();

          InterfaceDataUnit01Dto receivedIdu01Dto = targetAppHost.indication();

          assertNotNull(receivedIdu01Dto);
        }

        // Force garbage collection pour plus de précision
        System.gc();

        // Attendre que le GC fasse son travail
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        // Noter la différence de mémoire
        long currentMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long memoryDiff = currentMemory - initialMemory;
        memoryDifferences.add(memoryDiff);
      }

      // Afficher le résumé des différences de mémoire
      System.out.println("\n===== RÉSUMÉ DES DIFFÉRENCES DE MÉMOIRE =====");
      for (int i = 0; i < memoryDifferences.size(); i++) {
        System.out.println("Itération " + (i + 1) + ": " +
            testUtil.formatMemorySize(memoryDifferences.get(i)));
      }

      if (!testUtil.hasReachedPlateau(memoryDifferences)) {

        // Créer un message d'erreur détaillé avec toutes les différences de mémoire
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(
            "Détection potentielle d'une fuite de mémoire: la mémoire n'a pas atteint un plateau après ")
            .append(ITERATIONS)
            .append(" itérations.\n\nDétail des mesures de mémoire:\n");

        for (int i = 0; i < memoryDifferences.size(); i++) {
          errorMessage.append("Itération ").append(i + 1).append(": ")
              .append(testUtil.formatMemorySize(memoryDifferences.get(i)))
              .append("\n");
        }

        fail(errorMessage.toString());
      }
    }
  }

  private InterfaceDataUnit45Dto tranformIdu23ToIdu45(InterfaceDataUnit23Dto idu23Dto,
      int originPort, int targetPort) {
    return new InterfaceDataUnit45Dto(
        new Context45Dto("https://localhost:%1$s?code=host1".formatted(originPort),
            "https://localhost:%1$s?code=host2".formatted(targetPort), false),
        idu23Dto.getMessage());
  }

  private InterfaceDataUnit23Dto tranformIdu45ToIdu23(InterfaceDataUnit45Dto receivedIdu45Dto,
      String pga, String sourceCode, String destinationCode, String sdek) {
    return new InterfaceDataUnit23Dto(
        new ContextDto__1(pga, sourceCode, destinationCode, sdek, UUID.randomUUID(), false),
        receivedIdu45Dto.getMessage());
  }
}
