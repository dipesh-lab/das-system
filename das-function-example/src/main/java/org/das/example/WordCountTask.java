package org.das.example;

import org.das.core.function.CalcFunction;

public class WordCountTask {

	public CalcFunction function = (Object data) -> {
		System.out.println("I'm inside the function execution");
		System.out.println("Argument Type " + data.getClass().getName());
		return null;
	};

}