/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe TransferRequestHandler.
 * @brief @~english Contains description of TransferRequestHandler class.
 */

package ca.griis.speds.network.internal.handler;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5NETDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.js2p.gen.speds.network.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.network.internal.identification.IdentifierGenerator;
import ca.griis.speds.network.internal.security.CertificatePrivateKeyPair;
import ca.griis.speds.network.internal.security.SealManager;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

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
 * @brief @~french Traite un transfert d'une requête.
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
public final class TransferRequestHandler {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(TransferRequestHandler.class);

  private final IdentifierGenerator identifierGenerator;
  private final VersionDto version;
  private final CertificatePrivateKeyPair pair;
  private final SealManager sealManager;
  private final ObjectMapper objectMapper;
  private final Host host;

  public TransferRequestHandler(IdentifierGenerator generator, VersionDto version,
      CertificatePrivateKeyPair pair, CryptographyService service, ObjectMapper objectMapper,
      Host host) {
    this.identifierGenerator = generator;
    this.version = version;
    this.pair = pair;
    this.sealManager = new SealManager(service);
    this.objectMapper = objectMapper;
    this.host = host;
  }

  public Optional<String> handle(InterfaceDataUnit45Dto idu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> result = Optional.empty();
    try {
      final var nidu = createRequestTransfer(idu);
      final var future = host.submitIdu(nidu);
      final var linkConfirm = future.get().get();

      final var confirmLsdu = objectMapper.readValue(linkConfirm, InterfaceDataUnit56Dto.class);
      final var confirm = createConfirmTransfer(idu, confirmLsdu.getMessage());

      result = Optional.of(confirm);
    } catch (InterruptedException ex) {
      logger.error(Error.IGNORED_ERROR, ex);

      Thread.currentThread().interrupt();

      final var failed = "FAILED: Interruption exception";
      final var confirm = createConfirmTransfer(idu, failed);
      result = Optional.of(confirm);
    } catch (Exception ex) {
      logger.error(Error.IGNORED_ERROR, ex);

      final var failed = "FAILED: Impossible to submit the IDU";
      final var confirm = createConfirmTransfer(idu, failed);
      result = Optional.of(confirm);
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  private String createRequestTransfer(InterfaceDataUnit45Dto nsdu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "nsdu", nsdu);

    final var id = identifierGenerator.generateId();
    final var sourceIri = nsdu.getContext().getSourceIri();
    final var destinationIri = nsdu.getContext().getDestinationIri();

    final var headerDto = new HeaderDto(HeaderDto.Msgtype.RES_MSG_ENV, id, sourceIri,
        destinationIri, pair.getAuthentification(), false, version);

    final var header = objectMapper.writeValueAsString(headerDto);
    final var headerSeal = sealManager.createSeal(header, pair.getPrivateKey());
    final var contentSeal = sealManager.createSeal(nsdu.getMessage(), pair.getPrivateKey());

    final var pduDto = new ProtocolDataUnit5NETDto(headerDto, new StampDto(headerSeal, contentSeal),
        nsdu.getMessage());
    final var pdu = objectMapper.writeValueAsString(pduDto);

    final var nici =
        new Context56Dto(destinationIri, "transfer", Context56Dto.ServicePrimitive.REQUEST, false);
    final var niduDto = new InterfaceDataUnit56Dto(nici, pdu);
    final var nidu = objectMapper.writeValueAsString(niduDto);

    logger.trace(Trace.EXIT_METHOD_1, "nidu", nidu);
    return nidu;
  }

  private String createConfirmTransfer(InterfaceDataUnit45Dto nsdu, String message)
      throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "nsdu", nsdu);

    var ici =
        new Context45Dto(nsdu.getContext().getSourceIri(), nsdu.getContext().getDestinationIri(),
            "transfer", Context45Dto.ServicePrimitive.CONFIRM, false);
    var idu45 = new InterfaceDataUnit45Dto(ici, message);
    var confirm = objectMapper.writeValueAsString(idu45);

    logger.trace(Trace.EXIT_METHOD_1, "confirm", confirm);
    return confirm;
  }
}
