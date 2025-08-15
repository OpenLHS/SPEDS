/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface TransportClient.
 * @brief @~english Implementation of the TransportClient interface.
 */

package ca.griis.speds.transport.api;


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
 * @brief @~french Offre les services d'un client de la couche transport.
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
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface TransportClient {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Demande d’échanger des données.
   * @param idu L’interface d’unité de données entre la couche supérieure et la couche transport
   *        sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  void request(String idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Demande d’échanger des données.
   * @param idu L’interface d’unité de données entre la couche supérieure et la couche transport
   *        sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  @Deprecated
  void dataRequest(String idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Confirme l’échange de données.
   * @return L’interface d’unité de données entre la couche supérieure et la couche transport sous
   *         le format JSON.
   *
   * @par Tâches
   * @note - La méthode sera retirée, car son utilité n'y est plus. La confirmation est maintenant
   *       gérer automatiquement.
   */
  @Deprecated
  void dataConfirm();

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie si la demande d’échange de données a fait l’objet d’une confirmation.
   * @param msgId Identifiant du message de la couche transport à vérifier.
   * @return Vrai si la confirmation n'a pas été encore effectuée.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean isPending(String msgId);
}
