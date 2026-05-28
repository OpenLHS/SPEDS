package ca.griis.speds.link.integration.conception;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class TestSuite {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Cases cases = new Cases();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
  }

  @Test
  public void e01_test() throws Exception {
    try (
        ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0)) {
      freePorts(originSocket, targetSocket);

      Environment environment = createEnv(originSocket, targetSocket);

      cases.ct_001(environment);
      cases.ct_002(environment);
      cases.ct_003(environment);
    }
  }

  private Environment createEnv(ServerSocket originSocket, ServerSocket targetSocket)
      throws Exception {
    final Integer originPort = originSocket.getLocalPort();
    final Integer targetPort = targetSocket.getLocalPort();

    Environment environment = new Environment(originPort, "localhost",
        targetPort, "localhost", this.objectMapper);

    return environment;
  }

  private void freePorts(ServerSocket originSocket, ServerSocket targetSocket)
      throws IOException {
    originSocket.setReuseAddress(true);
    originSocket.close();
    targetSocket.setReuseAddress(true);
    targetSocket.close();
  }
}
