/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SentMessageIdSet.java
 * @brief @~english Contains description of SentMessageIdSet.java class.
 */

package ca.griis.speds.network.service.host;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
 * @brief @~french Modélise l'état pour la table des identifiants de message en attente
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
 *      2025-06-26 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class SentMessageIdSet {

  private final Set<String> sentMessageIds;

  public SentMessageIdSet() {
    this.sentMessageIds = ConcurrentHashMap.newKeySet();
  }

  /**
   * @brief @~french Ajouter un nouvel identifiant de message
   * @param messageId l'identifiant de message
   * @return si le message a bien été ajouté à la liste
   */
  public boolean addMessageId(String messageId) {
    return sentMessageIds.add(messageId);
  }

  /**
   * @brief @~french Vérifier si le message est dans la liste
   * @param messageId l'identifiant de message
   * @return retourne vrai si le message est dans la liste
   */
  public boolean containsMessageId(String messageId) {
    return sentMessageIds.contains(messageId);
  }

  /**
   * @brief @~french Retirer le message de la liste
   * @param messageId l'identifiant du message
   * @return si le message a bien été retiré s'il était présent
   */
  public boolean removeMessageId(String messageId) {
    return sentMessageIds.remove(messageId);
  }

  /**
   * @brief @~french Retire tout message présent dans la liste
   */
  public void clearMessageIds() {
    sentMessageIds.clear();
  }
}
