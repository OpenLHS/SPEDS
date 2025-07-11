/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe MessageDispatcher.
 * @brief @~english Implementation of the MessageDispatcher class.
 */

package ca.griis.speds.session.internal.processing;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.exception.DeserializationException;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.handler.HandlerRegistry;
import ca.griis.speds.session.internal.handler.MessageHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * @brief @~french Implémentation d’un répartiteur interne de messages
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
public final class MessageDispatcher {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(MessageDispatcher.class);

  private final ObjectMapper sharedMapper;
  private final Map<MsgType, MessageHandler> handlerMap = new EnumMap<>(MsgType.class);

  public MessageDispatcher(ObjectMapper sharedMapper) {
    logger.trace(Trace.ENTER_METHOD_1, "sharedMapper", sharedMapper);
    this.sharedMapper = sharedMapper;
  }

  public void dispatch(String msg) throws DeserializationException {
    logger.trace(Trace.ENTER_METHOD_1, "msg", msg);

    ExpandedSidu expandedSidu;

    try {
      Sidu sidu = sharedMapper.readValue(msg, Sidu.class);
      Spdu spdu = sharedMapper.readValue(sidu.getMessage(), Spdu.class);
      expandedSidu = new ExpandedSidu(sidu, spdu, MsgType.from(spdu.getHeader().getMsgtype()));
    } catch (JsonProcessingException e) {
      throw new DeserializationException("Message non-désérialisable", e);
    }

    MessageHandler handler = handlerMap.get(expandedSidu.msgType());

    try {
      handler.handle(expandedSidu);
    } catch (SilentIgnoreException e) {
      logger.warn(Error.IGNORED_ERROR, e.getMessage());
    }
    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void registerHandlers(HandlerRegistry handlerRegistry) {
    handlerRegistry.getHandlers().forEach(x -> this.handlerMap.put(x.getHandledType(), x));
  }
}
