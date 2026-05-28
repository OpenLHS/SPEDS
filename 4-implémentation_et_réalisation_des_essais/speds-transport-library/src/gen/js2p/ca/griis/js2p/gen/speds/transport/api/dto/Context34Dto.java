
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
    "source_code",
    "destination_code",
    "source_iri",
    "service",
    "service_primitive",
    "destination_iri",
    "options"
})
@Generated("jsonschema2pojo")
public class Context34Dto implements Serializable
{

    /**
     * The source entity code (a Lodad GUID)
     * (Required)
     * 
     */
    @JsonProperty("source_code")
    @JsonPropertyDescription("The source entity code (a Lodad GUID)")
    @NotNull
    private String sourceCode;
    /**
     * The destination entity code (a Lodad GUID)
     * (Required)
     * 
     */
    @JsonProperty("destination_code")
    @JsonPropertyDescription("The destination entity code (a Lodad GUID)")
    @NotNull
    private String destinationCode;
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
     * The service call from the initiating layer
     * (Required)
     * 
     */
    @JsonProperty("service")
    @JsonPropertyDescription("The service call from the initiating layer")
    @NotNull
    private Context34Dto.Service service;
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
     * The destination entity IRI (according to RFC3987).
     * (Required)
     * 
     */
    @JsonProperty("destination_iri")
    @JsonPropertyDescription("The destination entity IRI (according to RFC3987).")
    @NotNull
    private String destinationIri;
    /**
     * The options shared between the layers.
     * (Required)
     * 
     */
    @JsonProperty("options")
    @JsonPropertyDescription("The options shared between the layers.")
    @NotNull
    private Object options;
    private final static long serialVersionUID = -8344282517312366064L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Context34Dto() {
    }

    /**
     * 
     * @param sourceCode
     *     The source entity code (a Lodad GUID).
     * @param destinationCode
     *     The destination entity code (a Lodad GUID).
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
    public Context34Dto(String sourceCode, String destinationCode, String sourceIri, Context34Dto.Service service, ServicePrimitive servicePrimitive, String destinationIri, Object options) {
        super();
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
        this.sourceIri = sourceIri;
        this.service = service;
        this.servicePrimitive = servicePrimitive;
        this.destinationIri = destinationIri;
        this.options = options;
    }

    /**
     * The source entity code (a Lodad GUID)
     * (Required)
     * 
     */
    @JsonProperty("source_code")
    public String getSourceCode() {
        return sourceCode;
    }

    /**
     * The destination entity code (a Lodad GUID)
     * (Required)
     * 
     */
    @JsonProperty("destination_code")
    public String getDestinationCode() {
        return destinationCode;
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
     * The service call from the initiating layer
     * (Required)
     * 
     */
    @JsonProperty("service")
    public Context34Dto.Service getService() {
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
     * The destination entity IRI (according to RFC3987).
     * (Required)
     * 
     */
    @JsonProperty("destination_iri")
    public String getDestinationIri() {
        return destinationIri;
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
        sb.append(Context34Dto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("sourceCode");
        sb.append('=');
        sb.append(((this.sourceCode == null)?"<null>":this.sourceCode));
        sb.append(',');
        sb.append("destinationCode");
        sb.append('=');
        sb.append(((this.destinationCode == null)?"<null>":this.destinationCode));
        sb.append(',');
        sb.append("sourceIri");
        sb.append('=');
        sb.append(((this.sourceIri == null)?"<null>":this.sourceIri));
        sb.append(',');
        sb.append("service");
        sb.append('=');
        sb.append(((this.service == null)?"<null>":this.service));
        sb.append(',');
        sb.append("servicePrimitive");
        sb.append('=');
        sb.append(((this.servicePrimitive == null)?"<null>":this.servicePrimitive));
        sb.append(',');
        sb.append("destinationIri");
        sb.append('=');
        sb.append(((this.destinationIri == null)?"<null>":this.destinationIri));
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
        result = ((result* 31)+((this.sourceCode == null)? 0 :this.sourceCode.hashCode()));
        result = ((result* 31)+((this.destinationCode == null)? 0 :this.destinationCode.hashCode()));
        result = ((result* 31)+((this.sourceIri == null)? 0 :this.sourceIri.hashCode()));
        result = ((result* 31)+((this.service == null)? 0 :this.service.hashCode()));
        result = ((result* 31)+((this.destinationIri == null)? 0 :this.destinationIri.hashCode()));
        result = ((result* 31)+((this.options == null)? 0 :this.options.hashCode()));
        result = ((result* 31)+((this.servicePrimitive == null)? 0 :this.servicePrimitive.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Context34Dto) == false) {
            return false;
        }
        Context34Dto rhs = ((Context34Dto) other);
        return ((((((((this.sourceCode == rhs.sourceCode)||((this.sourceCode!= null)&&this.sourceCode.equals(rhs.sourceCode)))&&((this.destinationCode == rhs.destinationCode)||((this.destinationCode!= null)&&this.destinationCode.equals(rhs.destinationCode))))&&((this.sourceIri == rhs.sourceIri)||((this.sourceIri!= null)&&this.sourceIri.equals(rhs.sourceIri))))&&((this.service == rhs.service)||((this.service!= null)&&this.service.equals(rhs.service))))&&((this.destinationIri == rhs.destinationIri)||((this.destinationIri!= null)&&this.destinationIri.equals(rhs.destinationIri))))&&((this.options == rhs.options)||((this.options!= null)&&this.options.equals(rhs.options))))&&((this.servicePrimitive == rhs.servicePrimitive)||((this.servicePrimitive!= null)&&this.servicePrimitive.equals(rhs.servicePrimitive))));
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
        private final static Map<String, Context34Dto.Service> CONSTANTS = new HashMap<String, Context34Dto.Service>();

        static {
            for (Context34Dto.Service c: values()) {
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
        public static Context34Dto.Service fromValue(String value) {
            Context34Dto.Service constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
