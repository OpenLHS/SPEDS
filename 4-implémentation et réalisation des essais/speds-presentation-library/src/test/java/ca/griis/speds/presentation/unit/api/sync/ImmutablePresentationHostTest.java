package ca.griis.speds.presentation.unit.api.sync;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.encryption.AesGcmEncryptor;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import ca.griis.js2p.gen.speds.presentation.api.dto.*;
import ca.griis.speds.presentation.api.exception.CipherException;
import ca.griis.speds.presentation.api.exception.DeserializationException;
import ca.griis.speds.presentation.api.exception.InvalidPduId;
import ca.griis.speds.presentation.api.sync.ImmutablePresentationHost;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.session.api.SessionHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ImmutablePresentationHostTest {

  private ImmutablePresentationHost host;

  @Mock
  private ObjectMapper mockObjectMapper;

  @Mock
  private SessionHost mockSessionHost;

  private ObjectMapper objectMapper = new ObjectMapper();

  private String spedsVersion;
  private String spedsRef;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    spedsVersion = "1.0.0";
    spedsRef = "a ref";
  }

  @Test
  public void closeTest() throws NoSuchFieldException, IllegalAccessException {
    // given
    doNothing().when(mockSessionHost).close();
    ImmutablePresentationHost host = new ImmutablePresentationHost(mockSessionHost,
        spedsVersion, spedsRef, mockObjectMapper);
    Field field = host.getClass().getDeclaredField("clientMsgTracking");
    field.setAccessible(true);
    Set<UUID> clientTracking = (Set<UUID>) field.get(host);
    field = host.getClass().getDeclaredField("serverTracking");
    field.setAccessible(true);
    ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking =
        (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    serverTracking.put(new PresentationTracking(UUID.randomUUID()),
        new TrackingInformation(UUID.randomUUID(), null));
    clientTracking.add(UUID.randomUUID());

    // when
    host.close();

    // then
    assertTrue(clientTracking.isEmpty());
    assertTrue(serverTracking.isEmpty());
    verify(mockSessionHost, times(1)).close();
  }

  @Test
  public void request_msgFail_badArgs_Test() {
    // given
    String idu = "something that's not json";
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, objectMapper);

    // then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      host.request(idu);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Presentation unable to deserialize IDU"));
  }

  @Test
  public void request_msgFail_badPreMsgSerial_Test() throws JsonProcessingException {
    // given
    String fakeSerial = "We override this with fakeIdu";
    InterfaceDataUnit12Dto fakeIdu = new InterfaceDataUnit12Dto(
        new ContextDto(
            "pga",
            "source",
            "dest",
            UUID.randomUUID(),
            Boolean.FALSE),
        "mesasge");
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit12Dto.class))
        .thenReturn(fakeIdu);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenThrow(new JsonProcessingException("Serialization failed") {});
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      host.request(fakeSerial);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Serialization of preMsgEnv failed unexpectedly"));
  }

  @Test
  public void request_msgFail_basIduSerial_Test() throws JsonProcessingException {
    // given
    String fakeSerial = "We override this with fakeIdu";
    InterfaceDataUnit12Dto fakeIdu = new InterfaceDataUnit12Dto(
        new ContextDto(
            "pga",
            "source",
            "dest",
            UUID.randomUUID(),
            Boolean.FALSE),
        "mesasge");
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit12Dto.class))
        .thenReturn(fakeIdu);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenReturn("ok - Any value is ok here")
        .thenThrow(new JsonProcessingException("Serialization failed") {});
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      host.request(fakeSerial);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage()
        .contains("Unexpected JSON serialization failure for InterfaceDataUnit23Dto"));
  }

  @Test
  public void request_msgsent_Test() throws JsonProcessingException {
    // given
    InterfaceDataUnit12Dto fakeIdu = new InterfaceDataUnit12Dto(
        new ContextDto(
            "pga",
            "source",
            "dest",
            UUID.randomUUID(),
            Boolean.FALSE),
        "mesasge");

    String idu = "any";
    when(mockObjectMapper.readValue(idu, InterfaceDataUnit12Dto.class))
        .thenReturn(fakeIdu);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenReturn("ok - Any value is ok here")
        .thenReturn("ok - Any value is ok here, but it needs to be validated");
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // when
    host.request(idu);

    // then
    verify(mockSessionHost, times(1))
        .request("ok - Any value is ok here, but it needs to be validated");
  }

  @Test
  public void confirm_msgFail_badArgs_Test() {
    // given
    String idu = "something that's not json";
    when(mockSessionHost.confirm()).thenReturn(idu);
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, objectMapper);

    // then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      host.confirm();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Presentation unable to deserialize IDU"));
  }

  @Test
  public void confirm_msgFail_deserialize_Test() throws JsonProcessingException {
    // given
    String fakeSerial = "We override this with fakeIdu";
    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            "not needed",
            UUID.randomUUID(),
            Boolean.FALSE),
        "mesasge");

    when(mockSessionHost.confirm()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);
    when(mockObjectMapper.readValue("mesasge", ProtocolDataUnit2PREDto.class))
        .thenThrow(new JsonProcessingException("Deserialization error. Unable to deserialize") {});
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // then
    DeserializationException exception = assertThrows(DeserializationException.class, () -> {
      host.confirm();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Deserialization error. Unable to deserialize"));
  }

  @Test
  public void confirm_msgFail_notRec_Test() throws JsonProcessingException {
    // given
    String fakeSerial = "We override this with fakeIdu";
    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            "not needed",
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.confirm()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    // MD - Notice the type is Env
    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_ENV,
            UUID.randomUUID(),
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), "content");
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // then
    DeserializationException exception = assertThrows(DeserializationException.class, () -> {
      host.confirm();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage()
        .contains("Deserialization error. Type of SDU must be PRE_MSG_REC, is:"));
  }

  @Test
  public void confirm_msgFail_noIdMatch_Test() throws JsonProcessingException {
    // given
    String fakeSerial = "We override this with fakeIdu";
    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            "not needed",
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.confirm()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_REC,
            UUID.randomUUID(),
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), "content");
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // then
    InvalidPduId exception = assertThrows(InvalidPduId.class, () -> {
      host.confirm();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("The response SDU identifier has no match"));
  }

  @Test
  public void confirm_illegalCipher_Test() throws JsonProcessingException,
      NoSuchFieldException, IllegalAccessException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    byte[] keyBytes = secretKey.getEncoded();
    String serialKey = Base64.getEncoder().encodeToString(keyBytes);

    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            serialKey,
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.confirm()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    UUID presMsgId = UUID.randomUUID();
    String base64Content = Base64.getEncoder().encodeToString(("a long enough byte array to " +
        "get over bufferunderflow").getBytes(StandardCharsets.UTF_8));
    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_REC,
            presMsgId,
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), base64Content);
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    Field field = host.getClass().getDeclaredField("clientMsgTracking");
    field.setAccessible(true);
    field.set(host, new HashSet<>(Set.of(presMsgId)));

    // then
    CipherException exception = assertThrows(CipherException.class, () -> {
      host.confirm();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Unable to decipher the SDU"));
    assertEquals(SecurityException.class, exception.getCause().getClass());
  }

  @Test
  public void confirm_badIduSerial_Test() throws JsonProcessingException,
      NoSuchFieldException, IllegalAccessException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    // Générer une clé AES 256 bits
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    AesGcmEncryptor encryptor = new AesGcmEncryptor(secretKey);
    byte[] encryptedMessageBytes = encryptor.encrypt("content".getBytes(StandardCharsets.UTF_8));
    String cipheredSdu = Base64.getEncoder().encodeToString(encryptedMessageBytes);
    String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            encodedKey,
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.confirm()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    UUID presMsgId = UUID.randomUUID();
    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_REC,
            presMsgId,
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), cipheredSdu);
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenThrow(new JsonProcessingException("Trigger") {});

    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    Field field = host.getClass().getDeclaredField("clientMsgTracking");
    field.setAccessible(true);
    field.set(host, new HashSet<>(Set.of(presMsgId)));

    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      host.confirm();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage()
        .contains("Unexpected JSON serialization failure for InterfaceDataUnit12Dto"));
  }

  @Test
  public void confirm_msgSent_Test() throws JsonProcessingException,
      NoSuchFieldException, IllegalAccessException, CipherException, InvalidPduId,
      DeserializationException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    // Générer une clé AES 256 bits
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    AesGcmEncryptor encryptor = new AesGcmEncryptor(secretKey);
    byte[] encryptedMessageBytes = encryptor.encrypt("content".getBytes(StandardCharsets.UTF_8));
    String cipheredSdu = Base64.getEncoder().encodeToString(encryptedMessageBytes);
    String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            encodedKey,
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.confirm()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    UUID presMsgId = UUID.randomUUID();
    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_REC,
            presMsgId,
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), cipheredSdu);
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenReturn("ok");

    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    Field field = host.getClass().getDeclaredField("clientMsgTracking");
    field.setAccessible(true);
    field.set(host, new HashSet<>(Set.of(presMsgId)));

    // When
    String result = host.confirm();
    assertTrue(result.contains("ok"));
  }

  @Test
  public void indicate_msgFail_badArgs_Test() {
    // given
    String idu = "something that's not json";
    when(mockSessionHost.indicateDataExchange()).thenReturn(idu);
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, objectMapper);

    // then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      host.indication();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Presentation unable to deserialize IDU"));
  }

  @Test
  public void indicate_msgFail_badPreMsgSerial_Test() throws JsonProcessingException {
    // given
    String fakeSerial = "We override this with fakeIdu";
    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            "not needed",
            UUID.randomUUID(),
            Boolean.FALSE),
        "mesasge");

    when(mockSessionHost.indicateDataExchange()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);
    when(mockObjectMapper.readValue("mesasge", ProtocolDataUnit2PREDto.class))
        .thenThrow(new JsonProcessingException("Deserialization error. Unable to deserialize") {});
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // then
    DeserializationException exception = assertThrows(DeserializationException.class, () -> {
      host.indication();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Deserialization error. Unable to deserialize"));
  }

  @Test
  public void indication_msgFail_notEnv_Test()
      throws JsonProcessingException, NoSuchAlgorithmException {
    // given
    String fakeSerial = "We override this with fakeIdu";
    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            "not needed",
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.indicateDataExchange()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    // MD - Notice the type is Env
    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_REC,
            UUID.randomUUID(),
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), "content");
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // then
    DeserializationException exception = assertThrows(DeserializationException.class, () -> {
      host.indication();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage()
        .contains("Type of outboundSdu must be: " + HeaderDto.Msgtype.PRE_MSG_ENV));
  }

  @Test
  public void indicate_illegalCipher_Test()
      throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
    // given
    String fakeSerial = "We override this with fakeIdu";
    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            "not a key",
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.indicateDataExchange()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    UUID presMsgId = UUID.randomUUID();
    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_ENV,
            presMsgId,
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), "content");
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    Field field = host.getClass().getDeclaredField("clientMsgTracking");
    field.setAccessible(true);
    field.set(host, new HashSet<>(Set.of(presMsgId)));

    // then
    CipherException exception = assertThrows(CipherException.class, () -> {
      host.indication();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Unable to cipher the SDU"));
    assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
  }

  @Test
  public void indication_badIduSerial_Test() throws JsonProcessingException,
      NoSuchFieldException, IllegalAccessException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    // Générer une clé AES 256 bits
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    AesGcmEncryptor encryptor = new AesGcmEncryptor(secretKey);
    byte[] encryptedMessageBytes = encryptor.encrypt("content".getBytes(StandardCharsets.UTF_8));
    String cipheredSdu = Base64.getEncoder().encodeToString(encryptedMessageBytes);
    String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            encodedKey,
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.indicateDataExchange()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    UUID presMsgId = UUID.randomUUID();
    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_ENV,
            presMsgId,
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), cipheredSdu);
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenThrow(new JsonProcessingException("Trigger") {});

    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    Field field = host.getClass().getDeclaredField("clientMsgTracking");
    field.setAccessible(true);
    field.set(host, new HashSet<>(Set.of(presMsgId)));

    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      host.indication();
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage()
        .contains("Unexpected JSON serialization failure for InterfaceDataUnit12Dto"));
  }

  @Test
  public void indication_msgSent_Test() throws JsonProcessingException,
      NoSuchFieldException, IllegalAccessException, CipherException, InvalidPduId,
      DeserializationException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    // Générer une clé AES 256 bits
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    AesGcmEncryptor encryptor = new AesGcmEncryptor(secretKey);
    byte[] encryptedMessageBytes = encryptor.encrypt("content".getBytes(StandardCharsets.UTF_8));
    String cipheredSdu = Base64.getEncoder().encodeToString(encryptedMessageBytes);
    String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

    InterfaceDataUnit23Dto fakeIdu = new InterfaceDataUnit23Dto(
        new ContextDto__1(
            "pga",
            "source",
            "dest",
            encodedKey,
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");

    when(mockSessionHost.indicateDataExchange()).thenReturn(fakeSerial);
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit23Dto.class))
        .thenReturn(fakeIdu);

    UUID presMsgId = UUID.randomUUID();
    ProtocolDataUnit2PREDto fakeMsg =
        new ProtocolDataUnit2PREDto(new HeaderDto(HeaderDto.Msgtype.PRE_MSG_ENV,
            presMsgId,
            Boolean.FALSE,
            new SPEDSDto(spedsVersion, spedsRef)), cipheredSdu);
    when(mockObjectMapper.readValue("fakeMsg", ProtocolDataUnit2PREDto.class))
        .thenReturn(fakeMsg);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenReturn("ok");

    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    Field field = host.getClass().getDeclaredField("clientMsgTracking");
    field.setAccessible(true);
    field.set(host, new HashSet<>(Set.of(presMsgId)));

    // When
    String result = host.indication();

    assertTrue(result.contains("ok"));
  }

  @Test
  public void response_msgFail_badArgs_Test() {
    // given
    String idu = "something that's not json";
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, objectMapper);

    // then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      host.response(idu);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Presentation unable to deserialize IDU"));
  }

  @Test
  public void response_msgFail_badCipher_Test()
      throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    // Générer une clé AES 256 bits
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.DES, 56);

    InterfaceDataUnit12Dto fakeIdu = new InterfaceDataUnit12Dto(
        new ContextDto(
            "pga",
            "source",
            "dest",
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit12Dto.class))
        .thenReturn(fakeIdu);
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking =
        new ConcurrentHashMap<>();
    serverTracking.put(new PresentationTracking(fakeIdu.getContext().getTrackingNumber()),
        new TrackingInformation(UUID.randomUUID(), secretKey));
    Field field = host.getClass().getDeclaredField("serverTracking");
    field.setAccessible(true);
    ConcurrentHashMap<PresentationTracking, TrackingInformation> value =
        (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    assert value.isEmpty();
    field.set(host, serverTracking);

    // then
    CipherException exception = assertThrows(CipherException.class, () -> {
      host.response(fakeSerial);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Unable to cipher the SDU"));
    value = (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    assertTrue(value.isEmpty());
  }

  @Test
  public void response_msgFail_preSerial_Test()
      throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    // Générer une clé AES 256 bits
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);

    InterfaceDataUnit12Dto fakeIdu = new InterfaceDataUnit12Dto(
        new ContextDto(
            "pga",
            "source",
            "dest",
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit12Dto.class))
        .thenReturn(fakeIdu);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenThrow(new JsonProcessingException("fake") {});
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking =
        new ConcurrentHashMap<>();
    serverTracking.put(new PresentationTracking(fakeIdu.getContext().getTrackingNumber()),
        new TrackingInformation(UUID.randomUUID(), secretKey));
    Field field = host.getClass().getDeclaredField("serverTracking");
    field.setAccessible(true);
    ConcurrentHashMap<PresentationTracking, TrackingInformation> value =
        (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    assert value.isEmpty();
    field.set(host, serverTracking);

    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      host.response(fakeSerial);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Serialization of preMsgEnv failed unexpectedly"));
    value = (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    assertTrue(value.isEmpty());
  }

  @Test
  public void response_msgFail_sduSerial_Test()
      throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    // Générer une clé AES 256 bits
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);

    InterfaceDataUnit12Dto fakeIdu = new InterfaceDataUnit12Dto(
        new ContextDto(
            "pga",
            "source",
            "dest",
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit12Dto.class))
        .thenReturn(fakeIdu);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenReturn("ok")
        .thenThrow(new JsonProcessingException("fake") {});
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking =
        new ConcurrentHashMap<>();
    serverTracking.put(new PresentationTracking(fakeIdu.getContext().getTrackingNumber()),
        new TrackingInformation(UUID.randomUUID(), secretKey));
    Field field = host.getClass().getDeclaredField("serverTracking");
    field.setAccessible(true);
    ConcurrentHashMap<PresentationTracking, TrackingInformation> value =
        (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    assert value.isEmpty();
    field.set(host, serverTracking);

    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      host.response(fakeSerial);
    });

    assertNotNull(exception);
    assertTrue(exception.getMessage()
        .contains("Unexpected JSON serialization failure for InterfaceDataUnit23Dto"));
    value = (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    assertTrue(value.isEmpty());
  }

  @Test
  public void response_msgSent_Test() throws JsonProcessingException, NoSuchFieldException,
      IllegalAccessException, CipherException {
    // given
    String fakeSerial = "We override this with fakeIdu";

    // Générer une clé AES 256 bits
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);

    InterfaceDataUnit12Dto fakeIdu = new InterfaceDataUnit12Dto(
        new ContextDto(
            "pga",
            "source",
            "dest",
            UUID.randomUUID(),
            Boolean.FALSE),
        "fakeMsg");
    when(mockObjectMapper.readValue(fakeSerial, InterfaceDataUnit12Dto.class))
        .thenReturn(fakeIdu);
    when(mockObjectMapper.writeValueAsString(any()))
        .thenReturn("ok")
        .thenReturn("done");
    host = new ImmutablePresentationHost(mockSessionHost, spedsVersion, spedsRef, mockObjectMapper);

    // Just affect the private field
    ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking =
        new ConcurrentHashMap<>();
    serverTracking.put(new PresentationTracking(fakeIdu.getContext().getTrackingNumber()),
        new TrackingInformation(UUID.randomUUID(), secretKey));
    Field field = host.getClass().getDeclaredField("serverTracking");
    field.setAccessible(true);
    ConcurrentHashMap<PresentationTracking, TrackingInformation> value =
        (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    assert value.isEmpty();
    field.set(host, serverTracking);

    // then
    host.response(fakeSerial);

    value = (ConcurrentHashMap<PresentationTracking, TrackingInformation>) field.get(host);
    assertTrue(value.isEmpty());
    verify(mockSessionHost, times(1))
        .response("done");
  }
}
