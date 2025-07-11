package ca.griis.speds.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.integration.util.DomainProviderUtil;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.SyncNetworkFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.sync.SyncTransportFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ServerSocket;
import java.util.UUID;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class AppPresTransNetLinkSpedsIT {
  private PgaService pgaServiceMock;
  private SessionHost sessionHostMock;

  private SyncApplicationFactory appFactory;
  private MutablePresentationFactory presentationFactory;
  private SyncTransportFactory originTransFactory;
  private SyncTransportFactory targetTransFactory;
  private SyncNetworkFactory networkFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;

  @BeforeProperty
  public void setup() {
    pgaServiceMock = Mockito.mock(PgaService.class);
    sessionHostMock = Mockito.mock(SessionHost.class);

    presentationFactory = new MutablePresentationFactory(pgaServiceMock) {
      @Override
      public SessionHost initSessionHost(String parameters) {
        return sessionHostMock;
      }
    };

    networkFactory = new SyncNetworkFactory();

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }

  @Property(tries = 10)
  @Domain(DomainProviderUtil.class)
  public void netLinkExchangeTest(@ForAll InterfaceDataUnit01Dto idu01Dto) throws Exception {
    try (ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0)) {

      testUtil.freePorts(originSocket, targetSocket, null);

      final String originParams =
          """
              {
                "options": {
                  "speds.app.version":"2.0.0",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "MIIFqzCCA5OgAwIBAgIUd1YYBVI9wgr6ejuM8mTNX4/T+tkwDQYJKoZIhvcNAQELBQAwZTELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDDAKBgNVBAsMA0RldjESMBAGA1UEAwwJbG9jYWxob3N0MB4XDTI1MDcwODE2NDEwMloXDTI2MDcwODE2NDEwMlowZTELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDDAKBgNVBAsMA0RldjESMBAGA1UEAwwJbG9jYWxob3N0MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAmIWZnwyWwbUhUnpnSWHuJeVDzf5s5m42sMKPnPM0zon2H6+cCk6QrdCV9oiA5AgoTXpS5yUOXRoVzy1MX8PhDKzT6KyAqAZNjRxzkVB/G/XIRaWUxcrTs1Vu911jIKDmy+bfYxN2zx762yaj6krAzqIRz5k/+YNJDY31hWqYD8F6ciLmoo49xWfID5lNFNbGQsIKItrRrgFYUdc4UOwe9bJFoM0WfsP3aLOBLMu6fmvmI1zTGXkIcG9oskdZVwAAbk8Te3Q7hiTblil+4hicdOFx2IPLQLwf8QuzRBvM99mefL1iD6bLNJlmF3rmopn49fW/JMiK9ebJMPYFXz1y2vd4JMNn24Lt8SiIJKMCzulmPwO+212p/8gN4Uiql3B7hNx6Y/1YbYSsz0RjK8ghVPdEp/uk7sLaf5rPM8t167A66xK6VQAkYbY4RYC0wREhN5m3FSeeaGMYxfFwMekSymCN/scIpOD3DY9knXH6qukaMCn35F5YfJuPHhjxlSvyIJ7dImp9GjEkOtXvAMebB6cPqZjUFAj9JBP3USQAJsljA4vVlXEkqUw7qXYqCUIA0JZBqRwtcRlNx3q8gkKvz3lbCzOP3udiFMZ0GmI+pmTzWZBfpxvHggEGWpsmkLvhz8ZsmkRCMdTSPyPJHOoTBEAezDdACH4eiOz2LS1zKC8CAwEAAaNTMFEwHQYDVR0OBBYEFEzPVNLSooiVzf3TyYynFPWcNgIDMB8GA1UdIwQYMBaAFEzPVNLSooiVzf3TyYynFPWcNgIDMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggIBAEbSNBVY3lTZbwDHAhAlDbzKXsMrQzuJlzq2cAFLMOJrDsKJRzDEEgBSNQ8jkirBypSMWlYUGIBQNQdEk+06kvHTz3WiPrvLdx9bAMgGKAlrxlxRGqNVAuSo21/z4ud6VzuyFxPkxc/uZRAJ8/t/6KU94T/fpkzdr6ivF+TwVxKZx75tP4xd8FoUzFJJ4zAV4y9rr+ntKbgtPd1Uduv8L8cxTGejYBG9/y7xlPmC1cMLIxIKxrYGnKGJ0suz3oMssRxLqNYVQ99pfcN7illDvuM/fHfHCEXlb/4A+lD+C24rLn/pKhDaDNYH+HjF2SOBNCIb4RkSFmdwGngheVEeicib2Rca9es6C9Vm7cmBsZh/WR3palKhUu+VqbgrxFhWuc8TiuFUjdAiKeEZ2czNSA8M3XZLFr/Twps48AlOUOmxhbS+tXmOlQiN8roWmyPOvNmXaxIZTm2ef6HdSYsFlLBbNk51Vi6TVoX5RxlY7aVVhxfXQLs8zdYu/ufhdO6H3ropjgV99Rghwd8pR8BgnLvfPOCAKsNbTVA/Ejg12NbAN9J57mpcMU5z6BbqKea93RTrrZTeMJkOYtTy8YxhNj1j3PJpyPMakNjn35w5wmxtuvSyChQwjVM8K4mD5m6JEYUl/BA6FagEX3VUXxRdzbVIPezSd2b67ylyo/Ux7khb",
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
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "MIIFqzCCA5OgAwIBAgIUVSCMez9+mw3ce6xuiW8IQYiGsDUwDQYJKoZIhvcNAQELBQAwZTELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDDAKBgNVBAsMA0RldjESMBAGA1UEAwwJbG9jYWxob3N0MB4XDTI1MDcwODE2NDkzOFoXDTI2MDcwODE2NDkzOFowZTELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDDAKBgNVBAsMA0RldjESMBAGA1UEAwwJbG9jYWxob3N0MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAzPjdCqylhRwgjbLZlh1FvPDTmiG9PlOJg7cpwoVrjivd5PH3Dtq14b0B9qVdqSPb7RY1amryvRZJvI77kl8xC0QrzbKTjWFeEKjX16CfIcJ7UQsE9+zyJIgtp5/iBHhnEUSaaTSZWbTcfmoNOkqV6UrXQTNPyjRIhLGBC5Fr2D2LoGJFgREKA6XgY5fDDE5E1fXx7YQzLrnXTW2uFD1NXk88q4TTzto5DVGM5JOmcoTVd/FnS2zaIeNHtL+wUy8SILTMxlydnY+EvHXKzIYuLBW4G8+CHozbqUr8bG0+ihwVVRj6KuafX605cvPVqURQwGlQY/UR1zAyMdpjLBG92LmSsb1rv9Iem7MVEA4R7fY/+Ecf5i2fajaYEhL6amSh1qPCzy4m0ORcFZ4CvqjADfz+nIh14CrhIcmgmn211Mz/wD4c0wrFFlm3FIgLzXEl+a6vYChtpG2l8KDoyLQw00XcR2KIj/2VmDmNPCEDdnaIKelh2uNkzjyzsj0oi/TqepbY6MmQhX7lpfQWlllIMpUY5yJDTdi2cTRDfGEkz0H+rICA3tg1igj9wB6npF4+i57LX0NDmsalB2OwoMVOSJQx27iMCb2KVx5xKJcOOtdA1sBgt+duvvPJT5A87qIJWFw3/2yMZOIJUVDd4EB3w24khm5BQe5WR3fkHtd2ETUCAwEAAaNTMFEwHQYDVR0OBBYEFAUZHHltYsbVGQE+y/OZkmyydDeMMB8GA1UdIwQYMBaAFAUZHHltYsbVGQE+y/OZkmyydDeMMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggIBAG6OcGfD43K3+FI9LiWieQhCXszua116WawmJjlrUYimsbhfomPQADZqFFAca/FkSqgrUogKzDfaZxifBRkFP+3zvXXpHbnYQSJf2NlUHYpDXResVJCFMsFXVhej2Hc7cL5oHlj1o5ddTUlHQACkmemfDYU8Cipcf25z3TmUIWByf/++jmoLaQnhI3KoFUNX1CoCr7vFecluYKjros8HOl0tO1+ggFaSN0mGdR5MCONjqCohi5T7MjZ2Wce1piRW5Y1pbNoKbCgumZQ/yV1rqwOBcEQIiXfktsy5iVpu7noBkJ18eHNQBKJjjm0jHcTVxLzqds3Hbftp1/DJADcrjRQTkydc1Ahlp8jea6K7wLbpqE1ZDuDbE02UE+RP6J7AyUsZx0R/gn08DntFtf3W2wVMaWL9dain3bpidw3M4VcKjCXT14WH+fx21Bbugaftt/tU+xV+K03hIloa2yC/j+dDt+kw5cN8Qdkb1VU90FzMQCGkCa3NfsLCp1mYdnw7ze+SUvCmAjrIxL15AnQs3M2oxlwSmzq39jhapXmdlXQat2yBCNppZ6S+wJP0UmKSYjRi7Z49zeERugjoO/qEBERee4ZzPuxdJu36cAnWuGT5pDPghIe5SvNZWfCEuI1bVDL1XRDUmFHMKajPUFiMX12zyqNWePw+g0P9kyktsHZr",
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

      // ********************************
      // Client Initialisation
      // ********************************

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

      // ********************************
      // Server Initialisation
      // ********************************

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

      // ********************************
      // Exchange Processus
      // ********************************

      // app-client-request-success
      originAppHost.request(idu01Dto);

      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      verify(sessionHostMock, atLeastOnce()).request(captor.capture());

      String idu23 = captor.getValue();
      assertNotNull(idu23);

      InterfaceDataUnit23Dto idu23Dto =
          objectMapper.readValue(idu23, InterfaceDataUnit23Dto.class);

      // transform-idu
      InterfaceDataUnit34Dto idu34Dto =
          transformIdu23ToIdu34(idu23Dto, originSocket.getLocalPort(), targetSocket.getLocalPort());

      String idu34 = objectMapper.writeValueAsString(idu34Dto);

      // transport-client-request-success
      originTransHost.dataRequest(idu34);

      // transport-server-reply-success
      String receivedIdu34 = targetTransHost.dataReply();

      assertNotNull(receivedIdu34);
      assertNotEquals("", receivedIdu34);


      // app-server-indication-success
      doReturn(idu23).when(sessionHostMock).indicateDataExchange();

      InterfaceDataUnit01Dto receivedIdu01Dto = targetAppHost.indication();

      assertNotNull(receivedIdu01Dto);

      assertEquals(idu01Dto.getMessage(), receivedIdu01Dto.getMessage());
      assertEquals(idu01Dto.getContext().getPga(), receivedIdu01Dto.getContext().getPga());
      assertEquals(idu01Dto.getContext().getSourceCode(),
          receivedIdu01Dto.getContext().getSourceCode());
      assertEquals(idu01Dto.getContext().getDestinationCode(),
          receivedIdu01Dto.getContext().getDestinationCode());
    }
  }

  private InterfaceDataUnit34Dto transformIdu23ToIdu34(InterfaceDataUnit23Dto idu23Dto,
      int originPort, int targetPort) {
    return new InterfaceDataUnit34Dto(
        new Context34Dto("host1", "host2",
            "https://localhost:%1$s?code=host1".formatted(originPort), UUID.randomUUID(),
            "https://localhost:%1$s?code=host2".formatted(targetPort), false),
        idu23Dto.getMessage());
  }
}
