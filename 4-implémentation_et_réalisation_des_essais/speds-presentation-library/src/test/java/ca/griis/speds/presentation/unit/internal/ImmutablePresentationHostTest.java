package ca.griis.speds.presentation.unit.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.presentation.api.dto.VersionDto;
import ca.griis.speds.presentation.api.PresentationHostEvent;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.ImmutablePresentationHost;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ImmutablePresentationHostTest {

  private ImmutablePresentationHost host;

  @Mock
  private SessionHost mockSessionHost;

  private VersionDto version;

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private PresentationHostEvent presentationHostConsumer;

  private Cache<PresentationTracking, TrackingInformation> serverTracking;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    version = new VersionDto("7.0.0", "a ref");
    serverTracking = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(100_000)
        .<PresentationTracking, TrackingInformation>build();
  }

  @Test
  public void closeTest() throws NoSuchFieldException, IllegalAccessException {
    // given
    doNothing().when(mockSessionHost).close();
    ImmutablePresentationHost host = new ImmutablePresentationHost(mockSessionHost,
        version, cryptographyService, presentationHostConsumer, serverTracking);
    Field field = host.getClass().getDeclaredField("serverTracking");
    field.setAccessible(true);

    @SuppressWarnings("unchecked")
    Cache<PresentationTracking, TrackingInformation> serverTracking =
        (Cache<PresentationTracking, TrackingInformation>) field.get(host);
    serverTracking.put(new PresentationTracking(UUID.randomUUID()),
        new TrackingInformation(UUID.randomUUID(), null));

    // when
    host.close();

    // then
    assertTrue(serverTracking.asMap().isEmpty());
    verify(mockSessionHost, times(1)).close();
  }

  @Test
  public void notifyIdu_msgFail_badArgs_isEmpty_Test() throws Exception {
    // given
    String idu = "something that's not json";
    host = new ImmutablePresentationHost(mockSessionHost,
        version, cryptographyService, presentationHostConsumer, serverTracking);

    // then
    host.notifyIdu(idu);
  }

  @Test
  public void notifyIdu_Test() throws Exception {
    // given
    String idu = """
        {
          "context": {
            "PGA": "550e8400-e29b-41d4-a716-446655440000",
            "source_code": "source",
            "destination_code": "destination",
            "SDEK": "YWVzLTI1Ni1nY20tZW5jcnlwdGVkLWtleQ==",
            "service": "transfer",
            "service_primitive": "indication",
            "options": {
              "TM": "someUUID"
            }
          },
          "message": "UFBEVSBlbmNvZGVkIG1lc3NhZ2UgY29udGVudA=="
        }
        """;
    host = new ImmutablePresentationHost(mockSessionHost,
        version, cryptographyService, presentationHostConsumer, serverTracking);

    // then
    host.notifyIdu(idu);
  }

  @Test
  public void notifyIdu_primitive_error_Test() throws Exception {
    // given
    String idu = """
        {
          "context": {
            "PGA": "550e8400-e29b-41d4-a716-446655440000",
            "source_code": "source",
            "destination_code": "destination",
            "SDEK": "YWVzLTI1Ni1nY20tZW5jcnlwdGVkLWtleQ==",
            "service": "transfer",
            "service_primitive": "error",
            "options": {
              "TM": "someUUID"
            }
          },
          "message": "UFBEVSBlbmNvZGVkIG1lc3NhZ2UgY29udGVudA=="
        }
        """;
    host = new ImmutablePresentationHost(mockSessionHost,
        version, cryptographyService, presentationHostConsumer, serverTracking);

    // then
    host.notifyIdu(idu);
  }

  @Test
  public void notifyIdu_service_error_Test() throws Exception {
    // given
    String idu = """
        {
          "context": {
            "PGA": "550e8400-e29b-41d4-a716-446655440000",
            "source_code": "source",
            "destination_code": "destination",
            "SDEK": "YWVzLTI1Ni1nY20tZW5jcnlwdGVkLWtleQ==",
            "service": "error",
            "service_primitive": "request",
            "options": {
              "TM": "someUUID"
            }
          },
          "message": "UFBEVSBlbmNvZGVkIG1lc3NhZ2UgY29udGVudA=="
        }
        """;
    host = new ImmutablePresentationHost(mockSessionHost,
        version, cryptographyService, presentationHostConsumer, serverTracking);

    // then
    host.notifyIdu(idu);
  }

  @Test
  public void notifyIdu_all_error_Test() throws Exception {
    // given
    String idu = """
        {
          "context": {
            "PGA": "550e8400-e29b-41d4-a716-446655440000",
            "source_code": "source",
            "destination_code": "destination",
            "SDEK": "YWVzLTI1Ni1nY20tZW5jcnlwdGVkLWtleQ==",
            "service": "error",
            "service_primitive": "error",
            "options": {
              "TM": "someUUID"
            }
          },
          "message": "UFBEVSBlbmNvZGVkIG1lc3NhZ2UgY29udGVudA=="
        }
        """;
    host = new ImmutablePresentationHost(mockSessionHost,
        version, cryptographyService, presentationHostConsumer, serverTracking);

    // then
    host.notifyIdu(idu);
  }

  @Test
  void notifyIdu_success_notifiesConsumer_Test() throws Exception {
    // Mock decryptSymmetric pour que le handler réussisse
    when(cryptographyService.decryptSymmetric(any(), any(javax.crypto.SecretKey.class),
        any(byte[].class)))
            .thenReturn("decrypted content".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    when(cryptographyService.getAlgorithm(any(), any()))
        .thenReturn("AES");

    javax.crypto.SecretKey fakeKey = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
    String sdek = java.util.Base64.getEncoder().encodeToString(fakeKey.getEncoded());

    UUID trackingNumber = UUID.randomUUID();

    // Construire un vrai PDU JSON valide
    String innerPduJson = """
        {
          "header": {
            "msgtype": "PRE.MSG.ENV",
            "id": "11111111-1111-1111-1111-111111111111",
            "parameters": {},
            "version": {
              "number": "1.0.0",
              "reference": "presentation-v1"
            }
          },
          "content": "base64EncryptedContent"
        }
        """;

    com.fasterxml.jackson.databind.ObjectMapper mapper =
        new com.fasterxml.jackson.databind.ObjectMapper();
    String escapedPdu = mapper.writeValueAsString(
        mapper.writeValueAsString(mapper.readTree(innerPduJson)));

    String idu = "{\"context\":{\"PGA\":\"550e8400-e29b-41d4-a716-446655440000\","
        + "\"source_code\":\"source\",\"destination_code\":\"destination\","
        + "\"SDEK\":\"" + sdek + "\","
        + "\"service\":\"transfer\",\"service_primitive\":\"indication\","
        + "\"options\":{\"TN\":\"" + trackingNumber + "\"}},"
        + "\"message\":" + escapedPdu + "}";

    host = new ImmutablePresentationHost(mockSessionHost,
        version, cryptographyService, presentationHostConsumer, serverTracking);

    host.notifyIdu(idu);

    // Vérifie que presentationHostConsumer.notifyIdu a été appelé (ligne 56)
    verify(presentationHostConsumer).notifyIdu(any(String.class));
  }

  @Test
  void notifyIdu_emptyResult_doesNotNotifyConsumer_Test() {
    String idu = """
        {
          "context": {
            "PGA": "550e8400-e29b-41d4-a716-446655440000",
            "source_code": "source",
            "destination_code": "destination",
            "SDEK": "YWVzLTI1Ni1nY20tZW5jcnlwdGVkLWtleQ==",
            "service": "transfer",
            "service_primitive": "indication",
            "options": {
              "TN": "not-a-valid-uuid"
            }
          },
          "message": "not valid json for pdu"
        }
        """;

    host = new ImmutablePresentationHost(mockSessionHost,
        version, cryptographyService, presentationHostConsumer, serverTracking);

    host.notifyIdu(idu);

    verify(presentationHostConsumer, never()).notifyIdu(any(String.class));
  }
}
