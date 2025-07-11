package ca.griis.speds.integration.util;

import ca.griis.speds.session.api.PgaService;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import org.mockito.Mockito;

public class PgaServiceMock {

  public void mock(PgaService pgaService, int sourceport, int targetport)
      throws CertificateException {
    String pgaId = "someprojectnumber";
    String sourceCode = "source";
    String destinationCode = "destination";
    String sourceIri = "https://localhost:" + sourceport;
    String destinationIri = "https://localhost:" + targetport;

    mock(pgaService, pgaId, sourceCode, destinationCode, sourceIri, destinationIri);
  }


  public void mock(PgaService pgaService, String pgaId, String sourceCode, String destinationCode,
      String sourceIri, String destinationIri) throws CertificateException {

    String certificatePem = KeyVar.localhostCertRsa;

    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    Certificate certificate = cf
        .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));
    byte[] bytes = certificate.getPublicKey().getEncoded();
    String key = Base64.getEncoder().encodeToString(bytes);

    Mockito.when(pgaService.getIri(pgaId, sourceCode)).thenReturn(sourceIri);
    Mockito.when(pgaService.getPublicKey(pgaId, sourceCode)).thenReturn(key);
    Mockito.when(pgaService.getIri(pgaId, destinationCode)).thenReturn(destinationIri);
    Mockito.when(pgaService.getPublicKey(pgaId, destinationCode)).thenReturn(key);
    Mockito.when(pgaService.verifyLegitimacy(pgaId, sourceCode, key)).thenReturn(true);
  }
}
