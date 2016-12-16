package org.opinion.nlp.model;

public class CorefModel {

	private String mentionSpan = null;
	public int startIndex;
	public int headIndex;
	public String mentionType;
	public int sentNum;
	public int endIndex;

	public CorefModel() {
	}

	public String getMentionSpan() {
		return mentionSpan;
	}

	public void setMentionSpan(String mentionSpan) {
		this.mentionSpan = mentionSpan;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getHeadIndex() {
		return headIndex;
	}

	public void setHeadIndex(int headIndex) {
		this.headIndex = headIndex;
	}

	public String getMentionType() {
		return mentionType;
	}

	public void setMentionType(String mentionType) {
		this.mentionType = mentionType;
	}

	public int getSentNum() {
		return sentNum;
	}

	public void setSentNum(int sentNum) {
		this.sentNum = sentNum;
	}


}
