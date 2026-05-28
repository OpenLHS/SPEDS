
package ca.griis.js2p.gen.speds.application.api.dto;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * ProtocolDataUnit-1-APP (2026-01-21T15:00:00-0500)
 * <p>
 * For the Application Layer of SPEDS (Layer 1) : a unit of data which is transferred from one end of a connection to the other with no loss of identity or meaning.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "header",
    "content"
})
@Generated("jsonschema2pojo")
public class ProtocolDataUnit1APPDto implements Serializable
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
    @Valid
    @NotNull
    private Map<String, Object> content;
    private final static long serialVersionUID = 2938510430080345681L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProtocolDataUnit1APPDto() {
    }

    /**
     * 
     * @param header
     *     The header of a SPEDS message. Mandatory in all layers.
     * @param content
     *     The content of a SPEDS message. Mandatory in all layers.
     */
    public ProtocolDataUnit1APPDto(HeaderDto header, Map<String, Object> content) {
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
    public Map<String, Object> getContent() {
        return content;
    }

    @Override
    public java.lang.String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProtocolDataUnit1APPDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
    public boolean equals(java.lang.Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProtocolDataUnit1APPDto) == false) {
            return false;
        }
        ProtocolDataUnit1APPDto rhs = ((ProtocolDataUnit1APPDto) other);
        return (((this.header == rhs.header)||((this.header!= null)&&this.header.equals(rhs.header)))&&((this.content == rhs.content)||((this.content!= null)&&this.content.equals(rhs.content))));
    }

}
