package com.tyson.hpqcjapi.exceptions;

import com.hpe.infrastructure.Response;

/**
 * Exception for invalid user credentials to complete specified action
 * 
 * @author MARTINCORB
 */
public class HPALMRestPermException extends HPALMRestException {

	private static final long serialVersionUID = -6303220824943499440L;

	public HPALMRestPermException(Response response) {
		super(response);
	}

}
