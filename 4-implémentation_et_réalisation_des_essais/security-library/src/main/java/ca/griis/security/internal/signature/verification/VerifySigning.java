/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe VerifySigning.
 * @brief @~english Implementation of the class VerifySigning.
 */

package ca.griis.security.internal.signature.verification;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.security.api.domain.DigitalSignature;
import ca.griis.security.internal.algorithm.SignatureAlgorithm;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
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
 * @brief @~french Vérification de la signature d'une chaine de bits à l'aide d'un algorithme de
 *        chiffrement.
 * @par Détails
 *      Cette classe définit la procédure générique de vérification de signature dont les
 *      enfants pourront spécifier l'algorithme.
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
public abstract class VerifySigning {
  private static final GriisLogger logger = getLogger(VerifySigning.class);
  protected final SignatureAlgorithm signatureAlgorithm;
  protected final PublicKey publicKey;
  protected final AlgorithmParameterSpec params;

  protected VerifySigning(SignatureAlgorithm signatureAlgorithm, PublicKey publicKey,
      AlgorithmParameterSpec params) {
    this.signatureAlgorithm = signatureAlgorithm;
    this.publicKey = publicKey;
    this.params = params;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la signature.
   * @param message le message dont on veut vérifier la signature
   * @param signature la signature à valider
   * @return la valeur <code>true</code> si la signature est valide, la valeur <code>false</code>
   *         sinon
   * @par Tâches
   *      S.O.
   */
  public Boolean verify(byte[] message, DigitalSignature signature) {
    Boolean verified = false;
    try {
      Signature signatureAlgo = Signature.getInstance(signatureAlgorithm.getAlgorithm());
      signatureAlgo.initVerify(publicKey);

      signatureAlgo.setParameter(params);

      signatureAlgo.update(message);
      verified = signatureAlgo.verify(signature.bytes());
    } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
        | InvalidAlgorithmParameterException e) {
      logger.error(Error.GENERIC_ERROR, e);
    }

    return verified;
  }
}
