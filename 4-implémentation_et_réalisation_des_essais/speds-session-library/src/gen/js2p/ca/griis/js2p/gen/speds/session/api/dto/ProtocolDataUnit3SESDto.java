
package ca.griis.js2p.gen.speds.session.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * ProtocolDataUnit-3-SES (2026-01-15T10:20:00-0500)
 * <p>
 * For the Session Layer of SPEDS (Layer 3) : a unit of data which is transferred from one end of a connection to the other with no loss of identity or meaning.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "header",
    "stamp",
    "content"
})
@Generated("jsonschema2pojo")
public class ProtocolDataUnit3SESDto implements Serializable
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
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("stamp")
    @JsonPropertyDescription("A base-64 encoded bytestring")
    @NotNull
    private String stamp;
    /**
     * The content of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("The content of a SPEDS message. Mandatory in all layers.")
    @NotNull
    private Object content;
    private final static long serialVersionUID = 2382511379198406543L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProtocolDataUnit3SESDto() {
    }

    /**
     * 
     * @param header
     *     The header of a SPEDS message. Mandatory in all layers.
     * @param stamp
     *     The authentification seal (the stamp) of the sender of this message.
     * @param content
     *     The content of a SPEDS message. Mandatory in all layers.
     */
    public ProtocolDataUnit3SESDto(HeaderDto header, String stamp, Object content) {
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
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("stamp")
    public String getStamp() {
        return stamp;
    }

    /**
     * The content of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("content")
    public Object getContent() {
        return content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProtocolDataUnit3SESDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof ProtocolDataUnit3SESDto) == false) {
            return false;
        }
        ProtocolDataUnit3SESDto rhs = ((ProtocolDataUnit3SESDto) other);
        return ((((this.header == rhs.header)||((this.header!= null)&&this.header.equals(rhs.header)))&&((this.stamp == rhs.stamp)||((this.stamp!= null)&&this.stamp.equals(rhs.stamp))))&&((this.content == rhs.content)||((this.content!= null)&&this.content.equals(rhs.content))));
    }

}
