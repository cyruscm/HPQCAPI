package infrastructure;

/**
 * Created by MARTINCORB on 7/14/2017.
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, vhudson-jaxb-ri-2.1-456
// See http://www.oracle.com/technetwork/articles/javase/index-140168.html
// Any modifications to this file will be lost upon recompilation of the
// source schema.
// Generated on: 2010.05.31 at 03:35:17 PM IDT
//


import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Java class for anonymous complex type.
 * <p>
 * <p>
 * The following schema fragment specifies the expected content
 * contained within this class.
 * <p>
 * <complexType>
 * <complexContent>
 * <restriction base=
 * "{http://www.w3.org/2001/XMLSchema}anyType">
 * <sequence>
 * <element ref=
 * "{}Attribute" maxOccurs="unbounded"
 * minOccurs="0"/>
 * </sequence>
 * </restriction>
 * </complexContent>
 * </complexType>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"attribute"})
@XmlRootElement(name = "Attributes")
public class Attributes {

    @XmlElement(name = "Attribute")
    protected List<Attribute> attribute;

    /**
     * Gets the value of the attribute property.
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the attribute property.
     * <p>
     * For example, to add a new item, do as follows:
     * <p>
     * getAttribute().add(newItem);
     * <p>
     * Objects of the following type(s) are allowed in the
     * list {@link Attribute }
     */
    public List<Attribute> getAttribute() {
        if (attribute == null) {
            attribute = new ArrayList<Attribute>();
        }
        return this.attribute;
    }

}