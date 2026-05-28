
package ca.griis.js2p.gen.speds.session.api.dto.fin;

import java.io.Serializable;
import java.util.UUID;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;


/**
 * SesFinRec (2026-01-15T10:20:00-0500)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "token",
    "session"
})
@Generated("jsonschema2pojo")
public class SesFinRecDto implements Serializable
{

    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("token")
    @JsonPropertyDescription("A valid UUID according to RFC 4122")
    @NotNull
    private UUID token;
    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("session")
    @JsonPropertyDescription("A valid UUID according to RFC 4122")
    @NotNull
    private UUID session;
    private final static long serialVersionUID = -6740456980044304932L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SesFinRecDto() {
    }

    /**
     * 
     * @param session
     *     Session identifier.
     * @param token
     *     The token of the SES.FIN.ENV message.
     */
    public SesFinRecDto(UUID token, UUID session) {
        super();
        this.token = token;
        this.session = session;
    }

    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("token")
    public UUID getToken() {
        return token;
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
        sb.append(SesFinRecDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("token");
        sb.append('=');
        sb.append(((this.token == null)?"<null>":this.token));
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
        result = ((result* 31)+((this.session == null)? 0 :this.session.hashCode()));
        result = ((result* 31)+((this.token == null)? 0 :this.token.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SesFinRecDto) == false) {
            return false;
        }
        SesFinRecDto rhs = ((SesFinRecDto) other);
        return (((this.session == rhs.session)||((this.session!= null)&&this.session.equals(rhs.session)))&&((this.token == rhs.token)||((this.token!= null)&&this.token.equals(rhs.token))));
    }

}
