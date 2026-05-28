package ca.griis.speds.transport.integration;

import static org.mockito.Mockito.mock;

import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.NetworkHostEvent;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.TransportHostFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import java.util.UUID;

public final class Environment {
  private static final UUID messageId = UUID.randomUUID();

  private static final String version = "version";

  private static final String reference = "reference";
  private static final Integer confirmWindowMinutes = 2;
  private static final Integer responseWindowMinutes = 2;

  private static final String parameters =
      "{"
          + "\"options\":{"
          + "\"speds.tra.version\":\"" + version + "\","
          + "\"speds.tra.reference\":\"" + reference + "\","
          + "\"speds.tra.confirm.window.minutes\":" + confirmWindowMinutes + ","
          + "\"speds.tra.response.window.minutes\":" + responseWindowMinutes
          + "}"
          + "}";
  private final CryptographyService cryptographyService;

  private final TransportHost client;

  private final TransportHost server;

  private final NetworkHost networkHost = mock(NetworkHost.class);

  private NetworkHostEvent hostEventConsumer;

  private final TraEvent clientEvent = new TraEvent();
  private final TraEvent serverEvent = new TraEvent();

  private final Map<String, String> options = Map.of("TN", getMessageId().toString());

  public Environment(String env) throws JsonProcessingException {
    var config = """
                {
        "spedsProfile": [
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
    this.cryptographyService = new DefaultCryptographyFactory().initCipherSuite(config);
    if (env.equals("env1")) {
      this.client = getTransportFactory().initHost(parameters, clientEvent);
      this.server = null;
    } else if (env.equals("env2")) {
      this.client = null;
      this.server = getTransportFactory().initHost(parameters, serverEvent);
    } else {
      this.client = getTransportFactory().initHost(parameters, clientEvent);
      this.server = getTransportFactory().initHost(parameters, serverEvent);
    }
  }

  public void cleanUp() {
    if (client != null) {
      client.close();
    }

    if (server != null) {
      server.close();
    }
  }

  public UUID getMessageId() {
    return messageId;
  }

  public String getVersion() {
    return version;
  }

  public String getReference() {
    return reference;
  }

  public CryptographyService getCryptographyService() {
    return cryptographyService;
  }

  public TransportHost getClient() {
    return client;
  }

  public TransportHost getServer() {
    return server;
  }

  public NetworkHost getNetworkHost() {
    return networkHost;
  }

  public NetworkHostEvent getHostEventConsumer() {
    return hostEventConsumer;
  }

  public String getClientResult(Long times) throws InterruptedException {
    return clientEvent.getResult(times);
  }

  public void notifyClientResult(String clientResult) {
    clientEvent.notifyIdu(clientResult);
  }

  public String getServerResult(Long times) throws InterruptedException {
    return serverEvent.getResult(times);
  }

  public void notifyServerResult(String serverResult) {
    clientEvent.notifyIdu(serverResult);
  }

  public void setHostEventConsumer(NetworkHostEvent hostEventConsumer) {
    this.hostEventConsumer = hostEventConsumer;
  }

  public void networkHostNotifyIdu(String idu) {
    hostEventConsumer.notifyIdu(idu);
  }

  public Map<String, String> getOptions() {
    return options;
  }

  private TransportHostFactory getTransportFactory() {
    return new TransportHostFactory(() -> messageId, cryptographyService) {
      @Override
      public NetworkHost initNetworkHost(String parameters, NetworkHostEvent hostEventConsumer) {
        setHostEventConsumer(hostEventConsumer);
        return networkHost;
      }
    };
  }
}
