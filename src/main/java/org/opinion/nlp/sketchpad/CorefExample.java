package org.opinion.nlp.sketchpad;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.opinion.nlp.NPhraseExtractor;
import org.opinion.nlp.model.CorefModel;
import org.opinion.nlp.model.CorefModelCollection;
import org.opinion.nlp.model.SentenceCollection;

import java.net.URL;
import java.security.CodeSource;
import java.util.*;

public class CorefExample {

	public static void main(String[] args) throws Exception {

		String documentText = "I found Water for Elephants extremely entertaining, despite never seeing a circus or " +
				"having any interest in the subject.  The backdrop is the 1930's, and the book throws you into the era of carnivals, Prohibition, and the Great Depression.  You're led by slums with hobos, and everyone is dying for regular pay and something to eat.The story bounces back and forth between a man living out his twilight years in a nursing home, and his 23-year-old self... where circumstances find him joining a circus.  Life revolves around the train for these guys - they always travel in cramped space with all kinds of memorable characters.There are some great anecdotes so strange you'd think they were true.  My favorite was Rosie the bull elephant sneaking lemonade by pulling her stake out of the ground, and then being smart enough to replace it to avoid getting caught. The author respects the reader enough to let him figure out what's happening from contextual clues.  For instance, a show horse is put down, and the starving lions miraculously wind up fed.  You have to connect the dots to understand the whole story.Another positive: this book is not a tear-jerker and avoids supernatural components, unlike other popular fiction (e.g. Dean Koontz).  Note that there are some graphic sexual encounters and profanity that make this mature with a capital \\\"M\\\" - don't give it to anyone younger than a high school kid.";
		documentText = "This story helped me in gaining a better understanding of what it is to be elderly. The audio" +
				" version is narrated by two men, both are meant to be the main character, one voice representing his" +
				" twenties and the other is in his nineties.  It certainly isn't centered around being of an advanced" +
				" age, but as a side story, it was still very clear. I was pleasantly surprised by my conflicted response to the story.  On the one hand, his lamentations over being at the end of his life made me sad; on the other hand, he was funny and his life was a full one, giving him many wonderful memories. My advice to a prospective listener or reader?  If you are bored with the first chapter, hang in there, it picks up and is worth getting to the end.";
		Annotation document = new Annotation(documentText);
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
		props.put("pos.model", "/home/firzhan/operation-infinity/code/pos-tagger/src/lib/tagger" +
				".taggers/english-left3words-distsim.tagger");
		props.put("ner.model","/home/firzhan/operation-infinity/stanford-ner-2014-01-04/classifiers/english" +
				".conll.4class.distsim.crf.ser.gz");
		props.put("parse.model","/home/firzhan/operation-infinity/stanford-parser-full-2015-12-09/parser/englishPCFG" +
				".ser.gz");
		props.put("ner.useSUTime", "false");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		pipeline.annotate(document);



		Map<Integer, SentenceCollection> sentenceMap = new HashMap<Integer, SentenceCollection>();
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		int count = 1;
		for(CoreMap sentence: sentences) {
			// Iterate over all tokens in a sentence

			String tagged2 = NPhraseExtractor.getTagger().tagString(sentence.get(CoreAnnotations.TextAnnotation.class));
			System.out.println(tagged2);

			String[] sentenceArray = tagged2.split("\\._\\. ");

			StringBuilder stringBuilder = new StringBuilder();
			for(String sen : sentenceArray){
				stringBuilder.append(sen + " ");
			}

			StringTokenizer interSentenceTokenizer = new StringTokenizer(stringBuilder.toString());

			StringBuilder stringBuilder1 = new StringBuilder();
			int index = 0;
			while (interSentenceTokenizer.hasMoreTokens()){

				String tokenString = interSentenceTokenizer.nextToken();
				int lastIndex = tokenString.lastIndexOf("_");

				String word = tokenString.substring(0, lastIndex);
				stringBuilder1.append(word + " ");
			}

			String[] sentenceArray2 = stringBuilder1.toString().split(" ");

			SentenceCollection sentenceCollection = new SentenceCollection();
			sentenceCollection.stringArray = sentenceArray2;

			sentenceMap.put(count, sentenceCollection);

			count++;
			System.out.println(stringBuilder1.toString());
		}

		System.out.println("---");
		System.out.println("coref chains");
		Map<Integer, CorefModelCollection> corefModelCollectionMap = new HashMap<Integer, CorefModelCollection>();
		for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
			System.out.println("\t"+cc);
			System.out.println(cc.getMentionMap());
			List<CorefChain.CorefMention> corefMentions = cc.getMentionsInTextualOrder();
			System.out.println("size of the corefMentions:" + corefMentions.size());

			List<CorefModel> corefModelList = new ArrayList<CorefModel>();
			String nominalText = "";
			CorefModel nomailCorefModel = new CorefModel();
			for (CorefChain.CorefMention cm : corefMentions) {
				CorefModel corefModel = new CorefModel();
				System.out.println("---");
				System.out.println("full text: "+cm.mentionSpan);
				System.out.println("position: "+cm.position);
				System.out.println("start index of first word: "+cm.startIndex);
				System.out.println("head index:"+cm.headIndex);
				System.out.println("mention type:"+cm.mentionType.name());
				System.out.println("sentence num:"+cm.sentNum);

				if(cm.mentionType.name().equalsIgnoreCase("NOMINAL")){
					nomailCorefModel.setMentionSpan(cm.mentionSpan);
					nomailCorefModel.setStartIndex(cm.startIndex);
					nomailCorefModel.setHeadIndex(cm.headIndex);
					nomailCorefModel.setSentNum(cm.sentNum);
					nomailCorefModel.setMentionType(cm.mentionType.name());
					nomailCorefModel.endIndex = cm.endIndex;
					corefModelList.add(nomailCorefModel);
				} else {

					corefModel.setMentionSpan(cm.mentionSpan);
					corefModel.setStartIndex(cm.startIndex);
					corefModel.setHeadIndex(cm.headIndex);
					corefModel.setSentNum(cm.sentNum);
					corefModel.setMentionType(cm.mentionType.name());
					corefModel.endIndex = cm.endIndex;
					corefModelList.add(corefModel);
				}
			}

			CorefModelCollection corefModelCollection = new CorefModelCollection();
			corefModelCollection.corefModelList = corefModelList;
			corefModelCollection.nominalCorefModel = nomailCorefModel;

			corefModelCollectionMap.put(cc.getChainID(), corefModelCollection);


		}

		for (Map.Entry<Integer, CorefModelCollection>  corefModelCollectionEntry : corefModelCollectionMap.entrySet()){

			CorefModelCollection corefModelCollection = corefModelCollectionEntry.getValue();

			List<CorefModel> corefModelList = corefModelCollection.corefModelList;

			CorefModel nomailCorefModel = corefModelCollection.nominalCorefModel;

			for(CorefModel corefModel : corefModelList){

				SentenceCollection sentenceCollection = sentenceMap.get(corefModel.getSentNum());
				if(!corefModel.getMentionType().equalsIgnoreCase("NOMINAL") && corefModelCollection.nominalCorefModel
						.getMentionSpan() != null){

					if(sentenceCollection != null){
//						sentence = sentence.replace(corefModel.getMentionSpan(),nomailCorefModel.getMentionSpan());

						sentenceCollection.clearSentence(corefModel.getStartIndex(),corefModel.getHeadIndex());
						sentenceCollection.updateSentence(nomailCorefModel, corefModel);

					}


				}
				sentenceMap.put(corefModel.getSentNum(), sentenceCollection);
			}
		}

		String output = "";
		for (Map.Entry<Integer, SentenceCollection>  sentenceMapEntry : sentenceMap.entrySet()){

			SentenceCollection sentenceCollection = sentenceMapEntry.getValue();

			output += sentenceCollection.toString() + " .  ";
		}

		System.out.println(output);


/*		for (Map.Entry<Integer, CorefModelCollection>  corefModelCollectionEntry : corefModelCollectionMap.entrySet()){

			CorefModelCollection corefModelCollection = corefModelCollectionEntry.getValue();

			List<CorefModel> corefModelList = corefModelCollection.corefModelList;

			CorefModel nomailCorefModel = corefModelCollection.nominalCorefModel;

			for(CorefModel corefModel : corefModelList){

				if(!corefModel.getMentionType().equalsIgnoreCase("NOMINAL")){

					SentenceCollection sentenceCollection = sentenceMap.get(corefModel.getSentNum());

					if(sentenceCollection != null){
//
						String word = sentenceCollection.toString();
						System.out.println(word);
					}

				}
			}
		}*/




/*
		for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
			System.out.println("---");
			System.out.println("mentions");
			for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
				System.out.println("\t"+m);
			}
		}*/
	}
}
