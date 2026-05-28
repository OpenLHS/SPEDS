
package ca.griis.js2p.gen.speds.network.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * InterfaceDataUnit-4_5 (2025-11-28T10:00:00-0500)
 * <p>
 * An agreed way of communication among SPEDS layers Transport (4) and Network (5).
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "context",
    "message"
})
@Generated("jsonschema2pojo")
public class InterfaceDataUnit45Dto implements Serializable
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
    private Context45Dto context;
    /**
     * The message shared between these two SPEDS layers. This is the TPDU : the PDU (Protocol Data Unit) of the Transport layer (4).
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("The message shared between these two SPEDS layers. This is the TPDU : the PDU (Protocol Data Unit) of the Transport layer (4).")
    @NotNull
    private String message;
    private final static long serialVersionUID = -799086602950756600L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public InterfaceDataUnit45Dto() {
    }

    /**
     * 
     * @param context
     *     The context shared between these two SPEDS layers. This is the ICI (Interface Control Information), i.e. an information passed across an interface to coordinate interactions between two adjacent layers.
     * @param message
     *     The message shared between these two SPEDS layers. This is the TPDU : the PDU (Protocol Data Unit) of the Transport layer (4).
     */
    public InterfaceDataUnit45Dto(Context45Dto context, String message) {
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
    public Context45Dto getContext() {
        return context;
    }

    /**
     * The message shared between these two SPEDS layers. This is the TPDU : the PDU (Protocol Data Unit) of the Transport layer (4).
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
        sb.append(InterfaceDataUnit45Dto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof InterfaceDataUnit45Dto) == false) {
            return false;
        }
        InterfaceDataUnit45Dto rhs = ((InterfaceDataUnit45Dto) other);
        return (((this.context == rhs.context)||((this.context!= null)&&this.context.equals(rhs.context)))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))));
    }

}
