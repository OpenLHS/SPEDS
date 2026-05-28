
package ca.griis.js2p.gen.speds.presentation.api.dto;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "options"
})
@Generated("jsonschema2pojo")
public class InitInParamsDto implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("options")
    @Valid
    @NotNull
    private Map<String, Object> options;
    private final static long serialVersionUID = 7489624027368058657L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public InitInParamsDto() {
    }

    public InitInParamsDto(Map<String, Object> options) {
        super();
        this.options = options;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("options")
    public Map<String, Object> getOptions() {
        return options;
    }

    @Override
    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(InitInParamsDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("options");
        sb.append('=');
        sb.append(((this.options == null)?"<null>":this.options));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.options == null)? 0 :this.options.hashCode()));
        return result;
    }

    @Override
    public boolean equals(java.lang.Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InitInParamsDto) == false) {
            return false;
        }
        InitInParamsDto rhs = ((InitInParamsDto) other);
        return ((this.options == rhs.options)||((this.options!= null)&&this.options.equals(rhs.options)));
    }

}
