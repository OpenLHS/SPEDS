/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe AuthorizationService.
 * @brief @~english «File description»
 */

package ca.griis.speds.session.internal.security.authorization;

import ca.griis.speds.toolkit.project.ProjectService;
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
 * @brief @~french Offre des primitives de service liées aux projets qui ont été authentifiés et
 *        autorisés.
 * @par Détails
 *      <p>
 *      Une instance de projet associée à un identifiant de projet :
 *      - a été préalablement signée et approuvée par toutes les entités qui y participent ;
 *      - définit les entités autorisées à communiquer entre elles dans le cadre d’un identifiant de
 *      projet.
 *      </p>
 * @par Modèle
 *      S.O.
 * @par Conception
 *      Cette interface est injectée via le constructeur par l’entité qui utilise SPEDS et qui
 *      définit le comportement précis de ProjectService.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2026-04-27 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class AuthorizationService {
  private final ProjectService service;

  public AuthorizationService(ProjectService service) {
    this.service = service;
  }

  /**
   * @brief @~french Récupère l'IRI d'une entité.
   * @param projectId L’identifiant du projet en cours.
   * @param code Le code unique de l’entité spécifié dans le projet.
   * @return L'IRI de l’entité inscrite dans le projet.
   *
   * @note [FO] -L’IRI récupérée correspond à une adresse accessible, pouvant par exemple désigner
   *       un service mandataire. Chaque IRI est associée à une instance de projet ayant été
   *       préalablement autorisée.
   * 
   * @par Tâches
   *      S.O.
   */
  public IRI getEntityIri(String projectId, String code) {
    return service.getEntityIri(projectId, code);
  }

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
   * @note [FO] - Vérifie que le code de l’entité est bien rattaché à une instance de projet, et que
   *       la clé publique fournie correspond à celle associée à ce code dans cette instance. Le
   *       projet répertorie l’ensemble des identifiants d’entités qui lui sont liés, ainsi que
   *       leurs clés publiques respectives, préalablement autorisées et approuvées.
   * 
   * @par Tâches
   *      S.O.
   */
  public Boolean verifyEntityLegitimacy(String projectId, String code, PublicKey publicKey) {
    return service.verifyEntityLegitimacy(projectId, code, publicKey);
  }

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
   * @note [FO] - Récupère la clé publique associée à ce code au sein de cette instance de projet.
   *       Le service de projet connaît la stratégie utilisée pour son obtention et garantit que
   *       cette clé appartient bien à l’entité concernée.
   * @par Tâches
   *      S.O.
   */
  public PublicKey getEntityPublicKey(String projectId, String code) {
    return service.getEntityPublicKey(projectId, code);
  }

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
  public Boolean checkProjectActivity(String projectId) {
    return service.checkProjectActivity(projectId);
  }

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
  public Boolean checkPlanActivity(String projectId) {
    return service.checkPlanActivity(projectId);
  }
}
