package com.tyson.hpqcjapi.utils;

import java.util.LinkedList;

public class XMLCreator {
	
	private String complexType;
	private String subType;
	private LinkedList<String> fields;
	private LinkedList<String> extras;
	
	public XMLCreator(String complexType, String subType) {
		setComplexType(complexType);
		setSubType(subType);
		fields = new LinkedList<String>();
		extras = new LinkedList<String>();
	}
	
	public void setComplexType(String type) {
		this.complexType = type;
	}
	
	public void setSubType(String type) {
		this.subType = type;
	}
	
	public void addField(String name, String value) {
		fields.add(String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", name, value));
	}
	
	public void addCustomValue(String name, String value) {
		extras.add(String.format("<%s>%s</%s>", name, value, name));
	}
	
	public String publish() {
		return publish(true);
	}
	
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
}
