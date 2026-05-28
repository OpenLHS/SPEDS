/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface ProjectService.
 * @brief @~english Implementation of the ProjectService interface.
 */

package ca.griis.speds.toolkit.project;

import java.security.PublicKey;
import org.apache.jena.iri.IRI;

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
 * @brief @~french Définit le service de projet.
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
 *      2025-11-24 [FO] - Refactorisation majeure de l'interface.<br>
 *      2025-04-09 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface ProjectService {
  /**
   * @brief @~french Récupère l'IRI d'une entité.
   * @param projectId L’identifiant du projet en cours.
   * @param code Le code unique de l’entité spécifié dans le projet.
   * @return L'IRI de l’entité inscrite dans le projet.
   *
   * @par Tâches
   *      S.O.
   */
  IRI getEntityIri(String projectId, String code);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la légitimité d'une entité.
   * @param projectId L’identifiant du projet en cours.
   * @param code Le code unique de l’entité spécifié dans le projet.
   * @param publicKey La clé publique fournie pour attester de la légitimité de l’entité
   * @return Valeur booléenne confirmant la légitimité d’une entité.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean verifyEntityLegitimacy(String projectId, String code, PublicKey publicKey);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère la clé publique d'une entité.
   * @param projectId L’identifiant du projet en cours.
   * @param code Le code unique de l’entité spécifié dans le projet.
   * @return La clé publique utilisée par l’entité pour le projet donné.
   *
   * @par Tâches
   *      S.O.
   */
  PublicKey getEntityPublicKey(String projectId, String code);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérification de l’activité d’un projet.
   * @param projectId L’identifiant du projet en cours.
   * @return Valeur booléenne confirmant le profil actif d’un projet.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean checkProjectActivity(String projectId);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérification de l’activité d’un plan de projet.
   * @param projectId L’identifiant du projet en cours.
   * @return Valeur booléenne confirmant le profil actif d’un plan de projet.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean checkPlanActivity(String projectId);
}
