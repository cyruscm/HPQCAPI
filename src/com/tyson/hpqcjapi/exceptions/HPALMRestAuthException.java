package com.tyson.hpqcjapi.exceptions;

import infrastructure.Response;

/**
 * Exception for expired credentials or no set credentials.
 * @author MARTINCORB
 */
public class HPALMRestAuthException extends HPALMRestException {

	private static final long serialVersionUID = 6579807140789195697L;

	public HPALMRestAuthException(Response response) {
		super(response);
	}

}
