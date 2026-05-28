package ca.griis.speds.network.unit.internal.checker;

import static org.junit.jupiter.api.Assertions.assertFalse;

import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5NETDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.speds.link.internal.serializer.SharedObjectMapper;
import ca.griis.speds.network.internal.checker.NetworkPduChecker;
import ca.griis.speds.network.util.SecurityUtils;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkPduCheckerTest {
  private NetworkPduChecker checker;

  @BeforeEach
  public void setUp() throws Exception {
    CryptographyService service = SecurityUtils.createCryptographyService();
    checker = new NetworkPduChecker(service, SharedObjectMapper.getInstance().getMapper());
  }

  @Test
  public void checkWhenInvalidPdu() throws Exception {
    var header = new HeaderDto();
    var stamp = new StampDto();
    var content = "yeah!";
    ProtocolDataUnit5NETDto pdu = new ProtocolDataUnit5NETDto(header, stamp, content);

    var result = checker.check(pdu, "https://localhost:8080");

    assertFalse(result.isValid());
  }
}
