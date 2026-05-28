
package ca.griis.js2p.gen.speds.session.api.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
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
    private HeaderDto.Msgtype msgtype;
    /**
     * The identifier of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The identifier of a SPEDS message. Mandatory in all layers.")
    @NotNull
    private UUID id;
    /**
     * The parameters of the Session layer.
     * (Required)
     * 
     */
    @JsonProperty("parameters")
    @JsonPropertyDescription("The parameters of the Session layer.")
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
    private final static long serialVersionUID = -2087233081400368709L;

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
     *     The parameters of the Session layer.
     */
    public HeaderDto(HeaderDto.Msgtype msgtype, UUID id, Object parameters, VersionDto version) {
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
    public HeaderDto.Msgtype getMsgtype() {
        return msgtype;
    }

    /**
     * The identifier of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    /**
     * The parameters of the Session layer.
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


    /**
     * The type of a SPEDS message. Mandatory in all layers.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Msgtype {

        SES_PUB_ENV("SES.PUB.ENV"),
        SES_PUB_REC("SES.PUB.REC"),
        SES_CLE_ENV("SES.CLE.ENV"),
        SES_CLE_REC("SES.CLE.REC"),
        SES_SAK_ENV("SES.SAK.ENV"),
        SES_SAK_REC("SES.SAK.REC"),
        SES_MSG_ENV("SES.MSG.ENV"),
        SES_MSG_REC("SES.MSG.REC"),
        SES_FIN_ENV("SES.FIN.ENV"),
        SES_FIN_REC("SES.FIN.REC");
        private final String value;
        private final static Map<String, HeaderDto.Msgtype> CONSTANTS = new HashMap<String, HeaderDto.Msgtype>();

        static {
            for (HeaderDto.Msgtype c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Msgtype(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static HeaderDto.Msgtype fromValue(String value) {
            HeaderDto.Msgtype constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
