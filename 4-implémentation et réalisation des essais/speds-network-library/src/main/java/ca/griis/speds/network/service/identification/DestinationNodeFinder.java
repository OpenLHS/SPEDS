/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe DestinationNodeFinder.
 * @brief @~english Contains description of DestinationNodeFinder class.
 */

package ca.griis.speds.network.service.identification;

import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;

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
 * @brief @~french Le mécanisme permettant de déterminer l'adresse du prochain noeud
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
 *      2025-06-26 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class DestinationNodeFinder {

  private DestinationNodeFinder() {}

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french La version actuelle du mécanisme se contente d’envoyer le message directement à
   *        destination_iri s’il s’agit d’un RES.ENV qui est envoyé et à source_iri s’il s’agit d’un
   *        RES.REC qui est envoyé.
   * @param type Le type de message
   * @param source source_iri
   * @param destination destination_iri
   * @return le noeud d'envoi
   */
  public static String nextNode(HeaderDto.Msgtype type, String source, String destination) {
    String nextNode = "";
    if (type == HeaderDto.Msgtype.RES_ENV) {
      nextNode = destination;
    } else {
      nextNode = source;
    }
    return nextNode;
  }
}
