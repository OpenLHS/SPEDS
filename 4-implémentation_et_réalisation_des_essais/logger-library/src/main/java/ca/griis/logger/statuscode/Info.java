/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération Info.
 * @brief @~english «File description»
 */

package ca.griis.logger.statuscode;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("info/info")
@LocaleData(defaultCharset = "UTF8",
    value = {@Locale("code"), /* @Locale("fr_ca"), */ @Locale("en_CA")})
public enum Info {

  /** @~french Indique que l'application est en cours de démarrage. */
  /** @~english «First item description» */
  APP_STARTING,
  /** @~french Indique que l'application est prête. */
  /** @~english «First item description» */
  APP_READY,
  /** @~french Indique que l'application se prépare à fermer. */
  /** @~english «First item description» */
  APP_PREPARING_TO_CLOSE,
  /** @~french Indique que l'application est en cours de fermeture. */
  /** @~english «First item description» */
  APP_CLOSING,
  /** @~french Indique que le composant est en cours de démarrage - component={componentName}. */
  /** @~english «First item description» */
  COMPONENT_STARTING,
  /** @~french Indique que le composant est prêt - component={componentName}. */
  /** @~english «First item description» */
  COMPONENT_READY,
  /** @~french Indique que le composant se prépare à fermer - component={componentName}. */
  /** @~english «First item description» */
  COMPONENT_PREPARING_TO_CLOSE,
  /** @~french Indique que le composant est en cours de fermeture - component={componentName}. */
  /** @~english «First item description» */
  COMPONENT_CLOSING,
  /** @~french Désigne l'état du composant - component={componentName} state={state}. */
  /** @~english «First item description» */
  COMPONENT_STATE,

  /** @~french Envoie du message - pgaId={pgaId} msgType={type}. */
  /** @~english «First item description» */
  MESSAGE_SEND,
  /** @~french Réponds avec ce message - pgaId={pgaId} msgType={type}. */
  /** @~english «First item description» */
  MESSAGE_ANS,
  /** @~french Transmets ce message - pgaId={pgaId} msgType={type}. */
  /** @~english «First item description» */
  MESSAGE_FWD,
  /** @~french Reçois ce message - pgaId={pgaId} msgType={type}. */
  /** @~english «First item description» */
  MESSAGE_RECP,

  /**
   * @~french Reçoit le message - componentId={0} localAddress={1} source={2} sourceProxy={3}
   *          destinationProxy={4} destination={5} senderNumber={6} hashMessage={7} timestamp={8}.
   */
  /** @~english «First item description» */
  NETWORK_MESSAGE_SEND,

  /**
   * @~french Reçoit le message - componentId={0} localAddress={1} source={2} sourceProxy={3}
   *          destinationProxy={4} destination={5} senderNumber={6} hashMessage={7} timestamp={8}.
   */
  /** @~english «First item description» */
  NETWORK_MESSAGE_RECP,

  /** @~french Indique le nouvel état du PAD - oldState={state} newState-{state}. */
  /** @~english «First item description» */
  PAD_STATE,

  /** @~french Désigne le succès d'une fonction sans journaliser d'arguments. */
  /** @~english «First item description» */
  FUNCTION_SUCCESS_0,
  /** @~french Désigne le succès d'une fonction avec argument - {var1}={val1}. */
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

  /** @~french Journalise une variable - {var1}={val1} ... */
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
  VARIABLE_LOGGING_5,

  /** @~french Désigne le début d'une tâche {funcName}. */
  /** @~english «First item description» */
  FUNCTION_START

}
