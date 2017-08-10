package test.java;

import java.io.PrintWriter;
import java.io.StringWriter;

public class utils {

	public static String getStackStrace(Throwable throwable) {
	     final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     return sw.getBuffer().toString();	
	}
	
	public static String joinStringArray(String[] args) {
		StringBuilder b = new StringBuilder();
		for (String string : args) {
			if (b.length() > 0) {
				b.append(" ");
			}
			b.append(string);
		}
		return b.toString();
	}
}
