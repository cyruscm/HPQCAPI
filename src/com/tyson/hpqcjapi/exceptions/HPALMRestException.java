package com.tyson.hpqcjapi.exceptions;

import infrastructure.Response;

public class HPALMRestException extends Exception{
	
	private static final long serialVersionUID = 5880678163779743169L;
	private Response response;
	
	public HPALMRestException(Response response) {
		this.response = response;
	}
	
	public Response getResponse() {
		return response;
	}
}
