package org.opinion.nlp;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import org.opinion.nlp.model.ConceptModel;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OWLProcessor {

	private static String OWL_PATH = "/home/firzhan/operation-infinity/ontology/bookontology2.owl";
	private OWLOntologyManager ontologyManager;

	private IDictionary dictionary;

	private Map<String, ConceptModel> conceptModelMap = new HashMap<String, ConceptModel>();

	public OWLProcessor(IDictionary dictionary) {
		this.dictionary = dictionary;
		this.ontologyManager = OWLManager.createOWLOntologyManager();
	}

	public void loadOWL() throws OWLOntologyCreationException {

		File file = new File(OWL_PATH);
		// Load the local copy
		OWLOntology localBookOntology = ontologyManager.loadOntologyFromOntologyDocument(file);
		System.out.println("Loaded ontology: " + localBookOntology);

		Set<OWLClass> owlClasses = localBookOntology.getClassesInSignature();

		System.out.println("Classes");
		System.out.println("--------------------------------");

		for (OWLClass owlClass : owlClasses) {
			System.out.println("+: " + owlClass.getIRI().getShortForm());

			for (OWLObjectPropertyRangeAxiom op : localBookOntology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE)) {

				if (op.getRange().equals(owlClass)) {

					ConceptModel conceptModel = new ConceptModel(dictionary);
					conceptModel.setClassName(owlClass.getIRI().getShortForm());
					List<String> labelList = new ArrayList<String>();
					for(OWLAnnotationAssertionAxiom a : localBookOntology.getAnnotationAssertionAxioms(owlClass.getIRI())) {
						if(a.getProperty().isLabel()) {
							if(a.getValue() instanceof OWLLiteral) {
								OWLLiteral val = (OWLLiteral) a.getValue();
								System.out.println(owlClass.getIRI().getShortForm() + " labelled " + val.getLiteral());
								labelList.add(val.getLiteral());
							}
						}
						conceptModel.setLabelList(labelList);
					}

					Map<String,String> propertyMap = new HashMap<String, String>();
					for(OWLObjectProperty oop : op.getObjectPropertiesInSignature()){
						System.out.println("\t\t +: " + oop.getIRI().getShortForm());
						propertyMap.put(oop.getIRI().getShortForm(),"");
					}
					conceptModel.setPropertyMap(propertyMap);
					conceptModelMap.put(conceptModel.getClassName(), conceptModel);
				}
			}
		}
	}

	public Map<String, ConceptModel> getConceptModelMap() {
		return conceptModelMap;
	}

	public ConceptModel getConcept(List<String> wordList) {

		for (String word : wordList) {

			//check NN and NNS with concepts
			for (Map.Entry<String, ConceptModel> conceptModelMapEntry : conceptModelMap.entrySet()) {

				ConceptModel conceptModel = conceptModelMapEntry.getValue();

				boolean labelExist = conceptModel.isLabelExist(word.toLowerCase().trim());

				if(labelExist)
					return conceptModel;
			}
		}
		return null;
	}

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {

		IDictionary dict = new Dictionary( new URL( "file", null, "/home/firzhan/operation-infinity/wordnet3.1/dict"));
		OWLProcessor owlProcessor = new OWLProcessor(dict);
		owlProcessor.loadOWL();
	}
}
