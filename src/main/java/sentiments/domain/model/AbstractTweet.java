package sentiments.domain.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@MappedSuperclass
public class AbstractTweet {

	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Id
	private Integer uid;
	@Lob
	private String text;

	private String language;

	private List<String> lemma;
	private Date crdate;
	private Date tmstamp;
	private boolean offensive;

	public AbstractTweet() {
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}


	public Date getCrdate() {
		return crdate;
	}

	public void setCrdate(Date crdate) {
		this.crdate = crdate;
	}

	public Date getTmstamp() {
		return tmstamp;
	}

	public void setTmstamp(Date tmstamp) {
		this.tmstamp = tmstamp;
	}

	public boolean isOffensive() {
		return offensive;
	}

	public void setOffensive(boolean offensive) {
		this.offensive = offensive;
	}

	public List<String> getLemma(){ return lemma; }

	public void setLemma(List<String> lemma){ this.lemma = lemma;}
}