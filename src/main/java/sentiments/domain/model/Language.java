package sentiments.domain.model;

import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.Entity;

@Entity
public class Language {

    @Indexed(unique = true)
    private String iso;

    private String name;

    private String classifierFilename;

    private String wordVectorsFilename;

    private boolean active;

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassifierFilename() {
        return classifierFilename;
    }

    public void setClassifierFilename(String classifierFilename) {
        this.classifierFilename = classifierFilename;
    }

    public String getWordVectorsFilename() {
        return wordVectorsFilename;
    }

    public void setWordVectorsFilename(String wordVectorsFilename) {
        this.wordVectorsFilename = wordVectorsFilename;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
