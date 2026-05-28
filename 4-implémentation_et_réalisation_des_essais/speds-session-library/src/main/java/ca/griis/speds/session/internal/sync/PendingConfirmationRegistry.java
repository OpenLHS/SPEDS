/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe PendingConfirmationRegistry.
 * @brief @~english Implementation of the PendingConfirmationRegistry class.
 */

package ca.griis.speds.session.internal.sync;

import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.domain.ExpandedSessionSidu;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.PendingConfirmation;
import ca.griis.speds.session.internal.domain.PendingMessage;
import java.util.UUID;
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
 * @brief @~french Permet de gérer la réception des messages de confirmation en attente.
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
 *      2026-03-13 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class PendingConfirmationRegistry {
  private final ConcurrentMap<UUID, PendingMessage> pendingResponse;

  public PendingConfirmationRegistry(
      ConcurrentMap<UUID, PendingMessage> pendingConfirmations) {
    this.pendingResponse = pendingConfirmations;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie si un message est attendu.
   * @param expendedSessionIdu Information d'un IDU qu'on vérifie si le mesage est attente.
   * @param type Le type de message attendu.
   * @exception PendingConfirmationException Erreur si le message n'est pas attendu.
   * @return Information concernant le message attendu.
   *
   * @par Tâches
   *      S.O.
   */
  public PendingConfirmation checkMessage(ExpandedSidu expendedSessionIdu, MsgType type)
      throws PendingConfirmationException {
    final var match = pendingResponse.remove(expendedSessionIdu.spdu().getHeader().getId());

    if (match == null) {
      throw new PendingConfirmationException("PendingResponses : Unique ID not found");
    }

    final var confirmation = (PendingConfirmation) match;
    if (confirmation.msgType() != type) {
      throw new PendingConfirmationException("PendingResponses : MsgType is not equivalent");
    }

    return confirmation;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Retire un message en attente.
   * @param expendedSessionIdu Un IDU qu'on veut retirer l'attente.
   *
   * @par Tâches
   *      S.O.
   */
  public void removeMessage(ExpandedSidu expendedSessionIdu) {
    pendingResponse.remove(expendedSessionIdu.spdu().getHeader().getId());
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Ajoute un message en attente.
   * @param expendedSessionIdu Un IDU qu'on veut retirer l'attente.
   * @par Tâches
   *      S.O.
   */
  public void addMessage(ExpandedSessionSidu expendedSessionIdu) {
    final var id = expendedSessionIdu.expandedSidu().spdu().getHeader().getId();
    final var confirm =
        new PendingConfirmation(id, expendedSessionIdu.sessionId(),
            expendedSessionIdu.expandedSidu().msgType());
    pendingResponse.put(id, confirm);
  }
}
