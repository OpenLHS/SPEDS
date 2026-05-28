/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SealManager.
 * @brief @~english SealManager class implementation.
 */

package ca.griis.speds.network.internal.security;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

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
 * @brief @~french Permet de créer et vérifier des sceaux sur des messages réseaux.
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
 *      2026-02-16 [FO] - Refactorisation pour simplifier sa fonction.
 *      2025-03-03 [CB] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class SealManager {
  private final CryptographyService service;

  public SealManager(CryptographyService service) {
    this.service = service;
  }

  public String createSeal(String message, PrivateKey privateKey) {
    final var data = message.getBytes(StandardCharsets.UTF_8);
    final var hash = service.hash(SpedsLayer.NETWORK, data);
    final var signedData = service.sign(SpedsLayer.NETWORK, privateKey, hash);
    final String result = Base64.getEncoder().encodeToString(signedData);
    return result;
  }

  public Boolean checkSeal(String message, PublicKey publicKey, String signature) {
    final var data = message.getBytes(StandardCharsets.UTF_8);
    final var hash = service.hash(SpedsLayer.NETWORK, data);
    final var sign = Base64.getDecoder().decode(signature);

    Boolean result = service.checkSignatureValidity(SpedsLayer.NETWORK, sign, publicKey, hash);
    return result;
  }
}
