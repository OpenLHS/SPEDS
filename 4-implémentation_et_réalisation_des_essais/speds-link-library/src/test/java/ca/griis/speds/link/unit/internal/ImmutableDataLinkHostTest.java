package ca.griis.speds.link.unit.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.speds.communication.protocol.ProtocolHost;
import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.link.internal.ImmutableDataLinkHost;
import ca.griis.speds.link.internal.event.ProtocolEventhandler;
import ca.griis.speds.link.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImmutableDataLinkHostTest implements HostEvent {
  private ImmutableDataLinkHost dataLinkHost;
  private ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
  private ObjectMapper objectMapperSpy;
  private LinkedBlockingQueue<String> idus = new LinkedBlockingQueue<>();
  private ProtocolEventhandler handler;

  @Mock
  private ProtocolHost mockHost;

  @BeforeEach
  void setUp() {
    objectMapperSpy = Mockito.spy(objectMapper);
    handler = new ProtocolEventhandler();
    dataLinkHost = new ImmutableDataLinkHost(objectMapperSpy, mockHost, this);

    handler.register(dataLinkHost);
  }

  @Test
  public void invalidIduTest() throws JsonMappingException, JsonProcessingException,
      InterruptedException, ExecutionException {
    // Given
    String idu = "";
    // When
    String sdu = dataLinkHost.submitIdu(idu).get().get();

    InterfaceDataUnit56Dto iduDto = objectMapper.readValue(sdu, InterfaceDataUnit56Dto.class);

    // Then
    assertTrue(iduDto.getMessage().contains("FAILED"));
  }

  @Test
  public void invalidRequestTest() throws JsonMappingException, JsonProcessingException,
      InterruptedException, ExecutionException {
    // Given
    String idu = """
        {
        "context": {
        "destination_iri": "https://localhost:8081",
        "service" : "some service",
        "service_primitive" : "request",
        "options" : false
        },
        "message": "Allo!"
        }
        """;
    // When
    String sdu = dataLinkHost.submitIdu(idu).get().get();

    InterfaceDataUnit56Dto iduDto = objectMapper.readValue(sdu, InterfaceDataUnit56Dto.class);

    // Then
    assertTrue(iduDto.getMessage().contains("FAILED"));
  }

  @Test
  public void invalidResponseTest() throws JsonMappingException, JsonProcessingException,
      InterruptedException, ExecutionException {
    // Given
    String idu = """
        {
        "context": {
        "destination_iri": "https://localhost:8081",
        "service" : "some service",
        "service_primitive" : "response",
        "options" : false
        },
        "message": "Allo!"
        }
        """;
    // When
    String sdu = dataLinkHost.submitIdu(idu).get().get();

    InterfaceDataUnit56Dto iduDto = objectMapper.readValue(sdu, InterfaceDataUnit56Dto.class);

    // Then
    assertTrue(iduDto.getMessage().contains("FAILED"));
  }

  @Test
  public void validRequestTest() throws JsonMappingException, JsonProcessingException,
      InterruptedException, ExecutionException {
    // Given
    String idu = """
        {
        "context": {
        "destination_iri": "https://localhost:8081",
        "service" : "transfer",
        "service_primitive" : "request",
        "options" : false
        },
        "message": "Allo!"
        }
        """;
    doNothing().when(mockHost).send(any());

    // When
    String sdu = dataLinkHost.submitIdu(idu).get().get();

    InterfaceDataUnit56Dto iduDto = objectMapper.readValue(sdu, InterfaceDataUnit56Dto.class);

    // Then
    assertEquals("SUCCEED", iduDto.getMessage());
  }

  @Test
  public void validResponseTest() throws InterruptedException, ExecutionException {
    // Given
    String idu = """
        {
        "context": {
        "destination_iri": "https://localhost:8081",
        "service" : "transfer",
        "service_primitive" : "response",
        "options" : false
        },
        "message": "Allo!"
        }
        """;
    // When
    var result = dataLinkHost.submitIdu(idu).get();

    // Then
    assertEquals(result, Optional.empty());
  }

  @Test
  public void validIndicationTest() throws JsonProcessingException, InterruptedException {
    // Given
    ProtocolIdu receivedIdu = new ProtocolIdu("https://localhost:8081", "Allo!");

    handler.notifyIdu(receivedIdu);

    // When
    String iduJson = idus.poll(2, TimeUnit.SECONDS);

    // Then
    InterfaceDataUnit56Dto idu = SharedObjectMapper.getInstance().getMapper().readValue(iduJson,
        InterfaceDataUnit56Dto.class);
    assertEquals(receivedIdu.destinationUri(), idu.getContext().getDestinationIri());
    assertEquals(receivedIdu.sdu(), idu.getMessage());
  }

  @Override
  public void notifyIdu(String event) {
    idus.add(event);
  }

  @Override
  public void notifyException(Exception exception) {}
}
