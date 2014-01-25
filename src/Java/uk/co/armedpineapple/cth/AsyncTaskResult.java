/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

public class AsyncTaskResult<T> {
	private T					result;
	private Exception	error;

	public T getResult() {
		return result;
	}

	public Exception getError() {
		return error;
	}

	public AsyncTaskResult(T result) {
		super();
		this.result = result;
	}

	public AsyncTaskResult(Exception error) {
		super();
		this.error = error;
	}
}