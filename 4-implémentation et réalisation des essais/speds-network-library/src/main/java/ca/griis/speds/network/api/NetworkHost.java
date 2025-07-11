/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface NetworkHost.
 * @brief @~english Implementation of the NetworkHost interface.
 */

package ca.griis.speds.network.api;


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
 * @brief @~french Définit les services d'un hôte de la couche réseau.
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
public interface NetworkHost {
  /**
   * @brief @~english «Description of the function»
   *
   * @brief @~french Désalloue les ressources.
   *
   * @par Tâches
   *      S.O.
   */
  void close();

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Demande d’échanger des données (request).
   * @param idu L’interface d’unité de données de la couche supérieure et la couche réseau sous le
   *        format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  void request(String idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Demande d’échanger des données (response).
   * @param idu L’interface d’unité de données de la couche supérieure et la couche réseau sous le
   *        format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  void response(String idu);

  /**
   * @brief @~english «Description of the function»
   *
   * @brief @~french Confirme l’échange de données.
   *
   * @par Tâches
   *      S.O.
   */
  void confirm();

  /**
   * @brief @~english «Description of the function»
   * @return «Return description»
   *
   * @brief @~french Répond à une demande de requête de données reçue par l’initiateur de requête et
   *        rapporte (indique) les données à l’hôte.
   * @return L’interface d’unité de données de la couche supérieure et la couche réseau sous le
   *         format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  String indication();
}
