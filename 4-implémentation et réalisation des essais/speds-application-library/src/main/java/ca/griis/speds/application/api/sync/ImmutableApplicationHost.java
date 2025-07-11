/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ImmutableApplicationHost.
 * @brief @~english Contains description of ImmutableApplicationHost class.
 */

package ca.griis.speds.application.api.sync;

import ca.griis.js2p.gen.speds.application.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.SPEDSDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.exception.DeserializationException;
import ca.griis.speds.application.api.exception.InvalidPduIdException;
import ca.griis.speds.application.api.exception.SerializationException;
import ca.griis.speds.presentation.api.PresentationHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
 * @brief @~french Implémentation synchrone de l'interface du service ApplicationHost
 * @par Details
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-01-28 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutableApplicationHost implements ApplicationHost {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ImmutableApplicationHost.class);

  private final ObjectMapper sharedObject;
  private final Set<UUID> threadSafeUniqueMessageId;
  private final ConcurrentMap<UUID, UUID> threadSafeTrackingNumber;
  private final PresentationHost presentationHost;
  private final String spedsVersion;
  private final String spedsReference;

  ImmutableApplicationHost(PresentationHost host, ObjectMapper shareObject, String spedsVersion,
      String spedsReference) {
    this.presentationHost = host;
    this.sharedObject = shareObject;
    this.threadSafeUniqueMessageId = ConcurrentHashMap.newKeySet();
    this.threadSafeTrackingNumber = new ConcurrentHashMap<>();
    this.spedsVersion = spedsVersion;
    this.spedsReference = spedsReference;
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    threadSafeUniqueMessageId.clear();
    threadSafeTrackingNumber.clear();
    presentationHost.close();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void request(InterfaceDataUnit01Dto iduDto) {
    logger.trace(Trace.ENTER_METHOD_1, "iduDto", iduDto);

    // ajout de l'identifiant du message à notre liste d'envoi pour validation future
    try {
      commitMessageToMemory(iduDto.getMessage());

      ContextDto context =
          new ContextDto(iduDto.getContext().getPga(), iduDto.getContext().getSourceCode(),
              iduDto.getContext().getDestinationCode(), null, Boolean.FALSE);
      InterfaceDataUnit12Dto exit = new InterfaceDataUnit12Dto(context, iduDto.getMessage());

      // sérialisation du message
      String idu = sharedObject.writeValueAsString(exit);
      // call underline layer with idu
      presentationHost.request(idu);
    } catch (JsonProcessingException e) {
      throw new DeserializationException(e);
    }
    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public InterfaceDataUnit01Dto confirm() {
    logger.trace(Trace.ENTER_METHOD_0);
    String idu = presentationHost.confirm();
    InterfaceDataUnit12Dto iduIn;
    try {
      iduIn = sharedObject.readValue(idu, InterfaceDataUnit12Dto.class);

      // Vérifier et valider la confirmation reçue
      ProtocolDataUnit1APPDto receivedSdu = validatePdu(iduIn.getMessage());

      // Construction du Protocol Data Unit de la couche application à partir de celui reçu
      HeaderDto newHeader =
          new HeaderDto(receivedSdu.getHeader().getMsgtype(), receivedSdu.getHeader().getId(),
              Boolean.FALSE, new SPEDSDto(spedsVersion, spedsReference));
      ProtocolDataUnit1APPDto newPdu =
          new ProtocolDataUnit1APPDto(newHeader, receivedSdu.getContent());
      String newSdu = sharedObject.writeValueAsString(newPdu);

      ContextDto context =
          new ContextDto(iduIn.getContext().getPga(), iduIn.getContext().getSourceCode(),
              iduIn.getContext().getDestinationCode(), Boolean.FALSE);
      InterfaceDataUnit01Dto iduOut = new InterfaceDataUnit01Dto(context, newSdu);

      logger.trace(Trace.EXIT_METHOD_1, "iduOut", iduOut);
      return iduOut;
    } catch (JsonProcessingException e) {
      throw new DeserializationException("Received IDU is invalid.", e);
    }
  }

  @Override
  public InterfaceDataUnit01Dto indication() {
    logger.trace(Trace.ENTER_METHOD_0);
    String idu12 = presentationHost.indication();

    InterfaceDataUnit12Dto iduIn;
    try {
      iduIn = sharedObject.readValue(idu12, InterfaceDataUnit12Dto.class);

      String sdu = iduIn.getMessage();
      ProtocolDataUnit1APPDto receivedPdu = retrievePdu(sdu);

      // Sauvegarde du trackingNumber pour référence future dans response
      threadSafeTrackingNumber.put(receivedPdu.getHeader().getId(),
          iduIn.getContext().getTrackingNumber());

      // Construction du Protocol Data Unit de la couche application à partir de celui reçu
      HeaderDto newHeader =
          new HeaderDto(receivedPdu.getHeader().getMsgtype(), receivedPdu.getHeader().getId(),
              Boolean.FALSE, new SPEDSDto(spedsVersion, spedsReference));
      ProtocolDataUnit1APPDto newPdu =
          new ProtocolDataUnit1APPDto(newHeader, receivedPdu.getContent());
      String newSdu = sharedObject.writeValueAsString(newPdu);

      ContextDto context =
          new ContextDto(iduIn.getContext().getPga(), iduIn.getContext().getSourceCode(),
              iduIn.getContext().getDestinationCode(), Boolean.FALSE);
      InterfaceDataUnit01Dto iduOut = new InterfaceDataUnit01Dto(context, newSdu);

      logger.trace(Trace.EXIT_METHOD_1, "iduOut", iduOut);
      return iduOut;
    } catch (JsonProcessingException e) {
      throw new DeserializationException("Received IDU/SDU is invalid.", e);
    }
  }

  @Override
  public void response(InterfaceDataUnit01Dto iduDto) {
    logger.trace(Trace.ENTER_METHOD_1, "iduDto", iduDto);

    try {
      ProtocolDataUnit1APPDto pdu = this.retrievePdu(iduDto.getMessage());

      if (threadSafeTrackingNumber.containsKey(pdu.getHeader().getId())) {
        // Construction de l'IDU
        ContextDto context =
            new ContextDto(iduDto.getContext().getPga(), iduDto.getContext().getSourceCode(),
                iduDto.getContext().getDestinationCode(),
                threadSafeTrackingNumber.get(pdu.getHeader().getId()), Boolean.FALSE);

        // retirer le message id-trackingNumber de la map
        threadSafeTrackingNumber.remove(pdu.getHeader().getId());

        // On ne sérialise pas le message reçu en entré, car on prend pour acquis qu'il l'est déjà
        InterfaceDataUnit12Dto exit = new InterfaceDataUnit12Dto(context, iduDto.getMessage());

        // sérialisation de l'idu pour l'envoi
        String iduStr = sharedObject.writeValueAsString(exit);

        // appel de la couche d'en dessous avec le idu
        presentationHost.response(iduStr);
      } else {
        throw new InvalidPduIdException(
            "This response does not correspond to a preceding indication.");
      }
    } catch (JsonProcessingException e) {
      throw new SerializationException(e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  /**
   * @brief @~french Obtenir le ProtocolDataUnit à partir du JSON
   * @param pdu la chaîne de caractères JSON
   * @exception JsonProcessingException La chaîne de caractères ne correspond pas à un PDU
   * @return Le PDU
   */
  private ProtocolDataUnit1APPDto retrievePdu(String pdu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "pdu", pdu);
    ProtocolDataUnit1APPDto pduApp = sharedObject.readValue(pdu, ProtocolDataUnit1APPDto.class);
    logger.trace(Trace.EXIT_METHOD_1, "pduApp", pduApp);
    return pduApp;
  }

  /**
   * @brief @~french Valider que la chaîne de caractère est un PDU et que l'identifiant du message
   *        est reconnu
   * @param message le message à valider et vérifier
   * @return Le PDU reçu
   * @exception DeserializationException Le message n'est pas un pdu valide.
   * @exception InvalidPduIdException L'identifiant du message ne correspond pas à un envoi
   *            précédent.
   */
  private ProtocolDataUnit1APPDto validatePdu(String message)
      throws DeserializationException, InvalidPduIdException {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);
    try {
      ProtocolDataUnit1APPDto pdu = retrievePdu(message);
      verifyMessageId(pdu.getHeader().getId());
      logger.trace(Trace.EXIT_METHOD_1, "pdu", pdu);
      return pdu;
    } catch (JsonProcessingException ex) {
      throw new DeserializationException("Pdu is not as expected.", ex);
    }
  }

  /**
   * @brief @~french Ajout de l'identifiant d'un message à la liste d'envoi
   * @param messageStr Le message
   * @return le numéro du message
   */
  private UUID commitMessageToMemory(String messageStr) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "messageStr", messageStr);
    ProtocolDataUnit1APPDto pdu = retrievePdu(messageStr);
    UUID msgId = pdu.getHeader().getId();
    threadSafeUniqueMessageId.add(msgId);

    logger.trace(Trace.EXIT_METHOD_1, "messageId", msgId);
    return msgId;
  }

  /**
   * @brief @~french Vérifier que l'identifiant de message est la confirmation d'un message
   *        précédemment envoyé
   * @param messageId l'identifiant du message
   * @exception InvalidPduIdException confirmation d'aucun envoi
   */
  private void verifyMessageId(UUID messageId) throws InvalidPduIdException {
    logger.trace(Trace.ENTER_METHOD_1, "messageId", messageId);
    if (threadSafeUniqueMessageId.contains(messageId)) {
      threadSafeUniqueMessageId.remove(messageId);
    } else {
      throw new InvalidPduIdException("Le message est la confirmation d'aucun des envois.");
    }
    logger.trace(Trace.EXIT_METHOD_0);
  }
}
