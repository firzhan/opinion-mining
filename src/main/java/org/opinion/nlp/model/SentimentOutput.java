package org.opinion.nlp.model;

public class SentimentOutput {

	private String word;
	private double sentiment;
	private int index;

	public SentimentOutput() {
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public double getSentiment() {
		return sentiment;
	}

	public void setSentiment(double sentiment) {
		this.sentiment = sentiment;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
