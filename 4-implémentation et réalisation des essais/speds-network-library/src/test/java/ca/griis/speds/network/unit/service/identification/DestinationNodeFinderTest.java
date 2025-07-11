package ca.griis.speds.network.unit.service.identification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.speds.network.service.identification.DestinationNodeFinder;
import org.junit.jupiter.api.Test;

public class DestinationNodeFinderTest {

  @Test
  public void testNextNodeWhenEnv() {
    String received =
        DestinationNodeFinder.nextNode(HeaderDto.Msgtype.RES_ENV, "source", "destination");
    assertEquals("destination", received);
  }

  @Test
  public void testNextNodeWhenRec() {
    String received =
        DestinationNodeFinder.nextNode(HeaderDto.Msgtype.RES_REC, "source", "destination");
    assertEquals("source", received);
  }
}
