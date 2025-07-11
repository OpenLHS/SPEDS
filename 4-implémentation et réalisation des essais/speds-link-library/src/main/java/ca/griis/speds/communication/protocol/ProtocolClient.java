
package ca.griis.speds.communication.protocol;

import java.io.IOException;

public interface ProtocolClient {
  void request(ProtocolIdu idu) throws IOException, InterruptedException;

  ProtocolIdu confirm() throws IOException, InterruptedException;

  void close();
}
