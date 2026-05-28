/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération Trace.
 * @brief @~english «File description»
 */

package ca.griis.logger.statuscode;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("trace/trace")
@LocaleData(defaultCharset = "UTF8",
    value = {@Locale("code"), /* @Locale("fr_ca"), */ @Locale("en_CA")})
public enum Trace {
  /** @~french Journalise l'entrée de méthode sans arguments {funcName}. */
  /** @~english «First item description» */
  ENTER_METHOD_0,
  /** @~french Journalise l'entrée de méthode avec un argument {funcName} - {var1}={val1}. */
  /** @~english «First item description» */
  ENTER_METHOD_1,
  /** @~french Journalise l'entrée de méthode avec deux arguments {funcName} - {var1}={val1} ... */
  /** @~english «First item description» */
  ENTER_METHOD_2,
  /** @~french Journalise l'entrée de méthode avec trois arguments {funcName} - {var1}={val1} ... */
  /** @~english «First item description» */
  ENTER_METHOD_3,
  /**
   * @~french Journalise l'entrée de méthode avec quatre arguments {funcName} - {var1}={val1} ...
   */
  /** @~english «First item description» */
  ENTER_METHOD_4,
  /** @~french Journalise l'entrée de méthode avec cinq arguments {funcName} - {var1}={val1} ... */
  /** @~english «First item description» */
  ENTER_METHOD_5,
  /** @~french Journalise l'entrée de méthode avec six arguments {funcName} - {var1}={val1} ... */
  /** @~english «First item description» */
  ENTER_METHOD_6,
  /** @~french Journalise l'entrée de méthode avec sept arguments {funcName} - {var1}={val1} ... */
  /** @~english «First item description» */
  ENTER_METHOD_7,
  /** @~french Journalise la sortie de méthode sans arguments {funcName}. */
  /** @~english «First item description» */
  EXIT_METHOD_0,
  /** @~french Journalise la sortie de méthode avec un argument {funcName} - {var1}={val1}. */
  /** @~english «First item description» */
  EXIT_METHOD_1,
  /** @~french Journalise un algorithme avec un argument - {var1}={val1}. */
  /** @~english «First item description» */
  ALGORITHM_1,
  /** @~french Journalise un algorithme avec deux arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  ALGORITHM_2,
  /** @~french Journalise un algorithme avec trois arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  ALGORITHM_3,
  /** @~french Journalise un algorithme avec quatre arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  ALGORITHM_4,
  /** @~french Journalise un algorithme avec cinq arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  ALGORITHM_5
}
