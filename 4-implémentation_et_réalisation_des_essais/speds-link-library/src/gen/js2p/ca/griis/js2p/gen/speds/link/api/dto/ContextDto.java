
package ca.griis.js2p.gen.speds.link.api.dto;

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
 * The context shared between these two SPEDS layers. This is the ICI (Interface Control Information), i.e. an information passed across an interface to coordinate interactions between two adjacent layers.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "destination_iri",
    "service",
    "service_primitive",
    "options"
})
@Generated("jsonschema2pojo")
public class ContextDto implements Serializable
{

    /**
     * The destination node IRI (according to RFC3987).
     * (Required)
     * 
     */
    @JsonProperty("destination_iri")
    @JsonPropertyDescription("The destination node IRI (according to RFC3987).")
    @NotNull
    private String destinationIri;
    /**
     * The service call from the initiating layer
     * (Required)
     * 
     */
    @JsonProperty("service")
    @JsonPropertyDescription("The service call from the initiating layer")
    @NotNull
    private ContextDto.Service service;
    /**
     * The service primitive used to communicate among the layers.
     * (Required)
     * 
     */
    @JsonProperty("service_primitive")
    @JsonPropertyDescription("The service primitive used to communicate among the layers.")
    @NotNull
    private ContextDto.ServicePrimitive servicePrimitive;
    /**
     * The options shared between the layers.
     * (Required)
     * 
     */
    @JsonProperty("options")
    @JsonPropertyDescription("The options shared between the layers.")
    @NotNull
    private Object options;
    private final static long serialVersionUID = 8373204849861656918L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ContextDto() {
    }

    /**
     * 
     * @param service
     *     The service call from the initiating layer.
     * @param destinationIri
     *     The destination node IRI (according to RFC3987).
     * @param options
     *     The options shared between the layers.
     * @param servicePrimitive
     *     The service primitive used to communicate among the layers.
     */
    public ContextDto(String destinationIri, ContextDto.Service service, ContextDto.ServicePrimitive servicePrimitive, Object options) {
        super();
        this.destinationIri = destinationIri;
        this.service = service;
        this.servicePrimitive = servicePrimitive;
        this.options = options;
    }

    /**
     * The destination node IRI (according to RFC3987).
     * (Required)
     * 
     */
    @JsonProperty("destination_iri")
    public String getDestinationIri() {
        return destinationIri;
    }

    /**
     * The service call from the initiating layer
     * (Required)
     * 
     */
    @JsonProperty("service")
    public ContextDto.Service getService() {
        return service;
    }

    /**
     * The service primitive used to communicate among the layers.
     * (Required)
     * 
     */
    @JsonProperty("service_primitive")
    public ContextDto.ServicePrimitive getServicePrimitive() {
        return servicePrimitive;
    }

    /**
     * The options shared between the layers.
     * (Required)
     * 
     */
    @JsonProperty("options")
    public Object getOptions() {
        return options;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ContextDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("destinationIri");
        sb.append('=');
        sb.append(((this.destinationIri == null)?"<null>":this.destinationIri));
        sb.append(',');
        sb.append("service");
        sb.append('=');
        sb.append(((this.service == null)?"<null>":this.service));
        sb.append(',');
        sb.append("servicePrimitive");
        sb.append('=');
        sb.append(((this.servicePrimitive == null)?"<null>":this.servicePrimitive));
        sb.append(',');
        sb.append("options");
        sb.append('=');
        sb.append(((this.options == null)?"<null>":this.options));
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
        result = ((result* 31)+((this.destinationIri == null)? 0 :this.destinationIri.hashCode()));
        result = ((result* 31)+((this.options == null)? 0 :this.options.hashCode()));
        result = ((result* 31)+((this.service == null)? 0 :this.service.hashCode()));
        result = ((result* 31)+((this.servicePrimitive == null)? 0 :this.servicePrimitive.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ContextDto) == false) {
            return false;
        }
        ContextDto rhs = ((ContextDto) other);
        return (((((this.destinationIri == rhs.destinationIri)||((this.destinationIri!= null)&&this.destinationIri.equals(rhs.destinationIri)))&&((this.options == rhs.options)||((this.options!= null)&&this.options.equals(rhs.options))))&&((this.service == rhs.service)||((this.service!= null)&&this.service.equals(rhs.service))))&&((this.servicePrimitive == rhs.servicePrimitive)||((this.servicePrimitive!= null)&&this.servicePrimitive.equals(rhs.servicePrimitive))));
    }


    /**
     * The service call from the initiating layer
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Service {

        DELEGATE("delegate"),
        TRANSFER("transfer");
        private final String value;
        private final static Map<String, ContextDto.Service> CONSTANTS = new HashMap<String, ContextDto.Service>();

        static {
            for (ContextDto.Service c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Service(String value) {
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
        public static ContextDto.Service fromValue(String value) {
            ContextDto.Service constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * The service primitive used to communicate among the layers.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum ServicePrimitive {

        REQUEST("request"),
        CONFIRM("confirm"),
        RESPONSE("response"),
        INDICATION("indication");
        private final String value;
        private final static Map<String, ContextDto.ServicePrimitive> CONSTANTS = new HashMap<String, ContextDto.ServicePrimitive>();

        static {
            for (ContextDto.ServicePrimitive c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ServicePrimitive(String value) {
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
        public static ContextDto.ServicePrimitive fromValue(String value) {
            ContextDto.ServicePrimitive constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
