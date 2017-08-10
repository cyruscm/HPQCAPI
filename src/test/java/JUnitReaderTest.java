package test.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.tyson.hpqcjapi.JUnitReader;
import com.tyson.hpqcjapi.types.LinkedTestCase;
import com.tyson.hpqcjapi.utils.Logger;

public class JUnitReaderTest{

	private List<LinkedTestCase> deserializedSample = null;
	private InputStream SAMPLEJUNIT = getClass().getResourceAsStream("resources/sampleJunit.xml");
	private InputStream UPDATEDJUNIT = getClass().getResourceAsStream("resources/updatedJunit.xml");
	
	@Test
	public void InvalidPath() {
		try {
			new JUnitReader("nonexistant.file");
			fail("java.nio.file.NoSuchFileException expected, none thrown.");
		} catch (NoSuchFileException e) {
		} catch (Exception e) {
			fail(utils.getStackStrace(e));
		}
		
	}
	
	@Test
	public void ValidPath() {
		try {
			JUnitReader reader = new JUnitReader(SAMPLEJUNIT);
			assertEquals("JUnitReader got correct amount of entities from valid file", 25, reader.parseSuites().size()); 
		} catch (JAXBException e) {
			fail(utils.getStackStrace(e));
		}
		
	}
	
	@Test
	public void ParseTest() {
		try {
			JUnitReader reader = new JUnitReader(SAMPLEJUNIT);
			assertTrue(validateSampleJunit(reader.parseSuites()));
		} catch (Exception e) {
			 fail(utils.getStackStrace(e));
		}
	}
	
	@Test
	public void MultiParseTest() {
		try {
			JUnitReader reader = new JUnitReader(SAMPLEJUNIT);
			for (int i = 0; i < 50; i++) {
				assertTrue(validateSampleJunit(reader.parseSuites()));
			}
		} catch (Exception e) {
			 fail(utils.getStackStrace(e));
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private boolean validateSampleJunit(List<LinkedTestCase> items) throws IOException, ClassNotFoundException {
		if (deserializedSample == null) {
			InputStream in = getClass().getResourceAsStream("resources/serialized_sampleJunit");
			String serialized = readStream(in);
			byte[] decodedBytes = Base64.getDecoder().decode(serialized);
			ByteArrayInputStream bi = new ByteArrayInputStream(decodedBytes);
			ObjectInputStream si = new ObjectInputStream(bi);
			deserializedSample = (List<LinkedTestCase>) si.readObject();
		}
		
		
		for (int i = 0; i < deserializedSample.size(); i++) {
			if (!deserializedSample.get(i).equals(items.get(i))) {
				return false;
			}
		}
		
		return true;
			
	}
	
	@SuppressWarnings("resource")
	private String readStream(InputStream in) {
		java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
