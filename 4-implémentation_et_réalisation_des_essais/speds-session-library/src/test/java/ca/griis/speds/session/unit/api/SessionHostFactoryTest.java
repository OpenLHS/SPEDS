package ca.griis.speds.session.unit.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.speds.session.api.SessionFactory;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.api.SessionHostFactory;
import ca.griis.speds.session.api.exception.ParameterException;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import ca.griis.speds.toolkit.project.ProjectService;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.TransportHostEvent;
import ca.griis.speds.transport.internal.ImmutableTransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SessionHostFactoryTest {
  private ProjectService pgaServiceMock;
  private SessionHostFactory syncSessionFactory;
  private TransportHost mockTransportHost;
  private CryptographyService cryptographyService;
  private SessionHostEvent event;

  @BeforeEach
  void setUp() throws Exception {
    pgaServiceMock = mock(ProjectService.class);
    mockTransportHost = mock(ImmutableTransportHost.class);
    cryptographyService = mock(CryptographyService.class);
    event = mock(SessionHostEvent.class);
    this.syncSessionFactory =
        new SessionHostFactory(pgaServiceMock, cryptographyService,
            () -> "907bbd0b-be29-4569-9d6b-69fc38d62c76") {
          @Override
          public TransportHost initTransportHost(String parameters,
              TransportHostEvent hostEventConsumer) {
            return mockTransportHost;
          }
        };
  }

  @Test
  public void testInitHost() throws Exception {
    String params =
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
    SessionHost sessionHost = this.syncSessionFactory.initHost(params, event);
    assertNotNull(sessionHost);
  }

  @Test
  public void testInitHostParam_noSpedsVer_Exception() {
    // Given
    String params =
        """
            {
              "options": {
                "speds.ses.reference": "reference",
                "speds.ses.cert": "MIICUzCCAf2gAwIBAgIUR8a3V5ghieJbpRycmtUMj9vkpIMwDQYJKoZIhvcNAQELBQAwfjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEOMAwGA1UECgwFZ3JpaXMxETAPBgNVBAMMCGJlYWMxNzA0MSYwJAYJKoZIhvcNAQkBFhdiZWFjMTcwNEB1c2hlcmJyb29rZS5jYTAeFw0yNTAxMjcxOTA2MDJaFw0yNjAxMjcxOTA2MDJaMH4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxDjAMBgNVBAoMBWdyaWlzMREwDwYDVQQDDAhiZWFjMTcwNDEmMCQGCSqGSIb3DQEJARYXYmVhYzE3MDRAdXNoZXJicm9va2UuY2EwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABo1MwUTAdBgNVHQ4EFgQUvoJL+3H4jsl91l0+k8v9zlsuvlgwHwYDVR0jBBgwFoAUvoJL+3H4jsl91l0+k8v9zlsuvlgwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAANBAGrU7iJbWLq9QwCUm+iBoE4/a4wnnx/or80b5olHSvNiOdG+hak2r1XBhuAPmN8qtI/q9yF+fzM895v6Q5DHs3s=",
                "speds.ses.private.key": "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABAkEAlbbR7UzDO5rHDNmk/ME0ZMdvEUgzn8GJAyEGCkhxnlRVE5hoqmG6O1F/F8A3/W+aZZXIXtJ1oEiHe/iaee+JYQIhAPMPKP/tKHacUDPZBCMMc/TL9GP2FTudr6SIJrviBp2fAiEA4zmw2FFPfbDWyvy22N1Ji5ifrwo4g/9bSkTBYEGbPqMCIQCqG8MbErUOBipPjyPJD88b1Z5OU4zas2qZITzSo8bziwIgCg7isiCfSSwLxf57xYu+FqzR3LiyGb2y982lVbJxwcsCIQDuG8NE9X6+0ZtRt0bLkEmYAzjXLIluzVfbZ2IyDawR6Q=="
              }
            }
            """;

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      this.syncSessionFactory.initHost(params, event);
    });

    assertTrue(exception.getMessage()
        .contains("SPEDS version is missing in the initialization parameters."));
  }

  @Test
  public void testInitHostParam_noSpedsRef_Exception() {
    // Given
    String params =
        """
            {
              "options": {
                "speds.ses.version":"version",
                "speds.ses.cert": "MIICUzCCAf2gAwIBAgIUR8a3V5ghieJbpRycmtUMj9vkpIMwDQYJKoZIhvcNAQELBQAwfjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEOMAwGA1UECgwFZ3JpaXMxETAPBgNVBAMMCGJlYWMxNzA0MSYwJAYJKoZIhvcNAQkBFhdiZWFjMTcwNEB1c2hlcmJyb29rZS5jYTAeFw0yNTAxMjcxOTA2MDJaFw0yNjAxMjcxOTA2MDJaMH4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxDjAMBgNVBAoMBWdyaWlzMREwDwYDVQQDDAhiZWFjMTcwNDEmMCQGCSqGSIb3DQEJARYXYmVhYzE3MDRAdXNoZXJicm9va2UuY2EwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABo1MwUTAdBgNVHQ4EFgQUvoJL+3H4jsl91l0+k8v9zlsuvlgwHwYDVR0jBBgwFoAUvoJL+3H4jsl91l0+k8v9zlsuvlgwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAANBAGrU7iJbWLq9QwCUm+iBoE4/a4wnnx/or80b5olHSvNiOdG+hak2r1XBhuAPmN8qtI/q9yF+fzM895v6Q5DHs3s=",
                "speds.ses.private.key": "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABAkEAlbbR7UzDO5rHDNmk/ME0ZMdvEUgzn8GJAyEGCkhxnlRVE5hoqmG6O1F/F8A3/W+aZZXIXtJ1oEiHe/iaee+JYQIhAPMPKP/tKHacUDPZBCMMc/TL9GP2FTudr6SIJrviBp2fAiEA4zmw2FFPfbDWyvy22N1Ji5ifrwo4g/9bSkTBYEGbPqMCIQCqG8MbErUOBipPjyPJD88b1Z5OU4zas2qZITzSo8bziwIgCg7isiCfSSwLxf57xYu+FqzR3LiyGb2y982lVbJxwcsCIQDuG8NE9X6+0ZtRt0bLkEmYAzjXLIluzVfbZ2IyDawR6Q=="
              }
            }
            """;

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      this.syncSessionFactory.initHost(params, event);
    });

    assertTrue(exception.getMessage()
        .contains("SPEDS reference is missing in the initialization parameters."));
  }

  @Test
  public void testInitHostParam_noSpedsCert_Exception() {
    // Given
    String params =
        """
            {
              "options": {
                "speds.ses.version":"version",
                "speds.ses.reference": "reference",
                "speds.ses.private.key": "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABAkEAlbbR7UzDO5rHDNmk/ME0ZMdvEUgzn8GJAyEGCkhxnlRVE5hoqmG6O1F/F8A3/W+aZZXIXtJ1oEiHe/iaee+JYQIhAPMPKP/tKHacUDPZBCMMc/TL9GP2FTudr6SIJrviBp2fAiEA4zmw2FFPfbDWyvy22N1Ji5ifrwo4g/9bSkTBYEGbPqMCIQCqG8MbErUOBipPjyPJD88b1Z5OU4zas2qZITzSo8bziwIgCg7isiCfSSwLxf57xYu+FqzR3LiyGb2y982lVbJxwcsCIQDuG8NE9X6+0ZtRt0bLkEmYAzjXLIluzVfbZ2IyDawR6Q=="
              }
            }
            """;

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      this.syncSessionFactory.initHost(params, event);
    });

    assertTrue(exception.getMessage()
        .contains("Parameter speds.ses.cert is missing."));
  }

  @Test
  public void testInitHostParam_noSpedsPrivateKey_Exception() {
    // Given
    String params =
        """
            {
              "options": {
                "speds.ses.version":"version",
                "speds.ses.reference": "reference",
                "speds.ses.cert": "MIICUzCCAf2gAwIBAgIUR8a3V5ghieJbpRycmtUMj9vkpIMwDQYJKoZIhvcNAQELBQAwfjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEOMAwGA1UECgwFZ3JpaXMxETAPBgNVBAMMCGJlYWMxNzA0MSYwJAYJKoZIhvcNAQkBFhdiZWFjMTcwNEB1c2hlcmJyb29rZS5jYTAeFw0yNTAxMjcxOTA2MDJaFw0yNjAxMjcxOTA2MDJaMH4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxDjAMBgNVBAoMBWdyaWlzMREwDwYDVQQDDAhiZWFjMTcwNDEmMCQGCSqGSIb3DQEJARYXYmVhYzE3MDRAdXNoZXJicm9va2UuY2EwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABo1MwUTAdBgNVHQ4EFgQUvoJL+3H4jsl91l0+k8v9zlsuvlgwHwYDVR0jBBgwFoAUvoJL+3H4jsl91l0+k8v9zlsuvlgwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAANBAGrU7iJbWLq9QwCUm+iBoE4/a4wnnx/or80b5olHSvNiOdG+hak2r1XBhuAPmN8qtI/q9yF+fzM895v6Q5DHs3s="
              }
            }
            """;

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      this.syncSessionFactory.initHost(params, event);
    });

    assertTrue(exception.getMessage()
        .contains("Parameter speds.ses.private.key is missing."));
  }

  @Test
  public void testJsonProcessingException() {
    // Given
    String params = UUID.randomUUID().toString();

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      this.syncSessionFactory.initHost(params, event);
    });

    assertTrue(exception.getMessage().contains("Cannot read initialization parameters"));
  }

  @Test
  public void testInitHostTransportHost() throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException, JsonProcessingException {
    var config =
        """
            {
              "spedsProfile": [
                 {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "SYMM",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "ASYM",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "HASH",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "SIGN",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "DH",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "TRANSPORT",
                  "algorithmCategory": "HASH",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "NETWORK",
                  "algorithmCategory": "SIGN",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "NETWORK",
                  "algorithmCategory": "HASH",
                  "securityProfile": "STRONG"
                }
              ]
            }
                    """;
    final var cryptographyService = new DefaultCryptographyFactory().initCipherSuite(config);
    SessionFactory factory =
        new SessionHostFactory(pgaServiceMock, cryptographyService,
            () -> "907bbd0b-be29-4569-9d6b-69fc38d62c76");
    Field traFactory = factory.getClass().getDeclaredField("transportFactory");
    traFactory.setAccessible(true);
    TransportFactory mockTraFactory = mock(TransportFactory.class);
    traFactory.set(factory, mockTraFactory);

    when(mockTraFactory.initHost(anyString(), any())).thenReturn(mockTransportHost);

    String params =
        """
            {
              "options": {
                "speds.tra.version":"version",
                "speds.tra.reference": "reference",
                "speds.ses.cert": "MIICUzCCAf2gAwIBAgIUR8a3V5ghieJbpRycmtUMj9vkpIMwDQYJKoZIhvcNAQELBQAwfjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBnF1ZWJlYzETMBEGA1UEBwwKc2hlcmJyb29rZTEOMAwGA1UECgwFZ3JpaXMxETAPBgNVBAMMCGJlYWMxNzA0MSYwJAYJKoZIhvcNAQkBFhdiZWFjMTcwNEB1c2hlcmJyb29rZS5jYTAeFw0yNTAxMjcxOTA2MDJaFw0yNjAxMjcxOTA2MDJaMH4xCzAJBgNVBAYTAkNBMQ8wDQYDVQQIDAZxdWViZWMxEzARBgNVBAcMCnNoZXJicm9va2UxDjAMBgNVBAoMBWdyaWlzMREwDwYDVQQDDAhiZWFjMTcwNDEmMCQGCSqGSIb3DQEJARYXYmVhYzE3MDRAdXNoZXJicm9va2UuY2EwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABo1MwUTAdBgNVHQ4EFgQUvoJL+3H4jsl91l0+k8v9zlsuvlgwHwYDVR0jBBgwFoAUvoJL+3H4jsl91l0+k8v9zlsuvlgwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAANBAGrU7iJbWLq9QwCUm+iBoE4/a4wnnx/or80b5olHSvNiOdG+hak2r1XBhuAPmN8qtI/q9yF+fzM895v6Q5DHs3s=",
                "speds.ses.private.key": "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA1703ot50fyfd/+nx/IW/daRWx0HRTYbqovpVK/yN+aVz+2JEVWcX/oEpIDUbJrWqTPu5PFh9qKZ3w60FUqfePQIDAQABAkEAlbbR7UzDO5rHDNmk/ME0ZMdvEUgzn8GJAyEGCkhxnlRVE5hoqmG6O1F/F8A3/W+aZZXIXtJ1oEiHe/iaee+JYQIhAPMPKP/tKHacUDPZBCMMc/TL9GP2FTudr6SIJrviBp2fAiEA4zmw2FFPfbDWyvy22N1Ji5ifrwo4g/9bSkTBYEGbPqMCIQCqG8MbErUOBipPjyPJD88b1Z5OU4zas2qZITzSo8bziwIgCg7isiCfSSwLxf57xYu+FqzR3LiyGb2y982lVbJxwcsCIQDuG8NE9X6+0ZtRt0bLkEmYAzjXLIluzVfbZ2IyDawR6Q=="
              }
            }
            """;

    TransportHostEvent hostEventConsumer = mock(TransportHostEvent.class);
    factory.initTransportHost(params, hostEventConsumer);

    verify(mockTraFactory, times(1)).initHost(params, hostEventConsumer);
  }
}
