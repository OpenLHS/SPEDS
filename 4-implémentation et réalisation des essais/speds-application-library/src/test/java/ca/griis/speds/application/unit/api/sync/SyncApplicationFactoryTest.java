package ca.griis.speds.application.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.speds.application.api.ApplicationFactory;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.exception.ParameterException;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.presentation.api.PresentationFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.ImmutablePresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SyncApplicationFactoryTest {

  PgaService pgaServiceMock;
  PresentationHost mockPresentationHost;

  @BeforeEach
  public void setUp() throws Exception {
    pgaServiceMock = Mockito.mock(PgaService.class);
    mockPresentationHost = Mockito.mock(ImmutablePresentationHost.class);
  }

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
    SyncApplicationFactory factory = new SyncApplicationFactory(pgaServiceMock) {
      @Override
      public PresentationHost initPresentationHost(String parameters) {
        return mockPresentationHost;
      }
    };
    ApplicationHost appHost = factory.init(params);
    assertNotNull(appHost);

    // Accès aux champs privés via la réflexion
    Field spedsVersionField = appHost.getClass().getDeclaredField("spedsVersion");
    spedsVersionField.setAccessible(true);
    String actualSpedsVersion = (String) spedsVersionField.get(appHost);

    Field spedsReferenceField = appHost.getClass().getDeclaredField("spedsReference");
    spedsReferenceField.setAccessible(true);
    String actualSpedsReference = (String) spedsReferenceField.get(appHost);
    assertEquals("2.0.0", actualSpedsVersion);
    assertEquals("a reference", actualSpedsReference);

  }

  @Test
  public void testInitParam_noSpedsVer_Exception() {
    // Given
    String params = "{\"options\": {\"speds.app.reference\": \"any\"}}";

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      new SyncApplicationFactory(pgaServiceMock).init(params);
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
      new SyncApplicationFactory(pgaServiceMock).init(params);
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
      new SyncApplicationFactory(pgaServiceMock).init(params);
    });

    assertTrue(exception.getMessage().contains("Cannot read initialization parameters"));
  }

  @Test
  public void testPresentationInit() throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {
    ApplicationFactory factory = new SyncApplicationFactory(pgaServiceMock);
    Field presFactory = factory.getClass().getDeclaredField("presentationFactory");
    presFactory.setAccessible(true);
    PresentationFactory mockPresFactory = Mockito.mock(MutablePresentationFactory.class);
    presFactory.set(factory, mockPresFactory);

    when(mockPresFactory.init(anyString())).thenReturn(mockPresentationHost);

    String params = """
        {
          "options": {
            "speds.app.version":"2.0.0",
            "speds.app.reference": "a reference"
          }
        }
        """;
    factory.initPresentationHost(params);

    verify(mockPresFactory, times(1)).init(params);
  }
}
