package ca.griis.speds.session.unit.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.UUID;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

public class SessionInformationTest {

  private SecretKey generateSecretKey() throws Exception {
    return KeyGenerator.getInstance("AES").generateKey();
  }

  private KeyPair generateKeyPair() throws Exception {
    return KeyPairGenerator.getInstance("RSA").generateKeyPair();
  }

  @Test
  void testNumberOfMessage_setAndGetValue() {
    SessionInformation info = SessionInformation.builder()
        .numberOfMessage(42)
        .build();

    assertNotNull(info.numberOfMessage, "numberOfMessage devrait être non null");
    assertEquals(42, info.numberOfMessage);
  }

  @Test
  void testNumberOfMessage_defaultIsNull() {
    SessionInformation info = SessionInformation.builder().build();
    assertEquals(0, info.numberOfMessage, "numberOfMessage devrait être 0 par défaut");
  }

  @Test
  void testNumberOfMessage_copyWithOfBuilder() {
    SessionInformation original = SessionInformation.builder()
        .numberOfMessage(123)
        .trackingNumber(UUID.randomUUID())
        .build();

    SessionInformation copy = SessionInformation.builder()
        .of(original)
        .build();

    assertEquals(original.numberOfMessage, copy.numberOfMessage,
        "La copie devrait avoir la même valeur de numberOfMessage");
    assertEquals(original.trackingNumber, copy.trackingNumber,
        "La copie devrait aussi copier trackingNumber");
  }

  @Test
  void testBuilder_setsAllFieldsCorrectly() throws Exception {
    SessionId sessionId = new SessionId(UUID.randomUUID());
    String initiatorId = "initiator-A";
    String initiatorPubKey = "pubKey";
    String initiatorIri = "iri:A";

    String peerId = "peer-B";
    String peerIri = "iri:B";

    SecretKey skak = generateSecretKey();
    String pgaId = "pga-456";
    SecretKey sdek = generateSecretKey();
    UUID trackingNumber = UUID.randomUUID();
    int numberOfMessage = 12;

    String piduMessage = "hello-pidu";
    KeyPair firstChoice = generateKeyPair();

    SessionInformation info = SessionInformation.builder()
        .sessionId(sessionId)
        .initiatorId(initiatorId)
        .initiatorPubKey(initiatorPubKey)
        .initiatorIri(initiatorIri)
        .peerId(peerId)
        .peerIri(peerIri)
        .skak(skak)
        .pgaId(pgaId)
        .sdek(sdek)
        .trackingNumber(trackingNumber)
        .numberOfMessage(numberOfMessage)
        .piduMessage(piduMessage)
        .firstChoice(firstChoice)
        .build();

    assertEquals(sessionId, info.sessionId);
    assertEquals(initiatorId, info.initiatorId);
    assertEquals(initiatorPubKey, info.initiatorPubKey);
    assertEquals(initiatorIri, info.initiatorIri);
    assertEquals(peerId, info.peerId);
    assertEquals(peerIri, info.peerIri);
    assertEquals(skak, info.skak);
    assertEquals(pgaId, info.pgaId);
    assertEquals(sdek, info.sdek);
    assertEquals(trackingNumber, info.trackingNumber);
    assertEquals(numberOfMessage, info.numberOfMessage);
    assertEquals(piduMessage, info.piduMessage);
    assertEquals(firstChoice, info.firstChoice);
  }

  @Test
  void testOfBuilder_copiesAllFields() throws Exception {
    SessionInformation original = SessionInformation.builder()
        .sessionId(new SessionId(UUID.randomUUID()))
        .initiatorId("id1")
        .initiatorPubKey("pub1")
        .initiatorIri("iri1")
        .peerId("peerX")
        .peerIri("iriX")
        .skak(generateSecretKey())
        .pgaId("pga-X")
        .sdek(generateSecretKey())
        .trackingNumber(UUID.randomUUID())
        .numberOfMessage(999)
        .piduMessage("pidu!")
        .firstChoice(generateKeyPair())
        .build();

    SessionInformation copy = SessionInformation.builder()
        .of(original)
        .build();

    assertEquals(original.sessionId, copy.sessionId);
    assertEquals(original.initiatorId, copy.initiatorId);
    assertEquals(original.initiatorPubKey, copy.initiatorPubKey);
    assertEquals(original.initiatorIri, copy.initiatorIri);
    assertEquals(original.peerId, copy.peerId);
    assertEquals(original.peerIri, copy.peerIri);
    assertEquals(original.skak, copy.skak);
    assertEquals(original.pgaId, copy.pgaId);
    assertEquals(original.sdek, copy.sdek);
    assertEquals(original.trackingNumber, copy.trackingNumber);
    assertEquals(original.numberOfMessage, copy.numberOfMessage);
    assertEquals(original.piduMessage, copy.piduMessage);
    assertEquals(original.firstChoice, copy.firstChoice);
  }
}
