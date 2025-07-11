/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe IriManager.
 * @brief @~english IriManager class implementation.
 */

package ca.griis.speds.network.service.routing;

import static ca.griis.logger.GriisLoggerFactory.getLogger;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.service.exception.DeserializationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
 * @brief @~french Permet de gérer les IRI à manipuler par la couche Réseau.
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
 *      2025-03-18 [CB] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class IriManager {
  private static final GriisLogger logger = getLogger(IriManager.class);

  private static final String ENTITY_CODE_KEY = "code";

  /**
   * @brief @~english «Description of the function»
   * @param serializedIri «Parameter description»
   * @exception DeserializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère le code d'entité dans l'IRI fourni.
   * @param serializedIri L'IRI dont le code d'entité est à récupérer.
   * @exception DeserializationException Erreur survenue lors de la désérialisation de l'IRI.
   * @return Le code d'entité se trouvant dans l'IRI fourni.
   *
   * @par Tâches
   *      S.O.
   */
  public static String retrieveCode(String serializedIri) throws DeserializationException {
    logger.trace(Trace.ENTER_METHOD_1, "serializedIri", serializedIri);

    final String code;
    try {
      final URI objectIri = new URI(serializedIri);
      List<String> codes = splitQuery(objectIri).get(ENTITY_CODE_KEY);

      code = sanitizeCode(codes, serializedIri);
    } catch (URISyntaxException e) {
      final String exception =
          "IRI is not well structured: " + e.getMessage();
      logger.error(exception);
      throw new DeserializationException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "code", code);
    return code;
  }

  /**
   * @brief @~english «Description of the function»
   * @param serializedIri «Parameter description»
   * @param code «Parameter description»
   * @exception DeserializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Remplace le code d'entité dans l'IRI par un nouveau code d'entité.
   * @param serializedIri L'IRI dont le code d'entité est à remplacer.
   * @param code Le nouveau code d'entité qui figurera dans l'IRI.
   * @exception DeserializationException Erreur survenue lors de la désérialisation de l'IRI.
   * @return l'IRI avec le nouveau code d'entité.
   *
   * @par Tâches
   *      S.O.
   */
  public static String replaceCodeWith(String serializedIri, String code)
      throws DeserializationException {
    final String uriWithNewCode;
    try {
      final URI objectIri = new URI(serializedIri);
      Map<String, List<String>> queries = splitQuery(objectIri);
      queries.put(ENTITY_CODE_KEY, List.of(code));

      uriWithNewCode =
          new URI(objectIri.getScheme(), objectIri.getUserInfo(), objectIri.getHost(),
              objectIri.getPort(), objectIri.getPath(), joinQuery(queries),
              objectIri.getFragment()).toString();
    } catch (URISyntaxException e) {
      final String exception =
          "IRI is not well structured: " + e.getMessage();
      logger.error(exception);
      throw new DeserializationException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "uriWithNewCode", uriWithNewCode);
    return uriWithNewCode;
  }

  /**
   * @brief @~english «Description of the function»
   * @param serializedIri «Parameter description»
   * @param codes «Parameter description»
   * @exception DeserializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère le code d'entité et effectue des validations sur ce dernier.
   * @param serializedIri L'IRI contenant le code d'entité.
   * @param codes La séquence dans laquelle se trouve le code d'entité.
   * @exception DeserializationException Erreur survenue lors de validation du code d'entité.
   * @return Le code d'entité.
   *
   * @par Tâches
   *      S.O.
   */
  private static String sanitizeCode(List<String> codes, String serializedIri)
      throws DeserializationException {
    logger.trace(Trace.ENTER_METHOD_2, "codes", codes, "serializedIri", serializedIri);
    if (Objects.isNull(codes) || codes.isEmpty()) {
      final String exception =
          "No entity code found in the following IRI: " + serializedIri;
      logger.error(exception);
      throw new DeserializationException(exception);
    } else if (codes.size() > 1) {
      final String exception =
          "Several entity code found in the IRI: " + codes + ". Only one code is allowed.";
      logger.error(exception);
      throw new DeserializationException(exception);
    }

    final String result = codes.get(0);

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param uri «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Récupère la portion requête de l'URI et la divise en ensemble de clé-valeur.
   * @param uri L'URI contenant la portion requête à diviser.
   * @return L'entrée de clé-valeur résultante.
   *
   * @par Tâches
   *      S.O.
   */
  private static Map<String, List<String>> splitQuery(URI uri) {
    logger.trace(Trace.ENTER_METHOD_1, "uri", uri);

    final Map<String, List<String>> result;
    if (Objects.isNull(uri.getQuery()) || uri.getQuery().isEmpty()) {
      result = new ConcurrentHashMap<>();
    } else {
      result = Arrays.stream(uri.getQuery().split("&"))
          .map(IriManager::splitQueryParameter)
          .collect(groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new,
              mapping(Map.Entry::getValue, toList())));
      result.entrySet().forEach(entry -> entry.getValue().removeIf(Objects::isNull));
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param keyValue «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Divise une entrée de requête en constituants clé-valeur.
   * @param keyValue L'entrée de requête à diviser.
   * @return L'entrée de clé-valeur résultante.
   *
   * @par Tâches
   *      S.O.
   */
  private static SimpleImmutableEntry<String, String> splitQueryParameter(String keyValue) {
    logger.trace(Trace.ENTER_METHOD_1, "keyValue", keyValue);

    final int index = keyValue.indexOf('=');

    String key = keyValue;
    Optional<String> value = Optional.empty();

    if (index > -1) {
      key = keyValue.substring(0, index);
      value =
          Optional.ofNullable(keyValue.length() > index + 1 ? keyValue.substring(index + 1) : null);
    }

    final SimpleImmutableEntry<String, String> result = new SimpleImmutableEntry<>(
        URLDecoder.decode(key, StandardCharsets.UTF_8),
        value.map(s -> URLDecoder.decode(s, StandardCharsets.UTF_8)).orElse(null));

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param queries «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Forme une chaîne de caractères représentant la portion requête de l'URI.
   * @param queries Les requêtes sous forme d'ensemble de clé-valeur.
   * @return La chaîne de caractères représentant la portion requête de l'IRI.
   *
   * @par Tâches
   *      S.O.
   */
  private static String joinQuery(Map<String, List<String>> queries) {
    logger.trace(Trace.ENTER_METHOD_1, "queries", queries);

    final String result = queries.entrySet().stream()
        .map(IriManager::joinQueryParameter)
        .collect(joining("&"));

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param queryEntry «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Forme une entrée de requête en chaîne de caractères.
   * @param queryEntry L'entrée de requête à former.
   * @return La chaîne de caractères représentant l'entrée de requête.
   *
   * @par Tâches
   *      S.O.
   */
  private static String joinQueryParameter(Map.Entry<String, List<String>> queryEntry) {
    logger.trace(Trace.ENTER_METHOD_1, "queryEntry", queryEntry);

    final String result = queryEntry.getValue().stream()
        .map(value -> queryEntry.getKey() + "=" + value)
        .collect(joining("&"));

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
