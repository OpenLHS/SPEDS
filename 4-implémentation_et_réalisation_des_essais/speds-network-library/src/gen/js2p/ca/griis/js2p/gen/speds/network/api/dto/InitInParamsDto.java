
package ca.griis.js2p.gen.speds.network.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "options"
})
@Generated("jsonschema2pojo")
public class InitInParamsDto implements Serializable
{

    /**
     * Various options required in order for the Network layer to work properly.
     * (Required)
     * 
     */
    @JsonProperty("options")
    @JsonPropertyDescription("Various options required in order for the Network layer to work properly.")
    @NotNull
    private Object options;
    private final static long serialVersionUID = -4871511996367547627L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public InitInParamsDto() {
    }

    /**
     * 
     * @param options
     *     Various options required in order for the Network layer to work properly.
     */
    public InitInParamsDto(Object options) {
        super();
        this.options = options;
    }

    /**
     * Various options required in order for the Network layer to work properly.
     * (Required)
     * 
     */
    @JsonProperty("options")
    public Object getOptions() {
        return options;
    }

    @Override
    public String toString() {
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
    public boolean equals(Object other) {
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
