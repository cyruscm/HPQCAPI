package com.tyson.hpqcjapi.types;

public class TestStatus {
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
