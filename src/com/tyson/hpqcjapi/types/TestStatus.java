package com.tyson.hpqcjapi.types;

public class TestStatus implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8987325112742920835L;

	public enum STATUS_TYPE {
		FAILED, SKIPPED, PASSED;

		@Override
		public String toString() {
			switch (this) {
			case FAILED:
				return "Failed";
			case SKIPPED:
				return "No-run";
			case PASSED:
				return "Passed";
			default:
				return "";
			}
		}
	}

	private String message;
	private STATUS_TYPE type;
	

	public TestStatus(STATUS_TYPE type, String message) {
		this.message = message;
		this.type = type;
	}
	
	public boolean equals(Object obj) {
		if (obj.getClass() != TestStatus.class) {
			return false;
		}
		TestStatus mappedObj = (TestStatus) obj;
		if (mappedObj.getMessage().equals(this.getMessage()) && mappedObj.getType().equals(this.getType())) {
			return true;
		}
		return false;
	}

	public STATUS_TYPE getType() {
		return type;
	}

	public String getTypeString() {
		return type.toString();
	}

	public String getMessage() {
		return message;
	}
}
