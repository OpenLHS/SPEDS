package ca.griis.speds.toolkit.unit.crypto.internal.reader;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.toolkit.api.dto.CiphersuiteDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.speds.toolkit.crypto.internal.reader.CipherSuiteDtoReader;
import ca.griis.speds.toolkit.serializer.ToolkitSharedObjectMapper;
import ca.griis.speds.toolkit.unit.utilities.ConfigCreator;
import org.junit.jupiter.api.Test;

public class CiphersuiteDtoTest {
  @Test
  void getSecurityProfileEmpty() throws Exception {
    final String json = ConfigCreator.createProfileConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var result = reader.getSecurityProfile(SpedsLayer.NETWORK, AlgorithmCategory.SYMM);

    assertTrue(result.isEmpty());
  }

  @Test
  void getSecurityProfilePresent() throws Exception {
    final String json = ConfigCreator.createProfileConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var result = reader.getSecurityProfile(SpedsLayer.NETWORK, AlgorithmCategory.SIGN);

    assertTrue(result.isPresent());
    assertTrue(result.get().name().equals("STRONG"));
  }

  @Test
  void getSecurityAlgoEmpty() throws Exception {
    final String json = ConfigCreator.createAlgoConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    var result = reader.getSecurityAlgo(SpedsLayer.NETWORK, AlgorithmCategory.SYMM);

    assertTrue(result.isEmpty());
  }

  @Test
  void getSecurityAlgoPresent() throws Exception {
    final String json = ConfigCreator.createAlgoConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var result = reader.getSecurityAlgo(SpedsLayer.SESSION, AlgorithmCategory.SYMM);

    assertTrue(result.isPresent());
    assertTrue(result.get().equals("AES256-GCM"));
  }
}
