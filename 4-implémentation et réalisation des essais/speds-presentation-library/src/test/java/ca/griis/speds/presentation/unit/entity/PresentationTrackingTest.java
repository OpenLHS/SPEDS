package ca.griis.speds.presentation.unit.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.presentation.entity.PresentationTracking;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class PresentationTrackingTest {

  @Test
  public void instanceTest() {
    // Given
    UUID givenId = UUID.randomUUID();

    // When
    PresentationTracking presentationTracking = new PresentationTracking(givenId);

    // Then
    assertNotNull(presentationTracking);
    assertEquals(givenId, presentationTracking.uuid());
  }
}
