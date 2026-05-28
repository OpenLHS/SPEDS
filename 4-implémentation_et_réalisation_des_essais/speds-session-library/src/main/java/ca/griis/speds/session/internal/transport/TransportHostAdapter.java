

package ca.griis.speds.session.internal.transport;

import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
 * @brief @~french Intègre des services cpommuns d'un hôte de transport pour la couche session.
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
 *      2026-03-13 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class TransportHostAdapter {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(TransportHostAdapter.class);

  private final TransportHost transportHost;
  private final ObjectMapper mapper;
  private final ExecutorService executor;
  private final Integer transportConfirmationTimeout;

  public TransportHostAdapter(HostStartupContext ctx) {
    this.transportHost = ctx.transportHost();
    this.mapper = ctx.sharedMapper();
    this.executor = ctx.executor();
    this.transportConfirmationTimeout = ctx.transportConfirmationTimeout();
  }

  public Boolean request(String entityCode, SessionId sessionId, ExpandedSidu expandedSidu) {
    Boolean result = false;

    final String idu;
    try {
      idu = mapper.writeValueAsString(expandedSidu.sidu());
      CompletableFuture<Optional<String>> future = transportHost.submitIdu(idu);

      logger.info(Info.VARIABLE_LOGGING_5,
          "entityCode", entityCode,
          "msgId", expandedSidu.spdu().getHeader().getId(),
          "msgType", expandedSidu.spdu().getHeader().getMsgtype(),
          "sessionId", sessionId,
          "request", "****");

      CompletableFuture.runAsync(
          () -> confirm(future, transportConfirmationTimeout, entityCode, sessionId, expandedSidu),
          executor);

      result = true;
    } catch (IOException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    return result;
  }

  public Boolean response(String entityCode, SessionId sessionId, ExpandedSidu expandedSidu,
      String content) {
    Boolean result = false;
    try {
      final var params = mapper.convertValue(expandedSidu.sidu().getContext().getOptions(),
          new TypeReference<Map<String, String>>() {});
      final var id = params.get("TN");
      final var options = Map.of("TN", id);
      final var indicationCtx = expandedSidu.sidu().getContext();
      var context = new Context34Dto(
          indicationCtx.getSourceCode(),
          indicationCtx.getDestinationCode(),
          indicationCtx.getSourceIri(),
          Context34Dto.Service.TRANSFER,
          ServicePrimitive.RESPONSE,
          indicationCtx.getDestinationIri(),
          options);
      var response = new InterfaceDataUnit34Dto(context, content);
      var idu = mapper.writeValueAsString(response);

      final var future = transportHost.submitIdu(idu);

      logger.info(Info.VARIABLE_LOGGING_5,
          "entityCode", entityCode,
          "msgId", expandedSidu.spdu().getHeader().getId(),
          "msgType", expandedSidu.spdu().getHeader().getMsgtype(),
          "sessionId", sessionId,
          "response", content);

      future.get();

      result = true;
    } catch (InterruptedException e) {
      logger.error(Error.IGNORED_ERROR, e);

      Thread.currentThread().interrupt();
    } catch (JsonProcessingException | ExecutionException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    return result;
  }

  private void confirm(CompletableFuture<Optional<String>> future,
      Integer transportConfirmationTimeout, String entityCode, SessionId sessionId,
      ExpandedSidu expandedSidu) {
    try {
      Optional<String> confirmOpt = future.get(transportConfirmationTimeout, TimeUnit.SECONDS);
      if (confirmOpt.isPresent()) {
        final String confirm = confirmOpt.get();

        logger.info(Info.VARIABLE_LOGGING_5,
            "entityCode", entityCode,
            "msgId", expandedSidu.spdu().getHeader().getId(),
            "msgType", expandedSidu.spdu().getHeader().getMsgtype(),
            "sessionId", sessionId,
            "confirm", confirm);

        if (confirm.contains("FAILED")) {
          logger.error(Error.IGNORED_ERROR,
              new RuntimeException(
                  "The transport confirmation TRA.REC FAILED. Details: " + confirm));
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      logger.error(Error.IGNORED_ERROR, e);
    } catch (ExecutionException | TimeoutException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }
  }
}
