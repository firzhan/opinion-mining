package org.opinion.nlp.sketchpad;

import java.util.StringTokenizer;

public class StringTest {

	public static void main(String[] args) {

		String testString = "If you are bored with the first chapter, hang in there, it picks up and is worth getting to the end.";

		StringTokenizer stringTokenizer = new StringTokenizer(testString);

		while (stringTokenizer.hasMoreTokens()){
			System.out.println(stringTokenizer.nextToken());
		}
	}
}
