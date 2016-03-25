package vfcorp;

import java.util.ArrayList;
import java.util.List;

public class FieldDetails {

	private int characters;
	private int startLocation;
	private List<String> comments;
	
	public FieldDetails(int characters, int startLocation, String comments) {
		this.characters = characters;
		this.startLocation = startLocation;
		this.comments = new ArrayList<String>();
		for (String comment : comments.split(", ")) {
			this.comments.add(comment.toLowerCase());
		}
	}
	
	public int getCharacters() {
		return characters;
	}
	public void setCharacters(int characters) {
		this.characters = characters;
	}
	public int getStartLocation() {
		return startLocation;
	}
	public void setStartLocation(int startLocation) {
		this.startLocation = startLocation;
	}
	public List<String> getComments() {
		return comments;
	}
	public void setComments(List<String> comments) {
		this.comments = comments;
	}
}
