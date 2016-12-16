package org.opinion.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.opinion.nlp.model.SentenceCollection;
import org.opinion.nlp.util.NPhraseUtil;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class FeatureAverageCalculator {

	public FeatureAverageCalculator() {
	}



	public void readFiles() throws IOException, OWLOntologyCreationException {

		File directory = new File("/home/firzhan/operation-infinity/review");
		File files[] = directory.listFiles();

		for (File file : files) {

			String documentText = NPhraseUtil.readFile(file.getAbsolutePath());

			System.out.println("Starting Stanford Lemmatizer");
			NPhraseExtractor nPhraseExtractor = new NPhraseExtractor();

			nPhraseExtractor.prepareDocumentText(documentText);

			String lemmatizeString = nPhraseExtractor.lemmatize();
			System.out.println(lemmatizeString);

			List<CoreMap> sentences = nPhraseExtractor.getSentences();

			Map<Integer, SentenceCollection> sentenceMap = new HashMap<Integer, SentenceCollection>();
			int count = 1;
			for(CoreMap sentence: sentences) {
				// Iterate over all tokens in a sentence

				String tagged = NPhraseExtractor.getTagger().tagString(sentence.get(CoreAnnotations.TextAnnotation.class));
				//System.out.println(tagged2);

				String[] sentenceArray = tagged.split("\\._\\. ");

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

			String correfedString = nPhraseExtractor.identifyCoref(sentenceMap);

			nPhraseExtractor.prepareDocumentText(correfedString);

			lemmatizeString = nPhraseExtractor.lemmatize();
			//System.out.println(lemmatizeString);


			String tagged = NPhraseExtractor.getTagger().tagString(lemmatizeString);
			System.out.println("********************************************************************************");
			System.out.println(tagged);
			System.out.println("********************************************************************************");

			Map<String, Integer> nounfrequencyMap = nPhraseExtractor.calculateFrequencyOfConcept(tagged);
			Map<String, Double> conceptFrequencyMap = calculateFrequency(nounfrequencyMap);


		}
	}


	private Map<String, Double> calculateFrequency(Map<String, Integer> nounfrequencyMap){

		Map<String, Double> conceptFrequencyMap = new HashMap<String, Double>();
		Integer totalFrequency = 0;
		for (Map.Entry<String, Integer> nounfrequencyMapEntry : nounfrequencyMap.entrySet()){

			Integer frequencyValue = nounfrequencyMapEntry.getValue();
			totalFrequency += frequencyValue;
		}

		for (Map.Entry<String, Integer> nounfrequencyMapEntry : nounfrequencyMap.entrySet()){

			Integer frequencyValue = nounfrequencyMapEntry.getValue();
			String conceptKey = nounfrequencyMapEntry.getKey();

			double average = frequencyValue/(double)totalFrequency;

			conceptFrequencyMap.put(conceptKey, average);
		}
		return conceptFrequencyMap;
	}



	public static void main(String[] args) throws IOException, OWLOntologyCreationException {

		FeatureAverageCalculator featureAverageCalculator = new FeatureAverageCalculator();
		featureAverageCalculator.readFiles();
	}
}
