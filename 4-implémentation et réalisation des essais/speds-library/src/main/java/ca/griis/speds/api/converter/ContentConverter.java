/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'interface ContentToSpedsIduConverter.
 * @brief @~english Contains description of ContentToSpedsIduConverter interface.
 */

package ca.griis.speds.api.converter;

import ca.griis.js2p.gen.speds.application.api.dto.CommandeEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.CommandeReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.DataEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.DataReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.ExceptionEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.ExceptionReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.PgaConfirmationDto;
import ca.griis.js2p.gen.speds.application.api.dto.PgaFinDto;
import ca.griis.js2p.gen.speds.application.api.dto.StatutEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.StatutReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheReceptionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;

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
 * @brief @~french Définit un convertisseur de contenu de message du protocole SPEDS.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-04-07 [FO] - Première ébauche.<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface ContentConverter<T> {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de tâches - Convertit l'envoi de tâche en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content L'envoi de tâche.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      TacheEnvoiDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de tâches - Convertit la réception de tâche en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content La réception de tâche.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      TacheReceptionDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole de fin de tâches - Convertit l'envoi de fin de tâche en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content L'envoi de fin de tâche.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      FinEnvoiDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole de fin de tâches - Convertit la réception de tâche en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content La réception de tâche.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      FinReceptionDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'exception de tâches - Convertit l'envoi d'exception en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content L'envoi d'exception.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      ExceptionEnvoiDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'exception de tâches - Convertit la réception d'exception en une
   *        unité de travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content La réception d'exception.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      ExceptionReceptionDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de données - Convertit l'envoi de données en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content L'envoi de données.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      DataEnvoiDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de données - Convertit la réception de données en une unité
   *        de travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content La réception de données.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      DataReceptionDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de statuts - Convertit l'envoi de statut en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content L'envoi de statut.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      StatutEnvoiDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de statuts - Convertit la réception de statut en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content La réception de statut.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      StatutReceptionDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de commandes - Convertit l'envoi de commande en une unité de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content L'envoi de commande.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      CommandeEnvoiDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de commandes - Convertit la réception de commande en une
   *        unité de travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content La réception de commande.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      CommandeReceptionDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de fin de PGA - Convertit un envoi de fin de PGA en une unité
   *        de
   *        travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content L'envoi de fin de PGA.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      PgaFinDto content) throws JsonProcessingException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de fin de PGA - Convertit une confirmation de fin PGA en une
   *        unité de travail d'interface.
   * @param pgaId L'identifiant du PGA.
   * @param sourceCode L'identifiant de la source.
   * @param destinationCode L'identifiant de la destination.
   * @param msgId L'identifiant du message.
   * @param content Une confirmation de fin PGA.
   * @return L'unité de travail d'interface.
   *
   * @par Tâches
   *      S.O.
   */
  T convert(String pgaId, String sourceCode, String destinationCode, UUID msgId,
      PgaConfirmationDto content) throws JsonProcessingException;
}
