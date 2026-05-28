
package ca.griis.js2p.gen.speds.transport.api.dto;

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
    "source_iri",
    "destination_iri",
    "service",
    "service_primitive",
    "options"
})
@Generated("jsonschema2pojo")
public class Context45Dto implements Serializable
{

    /**
     * The source entity IRI (according to RFC3987).
     * (Required)
     * 
     */
    @JsonProperty("source_iri")
    @JsonPropertyDescription("The source entity IRI (according to RFC3987).")
    @NotNull
    private String sourceIri;
    /**
     * The destination entity IRI (according to RFC3987).
     * (Required)
     * 
     */
    @JsonProperty("destination_iri")
    @JsonPropertyDescription("The destination entity IRI (according to RFC3987).")
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
    private Context45Dto.Service service;
    /**
     * The service primitive used to communicate among the layers.
     * (Required)
     * 
     */
    @JsonProperty("service_primitive")
    @JsonPropertyDescription("The service primitive used to communicate among the layers.")
    @NotNull
    private ServicePrimitive servicePrimitive;
    /**
     * The options shared between the layers.
     * (Required)
     * 
     */
    @JsonProperty("options")
    @JsonPropertyDescription("The options shared between the layers.")
    @NotNull
    private Object options;
    private final static long serialVersionUID = -3751518103128232064L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Context45Dto() {
    }

    /**
     * 
     * @param sourceIri
     *     The source entity IRI (according to RFC3987).
     * @param service
     *     The service call from the initiating layer.
     * @param destinationIri
     *     The destination entity IRI (according to RFC3987).
     * @param options
     *     The options shared between the layers.
     * @param servicePrimitive
     *     The service primitive used to communicate among the layers.
     */
    public Context45Dto(String sourceIri, String destinationIri, Context45Dto.Service service, ServicePrimitive servicePrimitive, Object options) {
        super();
        this.sourceIri = sourceIri;
        this.destinationIri = destinationIri;
        this.service = service;
        this.servicePrimitive = servicePrimitive;
        this.options = options;
    }

    /**
     * The source entity IRI (according to RFC3987).
     * (Required)
     * 
     */
    @JsonProperty("source_iri")
    public String getSourceIri() {
        return sourceIri;
    }

    /**
     * The destination entity IRI (according to RFC3987).
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
    public Context45Dto.Service getService() {
        return service;
    }

    /**
     * The service primitive used to communicate among the layers.
     * (Required)
     * 
     */
    @JsonProperty("service_primitive")
    public ServicePrimitive getServicePrimitive() {
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
        sb.append(Context45Dto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("sourceIri");
        sb.append('=');
        sb.append(((this.sourceIri == null)?"<null>":this.sourceIri));
        sb.append(',');
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
        result = ((result* 31)+((this.sourceIri == null)? 0 :this.sourceIri.hashCode()));
        result = ((result* 31)+((this.service == null)? 0 :this.service.hashCode()));
        result = ((result* 31)+((this.servicePrimitive == null)? 0 :this.servicePrimitive.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Context45Dto) == false) {
            return false;
        }
        Context45Dto rhs = ((Context45Dto) other);
        return ((((((this.destinationIri == rhs.destinationIri)||((this.destinationIri!= null)&&this.destinationIri.equals(rhs.destinationIri)))&&((this.options == rhs.options)||((this.options!= null)&&this.options.equals(rhs.options))))&&((this.sourceIri == rhs.sourceIri)||((this.sourceIri!= null)&&this.sourceIri.equals(rhs.sourceIri))))&&((this.service == rhs.service)||((this.service!= null)&&this.service.equals(rhs.service))))&&((this.servicePrimitive == rhs.servicePrimitive)||((this.servicePrimitive!= null)&&this.servicePrimitive.equals(rhs.servicePrimitive))));
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
        private final static Map<String, Context45Dto.Service> CONSTANTS = new HashMap<String, Context45Dto.Service>();

        static {
            for (Context45Dto.Service c: values()) {
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
        public static Context45Dto.Service fromValue(String value) {
            Context45Dto.Service constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
