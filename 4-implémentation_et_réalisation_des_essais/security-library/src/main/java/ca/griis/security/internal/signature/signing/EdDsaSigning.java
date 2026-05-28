/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe EdDsaSigning.
 * @brief @~english Implementation of the class EdDsaSigning.
 */

package ca.griis.security.internal.signature.signing;

import ca.griis.security.internal.algorithm.SignatureAlgorithm;
import java.security.PrivateKey;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Signature d'une chaine de bits avec l'algorithme de signature EdDSA.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 * @par Historique
 *      2025-12-17 [BD] - Création de la classe.
 * @par Tâches
 *      S.O.
 */
public class EdDsaSigning extends Signing {
  public EdDsaSigning(PrivateKey privateKey) {
    super(SignatureAlgorithm.ed25519, privateKey, null);
  }
}
