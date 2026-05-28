/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Description de la classe X25519Provider.
 * @brief @~english Implements the X25519Provider class.
 */

package ca.griis.security.internal.keyexchange;

import ca.griis.security.internal.algorithm.KeyAgreementAlgorithm;
import ca.griis.security.internal.asymmetric.generator.X25519KeysGenerator;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyAgreement;

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
 * @brief @~french Fonctionnalités pour la mise en œuvre de l'accord de clé Diffie-Hellman avec
 *        courbe elliptique 25519.
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
 *      2025-12-17 [BD] - Implémentation initiale.<br>
 *
 * @par Tâches
 *      S.O.
 */
public class X25519Provider extends KeyAgreementProvider {
  public X25519Provider() throws NoSuchAlgorithmException {
    super(KeyAgreement.getInstance(KeyAgreementAlgorithm.X25519.getAlgorithm()),
        new X25519KeysGenerator());
  }
}
