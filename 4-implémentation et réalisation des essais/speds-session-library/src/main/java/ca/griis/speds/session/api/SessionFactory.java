/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface SessionFactory.
 * @brief @~english Implementation of the SessionFactory interface.
 */

package ca.griis.speds.session.api;

import ca.griis.speds.session.api.exception.ParameterException;
import ca.griis.speds.transport.api.TransportHost;

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
 * @brief @~french Définit une fabriques d'entités nécessaires à la couche session.
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
 *      2025-02-18 [MD] - Retirer PgaService; il sera dans le constructeur
 *      2025-02-10 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface SessionFactory {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   *
   * @brief @~french Construit un hôte de la couche session.
   * @param parameters Les paramètres de l'hôte de la couche session sous le format JSON.
   * @exception ParameterException Erreur lors de la récupération des paramètres d'initialisation.
   *
   * @par Tâches
   *      S.O.
   */
  SessionHost init(String parameters) throws ParameterException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Construit un service hôte de la couche transport.
   * @param parameters Les paramètres de l'hôte de la couche transport sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  TransportHost initTransportHost(String parameters);
}
