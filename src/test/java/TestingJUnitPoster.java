package test.java;

import java.util.List;

import com.tyson.hpqcjapi.JUnitPoster;
import com.tyson.hpqcjapi.JUnitReader;
import com.tyson.hpqcjapi.exceptions.HPALMRestException;
import com.tyson.hpqcjapi.exceptions.HPALMRestMissingException;
import com.tyson.hpqcjapi.types.LinkedTestCase;
import com.tyson.hpqcjapi.utils.ALMManager;
import com.tyson.hpqcjapi.utils.Logger;

/**
 * Wraps JUnitPoster with some getters that shouldn't be publicly accessible 
 * for the sake of testing verification
 * @author MARTINCORB
 *
 */
public class TestingJUnitPoster extends JUnitPoster {

	public TestingJUnitPoster(JUnitReader results) throws HPALMRestException {
		super(results);
	}
	
	public List<LinkedTestCase> getCases() {
		return cases;
	}
	
	public ALMManager getManager() {
		return con;
	}
	
	public void deleteInstance(String id) throws Exception {
		con.deleteTestInstance(id);
	}
	
	public boolean testInstanceExists(String id) throws Exception {
		return (con.getTestInstance(id) != null);
	}
	
	public String getRunStatus(String id) throws Exception {
		return getField(con.getRun(id), "status");
	}
	
	public String getRunStepStatus(String runId, String id) throws Exception {
		try {
			return getField(con.getRunStep(runId, id), "status");
		} catch (HPALMRestException e) {
			Logger.logError(new String(e.getResponse().getResponseData()));
			Logger.logError("" + e.getResponse().getStatusCode());
			Logger.logError("" + e.getResponse().getResponseHeaders());
			return null;
		}
	}
}
