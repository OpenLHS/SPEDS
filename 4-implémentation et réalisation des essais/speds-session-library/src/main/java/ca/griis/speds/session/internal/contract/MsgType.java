/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe MsgType.
 * @brief @~english Implementation of the MsgType class.
 */

package ca.griis.speds.session.internal.contract;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import java.util.EnumMap;
import java.util.Map;

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
 * @brief @~french Implémentation de MsgType
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
 *      2025-06-29 [MD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public enum MsgType {
  SES_PUB_ENV("SES.PUB.ENV"),
  SES_PUB_REC("SES.PUB.REC"),
  SES_CLE_ENV("SES.CLE.ENV"),
  SES_CLE_REC("SES.CLE.REC"),
  SES_SAK_ENV("SES.SAK.ENV"),
  SES_SAK_REC("SES.SAK.REC"),
  SES_MSG_ENV("SES.MSG.ENV"),
  SES_MSG_REC("SES.MSG.REC"),
  SES_FIN_ENV("SES.FIN.ENV"),
  SES_FIN_REC("SES.FIN.REC");

  private final String value;

  MsgType(String value) {
    this.value = value;
  }

  // --- Static maps for conversion ---
  private static final Map<HeaderDto.Msgtype, MsgType> FROM_GEN_MAP =
      new EnumMap<>(HeaderDto.Msgtype.class);
  private static final Map<MsgType, HeaderDto.Msgtype> TO_GEN_MAP = new EnumMap<>(MsgType.class);

  static {
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_PUB_ENV, SES_PUB_ENV);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_PUB_REC, SES_PUB_REC);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_CLE_ENV, SES_CLE_ENV);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_CLE_REC, SES_CLE_REC);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_SAK_ENV, SES_SAK_ENV);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_SAK_REC, SES_SAK_REC);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_MSG_ENV, SES_MSG_ENV);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_MSG_REC, SES_MSG_REC);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_FIN_ENV, SES_FIN_ENV);
    FROM_GEN_MAP.put(HeaderDto.Msgtype.SES_FIN_REC, SES_FIN_REC);

    // reverse mapping
    TO_GEN_MAP.put(SES_PUB_ENV, HeaderDto.Msgtype.SES_PUB_ENV);
    TO_GEN_MAP.put(SES_PUB_REC, HeaderDto.Msgtype.SES_PUB_REC);
    TO_GEN_MAP.put(SES_CLE_ENV, HeaderDto.Msgtype.SES_CLE_ENV);
    TO_GEN_MAP.put(SES_CLE_REC, HeaderDto.Msgtype.SES_CLE_REC);
    TO_GEN_MAP.put(SES_SAK_ENV, HeaderDto.Msgtype.SES_SAK_ENV);
    TO_GEN_MAP.put(SES_SAK_REC, HeaderDto.Msgtype.SES_SAK_REC);
    TO_GEN_MAP.put(SES_MSG_ENV, HeaderDto.Msgtype.SES_MSG_ENV);
    TO_GEN_MAP.put(SES_MSG_REC, HeaderDto.Msgtype.SES_MSG_REC);
    TO_GEN_MAP.put(SES_FIN_ENV, HeaderDto.Msgtype.SES_FIN_ENV);
    TO_GEN_MAP.put(SES_FIN_REC, HeaderDto.Msgtype.SES_FIN_REC);
  }

  public static MsgType from(HeaderDto.Msgtype gen) {
    MsgType result = FROM_GEN_MAP.get(gen);
    if (result == null) {
      throw new IllegalArgumentException("Inconnu : " + gen);
    }
    return result;
  }

  public HeaderDto.Msgtype toGen() {
    return TO_GEN_MAP.get(this);
  }
}
