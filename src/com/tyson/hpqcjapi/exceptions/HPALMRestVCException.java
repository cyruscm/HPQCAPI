package com.tyson.hpqcjapi.exceptions;

import com.hpe.infrastructure.Response;

/**
 * Exception for version control errors in HP ALM.
 * @author MARTINCORB
 *
 */
public class HPALMRestVCException extends HPALMRestException {

	private static final long serialVersionUID = -8974058400925084535L;

	public HPALMRestVCException(Response response) {
		super(response);
	}

}
