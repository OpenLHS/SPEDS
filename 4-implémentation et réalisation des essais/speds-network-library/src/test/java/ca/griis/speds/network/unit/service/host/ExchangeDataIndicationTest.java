package ca.griis.speds.network.unit.service.host;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.js2p.gen.speds.network.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.serialization.SharedObjectMapper;
import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.exception.InvalidSignatureException;
import ca.griis.speds.network.service.exception.MissingAuthenticationException;
import ca.griis.speds.network.service.host.ExchangeDataIndication;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.Seal;
import ca.griis.speds.network.signature.SealManager;
import ca.griis.speds.network.util.KeyVar;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeDataIndicationTest {

  private final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
  private ExchangeDataIndication exchangeIndication;

  @BeforeEach
  public void setUp() throws Exception {
    final String spedsVersion = "3.0.0";
    final String spedsReference = "https://reference.iri/speds";
    CertificatePrivateKeyPair cpkp = CertificatePrivateKeyPair.importFromPem(
        "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");
    this.exchangeIndication = new ExchangeDataIndication(new NetworkMarshaller(objectMapper),
        new SealManager(objectMapper), spedsVersion, spedsReference, cpkp);
  }

  @Test
  public void testValidateReceivedMessageSuccess() throws JsonProcessingException {
    Context56Dto cxt = new Context56Dto("https://host2.iri",
        UUID.fromString("75553e0e-cfcc-434a-9189-04df7ddb1544"), false);
    String auth = KeyVar.host1CertRsa;
    String pk = KeyVar.host1PrikeyRsa;
    CertificatePrivateKeyPair cpp = CertificatePrivateKeyPair.importFromPem(auth, pk);

    HeaderDto header =
        new HeaderDto(HeaderDto.Msgtype.RES_ENV, "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
            "https://host1.iri?code=host1", "https://proxy.iri?code=host1",
            cpp.getAuthentification(), false, new SPEDSDto("3.0.0", "https://reference.iri/speds"));
    SealManager sm = new SealManager(objectMapper);
    String headerSeal = sm.createSeal(header, Seal.header, cpp.privateKey());
    String contentSeal = sm.createSeal("Protocol Data Unit (PDU) sérialisé de la couche Transport",
        Seal.content, cpp.privateKey());
    StampDto stamp = new StampDto(headerSeal, contentSeal);
    ProtocolDataUnit5Dto pdu = new ProtocolDataUnit5Dto(header, stamp,
        "Protocol Data Unit (PDU) sérialisé de la couche Transport");

    InterfaceDataUnit56Dto idu56 =
        new InterfaceDataUnit56Dto(cxt, objectMapper.writeValueAsString(pdu));

    assertDoesNotThrow(
        () -> exchangeIndication.validateReceivedMessage(objectMapper.writeValueAsString(idu56)));
  }

  @Test
  public void testValidateReceivedMessage_WrongMessageTypeException() throws Exception {
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    assertThrows(DeserializationException.class,
        () -> exchangeIndication.validateReceivedMessage(idu56));
  }

  @Test
  public void testValidateReceivedMessage_MissingAughExceptionNull() throws Exception {
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":\\\"\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"i2W2VBeU1BQ/cl/JEz1qZKShtDItNZHdxre9+VJv8qr41/xhYZMlW2agYZNw2HLJXZUf+RXa+1P3Y35iNEvXTTY2abUDrxuUksQjcPisxTmTTvHlqEsXHA/uSnk9aRjs2A7eAUDWTTVyOdW441Er9Rtzk9Raiud6C9WsHZycvyJ/GedINqTUXk/Q9d5TCkpq8PftgOtnWxItSPGGWArj+61zOn6PrSmaiBndDm4klm3jy7M2bUyzM5fCWjOhjzgvNnp+M3vMGrbJavc2pPndvJf4KuRUUI1WyFLr2wNfv9bb37rYbIJ6A0/MMMgLUyYGHwLqrBGIBPeQ/L2QTFL7z6pQMwQqJvEkgpISwq5JotgROQTGtZUi8Ml57ytZ2I6JSnagp+/v+2gHtFsofwIPfmpe0x6zGjs86P08XTX80+Z9U3aoRUwFVp+PVfgpdrHyAXT7CnYwoYmxpe3gRc/aeAmDSdtqF7mip7sR3YGIpiW/w4kOWQFE73Rpdlj4uNMJKsbZ4FmHxUL+cmqDTyemUAMgtQLQIlO9GSUZ+gBjXzbgWhYw3dIlsHEapRrEL6nTFRUkhf/d/582iwjqInpj5Fiaw9XH9mTicDZtgos/f8RLni6NBPwATRmWFt9bdPjQV4BqwBgwU+q/8Gh0KrhNVrxeHsu2S1TKiuVoO7t3y2U=\\\",\\\"content_seal\\\":\\\"PBeEqelCgDRbT0+/uct1ZdlGI5Vpv5Hkr0PA3t55bvj8ha82cm1vW2KwW+f6aMKRB7orPEU+QC9o28gWidFmTS7OnPiclmbBc20f3c37FZ0TNlm8P/71m5lsxW73KWkdS+8+nzv0/56BiJrVnX44Vdpsdas+UFXWc9d/nm322qQUTqOFqzKaylYbilnA4+p1yzxULbv2/Zf2SqCh7+KQP7gyq7jCRtCTT8mgXdpxrRDnTlZMrsUeNUvqnaLLpvTHNlA6CFQ9I9nJkBctH0iLUwpwMVZOlXYg2RQ1hrQ9YI6aZ6StHN5KYiHPww26DLqZz1AraK070JBncD6VFGuhKrG22BIl0gcmMz2t75gE4U24VVJxzcvFZ5AEyHNdYk6QHLYOZ7MAKqbWgxfhFSN7q0oK/Is1R0aYFgMsEWWb2RE7n/sTay6gWZ4MhnSVTvpuFsR9v0WIw9M94yT14xlFZL/5rT6Y+BL9Kv7LhOLFO9uWkdGKMJMlvvBL2TBbjPK4hG3b+WBhRJzzvdrry6a1Ki73317IwYSEd8lX54SEKgQ4AXEiwvAweJRUXrJQn/+u8FsNAgrfKnqctNNsIbtxPag2Flz99rnblg75n81VLxFrKK94DLlcOK/cb6Pud3zwsM+6bbkej4l6n55qRCV4Xi5Cpd8nu1A57rPLSjjMSGY=\\\"},\\\"content\\\":\\\"Protocol Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    assertThrows(MissingAuthenticationException.class,
        () -> exchangeIndication.validateReceivedMessage(idu56));
  }

  @Test
  public void testValidateReceivedMessage_MissingAughException() throws Exception {
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"i2W2VBeU1BQ/cl/JEz1qZKShtDItNZHdxre9+VJv8qr41/xhYZMlW2agYZNw2HLJXZUf+RXa+1P3Y35iNEvXTTY2abUDrxuUksQjcPisxTmTTvHlqEsXHA/uSnk9aRjs2A7eAUDWTTVyOdW441Er9Rtzk9Raiud6C9WsHZycvyJ/GedINqTUXk/Q9d5TCkpq8PftgOtnWxItSPGGWArj+61zOn6PrSmaiBndDm4klm3jy7M2bUyzM5fCWjOhjzgvNnp+M3vMGrbJavc2pPndvJf4KuRUUI1WyFLr2wNfv9bb37rYbIJ6A0/MMMgLUyYGHwLqrBGIBPeQ/L2QTFL7z6pQMwQqJvEkgpISwq5JotgROQTGtZUi8Ml57ytZ2I6JSnagp+/v+2gHtFsofwIPfmpe0x6zGjs86P08XTX80+Z9U3aoRUwFVp+PVfgpdrHyAXT7CnYwoYmxpe3gRc/aeAmDSdtqF7mip7sR3YGIpiW/w4kOWQFE73Rpdlj4uNMJKsbZ4FmHxUL+cmqDTyemUAMgtQLQIlO9GSUZ+gBjXzbgWhYw3dIlsHEapRrEL6nTFRUkhf/d/582iwjqInpj5Fiaw9XH9mTicDZtgos/f8RLni6NBPwATRmWFt9bdPjQV4BqwBgwU+q/8Gh0KrhNVrxeHsu2S1TKiuVoO7t3y2U=\\\",\\\"content_seal\\\":\\\"PBeEqelCgDRbT0+/uct1ZdlGI5Vpv5Hkr0PA3t55bvj8ha82cm1vW2KwW+f6aMKRB7orPEU+QC9o28gWidFmTS7OnPiclmbBc20f3c37FZ0TNlm8P/71m5lsxW73KWkdS+8+nzv0/56BiJrVnX44Vdpsdas+UFXWc9d/nm322qQUTqOFqzKaylYbilnA4+p1yzxULbv2/Zf2SqCh7+KQP7gyq7jCRtCTT8mgXdpxrRDnTlZMrsUeNUvqnaLLpvTHNlA6CFQ9I9nJkBctH0iLUwpwMVZOlXYg2RQ1hrQ9YI6aZ6StHN5KYiHPww26DLqZz1AraK070JBncD6VFGuhKrG22BIl0gcmMz2t75gE4U24VVJxzcvFZ5AEyHNdYk6QHLYOZ7MAKqbWgxfhFSN7q0oK/Is1R0aYFgMsEWWb2RE7n/sTay6gWZ4MhnSVTvpuFsR9v0WIw9M94yT14xlFZL/5rT6Y+BL9Kv7LhOLFO9uWkdGKMJMlvvBL2TBbjPK4hG3b+WBhRJzzvdrry6a1Ki73317IwYSEd8lX54SEKgQ4AXEiwvAweJRUXrJQn/+u8FsNAgrfKnqctNNsIbtxPag2Flz99rnblg75n81VLxFrKK94DLlcOK/cb6Pud3zwsM+6bbkej4l6n55qRCV4Xi5Cpd8nu1A57rPLSjjMSGY=\\\"},\\\"content\\\":\\\"Protocol Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    assertThrows(MissingAuthenticationException.class,
        () -> exchangeIndication.validateReceivedMessage(idu56));
  }

  @Test
  public void testValidateReceivedMessage_HeaderSealException() throws Exception {
    // final String idu56 =
    // "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":\\\"MIIGATCCA+mgAwIBAgIUfqNQuNUyXJ3fXMmMg5dFoJ44s7EwDQYJKoZIhvcNAQELBQAwgY4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxFDASBgNVBAoMC3VzaGVyYnJvb2tlMQ4wDAYDVQQLDAVncmlpczEOMAwGA1UEAwwFYWRtaW4xIzAhBgkqhkiG9w0BCQEWFGFkbWluQHVzaGVyYnJvb2tlLmNhMCAXDTI1MDMxNDEzMzUxM1oYDzIxMjUwMjE4MTMzNTEzWjCBjjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEUMBIGA1UECgwLdXNoZXJicm9va2UxDjAMBgNVBAsMBWdyaWlzMQ4wDAYDVQQDDAVhZG1pbjEjMCEGCSqGSIb3DQEJARYUYWRtaW5AdXNoZXJicm9va2UuY2EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQC1MkppRVrXl4zy8z5f18FPxFv5a1LO0pv2DcqZg8ns0ZgHq4Vsu6OW+y8C2zrdPe1//3Fg3uXKDY02do2weIL/CNeS80CgIq7y+Q6XbWKQXrg8iF2FRTjXSYH4KQKfzZ3hlOj4b7Q4b7cASYyizLxtkxZK0iKQzloH4Hr4gxPdPg47k/PklVyZq6mx3WJbqoOu+n1I6gk8zNu36CMOV7dJlHVnyWjbJ6fLl7pmMMAJzbCisiNcooFl4IGOqmGFG91jSbsw5CSshGfHhTWgLj1MRakdyMNlRC6eHqGOCEkOy4QjvavF1RsYyg5k4ImSLK95kgRvR1sqZGt4U/3dp20aAOT9/CmRRpB7AIoz8/lC3KDDoKuV708tcydKkFDNVDj6KerEI3DP4n7eU+n/z1kPzGUx6SBgqLrDuRVDdB8Mk8F2BCzB3D56oD2ih70aGlx3mjPyJTuDpsuzaS77c42tPjT90FNFEWbfXcSm4COwmlP6luScMDjy+owIoy7JSgBIc/zdEPeJ83/wlCjumQyDyvZpjdYw1cqDSTiTa2TbIvakBEANt3HlIgIaVHtoNLnVzWim2np7bu/1sWOh+S/am1K1wg5Pnv0Z+/K//ebxMpXznl3yb3vG1SbHhtfQivVvp4WerRVVhW4ki2mLOuJI+VgP0fxupwm2Jtf4QDe+swIDAQABo1MwUTAdBgNVHQ4EFgQURGUiK3ofgoZoQGI2lXa9s7TZiHcwHwYDVR0jBBgwFoAURGUiK3ofgoZoQGI2lXa9s7TZiHcwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAQLr/w4Pmi3viyLgrO6kCNGFEdatO3p1h6sn2yHDNFJlBcPE6Aoy1+ejs1vhSWBff8a4GuI11ES6S8mQxvgl5hUIFpX41CmhAv6xMvOVj/1qyftiWpbG3VDSElv3J0tP+pskHwlc/cqbFVOwInj3R3lFRpbn1y5IxJfpmIIew7lvJYYA7mm/bLTWJImtogYnkPFwjkVZiHZ2e8+LBgU7R2bRVwWATvH9w7OWbz62PLSLwrZWEryL4wIKGyzgt8arW2P3jmn+OVYJNCXorv35L017d49uGeDu3eCChYtoirY6p/T0UgnXXP33TQYRy3P9iMmXy2RQhKkzRxaguNJuSg79cGoiu3UloSOrzhTW+ZAF5iERn+xZoOSu+/SkU9G1BQ1KcrWufG2mpNzTkAX5GGn+UqHgvWyD5HnTRVHLSc9+YFrcpePUcJrhpEyFSeRKVpicU3MpFO+UO71PhEE6xyG4IZe0Hn42Kk948P+kKZypWrnR0h3j8do0FMyXtOEMg8ZYt6juolbpIR9GIV/Wm2INV6Vv8D+0zoHT5pE34djUsjoHYQ4mOei09MU+zBqpu19Nsi0DS8DUXxS/YbxLeTIgqAUJdbe1GcSCfLSju7NyHJUfqrfhCdLCHFC5vPAuI5RY0XsJzUD3w9kIvSoK6ap3+DGC0iOyhfx5mUpLyP34=\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"2.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"i2W2VBeU1BQ/cl/JEz1qZKShtDItNZHdxre9+VJv8qr41/xhYZMlW2agYZNw2HLJXZUf+RXa+1P3Y35iNEvXTTY2abUDrxuUksQjcPisxTmTTvHlqEsXHA/uSnk9aRjs2A7eAUDWTTVyOdW441Er9Rtzk9Raiud6C9WsHZycvyJ/GedINqTUXk/Q9d5TCkpq8PftgOtnWxItSPGGWArj+61zOn6PrSmaiBndDm4klm3jy7M2bUyzM5fCWjOhjzgvNnp+M3vMGrbJavc2pPndvJf4KuRUUI1WyFLr2wNfv9bb37rYbIJ6A0/MMMgLUyYGHwLqrBGIBPeQ/L2QTFL7z6pQMwQqJvEkgpISwq5JotgROQTGtZUi8Ml57ytZ2I6JSnagp+/v+2gHtFsofwIPfmpe0x6zGjs86P08XTX80+Z9U3aoRUwFVp+PVfgpdrHyAXT7CnYwoYmxpe3gRc/aeAmDSdtqF7mip7sR3YGIpiW/w4kOWQFE73Rpdlj4uNMJKsbZ4FmHxUL+cmqDTyemUAMgtQLQIlO9GSUZ+gBjXzbgWhYw3dIlsHEapRrEL6nTFRUkhf/d/582iwjqInpj5Fiaw9XH9mTicDZtgos/f8RLni6NBPwATRmWFt9bdPjQV4BqwBgwU+q/8Gh0KrhNVrxeHsu2S1TKiuVoO7t3y2U=\\\",\\\"content_seal\\\":\\\"PBeEqelCgDRbT0+/uct1ZdlGI5Vpv5Hkr0PA3t55bvj8ha82cm1vW2KwW+f6aMKRB7orPEU+QC9o28gWidFmTS7OnPiclmbBc20f3c37FZ0TNlm8P/71m5lsxW73KWkdS+8+nzv0/56BiJrVnX44Vdpsdas+UFXWc9d/nm322qQUTqOFqzKaylYbilnA4+p1yzxULbv2/Zf2SqCh7+KQP7gyq7jCRtCTT8mgXdpxrRDnTlZMrsUeNUvqnaLLpvTHNlA6CFQ9I9nJkBctH0iLUwpwMVZOlXYg2RQ1hrQ9YI6aZ6StHN5KYiHPww26DLqZz1AraK070JBncD6VFGuhKrG22BIl0gcmMz2t75gE4U24VVJxzcvFZ5AEyHNdYk6QHLYOZ7MAKqbWgxfhFSN7q0oK/Is1R0aYFgMsEWWb2RE7n/sTay6gWZ4MhnSVTvpuFsR9v0WIw9M94yT14xlFZL/5rT6Y+BL9Kv7LhOLFO9uWkdGKMJMlvvBL2TBbjPK4hG3b+WBhRJzzvdrry6a1Ki73317IwYSEd8lX54SEKgQ4AXEiwvAweJRUXrJQn/+u8FsNAgrfKnqctNNsIbtxPag2Flz99rnblg75n81VLxFrKK94DLlcOK/cb6Pud3zwsM+6bbkej4l6n55qRCV4Xi5Cpd8nu1A57rPLSjjMSGY=\\\"},\\\"content\\\":\\\"Protocol
    // Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    Context56Dto cxt = new Context56Dto("https://host2.iri",
        UUID.fromString("75553e0e-cfcc-434a-9189-04df7ddb1544"), false);
    String auth = KeyVar.host1CertRsa;
    String pk = KeyVar.host1PrikeyRsa;
    CertificatePrivateKeyPair cpp = CertificatePrivateKeyPair.importFromPem(auth, pk);

    HeaderDto headerCorrect =
        new HeaderDto(HeaderDto.Msgtype.RES_ENV, "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
            "https://host1.iri?code=host1", "https://proxy.iri?code=host1",
            cpp.getAuthentification(), false, new SPEDSDto("3.0.0", "https://reference.iri/speds"));
    HeaderDto headerWrong =
        new HeaderDto(HeaderDto.Msgtype.RES_ENV, "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
            "https://proxy.iri?code=host1", "https://host1.iri?code=host1WRONG",
            cpp.getAuthentification(), false, new SPEDSDto("3.0.0", "https://reference.iri/speds"));
    SealManager sm = new SealManager(objectMapper);
    String headerSeal = sm.createSeal(headerWrong, Seal.header, cpp.privateKey());
    String contentSeal = sm.createSeal("Protocol Data Unit (PDU) sérialisé de la couche Transport",
        Seal.content, cpp.privateKey());
    StampDto stamp = new StampDto(headerSeal, contentSeal);
    ProtocolDataUnit5Dto pdu = new ProtocolDataUnit5Dto(headerCorrect, stamp,
        "Protocol Data Unit (PDU) sérialisé de la couche Transport");

    InterfaceDataUnit56Dto idu56 =
        new InterfaceDataUnit56Dto(cxt, objectMapper.writeValueAsString(pdu));

    assertThrows(InvalidSignatureException.class,
        () -> exchangeIndication.validateReceivedMessage(objectMapper.writeValueAsString(idu56)));
  }

  @Test
  public void testValidateReceivedMessage_ContentSealException() throws Exception {
    // Data Unit (PDU) sérialisé de la couche Transport (wrong)\\\"}\"}";
    Context56Dto cxt = new Context56Dto("https://host2.iri",
        UUID.fromString("75553e0e-cfcc-434a-9189-04df7ddb1544"), false);
    String auth = KeyVar.host1CertRsa;
    String pk = KeyVar.host1PrikeyRsa;
    CertificatePrivateKeyPair cpp = CertificatePrivateKeyPair.importFromPem(auth, pk);

    HeaderDto header =
        new HeaderDto(HeaderDto.Msgtype.RES_ENV, "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
            "https://host1.iri?code=host1", "https://proxy.iri?code=host1",
            cpp.getAuthentification(), false, new SPEDSDto("3.0.0", "https://reference.iri/speds"));
    SealManager sm = new SealManager(objectMapper);
    String headerSeal = sm.createSeal(header, Seal.header, cpp.privateKey());
    String contentSeal =
        sm.createSeal("Protocol Data Unit (PDU) sérialisé de la couche Transport WRONG",
            Seal.content, cpp.privateKey());
    StampDto stamp = new StampDto(headerSeal, contentSeal);
    ProtocolDataUnit5Dto pdu = new ProtocolDataUnit5Dto(header, stamp,
        "Protocol Data Unit (PDU) sérialisé de la couche Transport");

    InterfaceDataUnit56Dto idu56 =
        new InterfaceDataUnit56Dto(cxt, objectMapper.writeValueAsString(pdu));
    assertThrows(InvalidSignatureException.class,
        () -> exchangeIndication.validateReceivedMessage(objectMapper.writeValueAsString(idu56)));
  }

  @Test
  public void testValidateReceivedMessage_ContentSealExceptionNull() throws Exception {
    // Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    Context56Dto cxt = new Context56Dto("https://host2.iri",
        UUID.fromString("75553e0e-cfcc-434a-9189-04df7ddb1544"), false);
    String auth = KeyVar.host1CertRsa;
    String pk = KeyVar.host1PrikeyRsa;
    CertificatePrivateKeyPair cpp = CertificatePrivateKeyPair.importFromPem(auth, pk);

    HeaderDto header =
        new HeaderDto(HeaderDto.Msgtype.RES_ENV, "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
            "https://host1.iri?code=host1", "https://proxy.iri?code=host1",
            cpp.getAuthentification(), false, new SPEDSDto("3.0.0", "https://reference.iri/speds"));
    SealManager sm = new SealManager(objectMapper);
    String headerSeal = sm.createSeal(header, Seal.header, cpp.privateKey());

    StampDto stamp = new StampDto(headerSeal, "");
    ProtocolDataUnit5Dto pdu = new ProtocolDataUnit5Dto(header, stamp,
        "Protocol Data Unit (PDU) sérialisé de la couche Transport");

    InterfaceDataUnit56Dto idu56 =
        new InterfaceDataUnit56Dto(cxt, objectMapper.writeValueAsString(pdu));
    assertThrows(InvalidSignatureException.class,
        () -> exchangeIndication.validateReceivedMessage(objectMapper.writeValueAsString(idu56)));
  }

  @Test
  public void testDataReplyProcess() {
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":\\\""
            + KeyVar.host1CertRsa
            + "\\\"},\\\"content\\\":\\\"Protocol Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    final String expectedIdu56 =
        "{\"context\":{\"destination_iri\":\"https://proxy.iri?code=host1\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":\\\"MIID+jCCAuKgAwIBAgIUTG1FwzCHn8ZKsvcUKUCAl4ZSKEcwDQYJKoZIhvcNAQELBQAwSzELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAkdEMQswCQYDVQQHDAJTWjEOMAwGA1UECgwFZ3JpaXMxEjAQBgNVBAMMCWxvY2FsaG9zdDAeFw0yNTA3MDcxNTQwNTFaFw0zNTA3MDUxNTQwNTFaMEsxCzAJBgNVBAYTAkNOMQswCQYDVQQIDAJHRDELMAkGA1UEBwwCU1oxDjAMBgNVBAoMBWdyaWlzMRIwEAYDVQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDO5upTQnWC9jH//FtV13gjIhRtgpOSkAAxUWRB9U5WkvnwYtA0ppDg3YTEOHa3g/GPk9yFUbpPs7wU3DQ70Tgh9OQjbR5iFtVQUNDVMnFim5J1/UBDBvsRRH8PhK5KZ53LrEd9lqPUdVAJMLDblbqYOf4lsztL5BF0A1PXg4HWGD+MtqcSzwAKwjGnd5OK+mT9wkbveXKjq3Eg4egjiT/m8EGcaNzHy319wxvSOhnW7eDspq2nuo9pPvD5ZidxhdHBelO5mOG/hZzL3f1L9suTQg8Cjfq2Mqr3EJXC9mf43sA62e21qVT9u+wb+STi1wZVbuT0kwL7LKJ+AMYpHvCbAgMBAAGjgdUwgdIwHQYDVR0OBBYEFHvAQGdVVDH0YxLIle4Ncs6QkFazMB8GA1UdIwQYMBaAFHvAQGdVVDH0YxLIle4Ncs6QkFazMA8GA1UdEwEB/wQFMAMBAf8wMQYIKwYBBQUHAQEEJTAjMCEGCCsGAQUFBzABhhVodHRwOi8vMTI3LjAuMC4xOjk5OTkwJwYDVR0lBCAwHgYIKwYBBQUHAwkGCCsGAQUFBwMCBggrBgEFBQcDATAjBgNVHREEHDAagglsb2NhbGhvc3SCDXd3dy5sb2NhbGhvc3QwDQYJKoZIhvcNAQELBQADggEBAGV5TPMMYM3/q/31JfuF5Yg+7DUr1JJzLYLmvkUAB7A3bP7CwBqzpZMEDKzzqcooRkpRDALrdDt9Ap8kzIFGEG0lPuczRN1hH7ddY7pHjg8La1SUYFdeIpcq7PMXxwhA3lwpthYxsopXceskXOk6VvNlNWdfRj1Dyp6xUeME70ZzMXD9xsaHex4gnJ7NkPIjG3OofuXyqpVZ6vkHfTE/9Ap/hyxDJOBVuN2BjV5dbQ4Kz3rF4veu77Ef/UHi5cdYrC4VbChEepyhfx8XOPAuOKZlAz5B0BFvLthdnIbJWtZ/mZebZpgZqLOUu0OW1sAic6bFWSIv0zjEu5eMCVFBmd0=\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"S40n1wUxYyxujmz4OMLl2kI9jqP0nSsLmu6wEc1zd9OpQhG5se7trG1r7NOAyK3yA7biP/YvahGEUTFwfGyqhXVZl0/7DuygVYnE1jXjXb4WN9VeTyrgVBkPcHAx7TWscURPNt7RI80Ggh/7bY2JkLF9uvCR7SVxL02sbmRw0kC/RHNOCyNJIsuj+Ft7x0jRqWSoh3qAnzVl4COsrJOEYlBov5x2FBn4V0IDOlC74RUe12eQdfDIsGjMwTAvyyKAW8W0UgjwxrVHjmTCGHNLKLSgYTtCbXsqmo3Jr7uHGKwa/t5ZaFKJz1D7x2cmvVsbgph7u+ktlbLlP+/J+sU4Tg==\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    final String actualIdu56 = exchangeIndication.dataReplyProcess(idu56);

    assertEquals(expectedIdu56, actualIdu56);
  }

  @Test
  public void testDataIndicationProcess() {
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"i2W2VBeU1BQ/cl/JEz1qZKShtDItNZHdxre9+VJv8qr41/xhYZMlW2agYZNw2HLJXZUf+RXa+1P3Y35iNEvXTTY2abUDrxuUksQjcPisxTmTTvHlqEsXHA/uSnk9aRjs2A7eAUDWTTVyOdW441Er9Rtzk9Raiud6C9WsHZycvyJ/GedINqTUXk/Q9d5TCkpq8PftgOtnWxItSPGGWArj+61zOn6PrSmaiBndDm4klm3jy7M2bUyzM5fCWjOhjzgvNnp+M3vMGrbJavc2pPndvJf4KuRUUI1WyFLr2wNfv9bb37rYbIJ6A0/MMMgLUyYGHwLqrBGIBPeQ/L2QTFL7z6pQMwQqJvEkgpISwq5JotgROQTGtZUi8Ml57ytZ2I6JSnagp+/v+2gHtFsofwIPfmpe0x6zGjs86P08XTX80+Z9U3aoRUwFVp+PVfgpdrHyAXT7CnYwoYmxpe3gRc/aeAmDSdtqF7mip7sR3YGIpiW/w4kOWQFE73Rpdlj4uNMJKsbZ4FmHxUL+cmqDTyemUAMgtQLQIlO9GSUZ+gBjXzbgWhYw3dIlsHEapRrEL6nTFRUkhf/d/582iwjqInpj5Fiaw9XH9mTicDZtgos/f8RLni6NBPwATRmWFt9bdPjQV4BqwBgwU+q/8Gh0KrhNVrxeHsu2S1TKiuVoO7t3y2U=\\\",\\\"content_seal\\\":\\\"PBeEqelCgDRbT0+/uct1ZdlGI5Vpv5Hkr0PA3t55bvj8ha82cm1vW2KwW+f6aMKRB7orPEU+QC9o28gWidFmTS7OnPiclmbBc20f3c37FZ0TNlm8P/71m5lsxW73KWkdS+8+nzv0/56BiJrVnX44Vdpsdas+UFXWc9d/nm322qQUTqOFqzKaylYbilnA4+p1yzxULbv2/Zf2SqCh7+KQP7gyq7jCRtCTT8mgXdpxrRDnTlZMrsUeNUvqnaLLpvTHNlA6CFQ9I9nJkBctH0iLUwpwMVZOlXYg2RQ1hrQ9YI6aZ6StHN5KYiHPww26DLqZz1AraK070JBncD6VFGuhKrG22BIl0gcmMz2t75gE4U24VVJxzcvFZ5AEyHNdYk6QHLYOZ7MAKqbWgxfhFSN7q0oK/Is1R0aYFgMsEWWb2RE7n/sTay6gWZ4MhnSVTvpuFsR9v0WIw9M94yT14xlFZL/5rT6Y+BL9Kv7LhOLFO9uWkdGKMJMlvvBL2TBbjPK4hG3b+WBhRJzzvdrry6a1Ki73317IwYSEd8lX54SEKgQ4AXEiwvAweJRUXrJQn/+u8FsNAgrfKnqctNNsIbtxPag2Flz99rnblg75n81VLxFrKK94DLlcOK/cb6Pud3zwsM+6bbkej4l6n55qRCV4Xi5Cpd8nu1A57rPLSjjMSGY=\\\"},\\\"content\\\":\\\"Protocol Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";

    final String expectedIdu45 =
        "{\"context\":{\"source_iri\":\"https://proxy.iri?code=host1\",\"destination_iri\":\"https://host2.iri?code=host2\",\"tracking_number\":\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\",\"options\":false},\"message\":\"Protocol Data Unit (PDU) sérialisé de la couche Transport\"}";
    final String actualIdu45 = exchangeIndication.dataIndicationProcess(idu56);
    assertEquals(expectedIdu45, actualIdu45);
  }

}
