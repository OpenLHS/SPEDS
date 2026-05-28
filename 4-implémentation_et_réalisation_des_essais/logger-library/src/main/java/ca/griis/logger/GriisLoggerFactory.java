/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe GriisLoggerFactory.
 * @brief @~english «File description»
 */

package ca.griis.logger;

import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.MessageConveyor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import org.slf4j.cal10n.LocLoggerFactory;

/**
 * @brief @~english The standard logger for all the PARS3 components.
 * @par Details
 *      N.A.
 * @par Model
 *      N.A.
 * @par Conception
 *      N.A.
 * @par Limits
 *      N.A.
 *
 * @brief @~french Produis l'enregistreur automatique d'évènements standard GRIIS.
 * @par Details
 *      Même si le composant est une fabrique, il ne produit qu'un seul type d'enregistreur
 *      automatique d'évènements; un enregistreur automatique d'évènements GRIIS. Ce traitement suit
 *      la nomenclature employée par SLF4J.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 * @par Historique
 *      2020-01-30 [MD] - Implémentation initiale. <br>
 *
 * @par Tâches
 *      S.O.
 */
public class GriisLoggerFactory {
  private static IMessageConveyor messageConveyor;
  private static LocLoggerFactory llFactory;
  private static Boolean hasInit = false;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Initialise avec le format de la langue.
   * @param locale Le format de la langue.
   *
   * @par Tâches
   *      S.O.
   */
  public static void initWithLocal(String locale) {
    messageConveyor = new MessageConveyor(new Locale(locale));
    llFactory = new LocLoggerFactory(messageConveyor);
    hasInit = true;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Initialise la fabrique au premier appel et retourne un GriisLogger
   * @param clazz La classe pour laquelle on produit un enregistreur automatique d'évènements.
   *
   * @par Tâches
   *      S.O.
   */
  public static GriisLogger getLogger(Class<?> clazz) {
    if (!hasInit) {
      init();
    }

    return new GriisLogger(llFactory.getLocLogger(clazz), messageConveyor);
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Charge les paramètres régionaux désirés à partir des propriétés et initialise
   *        la classe.
   *
   * @par Tâches
   *      S.O.
   */
  private static void init() {
    String locale = "";
    Properties prop = new Properties();

    try (InputStream inputStream =
        GriisLoggerFactory.class.getClassLoader().getResourceAsStream("application.properties")) {

      prop.load(inputStream);
      locale = prop.getProperty("logger-griis.locale");
    } catch (IOException e) {
      throw new Error(e);
    }

    messageConveyor = new MessageConveyor(new Locale(locale));
    llFactory = new LocLoggerFactory(messageConveyor);
    hasInit = true;
  }

  private GriisLoggerFactory() {}
}
