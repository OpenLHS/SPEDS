package ca.griis.speds.integration.bottomup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.speds.integration.util.LinkDomainProviderUtil;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.link.api.sync.ImmutableDataLinkFactory;
import ca.griis.speds.link.serializer.SharedObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeProperty;

public class LinSpedsIt {

  private ImmutableDataLinkFactory dataLinkFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;

  @BeforeProperty
  public void setup() {

    dataLinkFactory = new ImmutableDataLinkFactory();

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }

  @Property(tries = 1)
  @Domain(LinkDomainProviderUtil.class)
  public void linkExchangeTest(@ForAll InterfaceDataUnit56Dto idu56Dto) throws IOException {
    try (ServerSocket originSocket = new ServerSocket(4050);
        ServerSocket targetSocket = new ServerSocket(4051)) {
      // todo
      testUtil.freePorts(originSocket, targetSocket, null);

      final String originParams =
          """
              {
                "options": {
                  "speds.app.version":"2.0.6",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
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
                  "speds.app.version":"2.0.7",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "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",
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

      // network-server-init-success
      DataLinkHost targetLinkHost = dataLinkFactory.init(targetParams);

      // network-client-request-success
      DataLinkHost originLinkHost = dataLinkFactory.init(originParams);
      String idu56 = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu56Dto);

      originLinkHost.request(idu56);

      String result = targetLinkHost.indication();
      InterfaceDataUnit56Dto resultDto =
          objectMapper.readValue(result, InterfaceDataUnit56Dto.class);

      originLinkHost.close();
      targetLinkHost.close();

      assertEquals(idu56Dto.getMessage(), resultDto.getMessage());
      assertEquals(idu56Dto.getContext().getDestinationIri(),
          resultDto.getContext().getDestinationIri());
      assertNotEquals(idu56Dto.getContext().getTrackingNumber(),
          resultDto.getContext().getTrackingNumber());
    }
  }

}
