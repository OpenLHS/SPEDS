/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SharedObjectMapper.
 * @brief @~english Contains description of SharedObjectMapper class.
 */

package ca.griis.speds.application.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 * @brief @~french Partage une instance de l'objet pour la sérialisation et désérialisation JSON.
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
 *      2025-02-05 [SSC] - Implémentation initiale<br>
 *
 * @note 2020-01-07 [FO] - Un seul objet est partagé pour l'ensemble du projet, ce que recommande la
 *       documentation officielle de la bibliothèque Jackson. Voir
 *       https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/ObjectMapper.html.
 *       Cet objet est toutefois partagé pour tous les fils d'exécution, ce qui semble provoquer
 *       des problèmes de performance pour certains de ces fils. Dans le cas où ce problème se
 *       produit, la solution serait de définir une instance d'objet par fil d'exécution et non une
 *       instance pour tous les fils d'exécution.
 *
 * @par Tâches
 *      S.O.
 */
public class SharedObjectMapper {
  private ObjectMapper mapper = new ObjectMapper();

  protected SharedObjectMapper() {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
  }

  private static class LazyHolder {
    static final SharedObjectMapper instance = new SharedObjectMapper();
  }

  public static SharedObjectMapper getInstance() {
    return LazyHolder.instance;
  }

  public ObjectMapper getMapper() {
    return mapper;
  }
}
