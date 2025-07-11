package ca.griis.speds.presentation.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.griis.speds.link.api.exception.ParameterException;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.ImmutableSessionFactory;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MutablePresentationFactoryTest {

  @Mock
  private PgaService pgaService;

  @Mock
  private SessionHost mockSessionHost;

  @InjectMocks
  private MutablePresentationFactory factory;

  @Mock
  private ImmutableSessionFactory sessionFactory;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testInitSuccess() throws Exception {
    // Given
    String parameters =
        "{\"options\": {\"speds.pre.version\": \"1.0.0\", \"speds.pre.reference\": \"ref-1234\"}}";
    MutablePresentationFactory factoryWithMockedSession = new MutablePresentationFactory(null) {
      @Override
      public SessionHost initSessionHost(String parameters) {
        return mockSessionHost;
      }
    };

    // When
    PresentationHost actual = factoryWithMockedSession.init(parameters);

    // Then
    // Accès aux champs privés via la réflexion
    Field spedsVersionField = actual.getClass().getDeclaredField("spedsVersion");
    spedsVersionField.setAccessible(true);
    String actualSpedsVersion = (String) spedsVersionField.get(actual);

    Field spedsReferenceField = actual.getClass().getDeclaredField("spedsReference");
    spedsReferenceField.setAccessible(true);
    String actualSpedsReference = (String) spedsReferenceField.get(actual);

    assertNotNull(actual);
    assertEquals("1.0.0", actualSpedsVersion);
    assertEquals("ref-1234", actualSpedsReference);
  }

  @Test
  void testInitParam_noSpedsVer_Exception() throws NoSuchFieldException, IllegalAccessException {
    // Given
    String parameters = "{\"options\": {\"speds.app.reference\": \"any\"}}";

    // Override exit
    when(sessionFactory.init(any())).thenReturn(mockSessionHost);
    Field factoryField = factory.getClass().getDeclaredField("factory");
    factoryField.setAccessible(true);
    factoryField.set(factory, sessionFactory);

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      factory.init(parameters);
    });

    assertTrue(exception.getMessage()
        .contains("SPEDS version or reference is missing in the initialization parameters."));
  }

  @Test
  void testInitParam_noSpedsRef_Exception() throws IllegalAccessException, NoSuchFieldException {
    // Given
    String parameters = "{\"options\": {\"speds.app.version\": \"1.0.0\"}}";

    // Override exit
    when(sessionFactory.init(any())).thenReturn(mockSessionHost);
    Field factoryField = factory.getClass().getDeclaredField("factory");
    factoryField.setAccessible(true);
    factoryField.set(factory, sessionFactory);

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      factory.init(parameters);
    });

    assertTrue(exception.getMessage()
        .contains("SPEDS version or reference is missing in the initialization parameters."));
  }

  @Test
  void testJsonProcessingException() throws NoSuchFieldException, IllegalAccessException {
    // Given
    String parameters = UUID.randomUUID().toString();

    // Override exit
    when(sessionFactory.init(any())).thenReturn(mockSessionHost);
    Field factoryField = factory.getClass().getDeclaredField("factory");
    factoryField.setAccessible(true);
    factoryField.set(factory, sessionFactory);

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      factory.init(parameters);
    });

    assertTrue(exception.getMessage().contains("Cannot read initialization parameters"));
  }

  @Test
  void testInitSessionHostCalled() throws NoSuchFieldException, IllegalAccessException {
    // Given
    String parameters = "someParameters";

    // Override exit
    when(sessionFactory.init(any())).thenReturn(mockSessionHost);
    Field factoryField = factory.getClass().getDeclaredField("factory");
    factoryField.setAccessible(true);
    factoryField.set(factory, sessionFactory);

    // When
    SessionHost actual = factory.initSessionHost(parameters);

    // Then
    assertNotNull(actual);
  }
}
