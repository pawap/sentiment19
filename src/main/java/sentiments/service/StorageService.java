package sentiments.service;

import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author Paw
 */
@Service
public class StorageService {

    private String baseDirSystemProperty;

    private String baseDirFileApi;

    private String storageDir;

    public StorageService() {
        this.baseDirSystemProperty = System.getProperty("user.dir");
        this.baseDirFileApi = new File("").getAbsolutePath();
        this.storageDir = baseDirSystemProperty;
    }

    public String getReport(){
        return "SystemProp: " + getBaseDirSystemProperty() + System.lineSeparator()
                + "FileApi: " + getBaseDirFileApi() + System.lineSeparator()
                + "BaseDir: " + getStorageDir();

    }

    public File getFile(String path) {
        return new File(this.getStorageDir() + "/" + path);
    }

    public String getBaseDirSystemProperty() {
        return this.baseDirSystemProperty;
    }

    public void setBaseDirSystemProperty(String baseDir) {
        this.baseDirSystemProperty = baseDir;
    }


    public String getBaseDirFileApi() {
        return baseDirFileApi;
    }

    public void setBaseDirFileApi(String baseDirFileApi) {
        this.baseDirFileApi = baseDirFileApi;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }
}
