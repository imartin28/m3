package es.ucm.fdi.iw.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
@NamedQueries({
@NamedQuery(name="readAllTags", query="SELECT t "
		+ " FROM Tag t "),
@NamedQuery(name="readTagsByUser", query="SELECT t "
		+ " FROM Tag t "
		+ " WHERE user_id = :userId"),
@NamedQuery(name="findById", query="SELECT t "
		+ " FROM Tag t"
		+ " WHERE id = :id"),
@NamedQuery(name="findByIdAndUserId", query="SELECT t "
		+ " FROM Tag t"
		+ " WHERE id = :id AND user_id = :userId"),
@NamedQuery(name="findByName", query="SELECT t "
		+ " FROM Tag t"
		+ " WHERE name = :name"),
@NamedQuery(name="readParentTag", query="SELECT tag "
		+ " FROM Tag tag"
		+ " WHERE id = :parentId"),

})
public class Tag {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	@NotNull
	private String name;
	
	private String color;
	
	@ManyToOne(targetEntity=Tag.class)
	private Tag parent;
	
	@OneToMany(targetEntity=Tag.class)
	@JoinColumn(name="parent_id")
	private List<Tag> children;
	
	@ManyToMany(targetEntity=CFile.class)
	private List<CFile> files;
	
	@ManyToOne(targetEntity=User.class)
	private User user;
	
	private boolean playlist;
	
	public Tag() {
		
	}
	
	public Tag(String name, String color, Tag parent, User user) {
		this.name = name;
		this.color = color;
		this.parent = parent;
		this.user = user;
		this.playlist = false;
	}
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	public Tag getParent() {
		return this.parent;
	}
	
	public void setParent(Tag parent) {
		this.parent = parent;
	}
	
	public List<Tag> getChildren() {
		return this.children;
	}
	
	public void setChildren(List<Tag> children) {
		this.children = children;
	}
	
	public List<CFile> getFiles() {
		return this.files;
	}
	
	public void setFiles(List<CFile> files) {
		this.files = files;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public boolean isPlaylist() {
		return playlist;
	}

	public void setPlaylist(boolean playlist) {
		this.playlist = playlist;
	}

	public JSONObject toDisplayJsonObject() throws JSONException {
		JSONObject tagJson = new JSONObject();
		tagJson.put("id", this.id);
		tagJson.put("name", name);
		tagJson.put("color", this.color);
		return tagJson;
	}
}
