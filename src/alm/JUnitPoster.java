package alm;

import java.util.HashMap;
import java.util.Map;

import infrastructure.Entities;
import infrastructure.Entity;
import infrastructure.Entity.Fields.Field;
import test.Assert;

public class JUnitPoster {

	ConnectionManager con;
	
	public enum Response {
		OK, RETRY, FAILURE, MISSING
	}
	
	private Response response;
	
	public JUnitPoster() {
		con = new ConnectionManager();
		response = Response.OK;
	}
	
	public void init() {
		try {
			con.validatedLogin();
		} catch (RuntimeException e) {
			response = Response.FAILURE;
		}
		response = Response.OK;
	}
	
	public void createTest(String name) {
		
	}

	
	public String getTestID(String name) {
		try {
			Map<String, String> queryParams = new HashMap<String, String>();
			queryParams.put("name", name);
			Entities response = con.queryEntity(Endpoints.TESTS, queryParams);
			
			if (response.Count() == 0) {
				Logger.logWarning("Test with name " + name + " doesn't exist. Please create one.");
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
	public String getTestIDSample(String name) {
		Logger.log(Endpoints.TESTS);
		try {
			Map<String, String> queryParams = new HashMap<String, String>();
			queryParams.put("name", "TestingRestOverPostman");
			Entities response = con.queryEntity(Endpoints.TESTS, queryParams);
			Logger.log("COUNT:" + response.Count());
			for (Entity entity : response.getEntities()) {
				Logger.log(entity.toString());
				for (Field field : entity.getFields().getField()) {
					Logger.log(field.toString());
					Logger.log(field.getName() + ":");
					for (String value : field.getValue()) {
						Logger.log(value);
					}
					
				}
			}
			//getFieldValue("name", response);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.log("Done");
		return null;
	}
}
