
package ca.griis.js2p.gen.speds.network.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "header_seal",
    "content_seal"
})
@Generated("jsonschema2pojo")
public class StampDto implements Serializable
{

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("header_seal")
    @JsonPropertyDescription("A base-64 encoded bytestring")
    @NotNull
    private String headerSeal;
    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("content_seal")
    @JsonPropertyDescription("A base-64 encoded bytestring")
    @NotNull
    private String contentSeal;
    private final static long serialVersionUID = -775711426866500070L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public StampDto() {
    }

    public StampDto(String headerSeal, String contentSeal) {
        super();
        this.headerSeal = headerSeal;
        this.contentSeal = contentSeal;
    }

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("header_seal")
    public String getHeaderSeal() {
        return headerSeal;
    }

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("content_seal")
    public String getContentSeal() {
        return contentSeal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StampDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("headerSeal");
        sb.append('=');
        sb.append(((this.headerSeal == null)?"<null>":this.headerSeal));
        sb.append(',');
        sb.append("contentSeal");
        sb.append('=');
        sb.append(((this.contentSeal == null)?"<null>":this.contentSeal));
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
        result = ((result* 31)+((this.headerSeal == null)? 0 :this.headerSeal.hashCode()));
        result = ((result* 31)+((this.contentSeal == null)? 0 :this.contentSeal.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StampDto) == false) {
            return false;
        }
        StampDto rhs = ((StampDto) other);
        return (((this.headerSeal == rhs.headerSeal)||((this.headerSeal!= null)&&this.headerSeal.equals(rhs.headerSeal)))&&((this.contentSeal == rhs.contentSeal)||((this.contentSeal!= null)&&this.contentSeal.equals(rhs.contentSeal))));
    }

}
