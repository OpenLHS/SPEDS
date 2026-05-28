/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'interface SecurityService.
 * @brief @~english Implements the SecurityService interface.
 */

package ca.griis.security.api;

import ca.griis.security.api.domain.Digest;
import ca.griis.security.api.domain.DigitalSignature;
import ca.griis.security.api.domain.SecurityProfile;
import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.certificate.CertificateVerifSpec;
import ca.griis.security.api.domain.spec.certificate.Hostname;
import ca.griis.security.api.domain.spec.certificate.IntermediateCertificates;
import ca.griis.security.api.domain.spec.certificate.RootCertificates;
import ca.griis.security.api.domain.spec.certificate.usage.CertificateKeyUsages;
import ca.griis.security.api.domain.spec.cipher.asym.AsymCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.SymCipherSpec;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.dh.KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.generator.asym.AsymKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.SymKeyGenSpec;
import ca.griis.security.api.domain.spec.hash.HashFnSpec;
import ca.griis.security.api.domain.spec.sign.SignatureFnSpec;
import ca.griis.security.api.exception.DecryptException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.crypto.SecretKey;

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
 * @brief @~french Définit le service de sécurité.
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
 *      «2025-12-17» [BD] - Implémentation initiale
 * @par Tâches
 *      S.O.
 */
public interface SecurityService {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère aux spécifications de sécurité avec leur identifiant.
   * @param securityProfile Le profil de sécurité.
   * @return Les spécifications de sécurité avec leur identifiant.
   *
   * @par Tâches
   *      S.O.
   */
  Map<String, SecuritySpec> getProfilSecuritySpecs(SecurityProfile securityProfile);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génére une empreinte numérique.
   * @param hashFnSpec Spécification de la fonction de hachage,
   * @param data Données utilisées pour calculer l’empreinte numérique.
   * @return Une empreinte numérique.
   *
   * @par Tâches
   *      S.O.
   */
  Digest generateDigest(HashFnSpec hashFnSpec, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie une empreinte numérique.
   * @param hashFnSpec Spécification de la fonction de hachage.
   * @param digest L’empreinte numérique à vérifier.
   * @param data Données utilisées pour vérifier l’empreinte numérique.
   * @return Le résultat de la vérification sous forme booléenne.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean verifyDigest(HashFnSpec hashFnSpec, Digest digest, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génère une clé symétrique.
   * @param csprngSpec Spécification du générateur de valeurs aléatoires.
   * @param symKeyGenSpec Spécification du générateur de clé symétrique.
   * @return La clé secrète générée.
   *
   * @par Tâches
   *      S.O.
   */
  SecretKey generateSecretKey(CsprngSpec csprngSpec, SymKeyGenSpec symKeyGenSpec);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Chiffre des données symétriquement.
   * @param csprngSpec Spécification du générateur de valeurs aléatoires.
   * @param symCipherSpec Spécification du chiffreur symétrique.
   * @param secretKey Clé secrète.
   * @param data Données à chiffrer.
   * @return Les données chiffrées.
   *
   * @par Tâches
   *      S.O.
   */
  byte[] symEncrypt(CsprngSpec csprngSpec, SymCipherSpec symCipherSpec, SecretKey secretKey,
      byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Déhiffre des données symétriquement.
   * @param csprngSpec Spécification du générateur de valeurs aléatoires.
   * @param symCipherSpec Spécification du déchiffreur symétrique.
   * @param secretKey Clé secrète.
   * @param encryptedData Données à déchiffrer.
   * @return Les données chiffrées.
   * @exception DecryptException Erreur lors du déchiffrement.
   * @return Les données déchiffrées.
   *
   * @par Tâches
   *      S.O.
   */
  byte[] symDecrypt(CsprngSpec csprngSpec, SymCipherSpec symCipherSpec, SecretKey secretKey,
      byte[] encryptedData);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french «Description de la fonction»
   * @param csprngSpec Spécification du générateur de valeurs aléatoires.
   * @param asymKeyPairGenSpec Spécification du générateur de paire de clés asymétrique.
   * @return La paire de clés générée.
   *
   * @par Tâches
   *      S.O.
   */
  KeyPair generateKeyPair(CsprngSpec csprngSpec, AsymKeyPairGenSpec asymKeyPairGenSpec);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Chiffre des données asymétriquement.
   * @param csprngSpec Spécification du générateur de valeurs aléatoires.
   * @param asymCipherSpec Spécification du chiffreur asymétrique.
   * @param publicKey Clé publique.
   * @param data Données à chiffrer.
   * @return Les données chiffrées.
   *
   * @par Tâches
   *      S.O.
   */
  byte[] asymEncrypt(CsprngSpec csprngSpec, AsymCipherSpec asymCipherSpec, PublicKey publicKey,
      byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Déhiffre des données asymétriquement.
   * @param csprngSpec Spécification du générateur de valeurs aléatoires.
   * @param asymCipherSpec Spécification du déchiffreur asymétrique.
   * @param privateKey Clé privée.
   * @param encryptedData Données chiffrées.
   * @exception DecryptException Erreur lors du déchiffrement.
   * @return Les données déchiffrées.
   *
   * @par Tâches
   *      S.O.
   */
  byte[] asymDecrypt(CsprngSpec csprngSpec, AsymCipherSpec asymCipherSpec, PrivateKey privateKey,
      byte[] encryptedData);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french «Description de la fonction»
   * @param csprngSpec Spécification du générateur de valeurs aléatoires.
   * @param keyAgreementFnSpec Spécification de la fonction d’échange de clé.
   * @param originatorKeyPair La paire de clés de l’initiateur de l’accord de clé.
   * @param recipientPublicKey La clé publique de l’autre partie à l’accord de clé.
   * @return La clé secrète commune négociée.
   *
   * @par Tâches
   *      S.O.
   */
  SecretKey generateSharedSecretKey(CsprngSpec csprngSpec, KeyAgreementFnSpec keyAgreementFnSpec,
      KeyPair originatorKeyPair, PublicKey recipientPublicKey);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génère une signature numérique.
   * @param csprngSpec Spécification du générateur de valeurs aléatoires.
   * @param signatureFnSpec Spécification de la fonction de signature numérique.
   * @param privateKey Clé privée.
   * @param data Données à signer.
   * @return Une signature numérique.
   *
   * @par Tâches
   *      S.O.
   */
  DigitalSignature generateSignature(CsprngSpec csprngSpec, SignatureFnSpec signatureFnSpec,
      PrivateKey privateKey, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie une signature numérique.
   * @param signatureFnSpec Spécification de la fonction de signature numérique.
   * @param publicKey Clé publique.
   * @param signedData Données signées
   * @param signature Signature numérique.
   * @return Le résultat de la vérification sous forme booléenne.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean verifySignature(SignatureFnSpec signatureFnSpec, PublicKey publicKey, byte[] signedData,
      DigitalSignature signature);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la validité temporelle d’un certificat.
   * @param certificate Certificat numérique à vérifier.
   * @return Le résultat de la vérification sous forme booléenne.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean verifyCertificateTemporalValidity(X509Certificate certificate);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la signature numérique d’un certificat.
   * @param certificate Certificat numérique à vérifier.
   * @param publicKey Clé publique du certificat qui a signé le certificat.
   * @return Le résultat de la vérification sous forme booléenne.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean verifyCertificateSignature(X509Certificate certificate, PublicKey publicKey);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la chaîne de certification.
   * @param certificateVerifSpec Spécification du vérificateur de certificat.
   * @param rootCertificates Certificats numériques racines.
   * @param intermidiateCertificates Certificats numériques intermédiaires.
   * @param certificate Certificat numérique à vérifier.
   * @return Le résultat de la vérification sous forme booléenne.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean verifyCertificateChain(CertificateVerifSpec certificateVerifSpec,
      RootCertificates rootCertificates, IntermediateCertificates intermidiateCertificates,
      X509Certificate certificate);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie les extensions du certificat.
   * @param certificate Certificat numérique à vérifier.
   * @param hostname Nom d’hôte du serveur à vérifier.
   * @param usages Les usages qui doivent être présent dans le certificat.
   * @return Le résultat de la vérification sous forme booléenne.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean verifyCertificateExtensions(X509Certificate certificate, Hostname hostname,
      CertificateKeyUsages usages);
}
