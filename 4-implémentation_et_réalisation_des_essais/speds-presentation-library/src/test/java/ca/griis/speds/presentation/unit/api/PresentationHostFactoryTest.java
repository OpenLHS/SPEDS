package ca.griis.speds.presentation.unit.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.PresentationHostEvent;
import ca.griis.speds.presentation.api.PresentationHostFactory;
import ca.griis.speds.presentation.api.exception.ParameterException;
import ca.griis.speds.session.api.SessionFactory;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.api.SessionHostFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

public class PresentationHostFactoryTest {
  private PresentationHostFactory factory;
  private PresentationHostFactory spyFactory;

  @Mock
  private ProjectService projectService;

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private PresentationHostEvent mockHostEventConsumer;

  @Mock
  private SessionHost mockSessionHost;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    try (MockedConstruction<SessionHostFactory> ignore =
        mockConstruction(SessionHostFactory.class)) {

      factory = new PresentationHostFactory(projectService, cryptographyService);
    }

    spyFactory = spy(factory);
    doReturn(mockSessionHost)
        .when(spyFactory).initSessionHost(anyString(), any(SessionHostEvent.class));
  }

  @Test
  void testInitSuccess() {
    String parameters = """
        {
          "options": {
            "speds.pre.version": "7.0.0",
            "speds.pre.reference": "presentation"
          }
        }
        """;

    PresentationHost host = spyFactory.initHost(parameters, mockHostEventConsumer);

    assertNotNull(host);
  }

  @Test
  void testInitMissingVersion() {
    String parameters = """
        {
          "options": {
            "speds.pre.reference": "presentation"
          }
        }
        """;

    assertThrows(ParameterException.class, () -> {
      spyFactory.initHost(parameters, mockHostEventConsumer);
    });
  }

  @Test
  void testInitMissingReference() {
    String parameters = """
        {
          "options": {
            "speds.pre.version": "7.0.0"
          }
        }
        """;

    assertThrows(ParameterException.class, () -> {
      spyFactory.initHost(parameters, mockHostEventConsumer);
    });
  }

  @Test
  void testInitBothMissing() {
    String parameters = """
        {
          "options": {}
        }
        """;

    assertThrows(ParameterException.class, () -> {
      spyFactory.initHost(parameters, mockHostEventConsumer);
    });
  }

  @Test
  void testInitInvalidJson() {
    assertThrows(ParameterException.class, () -> {
      spyFactory.initHost("not valid json", mockHostEventConsumer);
    });
  }

  @Disabled
  @Test
  void testInitSessionHost() throws Exception {
    SessionHostEvent mockEvent = mock(SessionHostEvent.class);
    SessionHost expectedHost = mock(SessionHost.class);

    // Mocker la SessionFactory interne
    SessionFactory mockFactory = mock(SessionFactory.class);
    when(mockFactory.initHost(anyString(), any(SessionHostEvent.class)))
        .thenReturn(expectedHost);

    // Injecter via reflection
    java.lang.reflect.Field factoryField =
        PresentationHostFactory.class.getDeclaredField("factory");
    factoryField.setAccessible(true);
    factoryField.set(factory, mockFactory);

    String parameters = """
        {
          "options": {
            "speds.pre.version": "7.0.0",
            "speds.pre.reference": "presentation"
          }
        }
        """;

    SessionHost host = factory.initSessionHost(parameters, mockEvent);

    assertNotNull(host);
    verify(mockFactory).initHost(parameters, mockEvent);
  }
}
