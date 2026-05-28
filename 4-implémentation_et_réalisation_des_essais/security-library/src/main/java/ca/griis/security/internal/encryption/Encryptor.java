/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe Encryptor.
 * @brief @~english Declaration of the Encryptor class.
 */

package ca.griis.security.internal.encryption;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
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
 * @brief @~french Définition des services d'un chiffreur.
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
 * @par Tâches
 *      S.O.
 */
public abstract class Encryptor {
  protected final CipherAlgorithm cipherAlgorithm;
  protected final Key key;

  protected Encryptor(CipherAlgorithm cipherAlgorithm, Key key) {
    this.cipherAlgorithm = cipherAlgorithm;
    this.key = key;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   * @brief @~french Chiffre un message.
   * @param message le message à chiffrer
   * @return un message chiffré
   * @par Tâches
   *      S.O.
   */
  public abstract byte[] encrypt(byte[] message, CsprngSpec csprngSpec);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Chiffre un message.
   * @param message le message à chiffrer
   * @param algorithmParameterSpec la spécification de l'algorithme
   * @return un message chiffré
   * @par Tâches
   *      S.O.
   */
  protected byte[] encrypt(byte[] message, AlgorithmParameterSpec algorithmParameterSpec,
      CsprngSpec csprngSpec) {
    SecureRandom random = RandomProvider.getSecureRandom(csprngSpec);

    byte[] encrypted;
    try {
      Cipher cipherAlgo = Cipher.getInstance(cipherAlgorithm.getAlgorithm());
      cipherAlgo.init(Cipher.ENCRYPT_MODE, key, algorithmParameterSpec, random);
      encrypted = cipherAlgo.doFinal(message);
    } catch (InvalidKeyException
        | IllegalBlockSizeException
        | BadPaddingException
        | NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidAlgorithmParameterException e) {
      throw new SecurityException("Unable to encrypt the message", e);
    }

    return encrypted;
  }
}
