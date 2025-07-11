/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ProtocolDataUnitConverter.
 * @brief @~english Contains description of ProtocolDataUnitConverter class.
 */

package ca.griis.speds.api.converter;

import ca.griis.js2p.gen.speds.application.api.dto.CommandeEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.CommandeReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.DataEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.DataReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.ExceptionEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.ExceptionReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.PgaConfirmationDto;
import ca.griis.js2p.gen.speds.application.api.dto.PgaFinDto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.StatutEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.StatutReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheReceptionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
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
 * @brief @~french Définit un convertisseur d'unité de données de protocole.
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
public final class ProtocolDataUnitConverter {
  private final ObjectMapper mapper;

  public ProtocolDataUnitConverter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de tâches - Convertit l'unité de travail du protocole en un
   *        envoi de tâche.
   * @param pdu L'unité de travail du protocole.
   * @return Un envoi de tâche.
   *
   * @par Tâches
   *      S.O.
   */
  public TacheEnvoiDto convertToTacheEnvoiDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, TacheEnvoiDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de tâches - Convertit l'unité de travail du protocole en une
   *        réception de tâche.
   * @param pdu L'unité de travail du protocole.
   * @return Une réception de tâche.
   *
   * @par Tâches
   *      S.O.
   */
  public TacheReceptionDto convertToTacheReceptionDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, TacheReceptionDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole de fin de tâches - Convertit l'unité de travail du protocole en un
   *        envoi de fin de tâche.
   * @param pdu L'unité de travail du protocole.
   * @return Une envoi de fin de tâche.
   *
   * @par Tâches
   *      S.O.
   */
  public FinEnvoiDto convertToFinEnvoiDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, FinEnvoiDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole de fin de tâches - Convertit l'unité de travail du protocole en une
   *        réception de fin de tâche.
   * @param pdu L'unité de travail du protocole.
   * @return Une réception de fin tâche.
   *
   * @par Tâches
   *      S.O.
   */
  public FinReceptionDto convertToFinReceptionDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, FinReceptionDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'exception de tâches - Convertit l'unité de travail du protocole en
   *        un envoi d'une exception de tâche.
   * @param pdu L'unité de travail du protocole.
   * @return Un envoi d'une exception de tâche.
   *
   * @par Tâches
   *      S.O.
   */
  public ExceptionEnvoiDto convertToExceptionEnvoiDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, ExceptionEnvoiDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'exception de tâches - Convertit l'unité de travail du protocole en
   *        une
   *        réception d'une exception de tâche.
   * @param pdu L'unité de travail du protocole.
   * @return Une réception d'une exception de tâche.
   *
   * @par Tâches
   *      S.O.
   */
  public ExceptionReceptionDto convertToExceptionReceptionDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, ExceptionReceptionDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de données - Convertit l'unité de travail du protocole en un
   *        envoi de données.
   * @param pdu L'unité de travail du protocole.
   * @return Un envoi de données.
   *
   * @par Tâches
   *      S.O.
   */
  public DataEnvoiDto convertToDataEnvoiDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, DataEnvoiDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de données - Convertit l'unité de travail du protocole en une
   *        réception de données.
   * @param pdu L'unité de travail du protocole.
   * @return Une réception de données.
   *
   * @par Tâches
   *      S.O.
   */
  public DataReceptionDto convertToDataReceptionDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, DataReceptionDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de statuts - Convertit l'unité de travail du protocole en un
   *        envoi de statut.
   * @param pdu L'unité de travail du protocole.
   * @return Un envoi de statut.
   *
   * @par Tâches
   *      S.O.
   */
  public StatutEnvoiDto convertToStatutEnvoiDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, StatutEnvoiDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de statuts - Convertit l'unité de travail du protocole en une
   *        réception de statut.
   * @param pdu L'unité de travail du protocole.
   * @return Une réception de statut.
   *
   * @par Tâches
   *      S.O.
   */
  public StatutReceptionDto convertToStatutReceptionDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, StatutReceptionDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de commande - Convertit l'unité de travail du protocole en un
   *        envoi de commande.
   * @param pdu L'unité de travail du protocole.
   * @return Un envoi de commande.
   *
   * @par Tâches
   *      S.O.
   */
  public CommandeEnvoiDto convertToCommandeEnvoiDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, CommandeEnvoiDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de commande - Convertit l'unité de travail du protocole en
   *        une
   *        réception de commande.
   * @param pdu L'unité de travail du protocole.
   * @return Une réception de commande.
   *
   * @par Tâches
   *      S.O.
   */
  public CommandeReceptionDto convertToCommandeReceptionDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, CommandeReceptionDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de fin de PGA - Convertit l'unité de travail du protocole en
   *        un envoi de fin de PGA.
   * @param pdu L'unité de travail du protocole.
   * @return Un envoi de fin de PGA.
   *
   * @par Tâches
   *      S.O.
   */
  public PgaFinDto convertToPgaFinDto(ProtocolDataUnit1APPDto pdu) throws JsonProcessingException {
    var content = convert(pdu, PgaFinDto.class);
    return content;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Protocole d'envoi de fin de PGA - Convertit l'unité de travail du protocole en
   *        une confirmation de fin de PGA.
   * @param pdu L'unité de travail du protocole.
   * @return Une confirmation de fin PGA.
   *
   * @par Tâches
   *      S.O.
   */
  public PgaConfirmationDto convertToPgaConfirmationDto(ProtocolDataUnit1APPDto pdu)
      throws JsonProcessingException {
    var content = convert(pdu, PgaConfirmationDto.class);
    return content;
  }

  protected <T> T convert(ProtocolDataUnit1APPDto pdu, Class<T> valueType)
      throws JsonProcessingException {
    var content = mapper.readValue(pdu.getContent(), valueType);
    return content;
  }
}
