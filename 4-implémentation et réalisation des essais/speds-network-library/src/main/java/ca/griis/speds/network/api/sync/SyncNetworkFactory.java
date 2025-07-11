/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SyncNetworkFactory.
 * @brief @~english SyncNetworkFactory class implementation.
 */

package ca.griis.speds.network.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;
import static ca.griis.speds.network.signature.CertificatePrivateKeyPair.importFromPem;

import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.network.api.dto.OptionsDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.link.api.sync.ImmutableDataLinkFactory;
import ca.griis.speds.network.api.NetworkFactory;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.serialization.SharedObjectMapper;
import ca.griis.speds.network.service.exception.ParameterException;
import ca.griis.speds.network.service.host.SentMessageIdSet;
import ca.griis.speds.network.service.identification.IdentifierGenerator;
import ca.griis.speds.network.signature.SealManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * @brief @~french Offre une fabrique pour construire les entités de la couche réseau.
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
 *      2025-02-10 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class SyncNetworkFactory implements NetworkFactory {
  private static final GriisLogger logger = getLogger(SyncNetworkFactory.class);

  private final IdentifierGenerator identifierGenerator;
  private final ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();
  private final ImmutableDataLinkFactory factory;

  public SyncNetworkFactory() {
    this(() -> UUID.randomUUID().toString());
  }

  public SyncNetworkFactory(IdentifierGenerator identifierGenerator) {
    logger.trace(Trace.ENTER_METHOD_1, "identifierGenerator", identifierGenerator);
    this.identifierGenerator = identifierGenerator;
    this.factory = new ImmutableDataLinkFactory();
  }

  @Override
  public NetworkHost initHost(String parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    final OptionsDto params = getInitParams(parameters);
    final String spedsVersion = params.getSpedsVersion();
    final String spedsReference = params.getSpedsReference();
    final String certificatePem = params.getCertificate();
    final String privateKeyPem = params.getPrivateKey();

    final NetworkHost netHost =
        new ImmutableNetworkHost(initDataLinkHost(parameters), spedsVersion, spedsReference,
            importFromPem(certificatePem, privateKeyPem), identifierGenerator,
            new NetworkMarshaller(mapper), new SealManager(mapper), new SentMessageIdSet());
    logger.trace(Trace.EXIT_METHOD_1, "netHost", netHost);
    return netHost;
  }

  @Override
  public DataLinkHost initDataLinkHost(String parameters) {
    return this.factory.init(parameters);
  }

  /**
   * @brief @~english «Description of the function»
   * @param parameters «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Récupère les valeurs sérialisées pour initialiser la couche Réseau.
   * @param parameters Les paramètres sous la forme d'une chaîne de caractères.
   * @return Les valeurs d'initialisation associées à la couche Réseau.
   *
   * @par Tâches
   *      S.O.
   */
  private OptionsDto getInitParams(String parameters) {
    final OptionsDto optionsDto;
    try {
      InitInParamsDto params = mapper.readValue(parameters, new TypeReference<>() {});
      optionsDto = mapper.convertValue(params.getOptions(), OptionsDto.class);
    } catch (JsonProcessingException e) {
      final String exception = "Cannot read initialization parameters: " + e.getMessage();
      logger.error(exception);
      throw new ParameterException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "optionsDto", optionsDto);
    return optionsDto;
  }
}
