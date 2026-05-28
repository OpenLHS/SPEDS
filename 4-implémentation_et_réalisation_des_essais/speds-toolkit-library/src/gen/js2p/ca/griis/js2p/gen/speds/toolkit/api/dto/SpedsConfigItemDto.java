
package ca.griis.js2p.gen.speds.toolkit.api.dto;

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
import jakarta.validation.constraints.NotNull;


/**
 * Flexible SPEDS configuration item
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "spedsLayer",
    "algorithmCategory",
    "securityProfile",
    "securityAlgorithm"
})
@Generated("jsonschema2pojo")
public class SpedsConfigItemDto implements Serializable
{

    /**
     * The speds layer
     * (Required)
     * 
     */
    @JsonProperty("spedsLayer")
    @JsonPropertyDescription("The speds layer")
    @NotNull
    private SpedsConfigItemDto.SpedsLayer spedsLayer;
    /**
     * The algorithm category
     * (Required)
     * 
     */
    @JsonProperty("algorithmCategory")
    @JsonPropertyDescription("The algorithm category")
    @NotNull
    private SpedsConfigItemDto.AlgorithmCategory algorithmCategory;
    /**
     * The security profile
     * 
     */
    @JsonProperty("securityProfile")
    @JsonPropertyDescription("The security profile")
    private SpedsConfigItemDto.SecurityProfile securityProfile;
    /**
     * The security algorithm name
     * 
     */
    @JsonProperty("securityAlgorithm")
    @JsonPropertyDescription("The security algorithm name")
    private String securityAlgorithm;
    private final static long serialVersionUID = -5313436218032216505L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SpedsConfigItemDto() {
    }

    /**
     * 
     * @param securityAlgorithm
     *     The security algorithm name.
     */
    public SpedsConfigItemDto(SpedsConfigItemDto.SpedsLayer spedsLayer, SpedsConfigItemDto.AlgorithmCategory algorithmCategory, SpedsConfigItemDto.SecurityProfile securityProfile, String securityAlgorithm) {
        super();
        this.spedsLayer = spedsLayer;
        this.algorithmCategory = algorithmCategory;
        this.securityProfile = securityProfile;
        this.securityAlgorithm = securityAlgorithm;
    }

    public SpedsConfigItemDto(SpedsConfigItemDto.SpedsLayer spedsLayer, SpedsConfigItemDto.AlgorithmCategory algorithmCategory) {
        super();
        this.spedsLayer = spedsLayer;
        this.algorithmCategory = algorithmCategory;
    }

    /**
     * The speds layer
     * (Required)
     * 
     */
    @JsonProperty("spedsLayer")
    public SpedsConfigItemDto.SpedsLayer getSpedsLayer() {
        return spedsLayer;
    }

    /**
     * The algorithm category
     * (Required)
     * 
     */
    @JsonProperty("algorithmCategory")
    public SpedsConfigItemDto.AlgorithmCategory getAlgorithmCategory() {
        return algorithmCategory;
    }

    /**
     * The security profile
     * 
     */
    @JsonProperty("securityProfile")
    public SpedsConfigItemDto.SecurityProfile getSecurityProfile() {
        return securityProfile;
    }

    /**
     * The security algorithm name
     * 
     */
    @JsonProperty("securityAlgorithm")
    public String getSecurityAlgorithm() {
        return securityAlgorithm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SpedsConfigItemDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("spedsLayer");
        sb.append('=');
        sb.append(((this.spedsLayer == null)?"<null>":this.spedsLayer));
        sb.append(',');
        sb.append("algorithmCategory");
        sb.append('=');
        sb.append(((this.algorithmCategory == null)?"<null>":this.algorithmCategory));
        sb.append(',');
        sb.append("securityProfile");
        sb.append('=');
        sb.append(((this.securityProfile == null)?"<null>":this.securityProfile));
        sb.append(',');
        sb.append("securityAlgorithm");
        sb.append('=');
        sb.append(((this.securityAlgorithm == null)?"<null>":this.securityAlgorithm));
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
        result = ((result* 31)+((this.algorithmCategory == null)? 0 :this.algorithmCategory.hashCode()));
        result = ((result* 31)+((this.spedsLayer == null)? 0 :this.spedsLayer.hashCode()));
        result = ((result* 31)+((this.securityProfile == null)? 0 :this.securityProfile.hashCode()));
        result = ((result* 31)+((this.securityAlgorithm == null)? 0 :this.securityAlgorithm.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SpedsConfigItemDto) == false) {
            return false;
        }
        SpedsConfigItemDto rhs = ((SpedsConfigItemDto) other);
        return (((((this.algorithmCategory == rhs.algorithmCategory)||((this.algorithmCategory!= null)&&this.algorithmCategory.equals(rhs.algorithmCategory)))&&((this.spedsLayer == rhs.spedsLayer)||((this.spedsLayer!= null)&&this.spedsLayer.equals(rhs.spedsLayer))))&&((this.securityProfile == rhs.securityProfile)||((this.securityProfile!= null)&&this.securityProfile.equals(rhs.securityProfile))))&&((this.securityAlgorithm == rhs.securityAlgorithm)||((this.securityAlgorithm!= null)&&this.securityAlgorithm.equals(rhs.securityAlgorithm))));
    }


    /**
     * The algorithm category
     * 
     */
    @Generated("jsonschema2pojo")
    public enum AlgorithmCategory {

        HASH("HASH"),
        SYMM("SYMM"),
        ASYM("ASYM"),
        DH("DH"),
        SIGN("SIGN");
        private final String value;
        private final static Map<String, SpedsConfigItemDto.AlgorithmCategory> CONSTANTS = new HashMap<String, SpedsConfigItemDto.AlgorithmCategory>();

        static {
            for (SpedsConfigItemDto.AlgorithmCategory c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        AlgorithmCategory(String value) {
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
        public static SpedsConfigItemDto.AlgorithmCategory fromValue(String value) {
            SpedsConfigItemDto.AlgorithmCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * The security profile
     * 
     */
    @Generated("jsonschema2pojo")
    public enum SecurityProfile {

        STRONG("STRONG"),
        EFFICIENT("EFFICIENT");
        private final String value;
        private final static Map<String, SpedsConfigItemDto.SecurityProfile> CONSTANTS = new HashMap<String, SpedsConfigItemDto.SecurityProfile>();

        static {
            for (SpedsConfigItemDto.SecurityProfile c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SecurityProfile(String value) {
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
        public static SpedsConfigItemDto.SecurityProfile fromValue(String value) {
            SpedsConfigItemDto.SecurityProfile constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * The speds layer
     * 
     */
    @Generated("jsonschema2pojo")
    public enum SpedsLayer {

        APPLICATION("APPLICATION"),
        PRESENTATION("PRESENTATION"),
        SESSION("SESSION"),
        TRANSPORT("TRANSPORT"),
        NETWORK("NETWORK"),
        LINK("LINK");
        private final String value;
        private final static Map<String, SpedsConfigItemDto.SpedsLayer> CONSTANTS = new HashMap<String, SpedsConfigItemDto.SpedsLayer>();

        static {
            for (SpedsConfigItemDto.SpedsLayer c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SpedsLayer(String value) {
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
        public static SpedsConfigItemDto.SpedsLayer fromValue(String value) {
            SpedsConfigItemDto.SpedsLayer constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
