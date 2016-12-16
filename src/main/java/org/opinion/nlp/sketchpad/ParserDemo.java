package org.opinion.nlp.sketchpad;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class ParserDemo {

	private static MaxentTagger tagger =  new MaxentTagger("/home/firzhan/operation-infinity/code/pos-tagger/src/lib/tagger.taggers/english-left3words-distsim.tagger");
	private StanfordCoreNLP pipeline;

	private Annotation document;


	public ParserDemo() {

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		//props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("pos.model", "/home/firzhan/operation-infinity/code/pos-tagger/src/lib/tagger.taggers/english-left3words-distsim.tagger");
		props.put("ner.model","/home/firzhan/operation-infinity/stanford-ner-2014-01-04/classifiers/english.conll.4class.distsim.crf.ser.gz");
		props.put("parse.model","/home/firzhan/operation-infinity/stanford-parser-full-2015-12-09/parser/englishPCFG.ser.gz");
		props.setProperty("ner.useSUTime", "false");

		this.pipeline = new StanfordCoreNLP(props);

	}

	public void parser(String documentText) {

		// Create an empty Annotation just with the given text
		this.document = new Annotation(documentText);

		// run all Annotators on this text
		this.pipeline.annotate(document);

		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {

			Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

			// Get dependency tree

			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
			Collection<TypedDependency> td = gs.typedDependenciesCollapsed();
			System.out.println(td);

			Object[] list = td.toArray();
			System.out.println(list.length);
			TypedDependency typedDependency;

			for (Object object : list) {
				typedDependency = (TypedDependency) object;
				System.out.println("Depdency Name:" + typedDependency.dep().lemma() + " :: " + " Node=" +
						typedDependency
						.reln());
				/*if (typedDependency.reln().getShortName().equals("nsubj")) {
					//your code
					System.out.println("gov:" + typedDependency.gov().lemma() + " index:" + typedDependency.gov().hashCode());
					System.out.println("dep:" + typedDependency.dep().lemma() + " index:" + typedDependency.dep()
							.hashCode());
				}*/
			}
		}
	}

	public static void main(String[] args) {

		ParserDemo parserDemo = new ParserDemo();
		parserDemo.parser("This story helped me in gaining a better understanding of what it is to be elderly.  The audio version is narrated by two men, both are meant to be the main character, one voice representing his twenties and the other is in his nineties.  It certainly isn't centered around being of an advanced age, but as a side story, it was still very clear.I was pleasantly surprised by my conflicted response to the story.  On the one hand, his lamentations over being at the end of his life made me sad; on the other hand, he was funny and his life was a full one, giving him many wonderful memories.My advice to a prospective listener or reader?  If you are bored with the first chapter, hang in there, it picks up and is worth getting to the end.");
	}

}
