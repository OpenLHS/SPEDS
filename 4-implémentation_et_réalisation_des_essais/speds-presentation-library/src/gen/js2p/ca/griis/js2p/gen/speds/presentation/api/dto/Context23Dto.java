
package ca.griis.js2p.gen.speds.presentation.api.dto;

import java.io.Serializable;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;


/**
 * The context shared between these two SPEDS layers. This is the ICI (Interface Control Information), i.e. an information passed across an interface to coordinate interactions between two adjacent layers.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "PGA",
    "source_code",
    "destination_code",
    "SDEK",
    "service",
    "service_primitive",
    "options"
})
@Generated("jsonschema2pojo")
public class Context23Dto implements Serializable
{

    /**
     * The PGA identifier (a Lodad GUID).
     * (Required)
     * 
     */
    @JsonProperty("PGA")
    @JsonPropertyDescription("The PGA identifier (a Lodad GUID).")
    @NotNull
    private String pga;
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
     * The symmetric data-encryption key.
     * (Required)
     * 
     */
    @JsonProperty("SDEK")
    @JsonPropertyDescription("The symmetric data-encryption key.")
    @NotNull
    private String sdek;
    /**
     * The service call from the initiating layer
     * 
     */
    @JsonProperty("service")
    @JsonPropertyDescription("The service call from the initiating layer")
    private String service;
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
    private final static long serialVersionUID = 8541963330142919543L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Context23Dto() {
    }

    /**
     * 
     * @param sourceCode
     *     The source entity code (a Lodad GUID).
     * @param destinationCode
     *     The destination entity code (a Lodad GUID).
     * @param sdek
     *     The symmetric data-encryption key.
     * @param service
     *     The service call from the initiating layer.
     * @param options
     *     The options shared between the layers.
     * @param pga
     *     The PGA identifier (a Lodad GUID).
     * @param servicePrimitive
     *     The service primitive used to communicate among the layers.
     */
    public Context23Dto(String pga, String sourceCode, String destinationCode, String sdek, String service, ServicePrimitive servicePrimitive, Object options) {
        super();
        this.pga = pga;
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
        this.sdek = sdek;
        this.service = service;
        this.servicePrimitive = servicePrimitive;
        this.options = options;
    }

    /**
     * 
     * @param sourceCode
     *     The source entity code (a Lodad GUID).
     * @param destinationCode
     *     The destination entity code (a Lodad GUID).
     * @param sdek
     *     The symmetric data-encryption key.
     * @param options
     *     The options shared between the layers.
     * @param pga
     *     The PGA identifier (a Lodad GUID).
     * @param servicePrimitive
     *     The service primitive used to communicate among the layers.
     */
    public Context23Dto(String pga, String sourceCode, String destinationCode, String sdek, ServicePrimitive servicePrimitive, Object options) {
        super();
        this.pga = pga;
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
        this.sdek = sdek;
        this.servicePrimitive = servicePrimitive;
        this.options = options;
    }

    /**
     * The PGA identifier (a Lodad GUID).
     * (Required)
     * 
     */
    @JsonProperty("PGA")
    public String getPga() {
        return pga;
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
     * The symmetric data-encryption key.
     * (Required)
     * 
     */
    @JsonProperty("SDEK")
    public String getSdek() {
        return sdek;
    }

    /**
     * The service call from the initiating layer
     * 
     */
    @JsonProperty("service")
    public String getService() {
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
        sb.append(Context23Dto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("pga");
        sb.append('=');
        sb.append(((this.pga == null)?"<null>":this.pga));
        sb.append(',');
        sb.append("sourceCode");
        sb.append('=');
        sb.append(((this.sourceCode == null)?"<null>":this.sourceCode));
        sb.append(',');
        sb.append("destinationCode");
        sb.append('=');
        sb.append(((this.destinationCode == null)?"<null>":this.destinationCode));
        sb.append(',');
        sb.append("sdek");
        sb.append('=');
        sb.append(((this.sdek == null)?"<null>":this.sdek));
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
        result = ((result* 31)+((this.sourceCode == null)? 0 :this.sourceCode.hashCode()));
        result = ((result* 31)+((this.destinationCode == null)? 0 :this.destinationCode.hashCode()));
        result = ((result* 31)+((this.sdek == null)? 0 :this.sdek.hashCode()));
        result = ((result* 31)+((this.service == null)? 0 :this.service.hashCode()));
        result = ((result* 31)+((this.options == null)? 0 :this.options.hashCode()));
        result = ((result* 31)+((this.pga == null)? 0 :this.pga.hashCode()));
        result = ((result* 31)+((this.servicePrimitive == null)? 0 :this.servicePrimitive.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Context23Dto) == false) {
            return false;
        }
        Context23Dto rhs = ((Context23Dto) other);
        return ((((((((this.sourceCode == rhs.sourceCode)||((this.sourceCode!= null)&&this.sourceCode.equals(rhs.sourceCode)))&&((this.destinationCode == rhs.destinationCode)||((this.destinationCode!= null)&&this.destinationCode.equals(rhs.destinationCode))))&&((this.sdek == rhs.sdek)||((this.sdek!= null)&&this.sdek.equals(rhs.sdek))))&&((this.service == rhs.service)||((this.service!= null)&&this.service.equals(rhs.service))))&&((this.options == rhs.options)||((this.options!= null)&&this.options.equals(rhs.options))))&&((this.pga == rhs.pga)||((this.pga!= null)&&this.pga.equals(rhs.pga))))&&((this.servicePrimitive == rhs.servicePrimitive)||((this.servicePrimitive!= null)&&this.servicePrimitive.equals(rhs.servicePrimitive))));
    }

}
