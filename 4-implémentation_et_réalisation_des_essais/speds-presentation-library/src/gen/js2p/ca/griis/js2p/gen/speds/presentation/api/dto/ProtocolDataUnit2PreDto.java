
package ca.griis.js2p.gen.speds.presentation.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * ProtocolDataUnit-2-PRE (2025-11-28T10:00:00-0500)
 * <p>
 * For the Presentation Layer of SPEDS (Layer 2) : a unit of data which is transferred from one end of a connection to the other with no loss of identity or meaning.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "header",
    "content"
})
@Generated("jsonschema2pojo")
public class ProtocolDataUnit2PreDto implements Serializable
{

    /**
     * The header of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("header")
    @JsonPropertyDescription("The header of a SPEDS message. Mandatory in all layers.")
    @Valid
    @NotNull
    private HeaderDto header;
    /**
     * The content of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("The content of a SPEDS message. Mandatory in all layers.")
    @NotNull
    private String content;
    private final static long serialVersionUID = 3938375508171282815L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProtocolDataUnit2PreDto() {
    }

    /**
     * 
     * @param header
     *     The header of a SPEDS message. Mandatory in all layers.
     * @param content
     *     The content of a SPEDS message. Mandatory in all layers.
     */
    public ProtocolDataUnit2PreDto(HeaderDto header, String content) {
        super();
        this.header = header;
        this.content = content;
    }

    /**
     * The header of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("header")
    public HeaderDto getHeader() {
        return header;
    }

    /**
     * The content of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProtocolDataUnit2PreDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("header");
        sb.append('=');
        sb.append(((this.header == null)?"<null>":this.header));
        sb.append(',');
        sb.append("content");
        sb.append('=');
        sb.append(((this.content == null)?"<null>":this.content));
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
        result = ((result* 31)+((this.header == null)? 0 :this.header.hashCode()));
        result = ((result* 31)+((this.content == null)? 0 :this.content.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProtocolDataUnit2PreDto) == false) {
            return false;
        }
        ProtocolDataUnit2PreDto rhs = ((ProtocolDataUnit2PreDto) other);
        return (((this.header == rhs.header)||((this.header!= null)&&this.header.equals(rhs.header)))&&((this.content == rhs.content)||((this.content!= null)&&this.content.equals(rhs.content))));
    }

}
