
package ca.griis.js2p.gen.speds.session.api.dto.cle;

import java.io.Serializable;
import java.util.UUID;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;


/**
 * SesCleEnv (2026-01-15T10:20:00-0500)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content",
    "session"
})
@Generated("jsonschema2pojo")
public class SesCleEnvDto implements Serializable
{

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("A base-64 encoded bytestring")
    @NotNull
    private String content;
    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("session")
    @JsonPropertyDescription("A valid UUID according to RFC 4122")
    @NotNull
    private UUID session;
    private final static long serialVersionUID = -8687122595390720381L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SesCleEnvDto() {
    }

    /**
     * 
     * @param session
     *     The session token.
     * @param content
     *     The encrypted 'key transfer' JSON object by the SKAK key, encoded into Base64.
     */
    public SesCleEnvDto(String content, UUID session) {
        super();
        this.content = content;
        this.session = session;
    }

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
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
        sb.append(SesCleEnvDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("content");
        sb.append('=');
        sb.append(((this.content == null)?"<null>":this.content));
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
        result = ((result* 31)+((this.content == null)? 0 :this.content.hashCode()));
        result = ((result* 31)+((this.session == null)? 0 :this.session.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SesCleEnvDto) == false) {
            return false;
        }
        SesCleEnvDto rhs = ((SesCleEnvDto) other);
        return (((this.content == rhs.content)||((this.content!= null)&&this.content.equals(rhs.content)))&&((this.session == rhs.session)||((this.session!= null)&&this.session.equals(rhs.session))));
    }

}
