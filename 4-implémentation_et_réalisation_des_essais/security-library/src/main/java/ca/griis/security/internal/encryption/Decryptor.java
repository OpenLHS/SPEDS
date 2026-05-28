/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe Decryptor.
 * @brief @~english Declaration of the Decryptor class.
 */

package ca.griis.security.internal.encryption;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.exception.DecryptException;
import ca.griis.security.internal.algorithm.CipherAlgorithm;
import ca.griis.security.internal.random.RandomProvider;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Définit les services d'un déchiffreur.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 * @par Historique
 *      2025-12-17 [BD] - Description de la classe.
 * @par Tâches S.O.
 *      S.O.
 */
public abstract class Decryptor {
  protected final CipherAlgorithm cipherAlgorithm;
  protected final Key key;

  protected Decryptor(CipherAlgorithm cipherAlgorithm, Key key) {
    this.cipherAlgorithm = cipherAlgorithm;
    this.key = key;
  }

  /**
   * @param «parameter name» «Parameter description»
   * @param encryptedMessage le message chiffré
   * @param csprngSpec
   * @return «Return description»
   * @return un message déchiffré
   * @throws «exception name» «Exception description»
   * @brief @~english «Description of the function»
   * @brief @~french Déchiffre un message.
   * @par Tâches
   *      S.O.
   */
  public abstract byte[] decrypt(byte[] encryptedMessage, CsprngSpec csprngSpec);

  /**
   * @param «parameter name» «Parameter description»
   * @param encryptedMessage le message chiffré
   * @param algorithmParameterSpec la spécification de l'algorithme
   * @param csprngSpec
   * @return «Return description»
   * @return un message déchiffré
   * @throws «exception name» «Exception description»
   * @brief @~english «Description of the function»
   * @brief @~french Déchiffre un message.
   * @par Tâches
   *      S.O.
   */
  protected byte[] decrypt(byte[] encryptedMessage, AlgorithmParameterSpec algorithmParameterSpec,
      CsprngSpec csprngSpec) {
    SecureRandom random = RandomProvider.getSecureRandom(csprngSpec);

    byte[] message;
    try {
      Cipher cipherAlgo = Cipher.getInstance(cipherAlgorithm.getAlgorithm());
      cipherAlgo.init(Cipher.DECRYPT_MODE, key, algorithmParameterSpec, random);
      message = cipherAlgo.doFinal(encryptedMessage);
    } catch (InvalidKeyException
        | NoSuchAlgorithmException
        | NoSuchPaddingException
        | IllegalBlockSizeException
        | BadPaddingException
        | InvalidAlgorithmParameterException e) {
      throw new DecryptException("Unable to decrypt " + key.getAlgorithm(), e);
    }

    return message;
  }
}
