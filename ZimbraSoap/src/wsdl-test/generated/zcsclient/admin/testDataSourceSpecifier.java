
package generated.zcsclient.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dataSourceSpecifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dataSourceSpecifier">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:zimbraAdmin}adminAttrsImpl">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{urn:zimbraAdmin}dataSourceType" />
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataSourceSpecifier")
public class testDataSourceSpecifier
    extends testAdminAttrsImpl
{

    @XmlAttribute(name = "type", required = true)
    protected testDataSourceType type;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link testDataSourceType }
     *     
     */
    public testDataSourceType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link testDataSourceType }
     *     
     */
    public void setType(testDataSourceType value) {
        this.type = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
