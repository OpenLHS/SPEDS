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
import ca.griis.speds.transport.internal.identification.IdentifierGenerator;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
public final class TransferRequestHandler {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(TransferRequestHandler.class);

  private final IdentifierGenerator identifierGenerator;
  private final Speds45Dto speds45Dto;
  private final ObjectMapper objectMapper;
  private final NetworkHost networkHost;
  private final CryptographyService cryptographyService;
  private final Map<UUID, Boolean> requestedMessages;
  private final ConfirmationRegistry confirmationRegistry;

  public TransferRequestHandler(IdentifierGenerator identifierGenerator, Speds45Dto version,
      ObjectMapper objectMapper, NetworkHost networkHost, CryptographyService cryptographyService,
      Map<UUID, Boolean> requestedMessages, ConfirmationRegistry confirmationRegistry) {
    this.identifierGenerator = identifierGenerator;
    this.speds45Dto = version;
    this.objectMapper = objectMapper;
    this.networkHost = networkHost;
    this.cryptographyService = cryptographyService;
    this.requestedMessages = requestedMessages;
    this.confirmationRegistry = confirmationRegistry;
  }

  public CompletableFuture<Optional<String>> handle(InterfaceDataUnit34Dto idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    // sauvgarder le message

    final UUID traId = identifierGenerator.generateId();

    // Récupérer le ICI et SDU de l'idu-3-4
    // préparer le message pour la couche inférieur
    try {
      final Context34Dto ici34 = idu.getContext();
      final String sduSes = idu.getMessage();

      // Création du sceau d'intégrité du sdu reçu
      final var hashSdu = cryptographyService.hash(SpedsConfigItemDto.SpedsLayer.TRANSPORT,
          sduSes.getBytes(StandardCharsets.UTF_8));
      final String sealContentSdu = Base64.getEncoder().encodeToString(hashSdu);
      // Construire le l'entête du message transport
      final String sourceCode = idu.getContext().getSourceCode();
      final String destinationCode = idu.getContext().getDestinationCode();

      // header
      final Header45Dto header45Dto = new Header45Dto(
          Header45Dto.Msgtype.TRA_MSG_ENV,
          traId,
          sourceCode,
          destinationCode,
          speds45Dto);

      // Création du sceau d'intégrité de l'entête du traMsgEnv
      final String traMsgHeaderSerialized =
          SharedObjectMapper.getInstance().getMapper().writeValueAsString(header45Dto);
      final var traHeader = cryptographyService.hash(SpedsConfigItemDto.SpedsLayer.TRANSPORT,
          traMsgHeaderSerialized.getBytes(StandardCharsets.UTF_8));
      final var sealheaderSdu = Base64.getEncoder().encodeToString(traHeader);
      final var traStamp = new StampDto(sealheaderSdu, sealContentSdu);

      // pdu
      final var pduDto = new ProtocolDataUnit4TraDto(header45Dto, traStamp, sduSes);

      final Context45Dto context = new Context45Dto(ici34.getSourceIri(),
          ici34.getDestinationIri(), Context45Dto.Service.TRANSFER, ServicePrimitive.REQUEST,
          Boolean.FALSE);

      final String serialTraMessage =
          SharedObjectMapper.getInstance().getMapper().writeValueAsString(pduDto);
      final var tiduDto =
          new InterfaceDataUnit45Dto(context, serialTraMessage);
      final String tidu =
          SharedObjectMapper.getInstance().getMapper().writeValueAsString(tiduDto);

      requestedMessages.putIfAbsent(traId, true);

      // envoi message IDU45
      final CompletableFuture<Optional<String>> futureConfirm = networkHost.submitIdu(tidu);

      return futureConfirm.thenCompose(confirmOpt -> {
        if (confirmOpt.isEmpty()) {
          throw new RuntimeException(
              "No confirmation is accessible from the request on the network layer");
        }

        CompletableFuture<Optional<String>> result;
        try {
          final InterfaceDataUnit45Dto confirmIdu =
              objectMapper.readValue(confirmOpt.get(), InterfaceDataUnit45Dto.class);
          final String content = (confirmIdu.getMessage() == null) ? "" : confirmIdu.getMessage();

          if (content.startsWith("FAILED")) {
            Context34Dto confirmContext = new Context34Dto(
                sourceCode,
                destinationCode,
                ici34.getSourceIri(),
                Context34Dto.Service.TRANSFER,
                ServicePrimitive.CONFIRM,
                ici34.getDestinationIri(),
                Boolean.FALSE);

            final InterfaceDataUnit34Dto confirm34 =
                new InterfaceDataUnit34Dto(confirmContext, content);
            final String confirm34Json = objectMapper.writeValueAsString(confirm34);
            result = CompletableFuture.completedFuture(Optional.of(confirm34Json));
          } else {
            result = confirmationRegistry.register(traId).thenApply(Optional::ofNullable);
          }

          return result;
        } catch (Exception e) {
          logger.error(Error.IGNORED_ERROR, e);

          throw new RuntimeException(
              "No confirmation is accessible from the request on the network layer.");
        }
      });

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
