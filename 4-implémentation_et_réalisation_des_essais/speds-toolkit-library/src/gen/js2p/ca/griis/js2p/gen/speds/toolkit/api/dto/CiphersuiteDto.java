
package ca.griis.js2p.gen.speds.toolkit.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * Cipher suite (2025-12-27T10:00:00-0500)
 * <p>
 * Security configuration - Cipher suite for SPEDS
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "spedsProfile"
})
@Generated("jsonschema2pojo")
public class CiphersuiteDto implements Serializable
{

    /**
     * Actual SPEDS security profile
     * (Required)
     * 
     */
    @JsonProperty("spedsProfile")
    @JsonPropertyDescription("Actual SPEDS security profile")
    @Valid
    @NotNull
    private List<SpedsConfigItemDto> spedsProfile = new ArrayList<SpedsConfigItemDto>();
    private final static long serialVersionUID = -2515469322688437399L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CiphersuiteDto() {
    }

    /**
     * 
     * @param spedsProfile
     *     Actual SPEDS security profile.
     */
    public CiphersuiteDto(List<SpedsConfigItemDto> spedsProfile) {
        super();
        this.spedsProfile = spedsProfile;
    }

    /**
     * Actual SPEDS security profile
     * (Required)
     * 
     */
    @JsonProperty("spedsProfile")
    public List<SpedsConfigItemDto> getSpedsProfile() {
        return spedsProfile;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CiphersuiteDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("spedsProfile");
        sb.append('=');
        sb.append(((this.spedsProfile == null)?"<null>":this.spedsProfile));
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
        result = ((result* 31)+((this.spedsProfile == null)? 0 :this.spedsProfile.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CiphersuiteDto) == false) {
            return false;
        }
        CiphersuiteDto rhs = ((CiphersuiteDto) other);
        return ((this.spedsProfile == rhs.spedsProfile)||((this.spedsProfile!= null)&&this.spedsProfile.equals(rhs.spedsProfile)));
    }

}
