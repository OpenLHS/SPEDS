
package ca.griis.speds.network.unit.signature;


import static ca.griis.speds.network.unit.signature.SignatureProvider.getCertificatePem;
import static ca.griis.speds.network.unit.signature.SignatureProvider.getKeyPem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import ca.griis.speds.network.service.exception.ParameterException;
import ca.griis.speds.network.service.exception.SerializationException;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CertificatePrivateKeyPairTest {
  @Test
  public void cpkpSuccess() throws Exception {
    // Given
    CertificatePrivateKeyPair cpkp =
        CertificatePrivateKeyPair.importFromPem(getCertificatePem(), getKeyPem());
    assertEquals(getCertificatePem(), cpkp.getAuthentification());
  }

  @Test
  public void certificateEncodingException() throws Exception {
    Certificate certificate = mock(Certificate.class);
    PrivateKey privateKey = mock(PrivateKey.class);

    CertificatePrivateKeyPair cpkp = new CertificatePrivateKeyPair(certificate, privateKey);
    doThrow(CertificateEncodingException.class).when(certificate).getEncoded();

    assertThrows(SerializationException.class, () -> cpkp.getAuthentification());
  }

  @Test
  public void badkeyException() throws Exception {
    assertThrows(ParameterException.class,
        () -> CertificatePrivateKeyPair.importFromPem(getCertificatePem(), "badkey"));
  }
}
