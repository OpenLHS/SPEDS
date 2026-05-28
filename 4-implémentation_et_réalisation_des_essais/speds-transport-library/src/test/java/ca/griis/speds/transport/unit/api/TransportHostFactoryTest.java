package ca.griis.speds.transport.unit.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import ca.griis.js2p.gen.speds.transport.api.dto.InitInParamsDto;
import ca.griis.speds.network.api.NetworkFactory;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.NetworkHostEvent;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.TransportHostEvent;
import ca.griis.speds.transport.api.TransportHostFactory;
import ca.griis.speds.transport.api.exception.ParameterException;
import ca.griis.speds.transport.internal.event.TransportEventHandler;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * "Description brève du composant (classe, interface, ...)"
 *
 * <h3>Historique</h3>
 * <p>
 * XXXX-XX-XX [AS] - Implémentation initiale<br>
 * </p>
 *
 * <h3>Tâches</h3>
 * S.O.
 *
 * @author [AS] ameni.souid@usherbrooke.ca
 * @since
 */

@ExtendWith(MockitoExtension.class)
public class TransportHostFactoryTest {

  private ObjectMapper mapper;
  private CryptographyService crypto;

  @BeforeEach
  void setUp() {
    mapper = SharedObjectMapper.getInstance().getMapper();
    crypto = mock(CryptographyService.class);
  }

  @Test
  void noArgConstructor() {
    TransportFactory factory = new TransportHostFactory(crypto);
    assertNotNull(factory);
  }

  @Test
  void initHost() throws Exception {
    String parameters = buildValidParametersJson();

    NetworkHost networkHost = mock(NetworkHost.class);
    doNothing().when(networkHost).close();

    TransportHostFactory factory = new TransportHostFactory(crypto) {
      @Override
      public NetworkHost initNetworkHost(String parameters, NetworkHostEvent hostEventConsumer) {
        assertNotNull(hostEventConsumer);
        assertTrue(hostEventConsumer instanceof TransportEventHandler);
        return networkHost;
      }
    };

    TransportHostEvent eventConsumer = mock(TransportHostEvent.class);

    TransportHost host = factory.initHost(parameters, eventConsumer);
    assertNotNull(host);

    host.close();
    verify(networkHost, times(1)).close();
  }

  @Test
  void initHostThrowParameterException() throws Exception {
    Map<String, Object> options = new HashMap<>();
    options.put("speds.tra.reference", "https://reference.iri/speds");
    String paramsJson = mapper.writeValueAsString(new InitInParamsDto(options));

    TransportHostFactory factory = new TransportHostFactory(crypto) {
      @Override
      public NetworkHost initNetworkHost(String parameters, NetworkHostEvent hostEventConsumer) {
        return mock(NetworkHost.class);
      }
    };

    TransportHostEvent eventConsumer = mock(TransportHostEvent.class);

    ParameterException ex = assertThrows(ParameterException.class,
        () -> factory.initHost(paramsJson, eventConsumer));

    assertTrue(ex.getMessage().contains("SPEDS parameter is missing"));
  }

  @Test
  void initHost_missingReferenceException() throws Exception {
    Map<String, Object> options = new HashMap<>();
    options.put("speds.tra.version", "7.1.1");
    String paramsJson = mapper.writeValueAsString(new InitInParamsDto(options));

    TransportHostFactory factory = new TransportHostFactory(crypto) {
      @Override
      public NetworkHost initNetworkHost(String parameters, NetworkHostEvent hostEventConsumer) {
        return mock(NetworkHost.class);
      }
    };

    TransportHostEvent eventConsumer = mock(TransportHostEvent.class);

    ParameterException ex = assertThrows(ParameterException.class,
        () -> factory.initHost(paramsJson, eventConsumer));

    assertTrue(ex.getMessage().contains("SPEDS parameter is missing"));
  }


  @Test
  void initNetworkHost() throws Exception {
    TransportHostFactory factory = new TransportHostFactory(crypto);

    NetworkFactory mockedNetworkFactory = mock(NetworkFactory.class);
    NetworkHost mockedNetworkHost = mock(NetworkHost.class);

    when(mockedNetworkFactory.initHost(anyString(), any(NetworkHostEvent.class)))
        .thenReturn(mockedNetworkHost);

    Field f = TransportHostFactory.class.getDeclaredField("networkFactory");
    f.setAccessible(true);
    f.set(factory, mockedNetworkFactory);

    String parameters = buildValidParametersJson();
    NetworkHostEvent consumer = mock(NetworkHostEvent.class);

    NetworkHost host = factory.initNetworkHost(parameters, consumer);

    assertNotNull(host);
    verify(mockedNetworkFactory, times(1)).initHost(eq(parameters), eq(consumer));
  }

  private String buildValidParametersJson() throws Exception {
    Map<String, Object> options = new HashMap<>();

    options.put("speds.tra.version", "7.1.1");
    options.put("speds.tra.reference", "https://reference.iri/speds");
    options.put("speds.tra.confirm.window.minutes", 2);
    options.put("speds.tra.response.window.minutes", 2);

    InitInParamsDto params = new InitInParamsDto(options);
    return mapper.writeValueAsString(params);
  }
}
