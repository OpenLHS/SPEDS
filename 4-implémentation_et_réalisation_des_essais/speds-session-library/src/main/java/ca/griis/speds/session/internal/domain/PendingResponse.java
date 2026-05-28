/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe PendingResponse.
 * @brief @~english Implementation of the PendingResponse class.
 */

package ca.griis.speds.session.internal.domain;

public record PendingResponse (SessionId sessionID) implements PendingMessage {
}
