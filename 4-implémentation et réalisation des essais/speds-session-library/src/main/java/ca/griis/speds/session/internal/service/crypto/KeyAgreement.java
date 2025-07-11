/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe KeyAgreementManager.java
 * @brief @~english Contains description of KeyAgreementManager.java class.
 */

package ca.griis.speds.session.internal.service.crypto;

import ca.griis.cryptography.keyexchange.KeyAgreementProvider;
import ca.griis.cryptography.keyexchange.X25519Provider;
import ca.griis.speds.session.api.exception.KeyAgreementException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

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
 * @brief @~french Service de gestion de l'entente sur la clé de session SKAK utilisant
 *        Diffie-Hellman avec une courbe elliptique
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
 *      2025-03-20 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class KeyAgreement {

  private final KeyAgreementProvider keyAgreementProvider;

  public KeyAgreement() {
    try {
      this.keyAgreementProvider = new X25519Provider();
    } catch (NoSuchAlgorithmException e) {
      throw new KeyAgreementException("Could not generate key choice", e);
    }
  }

  /**
   * @brief @~english «Description of the function»
   * @return «Return description»
   *
   * @brief @~french Génère un choix de point sur une courbe elliptique servant à négocier un
   *        accord de clé secrète
   * @return Une paire de clé privée et publique correspondant au choix d'un point sur une courbe
   *         elliptique
   *
   * @par Tâches
   *      S.O.
   */
  public KeyPair generateChoicePointKey() {
    final KeyPair keyPair = keyAgreementProvider.generateEphemeralKeys();
    return keyPair;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Complète la négotiation d'accord de clé secrète
   * @param sourceChoice La paire de clé privée et publique correspondant au choix de l'entité
   *        source sur la courbe elliptique pour la négotiation d'accord de clé secrète
   * @param destinationChoice La clé publique correspondant au choix de l'entité de destination sur
   *        la courbe elliptique pour la négotiation d'accord de clé secrète
   * @exception KeyAgreementException Erreur soulevée lors de la complétion de l'accord de clé
   *            secrète à partir des points choisis par les deux parties
   * @return La clé secrète partagée entre les deux parties sous forme de tableau d'octets.
   *
   * @par Tâches
   *      S.O.
   */
  public byte[] completeKeyAgreementNegotiation(KeyPair sourceChoice, PublicKey destinationChoice) {
    try {
      keyAgreementProvider.initializeAgreement(sourceChoice.getPrivate());
      byte[] sharedSecretKey = keyAgreementProvider.completeAgreement(destinationChoice);

      byte[] derivedSecuredKey = keyAgreementProvider.deriveSecureKey(sharedSecretKey,
          sourceChoice.getPublic(), destinationChoice);
      return derivedSecuredKey;
    } catch (InvalidKeyException | IllegalStateException | NoSuchAlgorithmException e) {
      throw new KeyAgreementException("", e);
    }
  }
}
