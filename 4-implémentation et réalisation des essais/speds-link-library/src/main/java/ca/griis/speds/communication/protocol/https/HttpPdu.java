package ca.griis.speds.communication.protocol.https;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record HttpPdu (
    @JsonProperty("remoteAddr") String remoteAddr,
    @JsonProperty("type") Type type,
    @JsonProperty("id") UUID id,
    @JsonProperty("sdu") String sdu) {
  public enum Type {
    request, response
  }

  @JsonCreator
  public HttpPdu {}
}
