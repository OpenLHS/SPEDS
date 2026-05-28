/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'interface CryptographyFactory.
 * @brief @~english Contains description of CryptographyFactory interface.
 */

package ca.griis.speds.toolkit.crypto.api;

import com.fasterxml.jackson.core.JsonProcessingException;

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
 * @brief @~french Définit une fabrique pour construire un service cryptographique.
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
 *      2025-11-28 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface CryptographyFactory {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construit un service cryptographique.
   * @param configJson La suite cryptographique comprenant tous les algorithmes cryptographiques à
   *        utiliser pour SPEDS.
   * @exception JsonProcessingException En cas d'erreur de convertissement de la config JSON.
   * @return Un service de cryptographie.
   */
  CryptographyService initCipherSuite(String configJson) throws JsonProcessingException;
}
