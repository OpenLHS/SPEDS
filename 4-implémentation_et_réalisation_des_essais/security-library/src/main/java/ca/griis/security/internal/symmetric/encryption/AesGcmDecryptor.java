/**
 * @file
 * @copyright @@GRIIS_COPYRIGHT@@
 * @licence @@GRIIS_LICENCE@@
 * @version @@GRIIS_VERSION@@
 * @brief @~french Contient la description de la classe AesGcmDecryptor.
 * @brief @~english Declaration of the Encryptor AesGcmDecryptor.
 */

package ca.griis.security.internal.symmetric.encryption;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.exception.DecryptException;
import ca.griis.security.internal.algorithm.CipherAlgorithm;
import ca.griis.security.internal.encryption.Decryptor;
import java.nio.ByteBuffer;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Déchiffreur symétrique basé sur l'algorithme de chiffrement AES avec le mode
 *        d'opération GCM.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      La méthode decrypt doit recevoir en entrée un tableau suffisamment grand sans quoi un
 *      BufferOverFlow </\br>
 *      va être déclenché. C'est parce que selon la clef générée, une certaine longueur de tableau
 *      </\br>
 *      est réservée par la clef et donc une taille minimum est attendue.
 * @par Historique
 *      2025-12-17 [BD] - Implémentation de la classe.
 * @par Tâches S.O.
 */
public class AesGcmDecryptor extends Decryptor {
  private static final Integer gcmTagBitSize = 128;
  private static final Integer gcmIvLength = 96;

  public AesGcmDecryptor(SecretKey key) {
    super(CipherAlgorithm.AESGCM, key);
  }

  @Override
  public byte[] decrypt(byte[] encryptedMessage, CsprngSpec csprngSpec) {
    ByteBuffer wrappedEncryptedMsg = ByteBuffer.wrap(encryptedMessage);

    byte[] iv = new byte[gcmIvLength / 8];

    try {
      wrappedEncryptedMsg.get(iv);
    } catch (java.nio.BufferUnderflowException e) {
      throw new DecryptException("Unable to decrypt " + key.getAlgorithm(), e);
    }

    byte[] encryptedPart = new byte[wrappedEncryptedMsg.remaining()];
    wrappedEncryptedMsg.get(encryptedPart);

    GCMParameterSpec parameterSpec = new GCMParameterSpec(gcmTagBitSize, iv);

    return decrypt(encryptedPart, parameterSpec, csprngSpec);
  }
}
