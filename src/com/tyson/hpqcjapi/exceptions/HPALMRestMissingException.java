package com.tyson.hpqcjapi.exceptions;

import infrastructure.Response;

/**
 * Exception for requested file missing.
 * @author MARTINCORB
 *
 */
public class HPALMRestMissingException extends HPALMRestException {

	private static final long serialVersionUID = -147268316808611995L;

	public HPALMRestMissingException(Response response) {
		super(response);
	}
}
