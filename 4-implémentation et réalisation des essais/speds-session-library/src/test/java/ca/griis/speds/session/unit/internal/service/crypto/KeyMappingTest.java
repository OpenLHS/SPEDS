package ca.griis.speds.session.unit.internal.service.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.cryptography.asymmetric.keypair.CertificatePrivateKeysEntry;
import ca.griis.speds.session.api.exception.CipherException;
import ca.griis.speds.session.api.exception.ParameterException;
import ca.griis.speds.session.internal.util.KeyAlgorithm;
import ca.griis.speds.session.internal.util.KeyMapping;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

public class KeyMappingTest {

  @Test
  public void testGetPublicKeyFromString_success() {
    String pem =
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAi7dTOm4wIDSos1B4Bucbu/0k4eaQW6xqQj8k4+e5jzOM5hFcC0NMCSP94WlLYqDMhIq3sovOsFunhZd+J0eaDHFovovIBPgfDfuYFrAevZ7mSDlrpHV+T8PFzyf/ZacjXclCVfzm8DKpYNWM88xzTRPIUxJta6g5FNBHip0ekgOdWUg+Fo3XFcSz/sEuNWg+W07IGSMbgIcjZyDFJ9PGcUeIudQo5wR27g5V8yKHu5Q/MO2EQ5uhP6xkiUkVVAuc9cMHh6u1jLg8S2xCMm3REq5zk0XnH+Fm9bPV7CLAwd2bOxxaYMBEmGQTKS3Ly4x90Hid3RG4FnchjbrJVjelmPVnapI/tVv5J12UQMA6hrK5+8gWxwZIt1WzB3r6HCFUvvktIQyfSel+W50v4jWe1sziyP0sK+0aBMqMqdaKK03XW2G5O7EUtl27ryC2uqh5nnKxnWoMhJQIuWIl66Ha5e8dB4tMRBtUm+P4SVQX3m0kPzieHQYYW3dNfrbxsYKEdTkfLgjzalTq9XHu/S9zQ9sLDV1KbUqaqj5z3FcFsukzmRt4oe4gfU7nXBjun1JS4Frg+TNOA1hWMJD+cp+E9fPeKsFLbJCsxiFYQ+lfnl3VWpprFYcoPKRdE3T45yEmINfPIBWVA8BsVaSKRCPPcLbqt8EkI1yjX9z3vbsUKokCAwEAAQ==";
    PublicKey key = KeyMapping.getPublicKeyFromString(pem, KeyAlgorithm.RSA);

    assertNotNull(key);
    assertEquals("RSA", key.getAlgorithm());
  }

  @Test
  public void testGetPublicKeyFromString_exception() {
    String pem = "badKey";

    assertThrows(CipherException.class, () -> {
      KeyMapping.getPublicKeyFromString(pem, KeyAlgorithm.RSA);
    });
  }

  @Test
  public void testGetAesSecretKeyFromByte_success() {
    String generatedKey = "ypJTUSvvo6SKqcYL9ynL+zKg61nNDo8dlS9/WouKDRM=";
    SecretKey key = KeyMapping.getAesSecretKeyFromByte(Base64.getDecoder().decode(generatedKey));

    assertNotNull(key);
    assertEquals("AES", key.getAlgorithm());
  }

  @Test
  public void testGetCertificatePrivateKey_success() throws CertificateEncodingException {
    String cert =
        "MIICUzCCAf2gAwIBAgIUR8a3V5ghieJbpRycmtUMj9vkpIMwDQYJKoZIhvcNAQELBQAwfjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEOMAwGA1UECgwFZ3JpaXMxETAPBgNVBAMMCGJlYWMxNzA0MSYwJAYJKoZIhvcNAQkBFhdiZWFjMTcwNEB1c2hlcmJyb29rZS5jYTAeFw0yNTAxMjcxOTA2MDJaFw0yNjAxMjcxOTA2MDJaMH4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxDjAMBgNVBAoMBWdyaWlzMREwDwYDVQQDDAhiZWFjMTcwNDEmMCQGCSqGSIb3DQEJARYXYmVhYzE3MDRAdXNoZXJicm9va2UuY2EwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABo1MwUTAdBgNVHQ4EFgQUvoJL+3H4jsl91l0+k8v9zlsuvlgwHwYDVR0jBBgwFoAUvoJL+3H4jsl91l0+k8v9zlsuvlgwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAANBAGrU7iJbWLq9QwCUm+iBoE4/a4wnnx/or80b5olHSvNiOdG+hak2r1XBhuAPmN8qtI/q9yF+fzM895v6Q5DHs3s=";
    String key =
        "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABAkEAlbbR7UzDO5rHDNmk/ME0ZMdvEUgzn8GJAyEGCkhxnlRVE5hoqmG6O1F/F8A3/W+aZZXIXtJ1oEiHe/iaee+JYQIhAPMPKP/tKHacUDPZBCMMc/TL9GP2FTudr6SIJrviBp2fAiEA4zmw2FFPfbDWyvy22N1Ji5ifrwo4g/9bSkTBYEGbPqMCIQCqG8MbErUOBipPjyPJD88b1Z5OU4zas2qZITzSo8bziwIgCg7isiCfSSwLxf57xYu+FqzR3LiyGb2y982lVbJxwcsCIQDuG8NE9X6+0ZtRt0bLkEmYAzjXLIluzVfbZ2IyDawR6Q==";

    CertificatePrivateKeysEntry entry = KeyMapping.getCertificatePrivateKey(cert, key);

    assertEquals(cert, Base64.getEncoder().encodeToString(entry.getCertficate().getEncoded()));
    assertEquals(key, Base64.getEncoder().encodeToString(entry.getPrivateKey().getEncoded()));
  }

  @Test
  public void testGetCertificatePrivateKey_badCertificate() {
    String cert = "badCertificate";
    String key =
        "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABAkEAlbbR7UzDO5rHDNmk/ME0ZMdvEUgzn8GJAyEGCkhxnlRVE5hoqmG6O1F/F8A3/W+aZZXIXtJ1oEiHe/iaee+JYQIhAPMPKP/tKHacUDPZBCMMc/TL9GP2FTudr6SIJrviBp2fAiEA4zmw2FFPfbDWyvy22N1Ji5ifrwo4g/9bSkTBYEGbPqMCIQCqG8MbErUOBipPjyPJD88b1Z5OU4zas2qZITzSo8bziwIgCg7isiCfSSwLxf57xYu+FqzR3LiyGb2y982lVbJxwcsCIQDuG8NE9X6+0ZtRt0bLkEmYAzjXLIluzVfbZ2IyDawR6Q==";

    assertThrows(ParameterException.class, () -> {
      KeyMapping.getCertificatePrivateKey(cert, key);
    });
  }
}
