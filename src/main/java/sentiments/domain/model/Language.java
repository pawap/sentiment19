package sentiments.domain.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Language {

    @Id
    private String iso;

    private String name;

    private String classifierFilename;

    private String wordvectorsFilename;

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

    public String getWordvectorsFilename() {
        return wordvectorsFilename;
    }

    public void setWordvectorsFilename(String wordvectorsFilename) {
        this.wordvectorsFilename = wordvectorsFilename;
    }

}
