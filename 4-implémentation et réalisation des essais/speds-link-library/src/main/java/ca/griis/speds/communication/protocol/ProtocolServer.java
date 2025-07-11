
package ca.griis.speds.communication.protocol;

import java.io.IOException;

public interface ProtocolServer {
  ProtocolIdu indicate() throws IOException, InterruptedException;

  void response(ProtocolIdu idu) throws IOException, InterruptedException;

  void close();
}
