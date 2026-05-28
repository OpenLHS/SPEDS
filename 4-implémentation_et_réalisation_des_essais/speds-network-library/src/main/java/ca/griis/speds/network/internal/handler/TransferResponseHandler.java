/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe TransferResponseHandler.
 * @brief @~english Contains description of TransferResponseHandler class.
 */

package ca.griis.speds.network.internal.handler;

import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.Host;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException0;
import org.apache.jena.iri.IRIFactory;

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
 * @brief @~french Traite un transfert d'une réponse.
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
 *      2026-02-18 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class TransferResponseHandler {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(TransferResponseHandler.class);
  private static final IRIFactory iriFactory = IRIFactory.iriImplementation();

  private final ObjectMapper objectMapper;
  private final Host host;
  private final Map<UUID, Boolean> indicatedMessages;

  public TransferResponseHandler(ObjectMapper objectMapper, Host host,
      Map<UUID, Boolean> indicatedMessages) {
    this.objectMapper = objectMapper;
    this.host = host;
    this.indicatedMessages = indicatedMessages;
  }

  public Optional<String> handle(InterfaceDataUnit45Dto idu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> result = Optional.empty();
    String niduMessage = idu.getMessage();

    try {
      final var params = objectMapper.convertValue(idu.getContext().getOptions(),
          new TypeReference<Map<String, String>>() {});
      final String tn = params.get("TN");
      final UUID msgId = UUID.fromString(tn);

      final IRI currentIri = iriFactory.construct(params.getOrDefault("IRI", "invalid"));
      final IRI destIri = iriFactory.construct(idu.getContext().getDestinationIri());

      if (indicatedMessages.remove(msgId) == null) {
        final var exMessage = "This tracking number from this transfer response does "
            + "not match any received message identifier, or "
            + "the message has already been processed by your response.";
        final var ex = new RuntimeException(exMessage);
        logger.error(Error.IGNORED_ERROR, ex);

        niduMessage = "FAILED: " + exMessage;
      } else if (destIri.equals(currentIri) == false) {
        final var exMessage =
            "This IRI from this transfer response does not match the destination IRI.";
        final var ex = new RuntimeException(exMessage);
        logger.error(Error.IGNORED_ERROR, ex);

        niduMessage = "FAILED: " + exMessage;
      }
    } catch (IllegalArgumentException ex) {
      logger.error(Error.IGNORED_ERROR, ex);

      niduMessage = "FAILED: Impossible to get the tracking number from the options IDU.";
    } catch (IRIException0 ex) {
      logger.error(Error.IGNORED_ERROR, ex);

      niduMessage = "FAILED: Invalid IRI.";
    }

    final var nici = new Context56Dto(idu.getContext().getSourceIri(), "transfer",
        Context56Dto.ServicePrimitive.RESPONSE, false);
    final var niduDto = new InterfaceDataUnit56Dto(nici, niduMessage);
    final var nidu = objectMapper.writeValueAsString(niduDto);

    try {
      result = host.submitIdu(nidu).get();
    } catch (InterruptedException ex) {
      logger.error(Error.IGNORED_ERROR, ex);

      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
