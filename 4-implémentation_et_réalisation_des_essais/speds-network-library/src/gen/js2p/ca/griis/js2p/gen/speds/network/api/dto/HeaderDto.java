
package ca.griis.js2p.gen.speds.network.api.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    "source_iri",
    "destination_iri",
    "authentification",
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
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A valid UUID according to RFC 4122")
    @NotNull
    private UUID id;
    /**
     * A valid IR according to RFC 3987
     * (Required)
     * 
     */
    @JsonProperty("source_iri")
    @JsonPropertyDescription("A valid IR according to RFC 3987")
    @NotNull
    private String sourceIri;
    /**
     * A valid IR according to RFC 3987
     * (Required)
     * 
     */
    @JsonProperty("destination_iri")
    @JsonPropertyDescription("A valid IR according to RFC 3987")
    @NotNull
    private String destinationIri;
    /**
     * The authentification method used
     * (Required)
     * 
     */
    @JsonProperty("authentification")
    @JsonPropertyDescription("The authentification method used")
    @NotNull
    private Object authentification;
    /**
     * The parameters of the Network layer.
     * (Required)
     * 
     */
    @JsonProperty("parameters")
    @JsonPropertyDescription("The parameters of the Network layer.")
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
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();
    private final static long serialVersionUID = -420676145832662824L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public HeaderDto() {
    }

    /**
     * 
     * @param sourceIri
     *     The source entity IRI (according to RFC3987).
     * @param authentification
     *     The authentification method used.
     * @param destinationIri
     *     The destination entity IRI (according to RFC3987).
     * @param id
     *     The identifier of a SPEDS message. Mandatory in all layers.
     * @param msgtype
     *     The type of a SPEDS message. Mandatory in all layers.
     * @param parameters
     *     The parameters of the Network layer.
     */
    public HeaderDto(HeaderDto.Msgtype msgtype, UUID id, String sourceIri, String destinationIri, Object authentification, Object parameters, VersionDto version) {
        super();
        this.msgtype = msgtype;
        this.id = id;
        this.sourceIri = sourceIri;
        this.destinationIri = destinationIri;
        this.authentification = authentification;
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
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    /**
     * A valid IR according to RFC 3987
     * (Required)
     * 
     */
    @JsonProperty("source_iri")
    public String getSourceIri() {
        return sourceIri;
    }

    /**
     * A valid IR according to RFC 3987
     * (Required)
     * 
     */
    @JsonProperty("destination_iri")
    public String getDestinationIri() {
        return destinationIri;
    }

    /**
     * The authentification method used
     * (Required)
     * 
     */
    @JsonProperty("authentification")
    public Object getAuthentification() {
        return authentification;
    }

    /**
     * The parameters of the Network layer.
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
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
        sb.append("sourceIri");
        sb.append('=');
        sb.append(((this.sourceIri == null)?"<null>":this.sourceIri));
        sb.append(',');
        sb.append("destinationIri");
        sb.append('=');
        sb.append(((this.destinationIri == null)?"<null>":this.destinationIri));
        sb.append(',');
        sb.append("authentification");
        sb.append('=');
        sb.append(((this.authentification == null)?"<null>":this.authentification));
        sb.append(',');
        sb.append("parameters");
        sb.append('=');
        sb.append(((this.parameters == null)?"<null>":this.parameters));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
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
        result = ((result* 31)+((this.sourceIri == null)? 0 :this.sourceIri.hashCode()));
        result = ((result* 31)+((this.authentification == null)? 0 :this.authentification.hashCode()));
        result = ((result* 31)+((this.destinationIri == null)? 0 :this.destinationIri.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
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
        return (((((((((this.sourceIri == rhs.sourceIri)||((this.sourceIri!= null)&&this.sourceIri.equals(rhs.sourceIri)))&&((this.authentification == rhs.authentification)||((this.authentification!= null)&&this.authentification.equals(rhs.authentification))))&&((this.destinationIri == rhs.destinationIri)||((this.destinationIri!= null)&&this.destinationIri.equals(rhs.destinationIri))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.msgtype == rhs.msgtype)||((this.msgtype!= null)&&this.msgtype.equals(rhs.msgtype))))&&((this.parameters == rhs.parameters)||((this.parameters!= null)&&this.parameters.equals(rhs.parameters))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))));
    }


    /**
     * The type of a SPEDS message. Mandatory in all layers.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Msgtype {

        RES_MSG_ENV("RES.MSG.ENV");
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
