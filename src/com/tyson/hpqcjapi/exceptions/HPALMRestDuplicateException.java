package com.tyson.hpqcjapi.exceptions;

import infrastructure.Response;

/**
 * Exception for multiple Entities returned when only one is available. 
 * @author MARTINCORB
 *
 */
public class HPALMRestDuplicateException extends HPALMRestException {

	private static final long serialVersionUID = -7117374899294895180L;

	public HPALMRestDuplicateException(Response response) {
		super(response);
	}

}
