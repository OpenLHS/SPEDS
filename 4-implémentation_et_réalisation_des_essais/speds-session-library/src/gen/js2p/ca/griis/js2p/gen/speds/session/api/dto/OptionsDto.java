
package ca.griis.js2p.gen.speds.session.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "speds.ses.version",
    "speds.ses.reference",
    "speds.ses.cert",
    "speds.ses.private.key"
})
@Generated("jsonschema2pojo")
public class OptionsDto implements Serializable
{

    /**
     * 
     * Corresponds to the "speds.ses.version" property.The version of the SPEDS protocol used by the Session layer.
     * 
     */
    @JsonProperty("speds.ses.version")
    @JsonPropertyDescription("The version of the SPEDS protocol used by the Session layer.")
    private String spedsVersion;
    /**
     * 
     * Corresponds to the "speds.ses.reference" property.The reference of the SPEDS protocol version used by the Session layer.
     * 
     */
    @JsonProperty("speds.ses.reference")
    @JsonPropertyDescription("The reference of the SPEDS protocol version used by the Session layer.")
    private String spedsReference;
    /**
     * 
     * Corresponds to the "speds.ses.cert" property.A valid certificate used to verify data integrity
     * 
     */
    @JsonProperty("speds.ses.cert")
    @JsonPropertyDescription("A valid certificate used to verify data integrity")
    private String certificate;
    /**
     * 
     * Corresponds to the "speds.ses.private.key" property.The private key associated with the previous certificate used to sign digital data
     * 
     */
    @JsonProperty("speds.ses.private.key")
    @JsonPropertyDescription("The private key associated with the previous certificate used to sign digital data")
    private String privateKey;
    private final static long serialVersionUID = 7999199497809078006L;

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
     * @param spedsVersion
     *     The version of the SPEDS protocol used by the Session layer.
     * @param certificate
     *     A valid certificate used to verify data integrity.
     * @param spedsReference
     *     The reference of the SPEDS protocol version used by the Session layer.
     */
    public OptionsDto(String spedsVersion, String spedsReference, String certificate, String privateKey) {
        super();
        this.spedsVersion = spedsVersion;
        this.spedsReference = spedsReference;
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    /**
     * 
     * Corresponds to the "speds.ses.version" property.The version of the SPEDS protocol used by the Session layer.
     * 
     */
    @JsonProperty("speds.ses.version")
    public String getSpedsVersion() {
        return spedsVersion;
    }

    /**
     * 
     * Corresponds to the "speds.ses.reference" property.The reference of the SPEDS protocol version used by the Session layer.
     * 
     */
    @JsonProperty("speds.ses.reference")
    public String getSpedsReference() {
        return spedsReference;
    }

    /**
     * 
     * Corresponds to the "speds.ses.cert" property.A valid certificate used to verify data integrity
     * 
     */
    @JsonProperty("speds.ses.cert")
    public String getCertificate() {
        return certificate;
    }

    /**
     * 
     * Corresponds to the "speds.ses.private.key" property.The private key associated with the previous certificate used to sign digital data
     * 
     */
    @JsonProperty("speds.ses.private.key")
    public String getPrivateKey() {
        return privateKey;
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
        result = ((result* 31)+((this.certificate == null)? 0 :this.certificate.hashCode()));
        result = ((result* 31)+((this.privateKey == null)? 0 :this.privateKey.hashCode()));
        result = ((result* 31)+((this.spedsVersion == null)? 0 :this.spedsVersion.hashCode()));
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
        return (((((this.certificate == rhs.certificate)||((this.certificate!= null)&&this.certificate.equals(rhs.certificate)))&&((this.privateKey == rhs.privateKey)||((this.privateKey!= null)&&this.privateKey.equals(rhs.privateKey))))&&((this.spedsVersion == rhs.spedsVersion)||((this.spedsVersion!= null)&&this.spedsVersion.equals(rhs.spedsVersion))))&&((this.spedsReference == rhs.spedsReference)||((this.spedsReference!= null)&&this.spedsReference.equals(rhs.spedsReference))));
    }

}
