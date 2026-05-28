package ca.griis.speds.communication.protocol;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import ca.griis.speds.link.api.exception.ProtocolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UdpHostTest {
  private ProtocolUdpHost host;

  private ObjectMapper objectMapper = new ObjectMapper();
  private ObjectMapper objectMapperSpy;

  @Mock
  private SslContext sslContextMock;

  @BeforeEach
  public void setUp() {
    objectMapperSpy = Mockito.spy(objectMapper);
    ProtocolHostEvent hostEvent = Mockito.mock(ProtocolHostEvent.class);
    host = new ProtocolUdpHost(objectMapperSpy, "localhost", 8080, hostEvent);
  }

  @AfterEach
  public void shutdown() {
    host.close();
  }

  @Test
  public void testRequest_JsonProcessingException() throws JsonProcessingException {
    ProtocolIdu pdu = new ProtocolIdu("udp://oneuri", "a message");

    doThrow(JsonProcessingException.class).when(objectMapperSpy).writeValueAsString(any());

    assertThrows(ProtocolException.class, () -> {
      host.send(pdu);
    });
  }
}
