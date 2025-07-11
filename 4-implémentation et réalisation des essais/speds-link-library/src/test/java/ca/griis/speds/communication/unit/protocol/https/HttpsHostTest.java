package ca.griis.speds.communication.unit.protocol.https;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import ca.griis.speds.communication.protocol.ProtocolIdu;
import ca.griis.speds.communication.protocol.https.HttpsHost;
import ca.griis.speds.link.api.exception.ProtocolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HttpsHostTest {

  private HttpsHost host;

  private ObjectMapper objectMapper = new ObjectMapper();
  private ObjectMapper objectMapperSpy;

  @Mock
  private SslContext sslContextMock;

  @BeforeEach
  public void setUp() {
    objectMapperSpy = Mockito.spy(objectMapper);
    host = new HttpsHost(objectMapperSpy, "0.0.0.0", 8080, sslContextMock, sslContextMock);
  }

  @AfterEach
  public void shutdown() {
    host.close();
  }

  @Test
  public void testRequest_JsonProcessingException() throws JsonProcessingException {
    ProtocolIdu pdu = new ProtocolIdu("http://oneuri", UUID.randomUUID(), "a message");

    doThrow(JsonProcessingException.class).when(objectMapperSpy).writeValueAsString(any());

    assertThrows(ProtocolException.class, () -> {
      host.request(pdu);
    });
  }

  @Test
  public void testResponse() throws JsonProcessingException {
    ProtocolIdu pdu = new ProtocolIdu("http://oneuri", UUID.randomUUID(), "a message");

    doThrow(JsonProcessingException.class).when(objectMapperSpy).writeValueAsString(any());

    assertThrows(ProtocolException.class, () -> {
      host.response(pdu);
    });
  }
}
