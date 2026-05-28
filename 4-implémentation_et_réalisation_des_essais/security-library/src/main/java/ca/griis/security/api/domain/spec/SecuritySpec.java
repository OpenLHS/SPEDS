/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SecuritySpec.
 * @brief @~english Implementation of the class SecuritySpec.
 */

package ca.griis.security.api.domain.spec;

import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      «Model (Abstract, automation, etc.) (optional)»
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Définit la specification de sécurité.
 * @par Details
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      «2025-12-17» [BD] - Implémentation initiale
 * @par Tâches
 *      S.O.
 */
public abstract class SecuritySpec {
  protected final String algo;

  protected SecuritySpec(String algo) {
    this.algo = algo;
  }

  public String getAlgo() {
    return algo;
  }

  public abstract Map<String, String> getParameters();

  @Override
  public final boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof SecuritySpec)) {
      return false;
    }
    SecuritySpec that = (SecuritySpec) o;
    EqualsBuilder equalsBuilder = new EqualsBuilder();
    equalsBuilder.append(algo, that.algo);
    equalsBuilder.append(getParameters(), that.getParameters());
    return equalsBuilder.isEquals();
  }

  @Override
  public final int hashCode() {
    HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
    hashCodeBuilder.append(algo);
    hashCodeBuilder.append(getParameters());
    return hashCodeBuilder.hashCode();
  }

  @Override
  public final String toString() {
    ToStringBuilder stringBuilder = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    stringBuilder.append("algo", algo);
    stringBuilder.append("parameters", getParameters());
    return stringBuilder.toString();
  }
}
