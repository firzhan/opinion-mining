package org.opinion.nlp.model;

import edu.mit.jwi.IDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConceptModel {

	private IDictionary dictionary;

	private String className;

	private Map<String, String> propertyMap = new HashMap<String, String>();

	private List<String> labelList = new ArrayList<String>();

	public ConceptModel(IDictionary dictionary) {

		this.dictionary = dictionary;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}

	public void setPropertyMap(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}

	public boolean isLabelExist(String label){

		for (String label1 : labelList){
			if(label1.toLowerCase().trim().equalsIgnoreCase(label.toLowerCase().trim())){
				return true;
			}
		}

		return false;
	}
}
