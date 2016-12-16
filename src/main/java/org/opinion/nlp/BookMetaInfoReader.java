package org.opinion.nlp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.opinion.nlp.model.BookMetaModel;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookMetaInfoReader {

	public BookMetaInfoReader() {
	}

	public Map<String, BookMetaModel> loadBookModels(String path){

		JSONParser parser = new JSONParser();

		Map<String, BookMetaModel> bookMetaModelMap = new HashMap<String, BookMetaModel>();
		try {

			Object obj = parser.parse(new FileReader(path));

			JSONArray rootJsonArray = (JSONArray) obj;

			for (Object jsonObject : rootJsonArray){

				BookMetaModel bookMetaModel = new BookMetaModel();

				JSONObject bookJsonObject = (JSONObject) jsonObject;
				String asin = (String) bookJsonObject.get("asin");
				String name = (String) bookJsonObject.get("name");
				bookMetaModel.setAsinID(asin);
				bookMetaModel.setName(name);
				JSONArray authorListJsonArray = (JSONArray) bookJsonObject.get("author");
				JSONArray charactersListJsonArray = (JSONArray) bookJsonObject.get("characters");

				System.out.println("ASIN: " + asin);
				System.out.println("Name: " + name);
				System.out.println("Author List: ");
				List<String> authorList = new ArrayList<String>();
				for (String anAuthor : (Iterable<String>) authorListJsonArray) {
					authorList.add(anAuthor);
				}
				bookMetaModel.setAuthorList(authorList);

				List<String> characterList = new ArrayList<String>();
				System.out.println("Characters List:");
				for (String aCharacter : (Iterable<String>) charactersListJsonArray) {
					characterList.add(aCharacter);
				}
				bookMetaModel.setCharacterList(characterList);
				bookMetaModelMap.put(asin, bookMetaModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bookMetaModelMap;
	}

	public static void main(String[] args) {

		BookMetaInfoReader bookMetaInfoReader = new BookMetaInfoReader();
		bookMetaInfoReader.loadBookModels("/home/firzhan/operation-infinity/reviews/meta/book-meta.txt");
	}
}
