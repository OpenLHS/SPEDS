/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération Debug.
 * @brief @~english «File description»
 */

package ca.griis.logger.statuscode;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("debug/debug")
@LocaleData(defaultCharset = "UTF8",
    value = {@Locale("code"), /* @Locale("fr_ca"), */ @Locale("en_CA")})
public enum Debug {

  /** @~french Désigne le succès d'une fonction sans arguments. */
  /** @~english «First item description» */
  FUNCTION_SUCCESS_0,
  /** @~french Désigne le succès d'une fonction avec un seul argument - {var1}={val1}. */
  /** @~english «First item description» */
  FUNCTION_SUCCESS_1,
  /** @~french Désigne le succès d'une fonction avec deux arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_SUCCESS_2,
  /** @~french Désigne le succès d'une fonction avec trois arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_SUCCESS_3,
  /** @~french Désigne le succès d'une fonction avec quatre arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_SUCCESS_4,
  /** @~french Désigne le succès d'une fonction avec cinq arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_SUCCESS_5,

  /** @~french Désigne l'échec d'une fonction sans arguments. */
  /** @~english «First item description» */
  FUNCTION_FAIL_0,
  /** @~french Désigne l'échec d'une fonction avec un argument - {var1}={val1}. */
  /** @~english «First item description» */
  FUNCTION_FAIL_1,
  /** @~french Désigne l'échec d'une fonction avec deux arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_FAIL_2,
  /** @~french Désigne l'échec d'une fonction avec trois arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_FAIL_3,
  /** @~french Désigne l'échec d'une fonction avec quatre arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_FAIL_4,
  /** @~french Désigne l'échec d'une fonction avec cinq arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_FAIL_5,

  /** @~french Désigne le succès d'un branchement sans arguments. */
  /** @~english «First item description» */
  FUNCTION_BRANCH_SUCCESS_0,
  /** @~french Désigne le succès d'un branchement avec un argument - {var1}={val1}. */
  /** @~english «First item description» */
  FUNCTION_BRANCH_SUCCESS_1,
  /** @~french Désigne le succès d'un branchement avec deux arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_BRANCH_SUCCESS_2,
  /** @~french Désigne le succès d'un branchement avec trois arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_BRANCH_SUCCESS_3,
  /** @~french Désigne le succès d'un branchement avec quatre arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_BRANCH_SUCCESS_4,
  /** @~french Désigne le succès d'un branchement avec cinq arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_BRANCH_SUCCESS_5,

  /** @~french Désigne l'échec d'un branchement sans arguments. */
  /** @~english «First item description» */
  FUNCTION_BRANCH_FAILED_0,
  /** @~french Désigne l'échec d'un branchement avec un argument - {var1}={val1} */
  /** @~english «First item description» */
  FUNCTION_BRANCH_FAILED_1,
  /** @~french Désigne l'échec d'un branchement avec deux arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_BRANCH_FAILED_2,
  /** @~french Désigne l'échec d'un branchement avec trois arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_BRANCH_FAILED_3,
  /** @~french Désigne l'échec d'un branchement avec quatre arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_BRANCH_FAILED_4,
  /** @~french Désigne l'échec d'un branchement avec cinq arguments - {var1}={val1} ... */
  /** @~english «First item description» */
  FUNCTION_BRANCH_FAILED_5,

  /** @~french Journalisation d'une variable - {var1}={val1}. */
  /** @~english «First item description» */
  VARIABLE_LOGGING_1,
  /** @~french Journalisation de deux variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_2,
  /** @~french Journalisation de trois variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_3,
  /** @~french Journalisation de quatre variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_4,
  /** @~french Journalisation de cinq variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_5,
  /** @~french Journalisation de six variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_6,
  /** @~french Journalisation de sept variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_7,

  /** @~french Cette fonction n'est pas implémentée et ne fera rien */
  /** @~english «First item description» */
  UNSUPPORTED_OPERATION,
  /** @~french Cette fonction ne fait rien. */
  /** @~english «First item description» */
  NO_OPERATION
}
