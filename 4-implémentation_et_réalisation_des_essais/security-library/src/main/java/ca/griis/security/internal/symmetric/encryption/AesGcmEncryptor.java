/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe AesGcmEncryptor.
 * @brief @~english Declaration of the AesGcmEncryptor class.
 */

package ca.griis.security.internal.symmetric.encryption;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.internal.algorithm.CipherAlgorithm;
import ca.griis.security.internal.encryption.Encryptor;
import ca.griis.security.internal.random.RandomProvider;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Chiffreur symétrique basé sur l'algorithme de chiffrement AES avec le mode
 *        d'opération GCM.
 * @par Détails
 *      S.O.
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
public class AesGcmEncryptor extends Encryptor {
  private static final Integer gcmTagBitSize = 128;
  private static final Integer gcmIvLength = 96;

  public AesGcmEncryptor(SecretKey key) {
    super(CipherAlgorithm.AESGCM, key);
  }

  @Override
  public byte[] encrypt(byte[] message, CsprngSpec csprngSpec) {
    byte[] iv = createRandomIv(csprngSpec);
    GCMParameterSpec parameterSpec = new GCMParameterSpec(gcmTagBitSize, iv);

    byte[] encrypted = encrypt(message, parameterSpec, csprngSpec);

    byte[] encryptedWithIv = assembleEncryptedWithIv(iv, encrypted);
    return encryptedWithIv;
  }

  private static byte[] assembleEncryptedWithIv(byte[] iv, byte[] encrypted) {
    byte[] encryptedWithIv =
        ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
    return encryptedWithIv;
  }

  private static byte[] createRandomIv(CsprngSpec csprngSpec) {
    byte[] iv = new byte[gcmIvLength / 8];

    SecureRandom random = RandomProvider.getSecureRandom(csprngSpec);

    random.nextBytes(iv);
    return iv;
  }
}
