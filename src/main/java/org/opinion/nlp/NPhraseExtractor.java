package org.opinion.nlp;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import org.opinion.nlp.model.ConceptModel;
import org.opinion.nlp.model.CorefModel;
import org.opinion.nlp.model.CorefModelCollection;
import org.opinion.nlp.model.EmbeddedToken;
import org.opinion.nlp.model.SentenceCollection;
import org.opinion.nlp.model.SentimentOutput;
import org.opinion.nlp.model.WordModel;
import org.opinion.nlp.util.NPhraseUtil;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class NPhraseExtractor {

	private StanfordCoreNLP pipeline;
	private Annotation document;
	private static MaxentTagger tagger =  new MaxentTagger("/home/firzhan/operation-infinity/code/pos-tagger/src/lib/tagger.taggers/english-left3words-distsim.tagger");
	private static IDictionary dict;

	private OWLProcessor owlProcessor;
	private SentimentExtractor sentimentExtractor;


	public NPhraseExtractor() throws IOException, OWLOntologyCreationException {

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, mention, coref");
		//props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("pos.model", "/home/firzhan/operation-infinity/code/pos-tagger/src/lib/tagger.taggers/english-left3words-distsim.tagger");
		props.put("ner.model","/home/firzhan/operation-infinity/stanford-ner-2014-01-04/classifiers/english.conll.4class.distsim.crf.ser.gz");
		props.put("parse.model","/home/firzhan/operation-infinity/stanford-parser-full-2015-12-09/parser/englishPCFG.ser.gz");
		props.setProperty("ner.useSUTime", "false");

		this.pipeline = new StanfordCoreNLP(props);

		dict = new Dictionary( new URL( "file", null, "/home/firzhan/operation-infinity/wordnet3.1/dict"));
		dict.open();

		owlProcessor = new OWLProcessor(dict);
		owlProcessor.loadOWL();
		sentimentExtractor = new SentimentExtractor();

		NPhraseUtil.addNegationTerms();
		NPhraseUtil.addStopWords();
		NPhraseUtil.addStopWordsVerb();
	}


	public static MaxentTagger getTagger() {
		return tagger;
	}

	public static IDictionary getDict() {
		return dict;
	}

	public void prepareDocumentText(String documentText){

		// Create an empty Annotation just with the given text
		this.document = new Annotation(documentText);

		// run all Annotators on this text
		this.pipeline.annotate(document);
	}

	public String lemmatize(){
		//List<String> lemmas = new LinkedList<String>();
		String lemmas = "";

		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			// Iterate over all tokens in a sentence
			for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				// Retrieve and add the lemma for each word into the
				// list of lemmas
				//lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
				lemmas = lemmas + token.get(CoreAnnotations.LemmaAnnotation.class)+" ";

			}
		}
		return lemmas.trim();
	}

	public List<CoreMap> getSentences(){
		return this.document.get(CoreAnnotations.SentencesAnnotation.class);
	}

	public void loadOWL() throws OWLOntologyCreationException {

		//load the OWL
		//OWLProcessor owlProcessor = new OWLProcessor(NPhraseExtractor.getDict());
		owlProcessor.loadOWL();
		/*Map<String, ConceptModel> conceptModelMap = owlProcessor.getConceptModelMap();
		for (Map.Entry<String, ConceptModel> entry : conceptModelMap.entrySet()){

			ConceptModel conceptModel = entry.getValue();


		}*/
	}



	private void idenitifyNER() {

		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		StringBuilder stringBuilder = new StringBuilder();
		List<String> tokensList = new ArrayList<String>();
		for (CoreMap sentence : sentences) {

			String prevNeToken = "O";
			String currNeToken;
			boolean newToken = true;
			// Iterate over all tokens in a sentence
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

				currNeToken = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				String word = token.get(CoreAnnotations.TextAnnotation.class);

				if (currNeToken.equals("O")) {

					if (!prevNeToken.equals("O") && (stringBuilder.length() > 0)) {
						handleEntity(prevNeToken, stringBuilder, tokensList);
						newToken = true;
					}

					continue;
				}

				if (newToken) {
					prevNeToken = currNeToken;
					newToken = false;
					stringBuilder.append(word);
					continue;
				}

				if (currNeToken.equals(prevNeToken)) {
					stringBuilder.append(" ").append(word);
				} else {
					// We're done with the current entity - print it out and reset
					// TODO save this token into an appropriate ADT to return for useful processing..
					handleEntity(prevNeToken, stringBuilder, tokensList);
					newToken = true;
				}

				prevNeToken = currNeToken;

				// Retrieve and add the lemma for each word into the
				// list of lemmas
				//lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
				//lemmas = lemmas + token.get(CoreAnnotations.LemmaAnnotation.class)+" ";
			}
		}
		//TODO - do some cool stuff with these tokens!
		System.out.println("We extracted " + tokensList.size() + "tokens of interest from the input text");
	}

	private void handleEntity(String inKey, StringBuilder stringBuilder, List tokensList) {
		System.out.println("'" + stringBuilder + " ' is a " + inKey);
		tokensList.add(new EmbeddedToken(inKey, stringBuilder.toString()));
		stringBuilder.setLength(0);
	}


	public String identifyCoref(Map<Integer, SentenceCollection> sentenceMap){

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

		String coreffedString = "";
		for (Map.Entry<Integer, SentenceCollection>  sentenceMapEntry : sentenceMap.entrySet()){

			SentenceCollection sentenceCollection = sentenceMapEntry.getValue();

			coreffedString += sentenceCollection.toString() + " .  ";
		}

		System.out.println(coreffedString);

		return coreffedString;
	}

	public void assessPolarity(String posTaggedSentence){

		//this_DT be_VB really_RB a_DT fun_NN read_NN ._.

		System.out.println("Polarity is going to be assessed");

		String[] sentences = posTaggedSentence.split("\\._\\. ");
		List<SentimentOutput> sentimentOutputList = new ArrayList<SentimentOutput>();
		for (String sentence : sentences) {
			System.out.println("[" + sentence + "]");

			Map<Integer, WordModel> wordMap = new HashMap<Integer, WordModel>();
			Map<Integer, WordModel> nounMap = new HashMap<Integer, WordModel>();

			StringTokenizer interSentenceTokenizer = new StringTokenizer(sentence);

			int index = 0;
			while (interSentenceTokenizer.hasMoreTokens()){

				String tokenString = interSentenceTokenizer.nextToken();
				int lastIndex = tokenString.lastIndexOf("_");

				String word = tokenString.substring(0, lastIndex);
				String posTag = tokenString.substring(lastIndex+1);

				if(posTag.equalsIgnoreCase("DT")){
					continue;
				}

				WordModel wordModel = new WordModel();
				wordModel.setIndex(index);
				wordModel.setTag(posTag);
				wordModel.setWord(word);

				if(posTag.equals("NN") || posTag.equals("NNS")){
					nounMap.put(index, wordModel);
				}

				wordMap.put(index, wordModel);
				index++;
			}

			int totalCount = index;
			//check NN and NNS with concepts
			for (Map.Entry<Integer, WordModel> nounWordEntry : nounMap.entrySet()){

				WordModel nounWordModel = nounWordEntry.getValue();

				//look for spelling check



				//get the synoyms of this word

				System.out.println("Concept is going to be looked:" + nounWordModel.getWord());
				IIndexWord idxWord = dict.getIndexWord(nounWordModel.getWord(), POS.NOUN);

				List<String> wordList = new ArrayList<String>();

				System.out.println("*****************************************");
				System.out.println("[[");

				if(idxWord != null){
					IWordID wordID = idxWord.getWordIDs().get(0);
					IWord word = dict.getWord(wordID);

					//Adding Related Words to List of Realted Words
					ISynset synset = word.getSynset();
					for (IWord w : synset.getWords()) {
						System.out.println(w.getLemma() + ", ");
						wordList.add(w.getLemma());
					}
				} else {
					wordList.add(nounWordModel.getWord());
				}

				System.out.println("]]");
				System.out.println("*****************************************");

				//Check do we have concept for this word
				ConceptModel conceptModel = owlProcessor.getConcept(wordList);

				if(conceptModel == null){
					System.out.println("Concept:" + nounWordModel.getWord() + " doesn't exist as concept");
					continue;
					}

				index = nounWordModel.getIndex();
				int endingIndex = totalCount -1 ;
				int beginningIndex = 0;
				if(index +4 <= totalCount ){
					endingIndex = index + 3;
				}

				if(index -4 >= 0){
					beginningIndex = index - 3;
				}

				double sentiment = checkForAdjectives(nounWordModel, wordMap, beginningIndex, endingIndex);

				System.out.println("Sentiment for noun:" + nounWordModel.getWord() + "   sentiment:" + sentiment);
				SentimentOutput sentimentOutput = new SentimentOutput();
				sentimentOutput.setWord(nounWordModel.getWord());
				sentimentOutput.setIndex(1);
				sentimentOutput.setSentiment(sentiment);

				sentimentOutputList.add(sentimentOutput);
				System.out.println("\n\n\n\n\n\n");

			}

			System.out.println("********* Going to Print Output ***************");
			for (SentimentOutput sentimentOutput : sentimentOutputList){
				System.out.println("word:" + sentimentOutput.getWord() + "  sentiment:" + sentimentOutput
						.getSentiment() + "   ");
			}

			System.out.println("********* Ending of Printing Output ***************");
		}

	}

	private double checkForAdjectives(WordModel wordModel, Map<Integer, WordModel> wordMap, int beginningIndex, int
			endingIndex ) {

		int index = wordModel.getIndex();

		double overAllSentiment = 0;

		System.out.println("Going to Look for adjectives");
		//Check for adjectives
		for (int countingIndex = beginningIndex; countingIndex <= endingIndex; ++countingIndex) {

			if (countingIndex == index)
				continue;

			//start processing adjective
			boolean negative = false;
			double sentiment = 0;
			WordModel internalWordModel = wordMap.get(countingIndex);
			if (internalWordModel.getTag().equalsIgnoreCase("JJ")) {

				//check the polarity of the adjectives

				sentiment = sentimentExtractor.extract(internalWordModel.getWord(), "a");
				System.out.println("Sentiment for first adjective:" + internalWordModel.getWord() + " Sentiment: "
						+ sentiment );

				//look for any kind of and
				boolean breakLoop = false;
				int tempCountingIndex = countingIndex;
				int adjectiveCount = 1;
				while (!breakLoop  ) {

					tempCountingIndex += 1;

					if (tempCountingIndex == index)
						continue;

					if(tempCountingIndex == wordMap.size()){

						sentiment = sentiment/adjectiveCount;
						if(negative && overAllSentiment > 0){
							if(sentiment > 0){
								sentiment = -1 * sentiment;
							}
						}

						if(overAllSentiment != 0){
							overAllSentiment = overAllSentiment * sentiment;
						} else {
							overAllSentiment = sentiment;
						}

						breakLoop = true;
						continue;
					}

					WordModel adjectiveModel = wordMap.get(tempCountingIndex);

					System.out.println("tempCountingIndex:" + tempCountingIndex + " wordMap size:" + wordMap.size());
					if (adjectiveModel.getTag().equalsIgnoreCase(",")) {
						System.out.println("Encountered Tag" + adjectiveModel.getTag() + " No effect on sentiment");
						continue;
					} else if (adjectiveModel.getTag().equalsIgnoreCase("CC")) {
						System.out.println("Encountered Tag" + adjectiveModel.getTag());
						if (adjectiveModel.getWord().equalsIgnoreCase("AND")) {
							System.out.println("Encountered Tag" + adjectiveModel.getTag() + " continuing without " +
									"changing sentiment");
							continue;
						} else if (adjectiveModel.getWord().equalsIgnoreCase("NOR")) {
							System.out.println("Encountered Tag" + adjectiveModel.getTag() + " Marked the sentiment " +
									" to negative");
							negative = true;
						}
					} else if (adjectiveModel.getTag().equalsIgnoreCase("JJ")) {
						//check the polarity of the adjective
						double internalSentiment = sentimentExtractor.extract(adjectiveModel.getWord(), "a");
						sentiment += internalSentiment;
						System.out.println("Sentiment for internal adjective:" + adjectiveModel.getWord() +
								" sentiment:" + sentiment );
						++adjectiveCount;
					} else {

						if(tempCountingIndex > endingIndex){
							breakLoop = true;
						}
					}

					if(tempCountingIndex < wordMap.size()-1){
						breakLoop = true;
					}

					if(breakLoop){
						sentiment = sentiment/adjectiveCount;
						if(negative && overAllSentiment > 0){
							if(sentiment > 0){
								sentiment = -1 * sentiment;
							}
						}

						if(overAllSentiment != 0){
							overAllSentiment = overAllSentiment * sentiment;
						} else {
							overAllSentiment = sentiment;
						}
					}
				}
			}

			if(internalWordModel.getTag().equalsIgnoreCase("RB")) {
				double sentimentValue = sentimentExtractor.extract(internalWordModel.getWord(), "r");
				overAllSentiment += sentimentValue;
				System.out.println("Sentiment for adverb:" + internalWordModel.getWord() +
						" sentiment value:" + sentimentValue + " overAllSentiment:" + overAllSentiment);
			}
		}

		System.out.println("Over all sentiment for word:" + wordModel.getWord() + " sentiment:" + overAllSentiment);
		return overAllSentiment;
	}

	public Map<String, Integer> calculateFrequencyOfConcept(String posTaggedSentence){

		//this_DT be_VB really_RB a_DT fun_NN read_NN ._.

		System.out.println("Frequency of concepts are going to be assessed");

		String[] sentences = posTaggedSentence.split("\\._\\. ");
		Map<String, Integer> nounFrequencyMap = new HashMap<String, Integer>();
		for (String sentence : sentences) {
			System.out.println("[" + sentence + "]");

			Map<Integer, WordModel> wordMap = new HashMap<Integer, WordModel>();
			Map<Integer, WordModel> nounMap = new HashMap<Integer, WordModel>();

			StringTokenizer interSentenceTokenizer = new StringTokenizer(sentence);

			int index = 0;
			while (interSentenceTokenizer.hasMoreTokens()){

				String tokenString = interSentenceTokenizer.nextToken();
				int lastIndex = tokenString.lastIndexOf("_");

				String word = tokenString.substring(0, lastIndex);
				String posTag = tokenString.substring(lastIndex+1);

				WordModel wordModel = new WordModel();
				wordModel.setIndex(index);
				wordModel.setTag(posTag);
				wordModel.setWord(word);

				if(posTag.equals("NN") || posTag.equals("NNS")){
					nounMap.put(index, wordModel);
				}

				wordMap.put(index, wordModel);
				index++;
			}

			int totalCount = index;
			//check NN and NNS with concepts
			for (Map.Entry<Integer, WordModel> nounWordEntry : nounMap.entrySet()){

				WordModel nounWordModel = nounWordEntry.getValue();

				//look for spelling check



				//get the synoyms of this word

				System.out.println("Concept is going to be looked:" + nounWordModel.getWord());
				IIndexWord idxWord = dict.getIndexWord(nounWordModel.getWord(), POS.NOUN);

				List<String> wordList = new ArrayList<String>();

				System.out.println("*****************************************");
				System.out.println("[[");

				if(idxWord != null){
					IWordID wordID = idxWord.getWordIDs().get(0);
					IWord word = dict.getWord(wordID);

					//Adding Related Words to List of Realted Words
					ISynset synset = word.getSynset();
					for (IWord w : synset.getWords()) {
						System.out.println(w.getLemma() + ", ");
						wordList.add(w.getLemma());
					}
				} else {
					wordList.add(nounWordModel.getWord());
				}

				System.out.println("]]");
				System.out.println("*****************************************");

				//Check do we have concept for this word
				ConceptModel conceptModel = owlProcessor.getConcept(wordList);

				if(conceptModel == null){
					System.out.println("Concept:" + nounWordModel.getWord() + " doesn't exist as concept");
					continue;
				}

				index = nounWordModel.getIndex();
				boolean breakLoop = false;
				int incrementalIndex = index + 1;
				while (!breakLoop && incrementalIndex <= totalCount -1) {


					if(incrementalIndex == wordMap.size()-1){
						breakLoop = true;
						continue;
					}

					WordModel internalWordModel = wordMap.get(incrementalIndex);

					if (internalWordModel.getTag().equalsIgnoreCase("CC")) {

						if (internalWordModel.getWord().equalsIgnoreCase("AND") || internalWordModel.getWord()
								.equalsIgnoreCase("OR")) {

							++incrementalIndex;

							WordModel incrementalWordModel = wordMap.get(incrementalIndex);

							if (incrementalWordModel.getTag().equalsIgnoreCase("NN")) {

								Integer frequency = nounFrequencyMap.get(incrementalWordModel.getWord());

								if (frequency != null) {
									nounFrequencyMap.put(incrementalWordModel.getWord(), ++frequency);
								} else {
									nounFrequencyMap.put(incrementalWordModel.getWord(), 1);
								}

								breakLoop = true;

							}
						}
					} else if (internalWordModel.getTag().equalsIgnoreCase(",")) {

						++incrementalIndex;

						WordModel incrementalWordModel = wordMap.get(incrementalIndex);

						if (incrementalWordModel.getTag().equalsIgnoreCase("NN")) {

							Integer frequency = nounFrequencyMap.get(incrementalWordModel.getWord());

							if (frequency != null) {
								nounFrequencyMap.put(incrementalWordModel.getWord(), ++frequency);
							} else {
								nounFrequencyMap.put(incrementalWordModel.getWord(), 1);
							}


						} else {
							breakLoop = true;
						}

					} else {
						breakLoop = true;
					}

				}


				Integer frequency = nounFrequencyMap.get(nounWordModel.getWord());

				if(frequency != null){
					nounFrequencyMap.put(nounWordModel.getWord(), ++frequency);
				} else {
					nounFrequencyMap.put(nounWordModel.getWord(), 1);
				}

				//return nounFrequencyMap;

/*
				int endingIndex = totalCount -1 ;
				int beginningIndex = 0;
				if(index +1 <= totalCount ){
					endingIndex = index;
				}

				if(index >= 0){
					beginningIndex = index;
				}*/


				/*double sentiment = checkForAdjectives2(nounWordModel, wordMap, beginningIndex, endingIndex);

				System.out.println("Sentiment for noun:" + nounWordModel.getWord() + "   sentiment:" + sentiment);
				SentimentOutput sentimentOutput = new SentimentOutput();
				sentimentOutput.setWord(nounWordModel.getWord());
				sentimentOutput.setIndex(1);
				sentimentOutput.setSentiment(sentiment);

				sentimentOutputList.add(sentimentOutput);
				System.out.println("\n\n\n\n\n\n");*/

			}

			/*System.out.println("********* Going to Print Output ***************");
			for (SentimentOutput sentimentOutput : sentimentOutputList){
				System.out.println("word:" + sentimentOutput.getWord() + "  sentiment:" + sentimentOutput
						.getSentiment() + "   ");
			}

			System.out.println("********* Ending of Printing Output ***************");*/
		}

		return nounFrequencyMap;
	}


/*	private void updateWithSynonyms(List<String> labelList){

		List<String> newLabelList = new ArrayList<String>();

		for (String label : labelList){


		}
	}*/

	/*public static void main(String[] args) throws IOException, OWLOntologyCreationException {

		String documentText = "This is really a fun read. The story is interesting, the writing flows easily, the characters are real and believable, and even though I thought a story about a circus during the depression era might be depressing, it was extremely interesting. I really enjoyed it and have placed it amongst my 'keepers.";

		System.out.println("Starting Stanford Lemmatizer");
		NPhraseExtractor nPhraseExtractor = new NPhraseExtractor();

		nPhraseExtractor.prepareDocumentText(documentText);

		String lemmatizeString = nPhraseExtractor.lemmatize();
		System.out.println(lemmatizeString);

		String tagged = tagger.tagString(lemmatizeString);
		System.out.println(tagged);

		//load the OWL
		OWLProcessor owlProcessor = new OWLProcessor(dict);
		owlProcessor.loadOWL();
		Map<String, ConceptModel> conceptModelMap = owlProcessor.getConceptModelMap();

		nPhraseExtractor.assessPolarity(tagged);
		//nPhraseExtractor.idenitifyNER();

		//nPhraseExtractor.identifyCoref();

	}*/

}
