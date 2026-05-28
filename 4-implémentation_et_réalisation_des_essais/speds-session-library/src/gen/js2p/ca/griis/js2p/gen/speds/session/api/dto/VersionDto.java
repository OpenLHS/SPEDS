
package ca.griis.js2p.gen.speds.session.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "number",
    "reference"
})
@Generated("jsonschema2pojo")
public class VersionDto implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("number")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)(\\.[0-9]+[a-z]*)$")
    @NotNull
    private String number;
    /**
     * The reference of the present version of the layer.
     * (Required)
     * 
     */
    @JsonProperty("reference")
    @JsonPropertyDescription("The reference of the present version of the layer.")
    @NotNull
    private String reference;
    private final static long serialVersionUID = 551903186657520746L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public VersionDto() {
    }

    /**
     * 
     * @param reference
     *     The reference of the present version of the layer.
     * @param number
     *     The version of the current layer.
     */
    public VersionDto(String number, String reference) {
        super();
        this.number = number;
        this.reference = reference;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("number")
    public String getNumber() {
        return number;
    }

    /**
     * The reference of the present version of the layer.
     * (Required)
     * 
     */
    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(VersionDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("number");
        sb.append('=');
        sb.append(((this.number == null)?"<null>":this.number));
        sb.append(',');
        sb.append("reference");
        sb.append('=');
        sb.append(((this.reference == null)?"<null>":this.reference));
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
        result = ((result* 31)+((this.reference == null)? 0 :this.reference.hashCode()));
        result = ((result* 31)+((this.number == null)? 0 :this.number.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VersionDto) == false) {
            return false;
        }
        VersionDto rhs = ((VersionDto) other);
        return (((this.reference == rhs.reference)||((this.reference!= null)&&this.reference.equals(rhs.reference)))&&((this.number == rhs.number)||((this.number!= null)&&this.number.equals(rhs.number))));
    }

}
