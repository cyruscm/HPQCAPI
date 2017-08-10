package com.tyson.hpqcjapi.utils;

import java.util.LinkedList;

/**
 * A tool for creating ALM XML Elements
 * @author MARTINCORB
 *
 */
public class XMLCreator {

	private String complexType;
	private String subType;
	private LinkedList<String> fields;
	private LinkedList<String> extras;

	/**
	 * Prepare a XMLCreator
	 * s
	 * @param complexType wrapper type (such as <Entity></Entity>)
	 * @param subType internal type (such as <Entity type="test"></Entity>, can be null
	 */
	public XMLCreator(String complexType, String subType) {
		setComplexType(complexType);
		setSubType(subType);
		fields = new LinkedList<String>();
		extras = new LinkedList<String>();
	}

	/**
	 * Change the complexType
	 * @param type wrapper type (such as <Entity></Entity>)
	 */
	public void setComplexType(String type) {
		this.complexType = type;
	}

	/**
	 * Change the subType
	 * @param type internal type (such as <Entity type="test"></Entity>, can be null
	 */
	public void setSubType(String type) {
		this.subType = type;
	}

	/**
	 * Add a ALM Field to the XML document
	 * @param name The name of the field to add (such as <Field Name="subtype-id"></Field>, cannot be null
	 * @param value The value of the field to add (such as <Field Name="subtype-id"><Value>MANUAL</Value></Field>)
	 */
	public void addField(String name, String value) {
		fields.add(String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", name, value));
	}

	/**
	 * Add a custom value to xml
	 * @param name Name of value, such as <CheckInComment></CheckInComment>
	 * @param value Value of custom element, such as <CheckInComment>This is a comment</CheckInComment>
	 */
	public void addCustomValue(String name, String value) {
		extras.add(String.format("<%s>%s</%s>", name, value, name));
	}

	/**
	 * Return the xml object in a string, deletes the internal data for reusage
	 * @return XML Data
	 */
	public String publish() {
		return publish(true);
	}


	/**
	 * Creates the XML object and returns it in a string
	 * @param clear Flag to either clear or keep the internal data for reusage
	 * @return XML Data 
	 */
	public String publish(boolean clear) {
		String output = createComplex(true);

		if (!fields.isEmpty()) {
			output += "<Fields>";
			for (String field : fields) {
				output += "\n" + field;
			}
			output += "</Fields>";
		}

		for (String extra : extras) {
			output += "" + extra;
		}

		output += createComplex(false);

		if (clear) {
			fields.clear();
			extras.clear();
		}
		return output;
	}
	
	/**
	 * Creates the complex type.
	 * @param open if its an opening (true) or closing (false) type
	 * @return String of complexType
	 */
	private String createComplex(boolean open) {
		String output = "<";

		if (!open) {
			output += "/";
		}

		if (open && subType != null) {
			output += String.format("%s type=\"%s\">", complexType, subType);
		} else {
			output += String.format("%s>", complexType);
		}
		return output;
	}
}
