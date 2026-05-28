
package ca.griis.js2p.gen.speds.network.api.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "speds.net.version",
    "speds.net.reference",
    "speds.net.cert",
    "speds.net.private.key",
    "speds.net.response.window.minutes"
})
@Generated("jsonschema2pojo")
public class OptionsDto implements Serializable
{

    /**
     * 
     * Corresponds to the "speds.net.version" property.The version of the SPEDS protocol used by the Network layer.
     * 
     */
    @JsonProperty("speds.net.version")
    @JsonPropertyDescription("The version of the SPEDS protocol used by the Network layer.")
    private String spedsVersion;
    /**
     * 
     * Corresponds to the "speds.net.reference" property.The reference of the SPEDS protocol version used by the Network layer.
     * 
     */
    @JsonProperty("speds.net.reference")
    @JsonPropertyDescription("The reference of the SPEDS protocol version used by the Network layer.")
    private String spedsReference;
    /**
     * 
     * Corresponds to the "speds.net.cert" property.A valid certificate used to verify data integrity
     * 
     */
    @JsonProperty("speds.net.cert")
    @JsonPropertyDescription("A valid certificate used to verify data integrity")
    private String certificate;
    /**
     * 
     * Corresponds to the "speds.net.private.key" property.The private key associated with the previous certificate used to sign digital data
     * 
     */
    @JsonProperty("speds.net.private.key")
    @JsonPropertyDescription("The private key associated with the previous certificate used to sign digital data")
    private String privateKey;
    /**
     * 
     * Corresponds to the "speds.net.response.window.minutes" property.Time period that a host can respond to a received message
     * 
     */
    @JsonProperty("speds.net.response.window.minutes")
    @JsonPropertyDescription("Time period that a host can respond to a received message")
    private Integer responseWindowMinutes;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();
    private final static long serialVersionUID = 4247609495003575243L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OptionsDto() {
    }

    /**
     * 
     * @param privateKey
     *     The private key associated with the previous certificate used to sign digital data.
     * @param responseWindowMinutes
     *     Time period that a host can respond to a received message.
     * @param spedsVersion
     *     The version of the SPEDS protocol used by the Network layer.
     * @param certificate
     *     A valid certificate used to verify data integrity.
     * @param spedsReference
     *     The reference of the SPEDS protocol version used by the Network layer.
     */
    public OptionsDto(String spedsVersion, String spedsReference, String certificate, String privateKey, Integer responseWindowMinutes) {
        super();
        this.spedsVersion = spedsVersion;
        this.spedsReference = spedsReference;
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.responseWindowMinutes = responseWindowMinutes;
    }

    /**
     * 
     * Corresponds to the "speds.net.version" property.The version of the SPEDS protocol used by the Network layer.
     * 
     */
    @JsonProperty("speds.net.version")
    public String getSpedsVersion() {
        return spedsVersion;
    }

    /**
     * 
     * Corresponds to the "speds.net.reference" property.The reference of the SPEDS protocol version used by the Network layer.
     * 
     */
    @JsonProperty("speds.net.reference")
    public String getSpedsReference() {
        return spedsReference;
    }

    /**
     * 
     * Corresponds to the "speds.net.cert" property.A valid certificate used to verify data integrity
     * 
     */
    @JsonProperty("speds.net.cert")
    public String getCertificate() {
        return certificate;
    }

    /**
     * 
     * Corresponds to the "speds.net.private.key" property.The private key associated with the previous certificate used to sign digital data
     * 
     */
    @JsonProperty("speds.net.private.key")
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * 
     * Corresponds to the "speds.net.response.window.minutes" property.Time period that a host can respond to a received message
     * 
     */
    @JsonProperty("speds.net.response.window.minutes")
    public Integer getResponseWindowMinutes() {
        return responseWindowMinutes;
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
        sb.append(OptionsDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("spedsVersion");
        sb.append('=');
        sb.append(((this.spedsVersion == null)?"<null>":this.spedsVersion));
        sb.append(',');
        sb.append("spedsReference");
        sb.append('=');
        sb.append(((this.spedsReference == null)?"<null>":this.spedsReference));
        sb.append(',');
        sb.append("certificate");
        sb.append('=');
        sb.append(((this.certificate == null)?"<null>":this.certificate));
        sb.append(',');
        sb.append("privateKey");
        sb.append('=');
        sb.append(((this.privateKey == null)?"<null>":this.privateKey));
        sb.append(',');
        sb.append("responseWindowMinutes");
        sb.append('=');
        sb.append(((this.responseWindowMinutes == null)?"<null>":this.responseWindowMinutes));
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
        result = ((result* 31)+((this.privateKey == null)? 0 :this.privateKey.hashCode()));
        result = ((result* 31)+((this.responseWindowMinutes == null)? 0 :this.responseWindowMinutes.hashCode()));
        result = ((result* 31)+((this.spedsVersion == null)? 0 :this.spedsVersion.hashCode()));
        result = ((result* 31)+((this.certificate == null)? 0 :this.certificate.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.spedsReference == null)? 0 :this.spedsReference.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OptionsDto) == false) {
            return false;
        }
        OptionsDto rhs = ((OptionsDto) other);
        return (((((((this.privateKey == rhs.privateKey)||((this.privateKey!= null)&&this.privateKey.equals(rhs.privateKey)))&&((this.responseWindowMinutes == rhs.responseWindowMinutes)||((this.responseWindowMinutes!= null)&&this.responseWindowMinutes.equals(rhs.responseWindowMinutes))))&&((this.spedsVersion == rhs.spedsVersion)||((this.spedsVersion!= null)&&this.spedsVersion.equals(rhs.spedsVersion))))&&((this.certificate == rhs.certificate)||((this.certificate!= null)&&this.certificate.equals(rhs.certificate))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.spedsReference == rhs.spedsReference)||((this.spedsReference!= null)&&this.spedsReference.equals(rhs.spedsReference))));
    }

}
