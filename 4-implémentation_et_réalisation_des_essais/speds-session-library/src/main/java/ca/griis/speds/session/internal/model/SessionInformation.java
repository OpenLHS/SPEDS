/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SessionInformation.
 * @brief @~english Implementation of the SessionInformation class.
 */

package ca.griis.speds.session.internal.model;

import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.security.crypto.SessionKeyDestroyer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.UUID;
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
 * @brief @~french Implémentation du modèle des informations de sessions
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
 *      2025-06-29 [MD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SessionInformation {
  private SessionId sessionId;
  private String initiatorId;
  private String initiatorIri;
  private String peerId;
  private String peerIri;

  private PublicKey initiatorPubKey;
  private SecretKey skak;
  private SecretKey sdek;
  private Integer numberOfMessage = 0;

  private String pgaId;
  private String piduMessage;
  private KeyPair firstChoice;

  private UUID token;

  public SessionInformation() {}

  public SessionId sessionId() {
    return sessionId;
  }

  public String initiatorId() {
    return initiatorId;
  }

  public String initiatorIri() {
    return initiatorIri;
  }

  public String peerId() {
    return peerId;
  }

  public String peerIri() {
    return peerIri;
  }

  public PublicKey initiatorPubKey() {
    return initiatorPubKey;
  }

  public SecretKey skak() {
    return skak;
  }

  public SecretKey sdek() {
    return sdek;
  }

  public Integer numberOfMessage() {
    return numberOfMessage;
  }

  public String pgaId() {
    return pgaId;
  }

  public String piduMessage() {
    return piduMessage;
  }

  public KeyPair firstChoice() {
    return firstChoice;
  }

  public UUID token() {
    return token;
  }

  public void cleanUp() {
    if (skak != null) {
      SessionKeyDestroyer.destroy(skak);
      skak = null;
    }

    if (sdek != null) {
      SessionKeyDestroyer.destroy(sdek);
      sdek = null;
    }

    if (firstChoice != null) {
      SessionKeyDestroyer.destroy(firstChoice.getPrivate());

      firstChoice = null;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final SessionInformation instance = new SessionInformation();

    public Builder sessionId(SessionId sessionId) {
      instance.sessionId = sessionId;
      return this;
    }

    public Builder initiatorId(String initiatorId) {
      instance.initiatorId = initiatorId;
      return this;
    }

    public Builder initiatorPubKey(PublicKey initiatorPubKey) {
      instance.initiatorPubKey = initiatorPubKey;
      return this;
    }

    public Builder initiatorIri(String initiatorIri) {
      instance.initiatorIri = initiatorIri;
      return this;
    }

    public Builder peerId(String peerId) {
      instance.peerId = peerId;
      return this;
    }

    public Builder peerIri(String peerIri) {
      instance.peerIri = peerIri;
      return this;
    }

    public Builder skak(SecretKey skak) {
      instance.skak = skak;
      return this;
    }

    public Builder pgaId(String pgaId) {
      instance.pgaId = pgaId;
      return this;
    }

    public Builder sdek(SecretKey sdek) {
      instance.sdek = sdek;
      return this;
    }

    public Builder numberOfMessage(Integer numberOfMessage) {
      instance.numberOfMessage = numberOfMessage;
      return this;
    }

    public Builder piduMessage(String piduMessage) {
      instance.piduMessage = piduMessage;
      return this;
    }

    public Builder firstChoice(KeyPair firstChoice) {
      instance.firstChoice = firstChoice;
      return this;
    }

    public Builder token(UUID token) {
      instance.token = token;
      return this;
    }

    public SessionInformation build() {
      return instance;
    }

    public Builder of(SessionInformation sessionInformation) {
      instance.sessionId = sessionInformation.sessionId;
      instance.initiatorId = sessionInformation.initiatorId;
      instance.initiatorPubKey = sessionInformation.initiatorPubKey;
      instance.initiatorIri = sessionInformation.initiatorIri;

      instance.peerId = sessionInformation.peerId;
      instance.peerIri = sessionInformation.peerIri;

      instance.skak = sessionInformation.skak;
      instance.pgaId = sessionInformation.pgaId;
      instance.sdek = sessionInformation.sdek;
      instance.numberOfMessage = sessionInformation.numberOfMessage;

      instance.piduMessage = sessionInformation.piduMessage;
      instance.firstChoice = sessionInformation.firstChoice;
      instance.token = sessionInformation.token;
      return this;
    }
  }
}
