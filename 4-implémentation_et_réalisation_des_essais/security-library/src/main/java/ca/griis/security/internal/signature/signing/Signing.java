/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe Signing.
 * @brief @~english Implementation of the class Signing.
 */

package ca.griis.security.internal.signature.signing;

import ca.griis.security.api.domain.DigitalSignature;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.internal.algorithm.SignatureAlgorithm;
import ca.griis.security.internal.random.RandomProvider;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Signature d'une chaine de bits avec un algorithme de signature.
 * @par Détails
 *      Cette classe définit la procédure générique de signature dont les enfants pourront en
 *      spécifier l'algorithme.
 *
 *      Comme cette classe utilise la classe Signature de la JDK et que la documentation de la JDK
 *      n'est pas tout à fait claire au sujet de l'inclusion ou de l'ommission de l'étape de
 *      hachage des données (recommandée dans la littérature), nous précisons que la classe
 *      Signature inclut cette étape jugée cruciale.
 *
 *      Suivent deux blogs qui atteste le fonctionnement souhaité.
 *
 *      https://www.baeldung.com/java-digital-signature
 *      https://www.quickprogrammingtips.com/java/how-to-create-sha256-rsa-signature-using-java.html
 *
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
public abstract class Signing {
  protected final SignatureAlgorithm signatureAlgorithm;
  protected final PrivateKey privateKey;
  protected final AlgorithmParameterSpec params;

  protected Signing(
      SignatureAlgorithm signatureAlgorithm, PrivateKey privateKey, AlgorithmParameterSpec params) {
    this.signatureAlgorithm = signatureAlgorithm;
    this.privateKey = privateKey;
    this.params = params;
  }

  /**
   * @param «parameter name» «Parameter description»
   * @param message message à partir duquel il faut générer une signature
   * @param csprngSpec
   * @return «Return description»
   * @return la signature
   * @throws «exception name» «Exception description»
   * @brief @~english «Description of the function»
   * @brief @~french Crée une signature à partir d'un message.
   * @par Tâches S.O.
   */
  public DigitalSignature sign(byte[] message, CsprngSpec csprngSpec) {
    SecureRandom random = RandomProvider.getSecureRandom(csprngSpec);

    byte[] signedHash;
    try {
      Signature signatureAlgo = Signature.getInstance(signatureAlgorithm.getAlgorithm());
      signatureAlgo.initSign(privateKey, random);

      signatureAlgo.setParameter(params);

      signatureAlgo.update(message);
      signedHash = signatureAlgo.sign();
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException
        | InvalidAlgorithmParameterException e) {
      throw new SecurityException("Unable to sign with " + privateKey.getAlgorithm(), e);
    }

    return new DigitalSignature(signedHash);
  }
}
