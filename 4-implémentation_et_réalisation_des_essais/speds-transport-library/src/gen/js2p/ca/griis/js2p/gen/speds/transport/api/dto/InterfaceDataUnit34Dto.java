
package ca.griis.js2p.gen.speds.transport.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * InterfaceDataUnit-3_4 (2026-01-20T10:20:00-0500)
 * <p>
 * An agreed way of communication among SPEDS layers Session (3) and Transport (4).
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "context",
    "message"
})
@Generated("jsonschema2pojo")
public class InterfaceDataUnit34Dto implements Serializable
{

    /**
     * The context shared between these two SPEDS layers. This is the ICI (Interface Control Information), i.e. an information passed across an interface to coordinate interactions between two adjacent layers.
     * (Required)
     * 
     */
    @JsonProperty("context")
    @JsonPropertyDescription("The context shared between these two SPEDS layers. This is the ICI (Interface Control Information), i.e. an information passed across an interface to coordinate interactions between two adjacent layers.")
    @Valid
    @NotNull
    private Context34Dto context;
    /**
     * The message shared between these two SPEDS layers. This is the SPDU : the PDU (Protocol Data Unit) of the Session layer (3).
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("The message shared between these two SPEDS layers. This is the SPDU : the PDU (Protocol Data Unit) of the Session layer (3).")
    @NotNull
    private String message;
    private final static long serialVersionUID = -3815156692351484753L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public InterfaceDataUnit34Dto() {
    }

    /**
     * 
     * @param context
     *     The context shared between these two SPEDS layers. This is the ICI (Interface Control Information), i.e. an information passed across an interface to coordinate interactions between two adjacent layers.
     * @param message
     *     The message shared between these two SPEDS layers. This is the SPDU : the PDU (Protocol Data Unit) of the Session layer (3).
     */
    public InterfaceDataUnit34Dto(Context34Dto context, String message) {
        super();
        this.context = context;
        this.message = message;
    }

    /**
     * The context shared between these two SPEDS layers. This is the ICI (Interface Control Information), i.e. an information passed across an interface to coordinate interactions between two adjacent layers.
     * (Required)
     * 
     */
    @JsonProperty("context")
    public Context34Dto getContext() {
        return context;
    }

    /**
     * The message shared between these two SPEDS layers. This is the SPDU : the PDU (Protocol Data Unit) of the Session layer (3).
     * (Required)
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(InterfaceDataUnit34Dto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("context");
        sb.append('=');
        sb.append(((this.context == null)?"<null>":this.context));
        sb.append(',');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
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
        result = ((result* 31)+((this.context == null)? 0 :this.context.hashCode()));
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InterfaceDataUnit34Dto) == false) {
            return false;
        }
        InterfaceDataUnit34Dto rhs = ((InterfaceDataUnit34Dto) other);
        return (((this.context == rhs.context)||((this.context!= null)&&this.context.equals(rhs.context)))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))));
    }

}
