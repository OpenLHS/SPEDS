
package ca.griis.js2p.gen.speds.transport.api.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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
    "source_code",
    "destination_code",
    "version"
})
@Generated("jsonschema2pojo")
public class Header45Dto implements Serializable
{

    /**
     * The type of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("msgtype")
    @JsonPropertyDescription("The type of a SPEDS message. Mandatory in all layers.")
    @NotNull
    private Header45Dto.Msgtype msgtype;
    /**
     * The identifier of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The identifier of a SPEDS message. Mandatory in all layers.")
    @NotNull
    private Object id;
    /**
     * The source entity code (a Lodad GUID)
     * (Required)
     * 
     */
    @JsonProperty("source_code")
    @JsonPropertyDescription("The source entity code (a Lodad GUID)")
    @NotNull
    private String sourceCode;
    /**
     * The destination entity code (a Lodad GUID)
     * (Required)
     * 
     */
    @JsonProperty("destination_code")
    @JsonPropertyDescription("The destination entity code (a Lodad GUID)")
    @NotNull
    private String destinationCode;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    @Valid
    @NotNull
    private Speds45Dto version;
    private final static long serialVersionUID = -9021992884385672942L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Header45Dto() {
    }

    /**
     * 
     * @param sourceCode
     *     The source entity code (a Lodad GUID).
     * @param destinationCode
     *     The destination entity code (a Lodad GUID).
     * @param id
     *     The identifier of a SPEDS message. Mandatory in all layers.
     * @param msgtype
     *     The type of a SPEDS message. Mandatory in all layers.
     */
    public Header45Dto(Header45Dto.Msgtype msgtype, Object id, String sourceCode, String destinationCode, Speds45Dto version) {
        super();
        this.msgtype = msgtype;
        this.id = id;
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
        this.version = version;
    }

    /**
     * The type of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("msgtype")
    public Header45Dto.Msgtype getMsgtype() {
        return msgtype;
    }

    /**
     * The identifier of a SPEDS message. Mandatory in all layers.
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Object getId() {
        return id;
    }

    /**
     * The source entity code (a Lodad GUID)
     * (Required)
     * 
     */
    @JsonProperty("source_code")
    public String getSourceCode() {
        return sourceCode;
    }

    /**
     * The destination entity code (a Lodad GUID)
     * (Required)
     * 
     */
    @JsonProperty("destination_code")
    public String getDestinationCode() {
        return destinationCode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public Speds45Dto getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Header45Dto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("msgtype");
        sb.append('=');
        sb.append(((this.msgtype == null)?"<null>":this.msgtype));
        sb.append(',');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("sourceCode");
        sb.append('=');
        sb.append(((this.sourceCode == null)?"<null>":this.sourceCode));
        sb.append(',');
        sb.append("destinationCode");
        sb.append('=');
        sb.append(((this.destinationCode == null)?"<null>":this.destinationCode));
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
        result = ((result* 31)+((this.sourceCode == null)? 0 :this.sourceCode.hashCode()));
        result = ((result* 31)+((this.destinationCode == null)? 0 :this.destinationCode.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.msgtype == null)? 0 :this.msgtype.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Header45Dto) == false) {
            return false;
        }
        Header45Dto rhs = ((Header45Dto) other);
        return ((((((this.sourceCode == rhs.sourceCode)||((this.sourceCode!= null)&&this.sourceCode.equals(rhs.sourceCode)))&&((this.destinationCode == rhs.destinationCode)||((this.destinationCode!= null)&&this.destinationCode.equals(rhs.destinationCode))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.msgtype == rhs.msgtype)||((this.msgtype!= null)&&this.msgtype.equals(rhs.msgtype))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))));
    }


    /**
     * The type of a SPEDS message. Mandatory in all layers.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Msgtype {

        TRA_INF_ENV("TRA.INF.ENV"),
        TRA_INF_REC("TRA.INF.REC"),
        TRA_MSG_ENV("TRA.MSG.ENV"),
        TRA_MSG_REC("TRA.MSG.REC"),
        TRA_FIN_ENV("TRA.FIN.ENV"),
        TRA_FIN_REC("TRA.FIN.REC");
        private final String value;
        private final static Map<String, Header45Dto.Msgtype> CONSTANTS = new HashMap<String, Header45Dto.Msgtype>();

        static {
            for (Header45Dto.Msgtype c: values()) {
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
        public static Header45Dto.Msgtype fromValue(String value) {
            Header45Dto.Msgtype constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
