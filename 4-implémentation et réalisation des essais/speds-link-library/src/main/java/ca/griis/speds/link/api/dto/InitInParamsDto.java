
package ca.griis.speds.link.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "options"
})
@Generated("jsonschema2pojo")
public class InitInParamsDto implements Serializable {

  @JsonProperty("options")
  @Valid
  @NotNull
  private Map<String, Object> options;
  private static final long serialVersionUID = -2515935114264120861L;

  public InitInParamsDto() {}

  public InitInParamsDto(Map<String, Object> options) {
    super();
    this.options = options;
  }

  @JsonProperty("options")
  public Map<String, Object> getOptions() {
    return options;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(InitInParamsDto.class.getName()).append('[');
    sb.append("options");
    sb.append('=');
    sb.append((this.options == null) ? "<null>" : this.options);
    sb.append(']');
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = result * 31 + (this.options == null ? 0 : this.options.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof InitInParamsDto == false) {
      return false;
    }
    InitInParamsDto rhs = (InitInParamsDto) other;
    return this.options == rhs.options
        || this.options != null && this.options.equals(rhs.options);
  }
}
