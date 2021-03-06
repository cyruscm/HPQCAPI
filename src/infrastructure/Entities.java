package infrastructure;
/*

These files are supposed to be automatically generated. However, this is
a modification of the generated infrastructure.Entity to support multiple
entities to allow for uniform data structures for XML Parsing.

*/

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Java class for anonymous complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *<complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 * <complexType>
	 *   <complexContent>
	 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       <sequence>
	 *         <element name="Fields">
	 *           <complexType>
	 *             <complexContent>
	 *               <restriction base=
	 *                  "{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 <sequence>
	 *                   <element name="Field" maxOccurs="unbounded">
	 *                     <complexType>
	 *                       <complexContent>
	 *                         <restriction base=
	 *                            "{http://www.w3.org/2001/XMLSchema}anyType">
	 *                           <sequence>
	 *                             <element name="Value"
	 *                               type="{http://www.w3.org/2001/XMLSchema}string"
	 *                               maxOccurs="unbounded"/>
	 *                           </sequence>
	 *                           <attribute name="Name" use="required"
	 *                             type="{http://www.w3.org/2001/XMLSchema}string" />
	 *                         </restriction>
	 *                       </complexContent>
	 *                     </complexType>
	 *                   </element>
	 *                 </sequence>
	 *               </restriction>
	 *             </complexContent>
	 *           </complexType>
	 *         </element>
	 *       </sequence>
	 *       <attribute name="Type" use="required"
	 *           type="{http://www.w3.org/2001/XMLSchema}string" />
	 *     </restriction>
	 *   </complexCcontent>
	 * </complexType>
 *   </complexContent>
 * </complexType>
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Entities")
public class Entities {

    @XmlElement(name = "Entity", required = true)
    protected List<Entity> entities;
    @XmlAttribute(name = "TotalResults", required = true)
    protected String count;


    public Entities(Entities entity) {
        count = "" + entity.Count();
        entities = new ArrayList<Entity>(entity.getEntities());
    }

    public Entities() {}

    /**
     * Gets the value of the fields property.
     *
     * @return possible object is {@link Entity.Fields }
     *
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * Sets the value of the fields property.
     *
     * @param value
     *            allowed object is {@link Entity.Fields }
     *
     */
    public void setEntities(Entities value) {
        this.entities = value.getEntities();
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     *
     */
    public int Count() {
        return Integer.parseInt(count);
    }

}


