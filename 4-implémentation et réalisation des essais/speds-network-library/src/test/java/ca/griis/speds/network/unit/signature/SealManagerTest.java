package ca.griis.speds.network.unit.signature;

import static ca.griis.speds.network.unit.signature.SignatureProvider.getCertificatePem;
import static ca.griis.speds.network.unit.signature.SignatureProvider.getKeyPem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import ca.griis.js2p.gen.speds.network.api.dto.SPEDSDto;
import ca.griis.speds.network.serialization.SharedObjectMapper;
import ca.griis.speds.network.service.exception.SerializationException;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.Seal;
import ca.griis.speds.network.signature.SealManager;
import java.security.interfaces.RSAPrivateKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SealManagerTest {
  private SealManager sealManager;

  private CertificatePrivateKeyPair cpkp;

  @BeforeEach
  public void setUp() throws Exception {
    sealManager =
        new SealManager(SharedObjectMapper.getInstance().getMapper());

    cpkp = CertificatePrivateKeyPair.importFromPem(getCertificatePem(), getKeyPem());
  }

  @Test
  public void sealTestStringSuccess() throws Exception {
    final Object obj = "someObject";
    String seal = sealManager.createSeal(obj, Seal.header, cpkp.privateKey());
    assertTrue(sealManager.verifySeal(obj, Seal.header, cpkp.getAuthentification(), seal));
  }

  @Test
  public void sealTestObjectSuccess() throws Exception {
    final Object obj = new SPEDSDto("version", "reference");
    String seal = sealManager.createSeal(obj, Seal.header, cpkp.privateKey());
    assertTrue(sealManager.verifySeal(obj, Seal.header, cpkp.getAuthentification(), seal));
  }

  @Test
  public void createSealException() throws Exception {
    assertThrows(SerializationException.class,
        () -> sealManager.createSeal("someObject", Seal.content, mock(RSAPrivateKey.class)));
  }

  @Test
  public void createSeal2Exception() throws Exception {
    assertFalse(sealManager.verifySeal("someObject", Seal.content, cpkp.getAuthentification(),
        "badsignature"));
  }
}
