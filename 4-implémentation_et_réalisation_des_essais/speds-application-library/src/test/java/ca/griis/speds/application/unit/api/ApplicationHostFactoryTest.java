package ca.griis.speds.application.unit.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.VersionDto;
import ca.griis.speds.application.api.ApplicationFactory;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.ApplicationHostEvent;
import ca.griis.speds.application.api.exception.ParameterException;
import ca.griis.speds.application.internal.ApplicationHostFactory;
import ca.griis.speds.presentation.api.PresentationFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.PresentationHostEvent;
import ca.griis.speds.presentation.api.PresentationHostFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApplicationHostFactoryTest {

  @Mock
  private PresentationHost presentationHost;

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private ProjectService projectService;

  @Test
  public void testInit() throws Exception {
    String params = """
        {
          "options": {
            "speds.app.version":"2.0.0",
            "speds.app.reference": "a reference"
          }
        }
        """;
    ApplicationHostFactory factory =
        new ApplicationHostFactory(cryptographyService, projectService) {
          @Override
          public PresentationHost initPresentationHost(String parameters,
              PresentationHostEvent consumer) {
            return presentationHost;
          }
        };

    ApplicationHostEvent mock = mock(ApplicationHostEvent.class);
    ApplicationHost appHost = factory.init(params, mock, content -> true);
    assertNotNull(appHost);

    // Accès aux champs privés via la réflexion
    Field versionField = appHost.getClass().getDeclaredField("version");
    versionField.setAccessible(true);
    VersionDto actualVersion = (VersionDto) versionField.get(appHost);

    assertEquals("2.0.0", actualVersion.getNumber());
    assertEquals("a reference", actualVersion.getReference());

  }

  @Test
  public void testInitParam_noSpedsVer_Exception() {
    // Given
    String params = "{\"options\": {\"speds.app.reference\": \"any\"}}";

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      ApplicationHostEvent mock = mock(ApplicationHostEvent.class);
      new ApplicationHostFactory(cryptographyService, projectService).init(params,
          mock, content -> true);
    });

    assertTrue(exception.getMessage()
        .contains("SPEDS version or reference is missing in the initialization parameters."));
  }

  @Test
  public void testInitParam_noSpedsRef_Exception() {
    // Given
    String params = "{\"options\": {\"speds.app.version\": \"1.0.0\"}}";

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      ApplicationHostEvent mock = mock(ApplicationHostEvent.class);
      new ApplicationHostFactory(cryptographyService, projectService).init(params,
          mock, content -> true);
    });

    assertTrue(exception.getMessage()
        .contains("SPEDS version or reference is missing in the initialization parameters."));
  }

  @Test
  public void testJsonProcessingException() {
    // Given
    String params = UUID.randomUUID().toString();

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      ApplicationHostEvent mock = mock(ApplicationHostEvent.class);
      new ApplicationHostFactory(cryptographyService, projectService).init(params,
          mock, content -> true);
    });

    assertTrue(exception.getMessage().contains("Cannot read initialization parameters"));
  }

  @Test
  public void testPresentationInit()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException,
      IllegalAccessException {
    ApplicationFactory factory = new ApplicationHostFactory(cryptographyService, projectService);
    Field presFactory = factory.getClass().getDeclaredField("presentationHostFactory");
    presFactory.setAccessible(true);
    PresentationFactory mockPresFactory = mock(PresentationHostFactory.class);
    presFactory.set(factory, mockPresFactory);

    when(mockPresFactory.initHost(anyString(), any(PresentationHostEvent.class))).thenReturn(
        presentationHost);

    String params = """
        {
          "options": {
            "speds.app.version":"2.0.0",
            "speds.app.reference": "a reference"
          }
        }
        """;
    PresentationHostEvent mock = mock(PresentationHostEvent.class);
    factory.initPresentationHost(params, mock);

    verify(mockPresFactory, times(1)).initHost(anyString(), any(PresentationHostEvent.class));
  }
}
