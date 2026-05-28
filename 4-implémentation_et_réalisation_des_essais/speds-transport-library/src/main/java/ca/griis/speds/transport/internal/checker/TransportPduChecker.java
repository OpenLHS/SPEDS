package ca.griis.speds.transport.internal.checker;

import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Info;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.security.StampVerifier;

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
public final class TransportPduChecker {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(TransportPduChecker.class);

  private final StampVerifier stampVerifier;

  public TransportPduChecker(CryptographyService service) {
    this.stampVerifier = new StampVerifier(service);
  }

  public PduCheckerResult check(String entityCode, ProtocolDataUnit4TraDto pdu) {
    PduCheckerResult result;
    if (!stampVerifier.verifyHeader(pdu)) {
      result = new PduCheckerResult(false, "FAILED: Header seal are not the same");
    } else if (!stampVerifier.verifyContent(pdu)) {
      result = new PduCheckerResult(false, "FAILED: Content seal are not the same");
    } else {
      result = new PduCheckerResult(true, "SUCCEED");
    }

    logger.info(Info.VARIABLE_LOGGING_5,
        "entityCode", entityCode,
        "msgId", pdu.getHeader().getId(),
        "msgType", pdu.getHeader().getMsgtype(),
        "check", result.isValid(),
        "message", result.message());

    return result;
  }
}
