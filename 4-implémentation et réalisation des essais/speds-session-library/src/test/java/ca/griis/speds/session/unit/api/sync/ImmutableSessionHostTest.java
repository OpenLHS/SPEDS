package ca.griis.speds.session.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.SyncSessionFactory;
import ca.griis.speds.transport.api.TransportHost;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImmutableSessionHostTest {
  @Mock
  private PgaService pgaService;

  @Mock
  private TransportHost transportHost;

  @Test
  public void testClose() throws Exception {
    final SyncSessionFactory sessionFactory = new SyncSessionFactory(pgaService) {
      @Override
      public TransportHost initTransportHost(String parameters) {
        return transportHost;
      }
    };

    final String params =
        """
            {
              "options": {
                "speds.ses.version":"version",
                "speds.ses.reference": "reference",
                "speds.ses.cert": "MIICUzCCAf2gAwIBAgIUR8a3V5ghieJbpRycmtUMj9vkpIMwDQYJKoZIhvcNAQELBQAwfjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEOMAwGA1UECgwFZ3JpaXMxETAPBgNVBAMMCGJlYWMxNzA0MSYwJAYJKoZIhvcNAQkBFhdiZWFjMTcwNEB1c2hlcmJyb29rZS5jYTAeFw0yNTAxMjcxOTA2MDJaFw0yNjAxMjcxOTA2MDJaMH4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxDjAMBgNVBAoMBWdyaWlzMREwDwYDVQQDDAhiZWFjMTcwNDEmMCQGCSqGSIb3DQEJARYXYmVhYzE3MDRAdXNoZXJicm9va2UuY2EwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABo1MwUTAdBgNVHQ4EFgQUvoJL+3H4jsl91l0+k8v9zlsuvlgwHwYDVR0jBBgwFoAUvoJL+3H4jsl91l0+k8v9zlsuvlgwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAANBAGrU7iJbWLq9QwCUm+iBoE4/a4wnnx/or80b5olHSvNiOdG+hak2r1XBhuAPmN8qtI/q9yF+fzM895v6Q5DHs3s=",
                "speds.ses.private.key": "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABAkEAlbbR7UzDO5rHDNmk/ME0ZMdvEUgzn8GJAyEGCkhxnlRVE5hoqmG6O1F/F8A3/W+aZZXIXtJ1oEiHe/iaee+JYQIhAPMPKP/tKHacUDPZBCMMc/TL9GP2FTudr6SIJrviBp2fAiEA4zmw2FFPfbDWyvy22N1Ji5ifrwo4g/9bSkTBYEGbPqMCIQCqG8MbErUOBipPjyPJD88b1Z5OU4zas2qZITzSo8bziwIgCg7isiCfSSwLxf57xYu+FqzR3LiyGb2y982lVbJxwcsCIQDuG8NE9X6+0ZtRt0bLkEmYAzjXLIluzVfbZ2IyDawR6Q=="
              }
            }
            """;
    final SessionHost sessionHost = sessionFactory.init(params);

    assertDoesNotThrow(() -> sessionHost.close());
  }
}
