package org.opinion.nlp.sketchpad;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.SynsetID;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

public class SynsetExtractor {

	public static IDictionary dict;



	public static void main(String[] args) throws IOException {

		dict = new Dictionary( new URL( "file", null, "/home/firzhan/operation-infinity/wordnet3.1/dict"));

		dict.open();

		IIndexWord idxWord = dict.getIndexWord("protagonist", POS.NOUN);
		IWordID wordID = idxWord.getWordIDs().get(0);
		IWord word = dict.getWord(wordID);


		//Adding Related Words to List of Realted Words
		ISynset synset = word.getSynset();
		for (IWord w : synset.getWords()) {
			System.out.println(w.getLemma());
		}

	}
}
