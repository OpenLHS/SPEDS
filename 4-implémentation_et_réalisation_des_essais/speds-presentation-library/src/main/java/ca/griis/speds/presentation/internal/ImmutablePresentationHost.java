/**
 * @file
 * @copyright @@GRIIS_COPYRIGHT@@
 * @licence @@GRIIS_LICENCE@@
 * @version @@GRIIS_VERSION@@
 * @brief @~french Implémentation de la classe PresentationHost.
 * @brief @~english Implementation of the PresentationHost class.
 */

package ca.griis.speds.presentation.internal;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.PresentationHostEvent;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.dispatcher.NotifyDispatcher;
import ca.griis.speds.presentation.internal.dispatcher.SubmitDispatcher;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
 * @brief @~french Offre les services d'un hôte immutable de la couche présentation.
 * @par Détails
 *      - Un choix volontaire a été fait afin de ne pas envelopper (wrap) les classes générées,
 *      dans l'optique de ne pas inutilement alourdir la gestion de la mémoire.
 *      - Ce choix repose sur l'heuristique selon laquelle l'ajout d'un wrapper ne procure pas
 *      de bénéfices suffisants en regard du coût engendré :
 *      - Les objets générés sont directement compatibles avec nos besoins, rendant
 *      l'encapsulation superflue.
 *      - Une couche supplémentaire de wrappers augmenterait l'empreinte mémoire et
 *      la complexité sans justification suffisante.
 *      - La stabilité de la structure des objets générés permet de minimiser
 *      l'impact des évolutions futures sur le code client.
 *
 * @par Modèle
 *      ConcurrentHashMap<PresentationTracking, TrackingInformation> curator : </br>
 *      Contient les informations de suivi contextuelle de la couche pour les deux couches </br>
 *      intéragisseant avec la couche actuelle.
 *      Set<UUID> messageSent = new HashSet<>(); :
 *      Garde en mémoire les identifiants des messages envoyé pour faire la corercpondance </br>
 *      avec les messages de réponses reçu ultérieurement.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-02-18 [MD] - Implémentation des interfaces </br>
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutablePresentationHost implements PresentationHost, SessionHostEvent {
  private static final GriisLogger logger = getLogger(ImmutablePresentationHost.class);

  private final SubmitDispatcher submitDispatcher;
  private final NotifyDispatcher notifyDispatcher;
  private final SessionHost host;
  private final PresentationHostEvent presentationHostConsumer;
  private final Cache<PresentationTracking, TrackingInformation> serverTracking;

  public ImmutablePresentationHost(SessionHost host, VersionDto version,
      CryptographyService service, PresentationHostEvent presentationHostConsumer,
      Cache<PresentationTracking, TrackingInformation> serverTracking) {
    this.presentationHostConsumer = presentationHostConsumer;
    this.submitDispatcher = new SubmitDispatcher(version, service, host, serverTracking.asMap());
    this.notifyDispatcher = new NotifyDispatcher(service, host, serverTracking.asMap());
    this.host = host;
    this.serverTracking = serverTracking;
  }

  @Override
  public void close() {
    host.close();
    serverTracking.invalidateAll();
    serverTracking.cleanUp();
  }

  @Override
  public CompletableFuture<Optional<String>> submitIdu(String idu) throws JsonProcessingException {
    Optional<String> result = submitDispatcher.handle(idu);
    return CompletableFuture.completedFuture(result);
  }

  @Override
  public void notifyIdu(String idu) {
    try {
      InterfaceDataUnit23Dto sesIdu =
          SharedObjectMapper.getInstance().getMapper().readValue(idu, InterfaceDataUnit23Dto.class);
      Optional<String> result = notifyDispatcher.handle(sesIdu);

      if (result.isPresent()) {
        presentationHostConsumer.notifyIdu(result.get());
      } else {
        RuntimeException ex =
            new RuntimeException("An IDU23 was received by the host, with no resulting IDU12.");
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }
  }

  @Override
  public void notifyException(Exception exception) {
    presentationHostConsumer.notifyException(exception);
  }

  @Override
  public void notifyInitiatorSessionTerminatedSuccessfully(SessionId sessionId) {
    logger.info(Info.VARIABLE_LOGGING_1, "sessionId", sessionId);
  }

  @Override
  public void notifyPeerSessionTerminatedSuccessfully(SessionId sessionId) {
    logger.info(Info.VARIABLE_LOGGING_1, "sessionId", sessionId);
  }
}
