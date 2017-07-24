package alm;

import java.util.LinkedList;

public class XMLCreator {
	
	private String type;
	private LinkedList<String> fields;
	private LinkedList<String> extras;
	
	public XMLCreator(String type) {
		setType(type);
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void addField(String name, String value) {
		fields.add(String.format("<Field Name=\"%s\"><Value>%s</Value></Field>", name, value));
	}
	
	public void addCustomValue(String name, String value) {
		extras.add(String.format("<%s>%s</$s>", name, value, name));
	}
	
	public String publish() {
		return publish(true);
	}
	
	public String publish(boolean clear) {
		String output = String.format("<%s>", type);
		
		if (!fields.isEmpty()) {
			output += "\n<Fields>";
			for (String field : fields) {
				output += "\n" + field; 
			}
			output += "\n</Fields>";
		}
		
		for (String extra : extras) {
			output += "\n" + extra;
		}
		
		output += String.format("\n</%s>", type);

		type = "";
		fields.clear();
		extras.clear();
		
		return output;
	}
}
