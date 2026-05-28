/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération Error.
 * @brief @~english «File description»
 */

package ca.griis.logger.statuscode;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("error/error")
@LocaleData(defaultCharset = "UTF8",
    value = {@Locale("code"), /* @Locale("fr_ca"), */ @Locale("en_CA")})
public enum Error {

  /** @~french Indique une valeur Nul inattendue. */
  /** @~english «First item description» */
  VALUE_IS_NULL,
  /**
   * @~french Indique que les deux collections doivent avoir la même taille
   *          - {col1NameSize}={col1Size}
   *          {col2NameSize}={col2Size}.
   */
  /** @~english «First item description» */
  COLLECTION_SIZE_MISMATCH,
  /** @~french Indique que le résultat est introuvable pour l'entrant - {inputName}={inputValue}. */
  /** @~english «First item description» */
  RESULT_NOT_FOUND,
  /** @~french Indique qu'une exception de désérialisation s'est produite - exception={ex}. */
  /** @~english «First item description» */
  DESERIALIZATION_FAIL,
  /** @~french Indique qu'une exception de sérialisation s'est produite - exception={ex}. */
  /** @~english «First item description» */
  SERIALIZATION_FAIL,
  /** @~french Indique qu'une erreur s'est produite dans la base de données - exception={ex}. */
  /** @~english «First item description» */
  DATABASE_ERROR,
  /** @~french Indique que le destinataire est invalide - recipientId={recipientId}. */
  /** @~english «First item description» */
  INVALID_RECIPIENT,
  /** @~french Indique que la condition n'a pas été remplie - {cond}={val}. */
  /** @~english «First item description» */
  CONDITION_UNMET,
  /** @~french Indique que le réseau n'est pas joignable - ip={ip} port={port}. */
  /** @~english «First item description» */
  NETWORK_FAIL,
  /** @~french Indique qu'une exception s'est produite - exception={ex}. */
  /** @~english «First item description» */
  GENERIC_ERROR,
  /** @~french Indique qu'une exception s'est produite et a été ignorée - exception={ex}. */
  /** @~english «First item description» */
  IGNORED_ERROR

}
