package ca.griis.speds.presentation.unit.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.presentation.entity.TrackingInformation;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

public class TrackingInformationTest {

  @Test
  public void instanceTest() throws NoSuchAlgorithmException {
    // Given
    UUID givenId = UUID.randomUUID();
    // Any key will do
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(128); // Taille de la clé en bits (par ex. 128 bits)
    SecretKey givenKey = keyGenerator.generateKey();

    // When
    TrackingInformation trackingInformation = new TrackingInformation(givenId, givenKey);

    // Then
    assertNotNull(trackingInformation);
    assertEquals(givenId, trackingInformation.sessionTracking());
    assertEquals(givenKey, trackingInformation.sdek());
  }
}
