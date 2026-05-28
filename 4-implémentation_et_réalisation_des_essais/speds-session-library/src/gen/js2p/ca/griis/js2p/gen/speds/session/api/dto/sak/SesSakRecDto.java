
package ca.griis.js2p.gen.speds.session.api.dto.sak;

import java.io.Serializable;
import java.util.UUID;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;


/**
 * SesSakRec (2026-01-15T10:20:00-0500)
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
public class SesSakRecDto implements Serializable
{

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("A base-64 encoded bytestring")
    @NotNull
    private String value;
    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("session")
    @JsonPropertyDescription("A valid UUID according to RFC 4122")
    @NotNull
    private UUID session;
    private final static long serialVersionUID = 7424092272850349016L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SesSakRecDto() {
    }

    /**
     * 
     * @param session
     *     The session identifier.
     * @param value
     *     The value of the choice.
     */
    public SesSakRecDto(String value, UUID session) {
        super();
        this.value = value;
        this.session = session;
    }

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("session")
    public UUID getSession() {
        return session;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SesSakRecDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof SesSakRecDto) == false) {
            return false;
        }
        SesSakRecDto rhs = ((SesSakRecDto) other);
        return (((this.value == rhs.value)||((this.value!= null)&&this.value.equals(rhs.value)))&&((this.session == rhs.session)||((this.session!= null)&&this.session.equals(rhs.session))));
    }

}
