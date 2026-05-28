/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération Warn.
 * @brief @~english «File description»
 */

package ca.griis.logger.statuscode;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("warn/warn")
@LocaleData(defaultCharset = "UTF8",
    value = {@Locale("code"), /* @Locale("fr_CA"), */ @Locale("en_CA")})
public enum Warn {

  /** @~french Indique que le fichier est introuvable - path={path}. */
  /** @~english «First item description» */
  FILE_NOT_FOUND,
  /**
   * @~french Indique que l'élément existe déjà dans l'emplacement cible - {itemName}={itemVal}
   *          {collectionName}={collection}.
   */
  /** @~english «First item description» */
  DATA_ALREADY_EXIST,
  /** @~french Indique une valeur Nul inattendue */
  /** @~english «First item description» */
  VALUE_IS_NULL,
  /** @~french Indique que l'élément est introuvable - element={element} pour={pour}. */
  /** @~english «First item description» */
  NOT_FOUND_FOR,
  /** @~french Indique que le message est actuellement invalide - msgType={type}. */
  /** @~english «First item description» */
  INVALID_MESSAGE,

  /** @~french Indique que la condition n'est pas respectée - {condition}. */
  /** @~english «First item description» */
  UNMET_CONDITION_0,
  /** @~french Indique que la condition n'est pas respectée - {conditionName}={conditionVal}. */
  /** @~english «First item description» */
  UNMET_CONDITION_1,
  /** @~french Indique que la condition n'est pas respectée - {conditionName}={conditionVal} ... */
  /** @~english «First item description» */
  UNMET_CONDITION_2,
  /** @~french Indique que la condition n'est pas respectée - {conditionName}={conditionVal} ... */
  /** @~english «First item description» */
  UNMET_CONDITION_3,

  /** @~french Indique que la connexion réseau a échoué - ip={ip} port={port}. */
  /** @~english «First item description» */
  NETWORK_FAILED,

  /** @~french Journalise une variable - {var1}={val1} */
  /** @~english «First item description» */
  VARIABLE_LOGGING_1,
  /** @~french Journalise deux variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_2,
  /** @~french Journalise trois variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_3,
  /** @~french Journalise quatre variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_4,
  /** @~french Journalise cinq variables - {var1}={val1} ... */
  /** @~english «First item description» */
  VARIABLE_LOGGING_5;

}
