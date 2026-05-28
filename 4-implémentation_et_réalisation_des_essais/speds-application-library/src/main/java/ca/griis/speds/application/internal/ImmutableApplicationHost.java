/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ImmutableApplicationHost.
 * @brief @~english Contains description of ImmutableApplicationHost class.
 */

package ca.griis.speds.application.internal;

import static ca.griis.js2p.gen.speds.application.api.dto.Service.DELEGATE;
import static ca.griis.js2p.gen.speds.application.api.dto.Service.TRANSFER;
import static ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive.CONFIRM;
import static ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive.INDICATION;
import static ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive.REQUEST;
import static ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive.RESPONSE;

import ca.griis.js2p.gen.speds.application.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.ApplicationHostEvent;
import ca.griis.speds.application.internal.domain.ApplicationInterface;
import ca.griis.speds.application.internal.verification.InterfaceChecker;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.toolkit.project.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
 * @brief @~french Implémentation synchrone de l'interface du service ApplicationHost
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
 *      2025-01-28 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutableApplicationHost implements ApplicationHost {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ImmutableApplicationHost.class);

  private final PresentationHost host;
  private final ApplicationHostEvent consumer;
  private final ProjectService projectService;
  private final VersionDto version;
  private final InterfaceChecker interfaceChecker;

  ImmutableApplicationHost(PresentationHost host, ApplicationHostEvent consumer,
      ProjectService projectService, VersionDto version, InterfaceChecker interfaceChecker) {
    logger.trace(Trace.ENTER_METHOD_5, "host", host, "consumer", consumer, "projectService",
        projectService, "version", version, "interfaceChecker", interfaceChecker);
    this.host = host;
    this.consumer = consumer;
    this.projectService = projectService;
    this.version = version;
    this.interfaceChecker = interfaceChecker;
  }

  @Override
  public CompletableFuture<ApplicationInterface> submit(ApplicationInterface applicationInterface) {
    ApplicationInterface result;

    if (!interfaceChecker.test(applicationInterface.content())) {
      result = new ApplicationInterface(DELEGATE, CONFIRM,
          applicationInterface.sourceCode(),
          applicationInterface.destinationCode(),
          applicationInterface.projectId(),
          applicationInterface.msgId(),
          applicationInterface.msgType(),
          "FAILED: syntax");

      logger.info(Info.VARIABLE_LOGGING_5,
          "entityCode", applicationInterface.sourceCode(),
          "msgId", applicationInterface.msgId(),
          "msgType", applicationInterface.msgType(),
          "check", false,
          "message", "FAILED: syntax");
    } else if (!projectService.checkPlanActivity(applicationInterface.projectId())) {
      result = new ApplicationInterface(DELEGATE, CONFIRM,
          applicationInterface.sourceCode(),
          applicationInterface.destinationCode(),
          applicationInterface.projectId(),
          applicationInterface.msgId(),
          applicationInterface.msgType(),
          "FAILED: ended plan");

      logger.info(Info.VARIABLE_LOGGING_5,
          "entityCode", applicationInterface.sourceCode(),
          "msgId", applicationInterface.msgId(),
          "msgType", applicationInterface.msgType(),
          "check", false,
          "message", "FAILED: ended plan");
    } else {
      try {
        logger.info(Info.VARIABLE_LOGGING_5,
            "entityCode", applicationInterface.sourceCode(),
            "msgId", applicationInterface.msgId(),
            "msgType", applicationInterface.msgType(),
            "check", true,
            "message", "SUCCEED");

        final Map<String, Object> content = SharedObjectMapper.getInstance().getMapper()
            .readValue(applicationInterface.content(), new TypeReference<Map<String, Object>>() {});
        final ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
            new HeaderDto(applicationInterface.msgType(),
                UUID.fromString(applicationInterface.msgId()), false,
                version),
            content);
        final String serializedPdu =
            SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);
        final InterfaceDataUnit12Dto idu =
            new InterfaceDataUnit12Dto(new Context12Dto(applicationInterface.projectId(),
                applicationInterface.sourceCode(), applicationInterface.destinationCode(), DELEGATE,
                REQUEST, false), serializedPdu);
        final String request = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu);
        final CompletableFuture<Optional<String>> response = host.submitIdu(request);

        final String serializedConfirm = response.get()
            .orElseThrow(() -> new IllegalArgumentException("Invalid confirm message"));
        final InterfaceDataUnit12Dto iduConfirm = SharedObjectMapper.getInstance().getMapper()
            .readValue(serializedConfirm, InterfaceDataUnit12Dto.class);
        result = new ApplicationInterface(DELEGATE, CONFIRM,
            applicationInterface.sourceCode(),
            applicationInterface.destinationCode(),
            applicationInterface.projectId(),
            applicationInterface.msgId(),
            applicationInterface.msgType(),
            iduConfirm.getMessage());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();

        logger.error(Error.IGNORED_ERROR, e);

        result = new ApplicationInterface(DELEGATE, CONFIRM,
            applicationInterface.sourceCode(),
            applicationInterface.destinationCode(),
            applicationInterface.projectId(),
            applicationInterface.msgId(),
            applicationInterface.msgType(),
            "FAILED: transmission was interrupted.");
      } catch (JsonProcessingException | ExecutionException e) {
        logger.error(Error.IGNORED_ERROR, e);

        result = new ApplicationInterface(DELEGATE, CONFIRM,
            applicationInterface.sourceCode(),
            applicationInterface.destinationCode(),
            applicationInterface.projectId(),
            applicationInterface.msgId(),
            applicationInterface.msgType(),
            "FAILED: unknown confirmation message.");
      }
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return CompletableFuture.completedFuture(result);
  }

  @Override
  public void notifyIdu(String s) {
    final String serializedIdu;
    try {
      InterfaceDataUnit12Dto response;
      final InterfaceDataUnit12Dto idu =
          SharedObjectMapper.getInstance().getMapper().readValue(s, InterfaceDataUnit12Dto.class);
      final ProtocolDataUnit1APPDto pdu = SharedObjectMapper.getInstance().getMapper()
          .readValue(idu.getMessage(), ProtocolDataUnit1APPDto.class);
      final String content =
          SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu.getContent());

      if (!interfaceChecker.test(content)) {
        response =
            new InterfaceDataUnit12Dto(
                new Context12Dto(
                    idu.getContext().getPga(),
                    idu.getContext().getSourceCode(),
                    idu.getContext().getDestinationCode(),
                    TRANSFER, RESPONSE,
                    idu.getContext().getOptions()),
                "FAILED: syntax");

        logger.info(Info.VARIABLE_LOGGING_5,
            "entityCode", idu.getContext().getDestinationCode(),
            "msgId", pdu.getHeader().getId(),
            "msgType", pdu.getHeader().getMsgtype(),
            "check", false,
            "message", "FAILED: syntax");
      } else if (!projectService.checkPlanActivity(idu.getContext().getPga())) {
        response =
            new InterfaceDataUnit12Dto(
                new Context12Dto(
                    idu.getContext().getPga(),
                    idu.getContext().getSourceCode(),
                    idu.getContext().getDestinationCode(),
                    TRANSFER, RESPONSE,
                    idu.getContext().getOptions()),
                "FAILED: ended plan");

        logger.info(Info.VARIABLE_LOGGING_5,
            "entityCode", idu.getContext().getDestinationCode(),
            "msgId", pdu.getHeader().getId(),
            "msgType", pdu.getHeader().getMsgtype(),
            "check", false,
            "message", "FAILED: ended plan");
      } else {
        final ApplicationInterface applicationInterface =
            new ApplicationInterface(
                TRANSFER, INDICATION,
                idu.getContext().getSourceCode(),
                idu.getContext().getDestinationCode(),
                idu.getContext().getPga(),
                pdu.getHeader().getId().toString(),
                pdu.getHeader().getMsgtype(),
                content);
        CompletableFuture<ApplicationInterface> futureResponse =
            consumer.notify(applicationInterface);
        ApplicationInterface consumerResponse = futureResponse.get();
        response = new InterfaceDataUnit12Dto(
            new Context12Dto(consumerResponse.projectId(), consumerResponse.sourceCode(),
                consumerResponse.destinationCode(), TRANSFER, RESPONSE,
                idu.getContext().getOptions()),
            consumerResponse.content());

        logger.info(Info.VARIABLE_LOGGING_5,
            "entityCode", idu.getContext().getDestinationCode(),
            "msgId", pdu.getHeader().getId(),
            "msgType", pdu.getHeader().getMsgtype(),
            "check", true,
            "message", "SUCCEED");
      }

      serializedIdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(response);
      host.submitIdu(serializedIdu);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      logger.error(Error.IGNORED_ERROR, e);
    } catch (JsonProcessingException | ExecutionException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    host.close();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyException(Exception exception) {
    consumer.notifyException(exception);
  }
}
