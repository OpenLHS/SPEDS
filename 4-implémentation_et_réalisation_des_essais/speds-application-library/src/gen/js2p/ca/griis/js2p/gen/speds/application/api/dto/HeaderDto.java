
package ca.griis.js2p.gen.speds.application.api.dto;

import java.io.Serializable;
import java.util.UUID;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * The header of a SPEDS message. Mandatory in all layers.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "msgtype",
    "id",
    "parameters",
    "version"
})
@Generated("jsonschema2pojo")
public class HeaderDto implements Serializable
{

    /**
     * The type of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("msgtype")
    @JsonPropertyDescription("The type of a SPEDS message. Mandatory in all layers.")
    @NotNull
    private MsgType msgtype;
    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A valid UUID according to RFC 4122")
    @NotNull
    private UUID id;
    /**
     * The parameters of the Application layer.
     * (Required)
     * 
     */
    @JsonProperty("parameters")
    @JsonPropertyDescription("The parameters of the Application layer.")
    @NotNull
    private Object parameters;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    @Valid
    @NotNull
    private VersionDto version;
    private final static long serialVersionUID = 2152515779504372313L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public HeaderDto() {
    }

    /**
     * 
     * @param id
     *     The identifier of a SPEDS message. Mandatory in all layers.
     * @param msgtype
     *     The type of a SPEDS message. Mandatory in all layers.
     * @param parameters
     *     The parameters of the Application layer.
     */
    public HeaderDto(MsgType msgtype, UUID id, Object parameters, VersionDto version) {
        super();
        this.msgtype = msgtype;
        this.id = id;
        this.parameters = parameters;
        this.version = version;
    }

    /**
     * The type of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("msgtype")
    public MsgType getMsgtype() {
        return msgtype;
    }

    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    /**
     * The parameters of the Application layer.
     * (Required)
     * 
     */
    @JsonProperty("parameters")
    public Object getParameters() {
        return parameters;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public VersionDto getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(HeaderDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("msgtype");
        sb.append('=');
        sb.append(((this.msgtype == null)?"<null>":this.msgtype));
        sb.append(',');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("parameters");
        sb.append('=');
        sb.append(((this.parameters == null)?"<null>":this.parameters));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
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
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.msgtype == null)? 0 :this.msgtype.hashCode()));
        result = ((result* 31)+((this.parameters == null)? 0 :this.parameters.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HeaderDto) == false) {
            return false;
        }
        HeaderDto rhs = ((HeaderDto) other);
        return (((((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id)))&&((this.msgtype == rhs.msgtype)||((this.msgtype!= null)&&this.msgtype.equals(rhs.msgtype))))&&((this.parameters == rhs.parameters)||((this.parameters!= null)&&this.parameters.equals(rhs.parameters))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))));
    }

}
