
package ca.griis.js2p.gen.speds.network.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * ProtocolDataUnit-5-NET (2025-11-28T10:00:00-0500)
 * <p>
 * For the Network Layer of SPEDS (Layer 5) : a unit of data which is transferred from one end of a connection to the other with no loss of identity or meaning.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "header",
    "stamp",
    "content"
})
@Generated("jsonschema2pojo")
public class ProtocolDataUnit5NETDto implements Serializable
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("stamp")
    @Valid
    @NotNull
    private StampDto stamp;
    /**
     * The content of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("The content of a SPEDS message. Mandatory in all layers.")
    @NotNull
    private String content;
    private final static long serialVersionUID = -2565020448700899234L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProtocolDataUnit5NETDto() {
    }

    /**
     * 
     * @param header
     *     The header of a SPEDS message. Mandatory in all layers.
     * @param content
     *     The content of a SPEDS message. Mandatory in all layers.
     */
    public ProtocolDataUnit5NETDto(HeaderDto header, StampDto stamp, String content) {
        super();
        this.header = header;
        this.stamp = stamp;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("stamp")
    public StampDto getStamp() {
        return stamp;
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
        sb.append(ProtocolDataUnit5NETDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("header");
        sb.append('=');
        sb.append(((this.header == null)?"<null>":this.header));
        sb.append(',');
        sb.append("stamp");
        sb.append('=');
        sb.append(((this.stamp == null)?"<null>":this.stamp));
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
        result = ((result* 31)+((this.stamp == null)? 0 :this.stamp.hashCode()));
        result = ((result* 31)+((this.content == null)? 0 :this.content.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProtocolDataUnit5NETDto) == false) {
            return false;
        }
        ProtocolDataUnit5NETDto rhs = ((ProtocolDataUnit5NETDto) other);
        return ((((this.header == rhs.header)||((this.header!= null)&&this.header.equals(rhs.header)))&&((this.stamp == rhs.stamp)||((this.stamp!= null)&&this.stamp.equals(rhs.stamp))))&&((this.content == rhs.content)||((this.content!= null)&&this.content.equals(rhs.content))));
    }

}
