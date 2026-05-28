package ca.griis.speds.transport.unit.internal.serializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.network.internal.serialization.SharedObjectMapper;
import org.junit.jupiter.api.Test;

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
public class SharedObjectMapperTest {
  @Test
  public void mapperTest() throws Exception {
    assertNotNull(SharedObjectMapper.getInstance().getMapper());
  }
}
