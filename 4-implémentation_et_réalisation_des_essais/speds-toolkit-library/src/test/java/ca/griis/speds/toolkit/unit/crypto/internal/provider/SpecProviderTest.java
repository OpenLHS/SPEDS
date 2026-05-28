package ca.griis.speds.toolkit.unit.crypto.internal.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.js2p.gen.speds.toolkit.api.dto.CiphersuiteDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.security.api.domain.spec.derivation.HkdfSpec;
import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.speds.toolkit.crypto.internal.provider.SpecProvider;
import ca.griis.speds.toolkit.crypto.internal.reader.CipherSuiteDtoReader;
import ca.griis.speds.toolkit.serializer.ToolkitSharedObjectMapper;
import ca.griis.speds.toolkit.unit.utilities.ConfigCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SpecProviderTest {
  @Test
  void getSpecCipherAlgoFromProfileConfig() throws JsonMappingException, JsonProcessingException {
    final String json = ConfigCreator.createProfileConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var securityService = new DefaultSecurityService();
    var provider = new SpecProvider(securityService, reader);
    var spec = provider.getSpec(SpedsLayer.SESSION, AlgorithmCategory.SYMM, false);
    assertEquals(spec.get().getAlgo(), "AES/GCM/NoPadding");
  }

  @Test
  void getSpecCipherAlgoFromAlgoConfig() throws JsonMappingException, JsonProcessingException {
    final String json = ConfigCreator.createAlgoConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var securityService = new DefaultSecurityService();
    var provider = new SpecProvider(securityService, reader);
    var spec = provider.getSpec(SpedsLayer.SESSION, AlgorithmCategory.SYMM, false);
    assertEquals(spec.get().getAlgo(), "AES/GCM/NoPadding");
  }

  @Test
  void getSpecGenCipherAlgoFromProfileConfig()
      throws JsonMappingException, JsonProcessingException {
    final String json = ConfigCreator.createProfileConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var securityService = new DefaultSecurityService();
    var provider = new SpecProvider(securityService, reader);
    var spec = provider.getSpec(SpedsLayer.SESSION, AlgorithmCategory.SYMM, true);
    assertEquals(spec.get().getAlgo(), "AES");
    assertEquals(spec.get().getParameters(), Map.of("keyBitLength", "256"));
  }

  @Test
  void getSpecGenCipherAlgoFromAlgoConfig()
      throws JsonMappingException, JsonProcessingException {
    final String json = ConfigCreator.createAlgoConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var securityService = new DefaultSecurityService();
    var provider = new SpecProvider(securityService, reader);
    var spec = provider.getSpec(SpedsLayer.SESSION, AlgorithmCategory.SYMM, true);
    assertEquals(spec.get().getAlgo(), "AES");
    assertEquals(spec.get().getParameters(), Map.of("keyBitLength", "256"));
  }

  @Test
  void getDhSpecCipherAlgoFromProfileConfig() throws JsonMappingException, JsonProcessingException {
    final String json = ConfigCreator.createProfileConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var securityService = new DefaultSecurityService();
    var provider = new SpecProvider(securityService, reader);
    var spec = provider.getSpec(SpedsLayer.SESSION, AlgorithmCategory.DH, false);
    assertEquals(spec.get().getAlgo(), "X25519");

    Map<String, String> params = Map.of(
        "keyBitLength", "256",
        "hkdfSpec", new HkdfSpec("SHA-256").toString(),
        "hmac", "SHA-256");
    assertEquals(spec.get().getParameters(), params);
  }

  @Test
  void getDhSpecCipherAlgoFromAlgoConfig() throws JsonMappingException, JsonProcessingException {
    final String json = ConfigCreator.createAlgoConfig();
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(json, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    final var securityService = new DefaultSecurityService();
    var provider = new SpecProvider(securityService, reader);
    var spec = provider.getSpec(SpedsLayer.SESSION, AlgorithmCategory.DH, false);
    assertEquals(spec.get().getAlgo(), "X25519");

    Map<String, String> params = Map.of(
        "keyBitLength", "256",
        "hkdfSpec", new HkdfSpec("SHA-256").toString(),
        "hmac", "SHA-256");
    assertEquals(spec.get().getParameters(), params);
  }
}
