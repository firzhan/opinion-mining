package org.opinion.nlp.model;

import java.util.List;

public class BookMetaModel {

	private String asinID;
	private String name;
	private List<String> authorList;
	private List<String> charactersList;


	public BookMetaModel() {
	}

	public String getAsinID() {
		return asinID;
	}

	public void setAsinID(String asinID) {
		this.asinID = asinID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getAuthorList() {
		return authorList;
	}

	public void setAuthorList(List<String> authorList) {
		this.authorList = authorList;
	}

	public List<String> getCharactersList() {
		return charactersList;
	}

	public void setCharacterList(List<String> charactersList) {
		this.charactersList = charactersList;
	}
}
