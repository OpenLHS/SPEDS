
package ca.griis.js2p.gen.speds.session.api.dto.sak;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;


/**
 * SesSakEnv (2026-01-15T10:20:00-0500)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "value",
    "session"
})
@Generated("jsonschema2pojo")
public class SesSakEnvDto implements Serializable
{

    /**
     * The value of the choice
     * (Required)
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("The value of the choice")
    @NotNull
    private String value;
    /**
     * Unique session identifier
     * (Required)
     * 
     */
    @JsonProperty("session")
    @JsonPropertyDescription("Unique session identifier")
    @NotNull
    private String session;
    private final static long serialVersionUID = -9003032498662286773L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SesSakEnvDto() {
    }

    /**
     * 
     * @param session
     *     Unique session identifier.
     * @param value
     *     The value of the choice.
     */
    public SesSakEnvDto(String value, String session) {
        super();
        this.value = value;
        this.session = session;
    }

    /**
     * The value of the choice
     * (Required)
     * 
     */
    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    /**
     * Unique session identifier
     * (Required)
     * 
     */
    @JsonProperty("session")
    public String getSession() {
        return session;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SesSakEnvDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null)?"<null>":this.value));
        sb.append(',');
        sb.append("session");
        sb.append('=');
        sb.append(((this.session == null)?"<null>":this.session));
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
        result = ((result* 31)+((this.value == null)? 0 :this.value.hashCode()));
        result = ((result* 31)+((this.session == null)? 0 :this.session.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SesSakEnvDto) == false) {
            return false;
        }
        SesSakEnvDto rhs = ((SesSakEnvDto) other);
        return (((this.value == rhs.value)||((this.value!= null)&&this.value.equals(rhs.value)))&&((this.session == rhs.session)||((this.session!= null)&&this.session.equals(rhs.session))));
    }

}
