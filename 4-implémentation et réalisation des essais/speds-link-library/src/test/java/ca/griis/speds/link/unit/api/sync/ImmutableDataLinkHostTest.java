package ca.griis.speds.link.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.speds.communication.protocol.ProtocolIdu;
import ca.griis.speds.communication.protocol.https.HttpsHost;
import ca.griis.speds.link.api.exception.ProtocolException;
import ca.griis.speds.link.api.exception.SerializationException;
import ca.griis.speds.link.api.exception.VerificationException;
import ca.griis.speds.link.api.sync.ImmutableDataLinkHost;
import ca.griis.speds.link.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImmutableDataLinkHostTest {

  private ImmutableDataLinkHost dataLinkHost;

  private ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
  private ObjectMapper objectMapperSpy;

  @Mock
  HttpsHost mockHost;

  @BeforeEach
  void setUp() {
    objectMapperSpy = Mockito.spy(objectMapper);
    dataLinkHost = new ImmutableDataLinkHost(objectMapper, mockHost);
  }

  @Test
  public void validRequestTest() {
    // Given
    String idu = """
        {
            "context": {
               "destination_iri": "https://localhost:8081",
               "tracking_number" : "6d10e181-c637-4cba-ad26-1f81b52ce935",
               "options" : false
             },
            "message": "Allo!"
        }
        """;
    doNothing().when(mockHost).request(any());


    // When
    String sdu = dataLinkHost.request(idu);


    // Then
    assertEquals("Allo!", sdu);
  }


  @Test
  public void requestWithInvalidIduTest() {
    // Given
    String idu = """
        {
            "test": "wrongAttribute",
            "message": "Allo!"
        }
        """;

    // When
    SerializationException exception = assertThrows(SerializationException.class, () -> {
      dataLinkHost.request(idu);
    });

    // Then
    assertEquals("Invalid idu format !", exception.getMessage());
  }


  @Test
  public void requestWithEmptyIriTest() {
    // Given
    String idu = """
        {
            "context": {
                "destination_iri": "",
                "tracking_number" : "6d10e181-c637-4cba-ad26-1f81b52ce935",
                "options" : false
            },
            "message": "Allo!"
        }
        """;

    // When
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      dataLinkHost.request(idu);
    });

    // Then
    assertEquals("Invalid ici or IRI value !", exception.getMessage());
  }

  @Test
  public void requestWithInvalidIriTest() {
    // Given
    String idu = """
        {
            "context": {
                "destination_iri": "<<Invalid-iri>>",
                "tracking_number" : "6d10e181-c637-4cba-ad26-1f81b52ce935",
                "options" : false
            },
            "message": "Allo!"
        }
        """;

    // When
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      dataLinkHost.request(idu);
    });

    // Then
    assertEquals("Invalid ici or IRI value !", exception.getMessage());
  }


  @Test
  public void requestWithInvalidIciTest() {
    // Given
    String idu = """
        {
            "message": "Allo!"
        }
        """;

    // When
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      dataLinkHost.request(idu);
    });

    // Then
    assertEquals("Invalid ici or IRI value !", exception.getMessage());
  }


  @Test
  public void requestWithEmptySduTest() {
    // Given
    String idu = """
        {
            "context": {
               "destination_iri": "https://localhost:8081",
               "tracking_number" : "6d10e181-c637-4cba-ad26-1f81b52ce935",
               "options" : false
             },
            "message": ""
        }
        """;

    // When
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      dataLinkHost.request(idu);
    });

    // Then
    assertEquals("Invalid sdu value !", exception.getMessage());
  }


  @Test
  public void invalidTrackingNumberResponseTest()
      throws SecurityException, IllegalArgumentException {
    // Given
    String idu = """
        {
            "context": {
               "destination_iri": "https://localhost:8080",
               "tracking_number" : "6d10e181-c637-4cba-ad26-1f81b52ce935",
               "options" : false
             },
            "message": "Bye!"
        }
        """;

    // When
    assertThrows(ProtocolException.class, () -> {
      dataLinkHost.response(idu);
    });

    // Then
  }


  @Test
  public void validResponseTest() throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {
    // Given
    UUID trackingNumber = UUID.fromString("6d10e181-c637-4cba-ad26-1f81b52ce935");
    UUID messageId = UUID.randomUUID();
    Field field = dataLinkHost.getServer().getClass().getDeclaredField("trackingNumbers");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    ConcurrentHashMap<UUID, UUID> trackingNumbers =
        (ConcurrentHashMap<UUID, UUID>) field.get(dataLinkHost.getServer());
    trackingNumbers.put(trackingNumber, messageId);

    String idu = """
        {
            "context": {
               "destination_iri": "https://localhost:8080",
               "tracking_number" : "6d10e181-c637-4cba-ad26-1f81b52ce935",
               "options" : false
             },
            "message": "Bye!"
        }
        """;
    doNothing().when(mockHost).response(any());


    // When
    String sdu = dataLinkHost.response(idu);


    // Then
    assertEquals("Bye!", sdu);
  }


  @Test
  public void responseWithInvalidIduTest() {
    // Given
    String idu = """
        {
            "test": "wrongAttribute",
            "message": "Allo!"
        }
        """;

    // When
    SerializationException exception = assertThrows(SerializationException.class, () -> {
      dataLinkHost.response(idu);
    });

    // Then
    assertEquals("Invalid idu format !", exception.getMessage());
  }


  @Test
  public void responseWithInvalidIciTest() {
    // Given
    String idu = """
        {
            "message": "Allo!"
        }
        """;

    // When
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      dataLinkHost.response(idu);
    });

    // Then
    assertEquals("Invalid ici or IRI value !", exception.getMessage());
  }


  @Test
  public void responseWithEmptySduTest() {
    // Given
    String idu = """
        {
            "context": {
               "destination_iri": "https://localhost:8081",
               "tracking_number" : "6d10e181-c637-4cba-ad26-1f81b52ce935",
               "options" : false
             },
            "message": ""
        }
        """;

    // When
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      dataLinkHost.response(idu);
    });

    // Then
    assertEquals("Invalid sdu value !", exception.getMessage());
  }


  @Test
  public void validIndicationTest() throws JsonProcessingException {
    // Given
    ProtocolIdu receivedIdu = new ProtocolIdu("https://localhost:8081", UUID.randomUUID(), "Allo!");
    doReturn(receivedIdu).when(mockHost).indicate();

    // When
    String iduJson = dataLinkHost.indication();

    // Then
    InterfaceDataUnit56Dto idu = SharedObjectMapper.getInstance().getMapper().readValue(iduJson,
        InterfaceDataUnit56Dto.class);
    assertEquals(receivedIdu.destinationUri(), idu.getContext().getDestinationIri());
    assertEquals(receivedIdu.sdu(), idu.getMessage());
  }

  @Test
  public void invalidIndicationTest() throws JsonProcessingException {
    dataLinkHost = new ImmutableDataLinkHost(objectMapperSpy, mockHost);
    // Given
    ProtocolIdu receivedIdu = new ProtocolIdu("https://localhost:8081", UUID.randomUUID(), "Allo!");
    doReturn(receivedIdu).when(mockHost).indicate();

    doThrow(JsonProcessingException.class).when(objectMapperSpy).writeValueAsString(any());

    assertThrows(SerializationException.class, () -> {
      dataLinkHost.indication();
    });
  }

  @Test
  public void invalidTrackingNumberConfirmationTest()
      throws SecurityException, IllegalArgumentException {
    // Given
    UUID messageId = UUID.randomUUID();

    ProtocolIdu receivedIdu = new ProtocolIdu("https://localhost:8080", messageId, "Bye!");
    doReturn(receivedIdu).when(mockHost).confirm();

    // When
    assertThrows(ProtocolException.class, () -> {
      dataLinkHost.confirm();
    });

    // Then
  }


  @Test
  public void validConfirmationTest() throws JsonProcessingException, NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException {
    // Given
    UUID trackingNumber = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();
    Field field = dataLinkHost.getClient().getClass().getDeclaredField("trackingNumbers");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    ConcurrentHashMap<UUID, UUID> trackingNumbers =
        (ConcurrentHashMap<UUID, UUID>) field.get(dataLinkHost.getClient());
    trackingNumbers.put(messageId, trackingNumber);

    ProtocolIdu receivedIdu = new ProtocolIdu("https://localhost:8080", messageId, "Bye!");
    doReturn(receivedIdu).when(mockHost).confirm();

    // When

    String iduJson = dataLinkHost.confirm();

    // Then
    InterfaceDataUnit56Dto idu = SharedObjectMapper.getInstance().getMapper().readValue(iduJson,
        InterfaceDataUnit56Dto.class);
    assertEquals(receivedIdu.destinationUri(), idu.getContext().getDestinationIri());
    assertEquals(receivedIdu.sdu(), idu.getMessage());
  }

  @Test
  public void invalidConfirmationTest() throws JsonProcessingException, NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException {
    dataLinkHost = new ImmutableDataLinkHost(objectMapperSpy, mockHost);

    // Given
    UUID trackingNumber = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();
    Field field = dataLinkHost.getClient().getClass().getDeclaredField("trackingNumbers");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    ConcurrentHashMap<UUID, UUID> trackingNumbers =
        (ConcurrentHashMap<UUID, UUID>) field.get(dataLinkHost.getClient());
    trackingNumbers.put(messageId, trackingNumber);

    ProtocolIdu receivedIdu = new ProtocolIdu("https://localhost:8080", messageId, "Bye!");
    doReturn(receivedIdu).when(mockHost).confirm();

    doThrow(JsonProcessingException.class).when(objectMapperSpy).writeValueAsString(any());

    assertThrows(SerializationException.class, () -> {
      dataLinkHost.confirm();
    });
  }
}
