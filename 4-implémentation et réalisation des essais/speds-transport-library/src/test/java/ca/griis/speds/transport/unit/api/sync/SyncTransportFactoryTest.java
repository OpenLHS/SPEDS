package ca.griis.speds.transport.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.speds.network.api.NetworkFactory;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.SyncNetworkFactory;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.sync.SyncTransportFactory;
import ca.griis.speds.transport.exception.ParameterException;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SyncTransportFactoryTest {
  private NetworkHost mockNetworkHost;

  @BeforeEach
  public void setUp() throws Exception {
    mockNetworkHost = mock(NetworkHost.class);
  }

  @Test
  public void testInit() throws Exception {
    String params = """
        {
          "options": {
            "speds.tra.version":"2.0.0",
            "speds.tra.reference": "a reference"
          }
        }
        """;
    SyncTransportFactory factory = new SyncTransportFactory() {
      @Override
      public NetworkHost initNetworkHost(String parameters) {
        return mockNetworkHost;
      }
    };
    TransportHost host = factory.init(params);
    assertNotNull(host);

    // Accès aux champs privés via la réflexion
    Field spedsVersionField = host.getClass().getDeclaredField("spedsVersion");
    spedsVersionField.setAccessible(true);
    String actualSpedsVersion = (String) spedsVersionField.get(host);

    Field spedsReferenceField = host.getClass().getDeclaredField("spedsReference");
    spedsReferenceField.setAccessible(true);
    String actualSpedsReference = (String) spedsReferenceField.get(host);
    assertEquals("2.0.0", actualSpedsVersion);
    assertEquals("a reference", actualSpedsReference);
  }

  @Test
  public void testInitParam_noSpedsVer_Exception() {
    // Given
    String params = "{\"options\": {\"speds.tra.reference\": \"any\"}}";

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      new SyncTransportFactory().init(params);
    });

    assertTrue(exception.getMessage()
        .contains("SPEDS version is missing in the initialization parameters."));
  }

  @Test
  public void testInitParam_noSpedsRef_Exception() {
    // Given
    String params = "{\"options\": {\"speds.tra.version\": \"1.0.0\"}}";

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      new SyncTransportFactory().init(params);
    });

    assertTrue(exception.getMessage()
        .contains("SPEDS reference is missing in the initialization parameters."));
  }

  @Test
  public void testJsonProcessingException() {
    // Given
    String params = UUID.randomUUID().toString();

    // When & Then
    ParameterException exception = assertThrows(ParameterException.class, () -> {
      new SyncTransportFactory().init(params);
    });

    assertTrue(exception.getMessage().contains("Cannot read initialization parameters"));
  }

  @Test
  public void testNetworkInit() throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {
    TransportFactory factory = new SyncTransportFactory();
    Field netFactory = factory.getClass().getDeclaredField("networkFactory");
    netFactory.setAccessible(true);
    NetworkFactory mockNetFactory = mock(SyncNetworkFactory.class);
    netFactory.set(factory, mockNetFactory);

    when(mockNetFactory.initHost(anyString())).thenReturn(mockNetworkHost);

    String params = """
        {
          "options": {
            "speds.tra.version":"2.0.0",
            "speds.tra.reference": "a reference"
          }
        }
        """;
    factory.initNetworkHost(params);

    verify(mockNetFactory, times(1)).initHost(params);
  }
}
