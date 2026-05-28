/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation du record ApplicationInterface.
 * @brief @~english ApplicationInterface record implementation.
 */

package ca.griis.speds.application.internal.domain;

import ca.griis.js2p.gen.speds.application.api.dto.MsgType;
import ca.griis.js2p.gen.speds.application.api.dto.Service;
import ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive;
import java.util.Objects;

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
 * @brief @~french Représente l'interface entre l'utilisateur de la couche application et la couche
 *        application.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2026-03-04 [CB] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public record ApplicationInterface (Service service, ServicePrimitive servicePrimitive,
    String sourceCode, String destinationCode, String projectId, String msgId, MsgType msgType,
    String content) {

  public ApplicationInterface {
    Objects.requireNonNull(service, "Service must not be null.");
    Objects.requireNonNull(servicePrimitive, "Service primitive must not be null.");
    Objects.requireNonNull(sourceCode, "Source code must not be null.");
    Objects.requireNonNull(destinationCode, "Destination code must not be null.");
    Objects.requireNonNull(projectId, "Project ID must not be null.");
    Objects.requireNonNull(msgId, "Message ID must not be null.");
    Objects.requireNonNull(msgType, "Message type must not be null.");
    Objects.requireNonNull(content, "Content must not be null.");
  }
}
