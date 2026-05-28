package ca.griis.speds.presentation.unit.internal.dispatcher;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.dispatcher.NotifyDispatcher;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NotifyDispatcherTest {

  private ObjectMapper mapper = new ObjectMapper();
  private ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking;
  private NotifyDispatcher dispatcher;

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private SessionHost mockSessionHost;

  private SecretKey fakeKey;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    serverTracking = new ConcurrentHashMap<>();
    fakeKey = KeyGenerator.getInstance("AES").generateKey();

    dispatcher = new NotifyDispatcher(
        cryptographyService, mockSessionHost, serverTracking);
  }

  @Test
  void testHandleTransferIndication() throws Exception {
    // Mock decryptSymmetric
    when(cryptographyService.getAlgorithm(any(), any())).thenReturn("AES");
    when(cryptographyService.decryptSymmetric(any(), any(SecretKey.class), any(byte[].class)))
        .thenReturn("decrypted content".getBytes(StandardCharsets.UTF_8));

    // Construire le PDU interne (message)
    String sdek = Base64.getEncoder().encodeToString(fakeKey.getEncoded());
    UUID trackingNumber = UUID.randomUUID();

    String innerPduJson = """
        {
          "header": {
            "msgtype": "PRE.MSG.ENV",
            "id": "11111111-1111-1111-1111-111111111111",
            "parameters": {},
            "version": {
              "number": "7.0.0",
              "reference": "presentation"
            }
          },
          "content": "base64EncryptedContent"
        }
        """;

    Map<String, Object> options = new HashMap<>();
    options.put("TN", trackingNumber.toString());

    Context23Dto context = new Context23Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        sdek,
        "transfer",
        ServicePrimitive.INDICATION,
        options);

    // message doit être le JSON du PDU sérialisé en string
    String messageJson = mapper.writeValueAsString(mapper.readTree(innerPduJson));
    InterfaceDataUnit23Dto idu = new InterfaceDataUnit23Dto(context, messageJson);

    Optional<String> result = dispatcher.handle(idu);

    assertTrue(result.isPresent());
  }

  @Test
  void testHandleNotTransferService() {
    Context23Dto context = new Context23Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "someSdek",
        "UNKNOWN_SERVICE",
        ServicePrimitive.INDICATION,
        null);

    InterfaceDataUnit23Dto idu = new InterfaceDataUnit23Dto(context, "some message");

    Optional<String> result = dispatcher.handle(idu);

    assertTrue(result.isEmpty());
  }

  @Test
  void testHandleTransferButNotIndication() {
    Context23Dto context = new Context23Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "someSdek",
        "transfer",
        ServicePrimitive.REQUEST,
        null);

    InterfaceDataUnit23Dto idu = new InterfaceDataUnit23Dto(context, "some message");

    Optional<String> result = dispatcher.handle(idu);

    assertTrue(result.isEmpty());
  }

  @Test
  void testHandleTransferConfirmPrimitive() {
    Context23Dto context = new Context23Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "someSdek",
        "transfer",
        ServicePrimitive.CONFIRM,
        null);

    InterfaceDataUnit23Dto idu = new InterfaceDataUnit23Dto(context, "some message");

    Optional<String> result = dispatcher.handle(idu);

    assertTrue(result.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testHandleJsonProcessingException() throws Exception {
    SharedObjectMapper mockedMapper = mock(SharedObjectMapper.class);
    ObjectMapper mockedObjectMapper = mock(ObjectMapper.class);
    when(mockedMapper.getMapper()).thenReturn(mockedObjectMapper);

    when(mockedObjectMapper.readValue(any(String.class), any(Class.class)))
        .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "parse error"));
    when(mockedObjectMapper.writeValueAsString(any()))
        .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "serialize error"));

    NotifyDispatcher brokenDispatcher = new NotifyDispatcher(
        cryptographyService, mockSessionHost, serverTracking);

    String sdek = Base64.getEncoder().encodeToString(fakeKey.getEncoded());
    Context23Dto context = new Context23Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        sdek,
        "transfer",
        ServicePrimitive.INDICATION,
        null);

    InterfaceDataUnit23Dto idu = new InterfaceDataUnit23Dto(context, "some message");

    Optional<String> result = brokenDispatcher.handle(idu);

    assertTrue(result.isEmpty());
  }
}
