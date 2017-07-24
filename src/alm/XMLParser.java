package alm;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLParser {
	private Element getRootElement(String xml)
		throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		return document.getDocumentElement();
	}
	
	private String getString(String tagName, Element element) {
		NodeList list = element.getElementsByTagName(tagName);
		Logger.log("TagList: ");
		for (int i = 0; i < list.getLength(); i++)
			Logger.log(list.item(i).getNodeName() + " = " + list.item(i).getNodeValue());
		if (list != null && list.getLength() > 0) {
			NodeList subList = list.item(0).getChildNodes();
			Logger.log("subTagList: " + subList.toString());
			for (int i = 0; i < subList.getLength(); i++)
				Logger.log(subList.item(i).getNodeName() + " = " + subList.item(i).getNodeValue());
			
			if (subList != null && subList.getLength() > 0) {
				return subList.item(0).getNodeValue();
			}
		}
		return null;
	}
	
	private String getFieldValue(String tagName, String xml) 
			throws ParserConfigurationException, SAXException, IOException {
		return getString(tagName, getRootElement(xml));
	}

}
