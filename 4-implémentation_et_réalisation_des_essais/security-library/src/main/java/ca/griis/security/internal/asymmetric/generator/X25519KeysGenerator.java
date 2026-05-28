/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe X25519KeysGenerator.
 * @brief @~english Declaration of the X25519KeysGenerator class.
 */

package ca.griis.security.internal.asymmetric.generator;

import ca.griis.security.internal.algorithm.KeyPairGeneratorAlgorithm;
import java.security.spec.NamedParameterSpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Générateur d'une paire de clés asymétriques pour d'accord de clé Diffie-Hellman
 *        avec la courbe elliptique 25519.
 * @par Détails
 *      <p>
 *      Cette courbe elliptique est considérée à part dans les courbes elliptiques de Java.
 *
 *      Voir KeyPairGenerator et ParameterSpec à la page:
 *      https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html
 *      </p>
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 * @par Historique
 *      2025-12-17 [BD] - Implémentation de la classe.
 * @par Tâches
 *      S.O.
 */
public class X25519KeysGenerator extends AsymKeysGenerator {
  public X25519KeysGenerator() {
    super(KeyPairGeneratorAlgorithm.X25519, NamedParameterSpec.X25519);
  }
}
