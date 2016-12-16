package org.opinion.nlp.model;

public class SentenceCollection {

	public String[] stringArray;

	public void clearSentence(int beginIndex, int headIndex){

		beginIndex = beginIndex -1;
		headIndex = headIndex - 1;
		for (int i = beginIndex; i <= headIndex; i++){
			stringArray[i] = "";
		}
	}

	public void updateSentence(CorefModel nomailCorefModel, CorefModel corefModel){

		int overridingStringLength = nomailCorefModel.headIndex - nomailCorefModel.getStartIndex() + 1;
		int originalStringLength = corefModel.headIndex - corefModel.getStartIndex() + 1;

		stringArray[corefModel.getStartIndex() - 1] = nomailCorefModel.getMentionSpan();/*

		int indexSize = nomailCorefModel.headIndex - nomailCorefModel.getStartIndex() + 1;
		int endIndex = beginIndex + indexSize - 1;


		String mentionSpan = nomailCorefModel.getMentionSpan();
		String[] mentionSpanArray = mentionSpan.split(" ");

		for (int i = beginIndex, j = 0; i <= endIndex && j <mentionSpanArray.length ; i++, j++ ){

			stringArray[beginIndex] =
		}*/

	}


	public String toString(){

		StringBuilder stringBuilder = new StringBuilder();

		for(String word: stringArray){

			if(word != null && !word.equals("")){
				stringBuilder.append(word + " ");
			}
		}

		return stringBuilder.toString();
	}

}
