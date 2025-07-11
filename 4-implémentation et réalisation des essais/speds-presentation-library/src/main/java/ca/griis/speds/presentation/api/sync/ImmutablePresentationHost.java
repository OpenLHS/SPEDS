/**
 * @file
 * @copyright @@GRIIS_COPYRIGHT@@
 * @licence @@GRIIS_LICENCE@@
 * @version @@GRIIS_VERSION@@
 * @brief @~french Implémentation de la classe PresentationHost.
 * @brief @~english Implementation of the PresentationHost class.
 */

package ca.griis.speds.presentation.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.encryption.AesGcmDecryptor;
import ca.griis.cryptography.symmetric.encryption.AesGcmEncryptor;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import ca.griis.js2p.gen.speds.presentation.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ContextDto__1;
import ca.griis.js2p.gen.speds.presentation.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ProtocolDataUnit2PREDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.SPEDSDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.exception.CipherException;
import ca.griis.speds.presentation.api.exception.DeserializationException;
import ca.griis.speds.presentation.api.exception.InvalidPduId;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.session.api.SessionHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      «Model (Abstract, automation, etc.) (optional)»
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Offre les services d'un hôte immutable de la couche présentation.
 * @par Détails
 *      - Un choix volontaire a été fait afin de ne pas envelopper (wrap) les classes générées,
 *      dans l'optique de ne pas inutilement alourdir la gestion de la mémoire.
 *      - Ce choix repose sur l'heuristique selon laquelle l'ajout d'un wrapper ne procure pas
 *      de bénéfices suffisants en regard du coût engendré :
 *      - Les objets générés sont directement compatibles avec nos besoins, rendant
 *      l'encapsulation superflue.
 *      - Une couche supplémentaire de wrappers augmenterait l'empreinte mémoire et
 *      la complexité sans justification suffisante.
 *      - La stabilité de la structure des objets générés permet de minimiser
 *      l'impact des évolutions futures sur le code client.
 *
 * @par Modèle
 *      ConcurrentHashMap<PresentationTracking, TrackingInformation> curator : </br>
 *      Contient les informations de suivi contextuelle de la couche pour les deux couches </br>
 *      intéragisseant avec la couche actuelle.
 *      Set<UUID> messageSent = new HashSet<>(); :
 *      Garde en mémoire les identifiants des messages envoyé pour faire la corercpondance </br>
 *      avec les messages de réponses reçu ultérieurement.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-02-18 [MD] - Implémentation des interfaces </br>
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutablePresentationHost implements PresentationHost {
  private static final GriisLogger logger = getLogger(ImmutablePresentationHost.class);
  private final SessionHost host;

  private final ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking;
  private final Set<UUID> clientMsgTracking;

  private final String spedsVersion;
  private final String spedsReference;

  private final ObjectMapper objectMapper;

  public ImmutablePresentationHost(SessionHost host, String spedsVersion, String spedsReference,
      ObjectMapper mapper) {
    logger.trace(Trace.ENTER_METHOD_4, "host", host, "spedsVersion", spedsVersion, "spedsReference",
        spedsReference, "mapper", mapper);
    this.host = host;
    this.spedsVersion = spedsVersion;
    this.spedsReference = spedsReference;
    this.serverTracking = new ConcurrentHashMap<>();
    this.clientMsgTracking = ConcurrentHashMap.newKeySet();
    this.objectMapper = mapper;
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);
    serverTracking.clear();
    clientMsgTracking.clear();
    host.close();
  }

  @Override
  public void request(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    // [PRO-1.1] Retrieve the upper IDU's ICI and SDU
    InterfaceDataUnit12Dto upperIdu;
    try {
      upperIdu = objectMapper.readValue(idu, InterfaceDataUnit12Dto.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Presentation unable to deserialize IDU=" + idu);
    }
    ContextDto ici = upperIdu.getContext();
    String sdu = upperIdu.getMessage();

    // [PRO-1.2] Cipher the SDU
    // [PRO-1.2.1]
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    // [PRO-1.2.2]
    AesGcmEncryptor encryptor = new AesGcmEncryptor(secretKey);
    byte[] encryptedMessageBytes = encryptor.encrypt(sdu.getBytes(StandardCharsets.UTF_8));
    String cipheredSdu = Base64.getEncoder().encodeToString(encryptedMessageBytes);

    // [PRO-1.3] PRE.MSG.ENV Message
    // [PRO-1.3.1]
    UUID presentationId = UUID.randomUUID();
    HeaderDto.Msgtype msgtype = HeaderDto.Msgtype.PRE_MSG_ENV;
    Boolean options = Boolean.FALSE;
    SPEDSDto spedsDto = new SPEDSDto(spedsVersion, spedsReference);
    HeaderDto preMessageHeader = new HeaderDto(msgtype, presentationId, options, spedsDto);
    ProtocolDataUnit2PREDto preMsgEnv = new ProtocolDataUnit2PREDto(preMessageHeader, cipheredSdu);
    // [PRO-1.3.2]
    String serialPreMessage;
    try {
      serialPreMessage = objectMapper.writeValueAsString(preMsgEnv);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Serialization of preMsgEnv failed unexpectedly. "
          + "Check if the ObjectMapper is misconfigured or if the object has missing annotations.",
          e);
    }

    // [PRO-1.4] Create the lowerIdu
    // [PRO-1.4.1] - Create the ICI for the SDU
    byte[] keyBytes = secretKey.getEncoded();
    String serialKey = Base64.getEncoder().encodeToString(keyBytes);

    ContextDto__1 context = new ContextDto__1(
        ici.getPga(),
        ici.getSourceCode(),
        ici.getDestinationCode(),
        serialKey,
        null,
        ici.getOptions());

    // [PRO-1.4.2] "Create" the SDU for the IDU
    String outboudSdu = serialPreMessage;
    InterfaceDataUnit23Dto lowerIdu = new InterfaceDataUnit23Dto(context, outboudSdu);

    // [PRO-1.5]
    String outgoingSerial = serialiseOutboundMsg(lowerIdu);

    // NOTE - MD - At the end so we know everything went well before tracking it.
    clientMsgTracking.add(preMsgEnv.getHeader().getId());

    // send it below
    logger.trace(Trace.ALGORITHM_1, "outgoingSerial", outgoingSerial);
    host.request(outgoingSerial);
    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public String confirm() {
    logger.trace(Trace.ENTER_METHOD_0);
    // Convertir l'IDU dans le format de l'implémentation.
    // Appeler la méthode host.confirm.

    // Await incoming messages
    String inboundSerialIdu = host.confirm();

    // Message PRE.MSG.REC. [PRO-2.1]
    InterfaceDataUnit23Dto lowerIdu;
    try {
      lowerIdu = objectMapper.readValue(inboundSerialIdu, InterfaceDataUnit23Dto.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
          "Presentation unable to deserialize IDU=" + inboundSerialIdu);
    }

    // [PRO-2.1.1]
    String sdu = lowerIdu.getMessage();

    // [PRO-2.1.2]
    ProtocolDataUnit2PREDto inboundSdu;
    try {
      inboundSdu = objectMapper.readValue(sdu, ProtocolDataUnit2PREDto.class);
    } catch (JsonProcessingException e) {
      throw new DeserializationException("Deserialization error. Unable to deserialize", e);
    }
    if (!inboundSdu.getHeader().getMsgtype().equals(HeaderDto.Msgtype.PRE_MSG_REC)) {
      throw new DeserializationException(
          "Deserialization error. Type of SDU must be PRE_MSG_REC, is:"
              + inboundSdu.getHeader().getMsgtype());
    }

    // [PRO-2.1.3]
    UUID presentationMsgId = inboundSdu.getHeader().getId();
    validateMessageExist(presentationMsgId);

    // [PRO-2.1.4]
    // Récupérer la clef
    String decipheredSdu;
    try {
      String sdekSerial = lowerIdu.getContext().getSdek();
      byte[] keyByte = Base64.getDecoder().decode(sdekSerial);
      SecretKey key = new SecretKeySpec(keyByte, 0, keyByte.length, "AES");
      AesGcmDecryptor decryptor = new AesGcmDecryptor(key);
      byte[] decryptedBytes =
          decryptor.decrypt(Base64.getDecoder().decode(inboundSdu.getContent()));
      decipheredSdu = new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (SecurityException | IllegalArgumentException e) {
      throw new CipherException("Unable to decipher the SDU", e);
    }

    // Create the upper outbound IDU [PRO-2.2]
    // [PRO-2.2.1] Create the ICI for the IDU
    // Note MD : we get the ICI at step 2.2.1 instead of PRO-2.1.1 to comply to code
    // analysis tools
    ContextDto__1 ici = lowerIdu.getContext();
    ContextDto upperContext = new ContextDto(
        ici.getPga(),
        ici.getSourceCode(),
        ici.getDestinationCode(),
        presentationMsgId,
        ici.getOptions());

    // [PRO-2.2.2] "Create" the SDU for the IDU
    InterfaceDataUnit12Dto outboundIdu =
        new InterfaceDataUnit12Dto(upperContext, decipheredSdu);

    // [PRO-2.3]
    String outboundSerial;
    outboundSerial = serialiseOutboundMsg(outboundIdu);
    logger.trace(Trace.EXIT_METHOD_1, "outboundSerial", outboundSerial);
    return outboundSerial;
  }

  @Override
  public String indication() {
    logger.trace(Trace.EXIT_METHOD_0);

    String inboundSerialIdu = host.indicateDataExchange();

    // [PRO-3.1] Message PRE.MSG.ENV. [PRO-2.1]
    InterfaceDataUnit23Dto lowerIdu;
    try {
      lowerIdu = objectMapper.readValue(inboundSerialIdu, InterfaceDataUnit23Dto.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
          "Presentation unable to deserialize IDU=" + inboundSerialIdu);
    }

    // [PRO-3.1.1]
    String sdu = lowerIdu.getMessage();

    // [PRO-3.1.2]
    ProtocolDataUnit2PREDto inboundSdu;
    try {
      inboundSdu = objectMapper.readValue(sdu, ProtocolDataUnit2PREDto.class);
      if (!inboundSdu.getHeader().getMsgtype().equals(HeaderDto.Msgtype.PRE_MSG_ENV)) {
        throw new DeserializationException("Deserialization error."
            + "Type of outboundSdu must be: " + HeaderDto.Msgtype.PRE_MSG_ENV + ", "
            + "is: " + inboundSdu.getHeader().getMsgtype());
      }
    } catch (JsonProcessingException e) {
      throw new DeserializationException("Deserialization error. Unable to deserialize", e);
    }

    // [PRO-3.1.3]
    // Récupérer la clef
    String decipheredSdu;
    SecretKey key;
    try {
      String sdekSerial = lowerIdu.getContext().getSdek();
      byte[] keyByte = Base64.getDecoder().decode(sdekSerial);
      key = new SecretKeySpec(keyByte, 0, keyByte.length, "AES");
      AesGcmDecryptor decryptor = new AesGcmDecryptor(key);
      byte[] decryptedBytes =
          decryptor.decrypt(Base64.getDecoder().decode(inboundSdu.getContent()));
      decipheredSdu = new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      throw new CipherException("Unable to cipher the SDU", e);
    }

    serverTracking.put(new PresentationTracking(inboundSdu.getHeader().getId()),
        new TrackingInformation(lowerIdu.getContext().getTrackingNumber(), key));

    // [PRO-3.2] Create the upper outbound IDU
    // [PRO-3.2.1] - Create the ICI for IDU
    ContextDto__1 ici = lowerIdu.getContext();
    UUID presentationMsgId = inboundSdu.getHeader().getId();
    ContextDto upperContext = new ContextDto(
        ici.getPga(),
        ici.getSourceCode(),
        ici.getDestinationCode(),
        presentationMsgId,
        ici.getOptions());

    // [PRO-3.2.2] - "Create" SDU for IDU
    InterfaceDataUnit12Dto outboundIdu =
        new InterfaceDataUnit12Dto(upperContext, decipheredSdu);

    // [PRO-2.3]
    String outboundSerial = serialiseOutboundMsg(outboundIdu);
    logger.trace(Trace.EXIT_METHOD_1, "outboundSerial", outboundSerial);
    return outboundSerial;
  }

  @Override
  public void response(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    // [PRO-4.1] Retrieve the upper IDU's ICI and SDU
    InterfaceDataUnit12Dto upperIdu;
    try {
      upperIdu = objectMapper.readValue(idu, InterfaceDataUnit12Dto.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Presentation unable to deserialize IDU=" + idu);
    }
    ContextDto ici = upperIdu.getContext();
    String sdu = upperIdu.getMessage();

    // [PRO-4.2] Cipher the SDU
    // Get the key and purge
    String cipheredSdu;
    SecretKey secretKey;
    UUID sessionTracking;
    try {
      PresentationTracking presentationTracking =
          new PresentationTracking(upperIdu.getContext().getTrackingNumber());
      TrackingInformation trackingInfo = serverTracking.get(presentationTracking);
      sessionTracking = trackingInfo.sessionTracking();
      secretKey = trackingInfo.sdek();
      serverTracking.remove(presentationTracking);
      // cipher
      AesGcmEncryptor encryptor = new AesGcmEncryptor(secretKey);
      byte[] encryptedMessageBytes = encryptor.encrypt(sdu.getBytes(StandardCharsets.UTF_8));
      cipheredSdu = Base64.getEncoder().encodeToString(encryptedMessageBytes);
    } catch (SecurityException e) {
      throw new CipherException("Unable to cipher the SDU", e);
    }

    // [PRO-4.3] PRE.MSG.REC Message
    // [PRO-4.3.1]
    // [PRO-4.3.1.1]
    UUID presentationId = upperIdu.getContext().getTrackingNumber();
    HeaderDto.Msgtype msgtype = HeaderDto.Msgtype.PRE_MSG_REC;
    Boolean options = Boolean.FALSE;
    SPEDSDto spedsDto = new SPEDSDto(spedsVersion, spedsReference);
    HeaderDto preMessageHeader = new HeaderDto(msgtype, presentationId, options, spedsDto);
    ProtocolDataUnit2PREDto preMsgEnv = new ProtocolDataUnit2PREDto(preMessageHeader, cipheredSdu);

    // [PRO-4.3.2]
    String serialPreMessage;
    try {
      serialPreMessage = objectMapper.writeValueAsString(preMsgEnv);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Serialization of preMsgEnv failed unexpectedly. "
          + "Check if the ObjectMapper is misconfigured or if the object has missing annotations.",
          e);
    }

    // [PRO-4.4] Create the lowerIdu
    // [PRO-4.4.1] - Create ICI for IDU
    byte[] keyBytes = secretKey.getEncoded();
    String serialKey = Base64.getEncoder().encodeToString(keyBytes);

    ContextDto__1 context = new ContextDto__1(
        ici.getPga(),
        ici.getSourceCode(),
        ici.getDestinationCode(),
        serialKey,
        sessionTracking,
        ici.getOptions());

    // [PRO-4.4.2] - "Create" SDU for IDU
    String outboudSdu = serialPreMessage;
    InterfaceDataUnit23Dto lowerIdu = new InterfaceDataUnit23Dto(context, outboudSdu);

    // [PRO-4.5]
    String outboundSerial = serialiseOutboundMsg(lowerIdu);

    // send it below
    logger.trace(Trace.ALGORITHM_1, "outboundSerial", outboundSerial);
    host.response(outboundSerial);
    logger.trace(Trace.EXIT_METHOD_0);
  }

  private String serialiseOutboundMsg(Object outboundIdu) {
    logger.trace(Trace.ENTER_METHOD_1, outboundIdu, outboundIdu);
    String outboundSerial;
    try {
      outboundSerial = objectMapper.writeValueAsString(outboundIdu);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(
          "Unexpected JSON serialization failure for " + outboundIdu.getClass().getSimpleName()
              + ". "
              + "Ensure all fields have proper annotations and check ObjectMapper configuration.",
          e);
    }
    logger.trace(Trace.EXIT_METHOD_1, "outboundIdu", outboundIdu);
    return outboundSerial;
  }

  private void validateMessageExist(UUID presentationMsgId) throws InvalidPduId {
    logger.trace(Trace.ENTER_METHOD_1, "presentationMsgId", presentationMsgId);

    if (clientMsgTracking.contains(presentationMsgId)) {
      clientMsgTracking.remove(presentationMsgId);
    } else {
      throw new InvalidPduId("The response SDU identifier has no match"
          + "in the list of sent messages: idReceived=" + presentationMsgId
          + " List of messages waiting for response: " + clientMsgTracking);
    }
    logger.trace(Trace.EXIT_METHOD_0);
  }
}
