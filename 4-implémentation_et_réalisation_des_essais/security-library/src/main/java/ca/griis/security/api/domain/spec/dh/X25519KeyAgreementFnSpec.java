/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe X25519KeyAgreementFnSpec.
 * @brief @~english Implementation of the class X25519KeyAgreementFnSpec.
 */

package ca.griis.security.api.domain.spec.dh;

import ca.griis.security.api.domain.spec.derivation.HkdfSpec;
import java.util.Map;

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
 * @brief @~french Définit la specification de l'accord de clé X25519.
 * @par Details
 *      <p>
 *      Concernant l’algorithme de dérivation de clé (HKDF), l'entrée de la fonction est la clé
 *      partagée issue de l’échange Diffie-Hellman et aucun sel est paramètré.
 *      Aussi, le paramètre (« info ») de la fonction est composé de deux clés publiques : celle de
 *      l’entité réalisant la dérivation et celle de l’autre partie, qui doit également calculer la
 *      clé.
 *      </p>
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      «2025-12-17» [BD] - Implémentation initiale
 * @par Tâches
 *      S.O.
 */
public class X25519KeyAgreementFnSpec extends KeyAgreementFnSpec {
  private final Integer keyBitLength;
  private final String hashAlgo;
  private final HkdfSpec hkdfSpec;

  public X25519KeyAgreementFnSpec(Integer keyBitLength) {
    super("X25519");

    this.keyBitLength = keyBitLength;
    this.hashAlgo = "SHA-256";
    this.hkdfSpec = new HkdfSpec(hashAlgo);
  }

  public String getHashAlgo() {
    return hashAlgo;
  }

  public Integer getKeyBitLength() {
    return keyBitLength;
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of("keyBitLength", String.valueOf(keyBitLength), "hmac", hashAlgo, "hkdfSpec",
        hkdfSpec.toString());
  }
}
