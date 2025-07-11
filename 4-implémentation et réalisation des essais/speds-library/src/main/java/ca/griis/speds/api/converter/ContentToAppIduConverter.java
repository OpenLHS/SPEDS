/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ContentToAppIduConverter.
 * @brief @~english Contains description of ContentToAppIduConverter class.
 */

package ca.griis.speds.api.converter;

import ca.griis.js2p.gen.speds.application.api.dto.CommandeEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.CommandeReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.application.api.dto.DataEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.DataReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.ExceptionEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.ExceptionReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.PgaConfirmationDto;
import ca.griis.js2p.gen.speds.application.api.dto.PgaFinDto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.application.api.dto.StatutEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.StatutReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheReceptionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.UUID;

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
 * @brief @~french Définit un convertisseur de contenu de message du protocole SPEDS en une unité de
 *        travail d'interface venant de la couche application.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-04-07 [FO] - Première ébauche.<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class ContentToAppIduConverter implements ContentConverter<InterfaceDataUnit01Dto> {
  private final ObjectMapper mapper;
  private final String version;
  private final String reference;

  public ContentToAppIduConverter(ObjectMapper mapper, String version, String reference) {
    this.mapper = mapper;
    this.version = version;
    this.reference = reference;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, TacheEnvoiDto content) throws JsonProcessingException {
    InterfaceDataUnit01Dto idu =
        convert(pgaId, sourceCode, destinationCode, msgId, HeaderDto.Msgtype.TACHE_ENVOI, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, TacheReceptionDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.TACHE_RECEPTION, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, FinEnvoiDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu =
        convert(pgaId, sourceCode, destinationCode, msgId, HeaderDto.Msgtype.FIN_ENVOI, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, FinReceptionDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.FIN_RECEPTION, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, ExceptionEnvoiDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.EXCEPTION_ENVOI, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, ExceptionReceptionDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.EXCEPTION_RECEPTION, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, DataEnvoiDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu =
        convert(pgaId, sourceCode, destinationCode, msgId, HeaderDto.Msgtype.DATA_ENVOI, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, DataReceptionDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.DATA_RECEPTION, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, StatutEnvoiDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu =
        convert(pgaId, sourceCode, destinationCode, msgId, HeaderDto.Msgtype.STATUT_ENVOI, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, StatutReceptionDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.STATUT_RECEPTION, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, CommandeEnvoiDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.COMMANDE_ENVOI, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, CommandeReceptionDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.COMMANDE_RECEPTION, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, PgaFinDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu =
        convert(pgaId, sourceCode, destinationCode, msgId, HeaderDto.Msgtype.PGA_FIN, content);
    return idu;
  }

  @Override
  public InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, PgaConfirmationDto content)
      throws JsonProcessingException {
    InterfaceDataUnit01Dto idu = convert(pgaId, sourceCode, destinationCode, msgId,
        HeaderDto.Msgtype.PGA_CONFIRMATION, content);
    return idu;
  }

  protected InterfaceDataUnit01Dto convert(String pgaId, String sourceCode, String destinationCode,
      UUID msgId, HeaderDto.Msgtype type, Serializable content) throws JsonProcessingException {
    final String serializedContent = mapper.writeValueAsString(content);
    ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            type,
            msgId,
            false,
            new SPEDSDto(version, reference)),
        serializedContent);

    final String message = mapper.writeValueAsString(pdu);
    final ContextDto context = new ContextDto(
        pgaId,
        sourceCode,
        destinationCode,
        msgId,
        Boolean.FALSE);
    InterfaceDataUnit01Dto idu = new InterfaceDataUnit01Dto(context, message);
    return idu;
  }
}
