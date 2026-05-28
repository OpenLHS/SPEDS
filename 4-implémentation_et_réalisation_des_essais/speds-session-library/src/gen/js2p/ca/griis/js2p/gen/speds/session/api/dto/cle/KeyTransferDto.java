
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
 * KeyTransfer (2026-01-15T10:20:00-0500)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sdek",
    "pgaNumber",
    "token"
})
@Generated("jsonschema2pojo")
public class KeyTransferDto implements Serializable
{

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("sdek")
    @JsonPropertyDescription("A base-64 encoded bytestring")
    @NotNull
    private String sdek;
    /**
     * A valid Lodad GUID.
     * (Required)
     * 
     */
    @JsonProperty("pgaNumber")
    @JsonPropertyDescription("A valid Lodad GUID.")
    @NotNull
    private String pgaNumber;
    /**
     * A valid UUID according to RFC 4122
     * (Required)
     * 
     */
    @JsonProperty("token")
    @JsonPropertyDescription("A valid UUID according to RFC 4122")
    @NotNull
    private UUID token;
    private final static long serialVersionUID = 681852928655904160L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public KeyTransferDto() {
    }

    /**
     * 
     * @param pgaNumber
     *     The PGA number.
     * @param sdek
     *     The session data encryption key.
     * @param token
     *     Session token.
     */
    public KeyTransferDto(String sdek, String pgaNumber, UUID token) {
        super();
        this.sdek = sdek;
        this.pgaNumber = pgaNumber;
        this.token = token;
    }

    /**
     * A base-64 encoded bytestring
     * (Required)
     * 
     */
    @JsonProperty("sdek")
    public String getSdek() {
        return sdek;
    }

    /**
     * A valid Lodad GUID.
     * (Required)
     * 
     */
    @JsonProperty("pgaNumber")
    public String getPgaNumber() {
        return pgaNumber;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(KeyTransferDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("sdek");
        sb.append('=');
        sb.append(((this.sdek == null)?"<null>":this.sdek));
        sb.append(',');
        sb.append("pgaNumber");
        sb.append('=');
        sb.append(((this.pgaNumber == null)?"<null>":this.pgaNumber));
        sb.append(',');
        sb.append("token");
        sb.append('=');
        sb.append(((this.token == null)?"<null>":this.token));
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
        result = ((result* 31)+((this.pgaNumber == null)? 0 :this.pgaNumber.hashCode()));
        result = ((result* 31)+((this.sdek == null)? 0 :this.sdek.hashCode()));
        result = ((result* 31)+((this.token == null)? 0 :this.token.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof KeyTransferDto) == false) {
            return false;
        }
        KeyTransferDto rhs = ((KeyTransferDto) other);
        return ((((this.pgaNumber == rhs.pgaNumber)||((this.pgaNumber!= null)&&this.pgaNumber.equals(rhs.pgaNumber)))&&((this.sdek == rhs.sdek)||((this.sdek!= null)&&this.sdek.equals(rhs.sdek))))&&((this.token == rhs.token)||((this.token!= null)&&this.token.equals(rhs.token))));
    }

}
