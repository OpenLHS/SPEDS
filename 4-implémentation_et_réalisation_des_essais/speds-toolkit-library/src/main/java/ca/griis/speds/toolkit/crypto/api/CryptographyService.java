/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'interface CryptographyService.
 * @brief @~english Contains description of CryptographyService interface.
 */

package ca.griis.speds.toolkit.crypto.api;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.speds.toolkit.crypto.api.exception.NoSuchAlgorithmException;
import ca.griis.speds.toolkit.crypto.api.exception.NoSuchCategoryException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
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
 * @brief @~french Service de primitives cryptographiques.
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
 *      2025-11-24 [FO] - Refactorisation majeure<br>
 *      2025-04-09 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface CryptographyService {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Effectuer un chiffrement symétrique.
   * @param spedsLayer La couche qui désire chiffrer symétriquement des données.
   * @param key La clé secrète symétrique à utiliser pour le chiffrement
   * @param data Les données à chiffrer.
   * @return Les données chiffrées à l’aide de la clé secrète fournie.
   */
  byte[] encryptSymmetric(SpedsLayer spedsLayer, SecretKey secretKey, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Effectuer un déchiffrement symétrique.
   * @param spedsLayer La couche qui désire déchiffrer symétriquement des données.
   * @param secretKey La clé secrète symétrique à utiliser pour le déchiffrement.
   * @param encryptedData Les données à déchiffrer.
   * @return Les données déchiffrées à l’aide de la clé secrète fournie.
   */
  byte[] decryptSymmetric(SpedsLayer spedsLayer, SecretKey secretKey, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Générer une clé de chiffrement symétrique .
   * @param spedsLayer La couche qui désire générer une clé de chiffrement symétrique.
   * @return La clé de chiffrement symétrique générée.
   */
  SecretKey generateSymmetricKey(SpedsLayer spedsLayer);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Effectuer un chiffrement asymétrique.
   * @param spedsLayer
   * @param publicKey La clé publique à utiliser pour le chiffrement.
   * @param data Les données à chiffrer.
   * @return Les données chiffrées à l’aide de la clé publique fournie
   */
  byte[] encryptAsymmetric(SpedsLayer spedsLayer, PublicKey publicKey, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Effectuer un déchiffrement asymétrique.
   * @param spedsLayer La couche qui désire déchiffrer asymétriquement des données.
   * @param privateKey La clé privée à utiliser pour le déchiffrement.
   * @param data Les données à déchiffrer.
   * @return Les données déchiffrées à l’aide de la clé privée fournie.
   */
  byte[] decryptAsymmetric(SpedsLayer spedsLayer, PrivateKey privateKey, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Générer une empreinte numérique.
   * @param spedsLayer La couche qui désire créer une empreinte numérique.
   * @param data Les données qui serviront au calcul de l’empreinte numérique
   * @return L’empreinte numérique générée à partir des données
   */
  byte[] hash(SpedsLayer spedsLayer, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Apposer une signature.
   * @param spedsLayer La couche qui désire signer numériquement des données.
   * @param privateKey La clé privée à utiliser pour la signature.
   * @param data Les données à signer.
   * @return La signature numérique générée à partir des données et de la clé privée.
   */
  byte[] sign(SpedsLayer spedsLayer, PrivateKey privateKey, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérification la validité une signature.
   * @param spedsLayer La couche qui désire valider une signature numérique.
   * @param digitalSignature La signature à vérifier.
   * @param publicKey La clé publique à utiliser pour la vérification de la signature.
   * @param data Les données dont on veut vérifier la provenance.
   * @return Le résultat de la vérification de validité de la signature sous forme d’une valeur
   *         booléenne.
   */
  Boolean checkSignatureValidity(SpedsLayer spedsLayer, byte[] digitalSignature,
      PublicKey publicKey, byte[] data);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Choisir une valeur pour la méthode d’échange Diffie-Hellman.
   * @param spedsLayer La couche qui désire choisir une valeur pour la méthode d’échange.
   * @return La valeur générée pour la méthode d’échange.
   */
  KeyPair chooseDiffieHellmanValue(SpedsLayer spedsLayer);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Déterminer la clé secrète pour la méthode d’échange Diffie-Hellman.
   * @param spedsLayer La couche qui désire récupérer la clé secrète en lien avec la méthode
   *        d’échange.
   * @param choiceX La valeur choisie pour la méthode d’échange.
   * @param choiceY La valeur reçue d’un tiers pour la méthode d’échange.
   * @return La clé secrète résultante de la méthode d’échange.
   */
  SecretKey getDiffieHellmanSecretKey(SpedsLayer spedsLayer, KeyPair choiceX, PublicKey choiceY);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupérer l’algorithme disponible pour une catégorie.
   * @param spedsLayer La couche qui désire connaître le nom de l’algorithme disponible pour une
   *        catégorie donnée.
   * @param category La catégorie d’algorithme dans laquelle figure le nom de l’algorithme
   *        disponible.
   * @exception NoSuchCategoryException Erreur soulevée lorsque la catégorie d’algorithme n’est pas
   *            disponible pour la couche fournie.
   * @return Le nom de l’algorithme disponible en version standardisée.
   *
   * @par Tâches
   *      S.O.
   */
  String getAlgorithm(SpedsLayer spedsLayer, AlgorithmCategory category);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupérer les paramètres d’algorithme disponible pour une catégorie.
   * @param spedsLayer La couche qui désire connaître le nom de l’algorithme disponible pour une
   *        catégorie donnée.
   * @param algorithm Le nom de l’algorithme dont on veut connaître les paramètres.
   * @exception NoSuchAlgorithmException Erreur soulevée lorsque l’algorithme fournie n’est pas
   *            disponible dans l’environnement.
   * @return Tableau de correspondance entre le nom des paramètres et leur valeur pour l’algorithme
   *         donné.
   *
   * @par Tâches
   *      S.O.
   */
  Map<String, String> getAlgorithmParameters(SpedsLayer spedsLayer, String algorithm);
}
