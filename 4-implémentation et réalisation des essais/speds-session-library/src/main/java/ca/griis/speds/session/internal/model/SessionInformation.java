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
import java.security.KeyPair;
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
  public SessionId sessionId;
  public String initiatorId;
  public String initiatorPubKey;
  public String initiatorIri;

  public String peerId;
  public String peerIri;

  public SecretKey skak;
  public String pgaId;
  public SecretKey sdek;
  public UUID trackingNumber;
  public Integer numberOfMessage = 0;

  public String piduMessage;
  public KeyPair firstChoice;

  public UUID token;

  public SessionInformation() {}

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

    public Builder initiatorPubKey(String initiatorPubKey) {
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

    public Builder trackingNumber(UUID trackingNumber) {
      instance.trackingNumber = trackingNumber;
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
      instance.trackingNumber = sessionInformation.trackingNumber;
      instance.numberOfMessage = sessionInformation.numberOfMessage;

      instance.piduMessage = sessionInformation.piduMessage;
      instance.firstChoice = sessionInformation.firstChoice;
      instance.token = sessionInformation.token;
      return this;
    }
  }
}
