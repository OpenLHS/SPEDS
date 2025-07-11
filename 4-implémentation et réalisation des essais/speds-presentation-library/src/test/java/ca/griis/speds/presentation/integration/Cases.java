package ca.griis.speds.presentation.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.encryption.AesGcmDecryptor;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import ca.griis.js2p.gen.speds.presentation.api.dto.*;
import ca.griis.speds.presentation.api.exception.CipherException;
import ca.griis.speds.presentation.api.exception.DeserializationException;
import ca.griis.speds.presentation.api.exception.InvalidPduId;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.mockito.ArgumentCaptor;

public class Cases {

  public static void c01_client_request_success(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c01_client_request_success - l'antécédent requiert un 'hôte presentation' non null");

    // given
    // L’entrée est un message de la couche supérieure, soit un IDU 1_2 contenant le PDU 1 /
    // APP.MSG.
    // Le message applicatif est laissé au choix de l’implémenteur des tests.
    InterfaceDataUnit12Dto idu12Dto = new InterfaceDataUnit12Dto(
        new ContextDto(environment.pga, environment.source, environment.destination,
            environment.trackinNumber, false),
        "This is the message");

    environment.idu12ToSend = environment.objectMapper.writeValueAsString(idu12Dto);

    // when
    // Réception d’un message de la couche supérieure par un client et
    // production d’un message pour la couche inférieure
    environment.presentationHost.request(environment.idu12ToSend);

    // then
    // Le message de sortie est un message de la couche inférieure,
    // soit l’IDU 2_3 contenant le PDU 2 / PRE.MSG.ENV
    // Aucune exception n’est levée
    // Il est possible de déchiffré le content dans IDU_2_3.message de IDU 2_3 à l’aide de la clef
    // SDEK
    // Le message de sortie est conforme au format attendu
    ArgumentCaptor<String> idu23Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.sessionHost).request(idu23Captor.capture());

    environment.idu23Sent = idu23Captor.getValue();

    InterfaceDataUnit23Dto idu23Dto =
        environment.objectMapper.readValue(environment.idu23Sent,
            InterfaceDataUnit23Dto.class);

    assertEquals(environment.pga, idu23Dto.getContext().getPga());
    assertEquals(environment.source, idu23Dto.getContext().getSourceCode());
    assertEquals(environment.destination, idu23Dto.getContext().getDestinationCode());

    String sdek = idu23Dto.getContext().getSdek();

    assertNotNull(sdek);

    String message = idu23Dto.getMessage();

    ProtocolDataUnit2PREDto pduPresDto =
        environment.objectMapper.readValue(message, ProtocolDataUnit2PREDto.class);

    HeaderDto headerDto = pduPresDto.getHeader();
    assertEquals(HeaderDto.Msgtype.PRE_MSG_ENV, headerDto.getMsgtype());
    assertNotNull(headerDto.getId());

    String msgContent = pduPresDto.getContent();

    byte[] keyByte = Base64.getDecoder().decode(sdek);
    SecretKey key = new SecretKeySpec(keyByte, 0, keyByte.length, "AES");

    AesGcmDecryptor decryptor = new AesGcmDecryptor(key);

    byte[] decryptedBytes =
        decryptor.decrypt(Base64.getDecoder().decode(msgContent));
    String decipheredSdu = new String(decryptedBytes, StandardCharsets.UTF_8);

    assertEquals("This is the message", decipheredSdu);
  }

  public static void c02_server_indication_success(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c02_server_indication_success - l'antécédent requiert un 'hôte presentation' non null");

    assertNotNull(environment.sessionHost,
        "c02_server_indication_success - l'antécédent requiert un 'hôte session' non null");

    assertNotNull(environment.idu12ToSend,
        "c02_server_indication_success - l'antécédent requiert un 'idu12 à envoyer' non null");

    assertNotNull(environment.idu23Sent,
        "c02_server_indication_success - l'antécédent requiert un 'idu23 envoyé' non null");

    // given
    // Le message d’entrée est un message de la couche inférieure, soit l’IDU 2_3 contenant le PDU 2
    // / PRE.MSG.ENV.
    // Le message est laissé au choix de l’implémenteur des tests.
    doReturn(environment.idu23Sent).when(environment.sessionHost).indicateDataExchange();

    // when
    // Réception d’un message de la couche inférieure par un serveur et production d’un message
    // pour la couche supérieure
    String receivedIdu12 = environment.presentationHost.indication();

    // then
    // Le message de sortie est un message de la couche supérieure, soit l’IDU 1_2 contenant le PDU
    // 1 / APP.MSG
    // Aucune exception n’est levée
    // Le message de sortie est conforme au format attendu
    // Le message applicatif est bien celui attendu (le contenu déchiffré de content dans IDU
    // 2_3.message)
    environment.receivedIdu12 =
        environment.objectMapper.readValue(receivedIdu12, InterfaceDataUnit12Dto.class);

    InterfaceDataUnit12Dto expectedIdu12Dto = environment.objectMapper
        .readValue(environment.idu12ToSend, InterfaceDataUnit12Dto.class);

    assertEquals(expectedIdu12Dto.getContext().getPga(),
        environment.receivedIdu12.getContext().getPga());
    assertEquals(expectedIdu12Dto.getContext().getSourceCode(),
        environment.receivedIdu12.getContext().getSourceCode());
    assertEquals(expectedIdu12Dto.getContext().getDestinationCode(),
        environment.receivedIdu12.getContext().getDestinationCode());
    assertEquals(expectedIdu12Dto.getMessage(), environment.receivedIdu12.getMessage());
  }

  public static void c03_server_response_success(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c03_server_response_success - l'antécédent requiert un 'hôte presentation' non null");

    // given
    // Le message d’entrée est un message de la couche supérieure, soit l’IDU 1_2 contenant le PDU 1
    // / APP.MSG.
    // Le message est laissé au choix de l’implémenteur des tests.
    InterfaceDataUnit12Dto idu12Dto = new InterfaceDataUnit12Dto(
        new ContextDto(environment.pga, environment.destination, environment.source,
            environment.receivedIdu12.getContext().getTrackingNumber(), false),
        "This is the response");

    environment.idu12ToSend = environment.objectMapper.writeValueAsString(idu12Dto);

    // when
    // Réception d’un message de la couche supérieure par un serveur et production
    // d’un message pour la couche inférieure
    environment.presentationHost.response(environment.idu12ToSend);

    // Then
    // Le message de sortie est un message de la couche inférieur, soit l’IDU 2_3 contenant le PDU 2
    // / PRE.MSG.REC
    // Aucune exception n’est levée
    // Il est possible de déchiffrer le content dans IDU_2_3.message de IDU 2_3 à l’aide de la clef
    // SDEK
    // Le message de sortie est conforme au format attendu
    ArgumentCaptor<String> idu23Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.sessionHost).response(idu23Captor.capture());

    environment.idu23Sent = idu23Captor.getValue();

    InterfaceDataUnit23Dto idu23Dto =
        environment.objectMapper.readValue(environment.idu23Sent,
            InterfaceDataUnit23Dto.class);

    assertEquals(environment.pga, idu23Dto.getContext().getPga());
    assertEquals(environment.destination, idu23Dto.getContext().getSourceCode());
    assertEquals(environment.source, idu23Dto.getContext().getDestinationCode());

    String sdek = idu23Dto.getContext().getSdek();

    assertNotNull(sdek);

    String message = idu23Dto.getMessage();

    ProtocolDataUnit2PREDto pduPresDto =
        environment.objectMapper.readValue(message, ProtocolDataUnit2PREDto.class);

    HeaderDto headerDto = pduPresDto.getHeader();
    assertEquals(HeaderDto.Msgtype.PRE_MSG_REC, headerDto.getMsgtype());
    assertNotNull(headerDto.getId());

    String msgContent = pduPresDto.getContent();

    byte[] keyByte = Base64.getDecoder().decode(sdek);
    SecretKey key = new SecretKeySpec(keyByte, 0, keyByte.length, "AES");

    AesGcmDecryptor decryptor = new AesGcmDecryptor(key);

    byte[] decryptedBytes =
        decryptor.decrypt(Base64.getDecoder().decode(msgContent));
    String decipheredSdu = new String(decryptedBytes, StandardCharsets.UTF_8);

    assertEquals("This is the response", decipheredSdu);
  }

  public static void c04_client_confirm_success(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c04_client_confirm_success - l'antécédent requiert un 'hôte presentation' non null");

    assertNotNull(environment.sessionHost,
        "c04_client_confirm_success - l'antécédent requiert un 'hôte session' non null");

    assertNotNull(environment.idu12ToSend,
        "c04_client_confirm_success - l'antécédent requiert un 'idu12 à envoyer' non null");

    assertNotNull(environment.idu23Sent,
        "c04_client_confirm_success - l'antécédent requiert un 'idu23 envoyé' non null");

    // given
    // Le message d’entrée est un message de la couche inférieure, soit l’IDU 2_3 contenant le PDU 2
    // / PRE.MSG.ENV.
    // Le message est laissé au choix de l’implémenteur des tests.
    doReturn(environment.idu23Sent).when(environment.sessionHost).confirm();

    // when
    // Réception d’un message de la couche inférieure par un client
    // et production d’un message pour la couche supérieure
    String receivedIdu12 = environment.presentationHost.confirm();

    // then
    // Le message de sortie est un message de la couche supérieure, soit l’IDU 1_2 contenant le PDU
    // 1 / APP.MSG
    // Aucune exception n’est levée
    // Le message de sortie est conforme au format attendu
    // Le message applicatif est bien celui attendu (le contenu déchiffré de content dans IDU
    // 2_3.message)
    environment.receivedIdu12 =
        environment.objectMapper.readValue(receivedIdu12, InterfaceDataUnit12Dto.class);

    InterfaceDataUnit12Dto expectedIdu12Dto = environment.objectMapper
        .readValue(environment.idu12ToSend, InterfaceDataUnit12Dto.class);

    assertEquals(expectedIdu12Dto.getContext().getPga(),
        environment.receivedIdu12.getContext().getPga());
    assertEquals(expectedIdu12Dto.getContext().getSourceCode(),
        environment.receivedIdu12.getContext().getSourceCode());
    assertEquals(expectedIdu12Dto.getContext().getDestinationCode(),
        environment.receivedIdu12.getContext().getDestinationCode());
    assertEquals(expectedIdu12Dto.getMessage(), environment.receivedIdu12.getMessage());
  }

  public static void c05_server_indication_fail_deserialization(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c05_server_indication_fail_serialization - l'antécédent requiert un 'hôte presentation' non null");

    assertNotNull(environment.sessionHost,
        "c05_server_indication_fail_serialization - l'antécédent requiert un 'hôte session' non null");

    // given
    // Un message autre que le format attendu est reçu
    InterfaceDataUnit23Dto idu23Dto = new InterfaceDataUnit23Dto(
        new ContextDto__1(environment.pga, environment.source, environment.destination,
            environment.trackinNumber, false),
        "Random message");

    environment.idu23Sent = environment.objectMapper.writeValueAsString(idu23Dto);

    doReturn(environment.idu23Sent).when(environment.sessionHost).indicateDataExchange();

    // when
    // Réception d’un message de la couche inférieure par un serveur et
    // tentative de production d’un message pour la couche supérieure.
    // then
    // Une exception de type DeserializeException est levée
    // Le message de sortie est conforme au format attendu.
    assertThrows(DeserializationException.class, () -> {
      environment.presentationHost.indication();
    });
  }


  public static void c06_server_indication_fail_decryption(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c06_server_indication_fail_decryption - l'antécédent requiert un 'hôte presentation' non null");

    assertNotNull(environment.sessionHost,
        "c06_server_indication_fail_decryption - l'antécédent requiert un 'hôte session' non null");

    assertNotNull(environment.msgtype,
        "c06_server_indication_fail_decryption - l'antécédent requiert un 'type de message' non null");

    assertNotNull(environment.msgId,
        "c06_server_indication_fail_decryption - l'antécédent requiert un 'identifiant de message' non null");

    assertNotNull(environment.trackinNumber,
        "c06_server_indication_fail_decryption - l'antécédent requiert un 'numéro de suivi' non null");

    // given
    // La clé de chiffrement ne permet pas de déchiffrer le message
    ProtocolDataUnit2PREDto pdu2Dto = new ProtocolDataUnit2PREDto(
        new HeaderDto(environment.msgtype, environment.msgId, false,
            new SPEDSDto(environment.spedsVersion, environment.spedsReference)),
        "Random message content");

    String pdu2Message = environment.objectMapper.writeValueAsString(pdu2Dto);

    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    byte[] keyBytes = secretKey.getEncoded();
    String sdek = Base64.getEncoder().encodeToString(keyBytes);

    InterfaceDataUnit23Dto idu23Dto = new InterfaceDataUnit23Dto(
        new ContextDto__1(environment.pga, environment.source, environment.destination,
            sdek, environment.trackinNumber, false),
        pdu2Message);

    environment.idu23Sent = environment.objectMapper.writeValueAsString(idu23Dto);

    doReturn(environment.idu23Sent).when(environment.sessionHost).indicateDataExchange();

    // when
    // Réception d’un message de la couche inférieure par un serveur et
    // tentative de production d’un message pour la couche supérieure.
    // then
    // Une exception de type CipherException est levée
    assertThrows(CipherException.class, () -> {
      environment.presentationHost.indication();
    });
  }

  public static void c07_client_confirm_fail_deserialization(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c07_client_confirm_fail_deserialization - l'antécédent requiert un 'hôte presentation' non null");

    assertNotNull(environment.sessionHost,
        "c07_client_confirm_fail_deserialization - l'antécédent requiert un 'hôte session' non null");

    // given
    // Un message autre que le format attendu est reçu
    InterfaceDataUnit23Dto idu23Dto = new InterfaceDataUnit23Dto(
        new ContextDto__1(environment.pga, environment.destination, environment.source,
            environment.trackinNumber, false),
        "Random message");

    environment.idu23Sent = environment.objectMapper.writeValueAsString(idu23Dto);

    doReturn(environment.idu23Sent).when(environment.sessionHost).confirm();

    // when
    // Réception d’un message de la couche inférieure par un client et
    // tentative de production d’un message pour la couche supérieure.
    // then
    // Une exception de type DeserializeException est levée
    assertThrows(DeserializationException.class, () -> {
      environment.presentationHost.confirm();
    });
  }

  public static void c08_client_confirm_fail_decryption(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c08_client_confirm_fail_decryption - l'antécédent requiert un 'hôte presentation' non null");

    assertNotNull(environment.sessionHost,
        "c08_client_confirm_fail_decryption - l'antécédent requiert un 'hôte session' non null");

    assertNotNull(environment.msgtype,
        "c08_client_confirm_fail_decryption - l'antécédent requiert un 'type de message' non null");

    assertNotNull(environment.msgId,
        "c08_client_confirm_fail_decryption - l'antécédent requiert un 'identifiant de message' non null");

    assertNotNull(environment.trackinNumber,
        "c08_client_confirm_fail_decryption - l'antécédent requiert un 'numéro de suivi' non null");

    // given
    // La clé de chiffrement ne permet pas de déchiffrer le message
    ProtocolDataUnit2PREDto pdu2Dto = new ProtocolDataUnit2PREDto(
        new HeaderDto(environment.msgtype, environment.msgId, false,
            new SPEDSDto(environment.spedsVersion, environment.spedsReference)),
        "Random message content");

    String pdu2Message = environment.objectMapper.writeValueAsString(pdu2Dto);

    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    byte[] keyBytes = secretKey.getEncoded();
    String sdek = Base64.getEncoder().encodeToString(keyBytes);

    InterfaceDataUnit23Dto idu23Dto = new InterfaceDataUnit23Dto(
        new ContextDto__1(environment.pga, environment.source, environment.destination,
            sdek, environment.trackinNumber, false),
        pdu2Message);

    environment.idu23Sent = environment.objectMapper.writeValueAsString(idu23Dto);

    doReturn(environment.idu23Sent).when(environment.sessionHost).confirm();

    // when
    // Réception d’un message de la couche inférieure par un client et
    // tentative de production d’un message pour la couche supérieure.
    // then
    // Une exception de type CipherException est levée
    assertThrows(CipherException.class, () -> {
      environment.presentationHost.confirm();
    });
  }

  public static void c09_client_confirm_fail_message_id(Environment environment)
      throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.presentationHost,
        "c09_client_confirm_fail_message_id - l'antécédent requiert un 'hôte presentation' non null");

    assertNotNull(environment.sessionHost,
        "c09_client_confirm_fail_message_id - l'antécédent requiert un 'hôte session' non null");

    assertNotNull(environment.msgtype,
        "c09_client_confirm_fail_message_id - l'antécédent requiert un 'type de message' non null");

    assertNotNull(environment.msgId,
        "c09_client_confirm_fail_message_id - l'antécédent requiert un 'identifiant de message' non null");

    assertNotNull(environment.trackinNumber,
        "c09_client_confirm_fail_message_id - l'antécédent requiert un 'numéro de suivi' non null");

    // given
    // Un message d’envoi a été fait par le client précédemment avec un
    // identifiant de message différent que celui détaillé en donnée d’entrée

    InterfaceDataUnit23Dto idu23SentDto = environment.objectMapper
        .readValue(environment.idu23Sent, InterfaceDataUnit23Dto.class);

    ProtocolDataUnit2PREDto pdu2SentDto = environment.objectMapper
        .readValue(idu23SentDto.getMessage(), ProtocolDataUnit2PREDto.class);

    ProtocolDataUnit2PREDto receivedPdu2Dto = new ProtocolDataUnit2PREDto(
        new HeaderDto(environment.msgtype, environment.msgId, false,
            new SPEDSDto(environment.spedsVersion, environment.spedsReference)),
        pdu2SentDto.getContent());

    String receivedPdu2Message = environment.objectMapper.writeValueAsString(receivedPdu2Dto);

    InterfaceDataUnit23Dto receivedIdu23Dto = new InterfaceDataUnit23Dto(
        new ContextDto__1(environment.pga, environment.destination, environment.source,
            idu23SentDto.getContext().getSdek(), environment.trackinNumber, false),
        receivedPdu2Message);

    String receivedIdu23 = environment.objectMapper.writeValueAsString(receivedIdu23Dto);

    doReturn(receivedIdu23).when(environment.sessionHost).confirm();

    // when
    // Réception d’un message de la couche inférieure par un client et
    // tentative de production d’un message pour la couche supérieure.
    // then
    // Une exception de type InvalidPduId est levée
    assertThrows(InvalidPduId.class, () -> {
      environment.presentationHost.confirm();
    });
  }
}
