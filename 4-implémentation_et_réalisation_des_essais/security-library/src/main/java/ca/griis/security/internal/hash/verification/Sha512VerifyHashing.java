/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe Sha512VerifyHashing.
 * @brief @~english Implementation of the Sha512VerifyHashing class.
 */

package ca.griis.security.internal.hash.verification;

import ca.griis.security.internal.hash.hashing.Sha512Hashing;

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
 * @brief @~french Vérification qu'une empreinte numérique correspond à une entrée à l'aide
 *        d'une fonction de hachage SHA-512.
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
 *      2025-12-17 [BD] - Implémentation initiale.
 *
 * @par Tâches
 *      S.O.
 */
public class Sha512VerifyHashing extends VerifyHashing {
  public Sha512VerifyHashing() {
    super(new Sha512Hashing());
  }
}
