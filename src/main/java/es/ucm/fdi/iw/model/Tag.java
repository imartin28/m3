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

@Entity
@NamedQueries({
@NamedQuery(name="readAllTags", query="SELECT t "
		+ " FROM Tag t "),
@NamedQuery(name="findById", query="SELECT t "
		+ " FROM Tag t"
		+ " WHERE id = :id")
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
	
	@ManyToMany(targetEntity=CFile.class, mappedBy="tags")
	private List<CFile> files;
	
	public Tag() {
		
	}
	public Tag(String name, String color, Tag parent) {
		this.name = name;
		this.color = color;
		this.parent = parent;
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

}
