package ca.griis.speds.transport.unit.internal.checker;

import static org.junit.jupiter.api.Assertions.assertFalse;

import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.StampDto;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.checker.TransportPduChecker;
import ca.griis.speds.transport.unit.util.SecurityUtils;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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
@ExtendWith(MockitoExtension.class)
public class TransportPduCheckerTest {
  private TransportPduChecker checker;

  @BeforeEach
  public void setUp() throws Exception {
    CryptographyService service = SecurityUtils.createCryptographyService();
    checker = new TransportPduChecker(service);
  }

  @Test
  public void checkWhenInvalidPdu() {
    var header = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        UUID.randomUUID(),
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://reference.iri/speds"));
    var stamp = new StampDto("", "");
    var content = "yeah!";
    ProtocolDataUnit4TraDto pdu = new ProtocolDataUnit4TraDto(header, stamp, content);

    var result = checker.check("DEST_CODE", pdu);

    assertFalse(result.isValid());
  }
}
