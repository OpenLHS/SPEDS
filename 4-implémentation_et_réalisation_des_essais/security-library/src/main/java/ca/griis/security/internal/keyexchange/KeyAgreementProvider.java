/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Description de la classe KeyAgreementProvider.
 * @brief @~english Implements the KeyAgreementProvider class.
 */

package ca.griis.security.internal.keyexchange;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.csprng.StrongCsprngSpec;
import ca.griis.security.internal.asymmetric.generator.AsymKeysGenerator;
import ca.griis.security.internal.random.RandomProvider;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import javax.crypto.KeyAgreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

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
 * @brief @~french Fonctionnalités pour la mise en œuvre de l'accord de clé.
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
public abstract class KeyAgreementProvider {
  private final KeyAgreement keyAgreement;
  private final AsymKeysGenerator keyGenerator;

  public KeyAgreementProvider(KeyAgreement keyAgreement, AsymKeysGenerator keyGenerator) {
    this.keyAgreement = keyAgreement;
    this.keyGenerator = keyGenerator;
  }

  /**
   * @brief @~english «Description of the function»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génère la paire de clés requise par le protocole d'accord de clé.
   * @return la paire de clés
   */
  public KeyPair generateEphemeralKeys() {
    CsprngSpec csprngSpec = new StrongCsprngSpec();
    KeyPair keyPair = keyGenerator.generateKeyPair(csprngSpec);
    return keyPair;
  }

  /**
   * @brief @~english «Description of the function»
   * @param privateKey «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Initialise l'accord de clé.
   * @param privateKey la clé privée de l'entité locale
   * @exception InvalidKeyException le format de la clé est invalide
   */
  public void initializeAgreement(PrivateKey privateKey, CsprngSpec csprngSpec)
      throws InvalidKeyException {
    SecureRandom random = RandomProvider.getSecureRandom(csprngSpec);

    keyAgreement.init(privateKey, random);
  }

  /**
   * @brief @~english «Description of the function»
   * @param secondPk «Parameter description»
   * @exception IllegalStateException «Exception description»
   * @exception InvalidKeyException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Conclut l'accord de clé en produisant la clé secrète.
   * @param secondPk la clé publique de l'entité distante
   * @exception IllegalStateException l'accord de clé n'est pas conclu
   * @exception InvalidKeyException la clé publique de l'entité distante est invalide
   * @return la clé secrète commune aux deux entités
   */
  public byte[] completeAgreement(PublicKey secondPk)
      throws IllegalStateException, InvalidKeyException {
    keyAgreement.doPhase(secondPk, true);

    byte[] sharedKey = keyAgreement.generateSecret();
    return sharedKey;
  }

  /**
   * @brief @~english «Description of the function»
   * @param sharedSecret «Parameter description»
   * @param selfPk «Parameter description»
   * @param secondPk «Parameter description»
   * @exception NoSuchAlgorithmException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Dérive une clé sécuritaire à partir d'une clé secrète partagée et des clés
   *        publiques des deux parties.
   * @param sharedSecret la clé secrète partagée par les deux entités
   * @param selfPk la clé publique de l'entité locale
   * @param secondPk la clé publique de l'entité distante
   * @exception NoSuchAlgorithmException l'algorithme utilisé pour l'empreinte numérique est
   *            invalide
   * @return la valeur de la clé secrète dérivée
   *
   * @note 2020-10-15 [FO] - Favorable d'utiliser une clé dérivée plutôt d'utiliser la clé partagée
   *       pour le chiffrement.
   */
  public byte[] deriveSecure256BitsKey(byte[] sharedSecret, PublicKey selfPk, PublicKey secondPk)
      throws NoSuchAlgorithmException {
    byte[] result = deriveSecureKey(sharedSecret, selfPk, secondPk, 256);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param sharedSecret «Parameter description»
   * @param selfPk «Parameter description»
   * @param secondPk «Parameter description»
   * @exception NoSuchAlgorithmException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Dérive une clé sécuritaire à partir d'une clé secrète partagée et des clés
   *        publiques des deux parties.
   * @param sharedSecret la clé secrète partagée par les deux entités
   * @param selfPk la clé publique de l'entité locale
   * @param secondPk la clé publique de l'entité distante
   * @exception NoSuchAlgorithmException l'algorithme utilisé pour l'empreinte numérique est
   *            invalide
   * @return la valeur de la clé secrète dérivée
   *
   * @note 2020-10-15 [FO] - Favorable d'utiliser une clé dérivée plutôt d'utiliser la clé partagée
   *       pour le chiffrement.
   */
  public byte[] deriveSecure128BitsKey(byte[] sharedSecret, PublicKey selfPk, PublicKey secondPk)
      throws NoSuchAlgorithmException {
    byte[] result = deriveSecureKey(sharedSecret, selfPk, secondPk, 128);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Dérive une clé sécuritaire à partir d'une clé secrète partagée et des clés
   *        publiques des deux parties.
   * @param sharedSecret la clé secrète partagée par les deux entités
   * @param selfPk la clé publique de l'entité locale
   * @param secondPk la clé publique de l'entité distante
   * @param keySizeBits Longueur de la clé.
   * @exception IllegalArgumentException Longueur de clé incorrecte.
   * @return la valeur de la clé secrète dérivée
   *
   * @par Tâches
   *      S.O.
   */
  private byte[] deriveSecureKey(byte[] sharedSecret, PublicKey selfPk, PublicKey secondPk,
      Integer keySizeBits) {
    if (keySizeBits % 8 != 0) {
      throw new IllegalArgumentException("Key size must be a multiple of 8");
    }

    final var keySizeBytes = keySizeBits / 8;
    final List<byte[]> keys = Arrays.asList(selfPk.getEncoded(), secondPk.getEncoded());
    keys.sort(Arrays::compare);

    final HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());

    final byte[] x = keys.get(0);
    final byte[] y = keys.get(1);
    byte[] info = new byte[x.length + y.length];
    System.arraycopy(x, 0, info, 0, x.length);
    System.arraycopy(y, 0, info, x.length, y.length);

    final HKDFParameters params = new HKDFParameters(sharedSecret, null, info);
    hkdf.init(params);

    byte[] derivedKey = new byte[keySizeBytes];
    hkdf.generateBytes(derivedKey, 0, derivedKey.length);

    return derivedKey;
  }
}
