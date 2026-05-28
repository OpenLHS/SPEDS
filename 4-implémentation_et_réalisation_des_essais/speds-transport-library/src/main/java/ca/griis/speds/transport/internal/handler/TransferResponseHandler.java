package ca.griis.speds.transport.internal.handler;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.StampDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * "Description brève du composant (classe, interface, ...)"
 *
 * <h3>Historique</h3>
 * <p>
 * XXXX-XX-XX [AS] - Implémentation initiale<br>
 * </p>
 *
 * <h3>Tâches</h3>
 * S.O.
 *
 * @author [AS] ameni.souid@usherbrooke.ca
 * @since
 */
public final class TransferResponseHandler {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(TransferResponseHandler.class);

  private final ObjectMapper objectMapper;
  private final NetworkHost networkHost;
  private final Map<UUID, Boolean> indicatedMessages;
  private final Speds45Dto speds45Dto;
  private final CryptographyService cryptographyService;

  public TransferResponseHandler(ObjectMapper objectMapper, NetworkHost networkHost,
      Map<UUID, Boolean> indicatedMessages, Speds45Dto version,
      CryptographyService cryptographyService) {
    this.objectMapper = objectMapper;
    this.networkHost = networkHost;
    this.indicatedMessages = indicatedMessages;
    this.speds45Dto = version;
    this.cryptographyService = cryptographyService;
  }

  public CompletableFuture<Optional<String>> handle(InterfaceDataUnit34Dto idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    CompletableFuture<Optional<String>> result =
        CompletableFuture.completedFuture(Optional.empty());

    try {
      final Context34Dto ctx34 = idu.getContext();
      final String sdu = idu.getMessage();
      final var options = objectMapper.convertValue(
          ctx34.getOptions(),
          new TypeReference<Map<String, String>>() {});

      final String tn = options.get("TN");
      final UUID msgId = UUID.fromString(tn);

      var removed = indicatedMessages.remove(msgId);
      if (sdu.startsWith("FAILED")) {
        logger.error(Error.IGNORED_ERROR, new RuntimeException(sdu));
      } else if (removed == null) {
        final String exMessage =
            "This tracking number from this transfer response does not match any received message";
        logger.error(Error.IGNORED_ERROR, new RuntimeException(exMessage));
      } else {
        // request vers couche inférieure
        final Context45Dto ctx45 = new Context45Dto(
            ctx34.getDestinationIri(),
            ctx34.getSourceIri(),
            Context45Dto.Service.TRANSFER,
            ServicePrimitive.REQUEST,
            false);

        // Construire TPDU TRA.MSG.REC
        final Header45Dto header45Dto = new Header45Dto(
            Header45Dto.Msgtype.TRA_MSG_REC,
            msgId,
            ctx34.getSourceCode(),
            ctx34.getDestinationCode(),
            speds45Dto);

        // contenu vide pour TRA.MSG.REC
        final String content = "";
        final var hashSdu = cryptographyService.hash(SpedsConfigItemDto.SpedsLayer.TRANSPORT,
            content.getBytes(StandardCharsets.UTF_8));
        final String sealContentSdu = Base64.getEncoder().encodeToString(hashSdu);

        // Création du sceau d'intégrité de l'entête
        final String headerSerialized = objectMapper.writeValueAsString(header45Dto);
        final var traHeader = cryptographyService.hash(SpedsConfigItemDto.SpedsLayer.TRANSPORT,
            headerSerialized.getBytes(StandardCharsets.UTF_8));
        final var sealheaderSdu = Base64.getEncoder().encodeToString(traHeader);
        final var stamp = new StampDto(sealheaderSdu, sealContentSdu);

        final ProtocolDataUnit4TraDto pdu =
            new ProtocolDataUnit4TraDto(header45Dto, stamp, content);

        // Construire TIDU45
        final InterfaceDataUnit45Dto tidu45 = new InterfaceDataUnit45Dto(
            ctx45,
            objectMapper.writeValueAsString(pdu));
        final String tidu45Json = objectMapper.writeValueAsString(tidu45);

        // Attendre la confirmation
        try {
          Optional<String> confirm = networkHost.submitIdu(tidu45Json).get();
          if (confirm.isPresent() && confirm.get().startsWith("FAILED")) {
            logger.error(Error.IGNORED_ERROR, new RuntimeException(confirm.get()));
          }
        } catch (InterruptedException ex) {
          logger.error(Error.IGNORED_ERROR, ex);

          Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
          logger.error(Error.IGNORED_ERROR, ex);
        }
      }
    } catch (JsonProcessingException | RuntimeException ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
