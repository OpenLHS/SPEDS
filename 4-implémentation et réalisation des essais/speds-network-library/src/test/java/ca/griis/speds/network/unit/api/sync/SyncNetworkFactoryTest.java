package ca.griis.speds.network.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.link.api.sync.ImmutableDataLinkFactory;
import ca.griis.speds.network.api.NetworkFactory;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.SyncNetworkFactory;
import ca.griis.speds.network.service.exception.ParameterException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SyncNetworkFactoryTest {
  @Mock
  private DataLinkHost dataLinkHost;

  private NetworkFactory networkFactory;

  @Mock
  private ImmutableDataLinkFactory dataLinkFactory;

  @Captor
  ArgumentCaptor<String> messageCaptor;

  @Test
  public void noArgConstructorTest() throws Exception {
    networkFactory = new SyncNetworkFactory() {
      @Override
      public DataLinkHost initDataLinkHost(String parameters) {
        return dataLinkHost;
      }
    };
    assertNotNull(networkFactory);
  }

  @Test
  public void initHostSuccess() throws Exception {
    networkFactory = new SyncNetworkFactory(() -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f") {
      @Override
      public DataLinkHost initDataLinkHost(String parameters) {
        return dataLinkHost;
      }
    };
    InputStream inputStream =
        this.getClass().getClassLoader().getResourceAsStream("initClient.json");
    final String parameters = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    assertNotNull(networkFactory.initHost(parameters));
  }

  @Test
  public void initHostBadJsonParameters() throws Exception {
    networkFactory = new SyncNetworkFactory(() -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f") {
      @Override
      public DataLinkHost initDataLinkHost(String parameters) {
        return dataLinkHost;
      }
    };
    assertThrows(ParameterException.class, () -> networkFactory.initHost("wrongJson"));
  }

  @Test
  public void initHostRandomUUID() throws Exception {
    networkFactory = new SyncNetworkFactory() {
      @Override
      public DataLinkHost initDataLinkHost(String parameters) {
        return dataLinkHost;
      }
    };
    InputStream inputStream =
        this.getClass().getClassLoader().getResourceAsStream("initClient.json");
    final String parameters = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    final NetworkHost networkHost = networkFactory.initHost(parameters);
    final String idu45 = """
        {
          "context": {
            "source_iri": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;
    networkHost.request(idu45);
    networkHost.request(idu45);
    verify(dataLinkHost, times(2)).request(messageCaptor.capture());
    List<String> messages = messageCaptor.getAllValues();
    assertNotEquals(messages.get(0), messages.get(1));
  }

  @Test
  void testInitSessionHostCalled() throws NoSuchFieldException, IllegalAccessException {
    // Given
    String parameters = "someParameters";

    networkFactory = new SyncNetworkFactory();

    // Override exit
    when(dataLinkFactory.init(any())).thenReturn(dataLinkHost);
    Field factoryField = networkFactory.getClass().getDeclaredField("factory");
    factoryField.setAccessible(true);
    factoryField.set(networkFactory, dataLinkFactory);

    // When
    DataLinkHost actual = networkFactory.initDataLinkHost(parameters);

    // Then
    assertNotNull(actual);
  }
}
