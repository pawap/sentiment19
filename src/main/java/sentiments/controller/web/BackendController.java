package sentiments.controller.web;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sentiments.data.BasicDataImporter;
import sentiments.domain.model.Language;
import sentiments.domain.repository.TweetRepository;
import sentiments.domain.service.ClassifierService;
import sentiments.domain.service.LanguageService;
import sentiments.domain.service.StorageService;
import sentiments.domain.service.TaskService;
import sentiments.ml.WordVectorBuilder;
import sentiments.ml.WordVectorsService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@RestController
public class BackendController {

    private static final Logger log = LoggerFactory.getLogger(BackendController.class);


    @Autowired
    BasicDataImporter basicDataImporter;

    @Autowired
    ClassifierService classifierService;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    LanguageService languageService;

    @Autowired
    TaskService taskService;

    @Autowired
    StorageService storageService;

    @RequestMapping("/backend")
    public ResponseEntity<String> backend(String message, HttpStatus status) {
        message = message == null ? "" : message;
        status = status == null ? HttpStatus.OK : status;
        String response = "";
        try {
            File file = ResourceUtils.getFile(
                    "classpath:frontend/sentiment-backend.html");
            response = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            response = response.replaceAll("###MESSAGE###",message + System.lineSeparator()
                    + storageService.getReport() + System.lineSeparator()
                    + taskService.getLogContent());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        return new ResponseEntity<>(response, responseHeaders, status);
    }

    @RequestMapping("/backend/setBaseDir")
    public ResponseEntity<String> setBaseDir(@RequestParam( value = "dir", defaultValue = "") String dir) {

        storageService.setStorageDir(dir);

        return backend("done.", HttpStatus.ACCEPTED); //new ResponseEntity<String>(response, responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/backend/setTaskStatus")
    public ResponseEntity<String> setTaskStatus(@RequestParam( value = "task", defaultValue = "") String task,
                                                           @RequestParam( value = "enabled", defaultValue = "false") boolean enabled) {
        if (task == "") {
            return backend("", HttpStatus.NOT_FOUND);
        }
        String response = "setting task '" + task  + "' to " + (enabled ? "active" : "not active");
        taskService.setTaskStatus(task, enabled);

        return backend(response, HttpStatus.ACCEPTED); //new ResponseEntity<String>(response, responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/backend/import")
    public ResponseEntity<String> tweetimport() {

        this.basicDataImporter.importExampleJson();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        return new ResponseEntity<String>("finished", responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/backend/import/testandtrain")
    public ResponseEntity<String> testAndTrainimport(@RequestParam( value = "lang", defaultValue = "en") String lang) {
        System.out.println("testAndTrainimport was called with " + lang);
        this.basicDataImporter.importTsvTestAndTrain(languageService.getLanguage(lang));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        return new ResponseEntity<String>("finished", responseHeaders,HttpStatus.OK);
    }

    @RequestMapping("/backend/ml/w2vtraining")
    public ResponseEntity<String> w2vtraining(@RequestParam( value = "lang", defaultValue = "en") String lang) {
        WordVectorBuilder w2vb = new WordVectorBuilder(tweetRepository);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        try {
            Language language = languageService.getLanguage(lang);
            if (language == null) {
                return new ResponseEntity<String>("language not supported", responseHeaders,HttpStatus.NOT_FOUND);
            }
            w2vb.train(language);
            System.out.println("finished training");
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<String>("Request failed", responseHeaders,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("finished training", responseHeaders,HttpStatus.CREATED);
    }

    @RequestMapping("/backend/ml/w2vtest")
    public ResponseEntity<String> w2vtest(@RequestParam( value = "lang", defaultValue = "en") String lang) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        Language language = languageService.getLanguage(lang);
        if (language == null) {
            return new ResponseEntity<String>("language not supported", responseHeaders,HttpStatus.NOT_FOUND);
        }
        WordVectors word2VecModel = WordVectorsService.getWordVectors(language);

        String examples = "Some words with their closest neighbours: \n";

        Collection<String> list = word2VecModel.wordsNearest("woman" , 10);
        examples += " woman: " + list + ",  ";

        list = word2VecModel.wordsNearest("man" , 10);
        examples += " man: " + list + ",  ";

        list = word2VecModel.wordsNearest("girl" , 10);
        examples += " girl: " + list + ",  ";

        list = word2VecModel.wordsNearest("boy" , 10);
        examples += " boy: " + list + ",  ";

        list = word2VecModel.wordsNearest("day" , 10);
        examples += " day: " + list + ",  ";

        list = word2VecModel.wordsNearest("night" , 10);
        examples += " night: " + list + ",  ";

        list = word2VecModel.wordsNearest("shit" , 10);
        examples += " shit: " + list + ",  ";

        list = word2VecModel.wordsNearest("motherfucker" , 10);
        examples += " motherfucker: " + list + ",  ";

        list = word2VecModel.wordsNearest("cat" , 10);
        examples += " cat: " + list + ",  ";

        list = word2VecModel.wordsNearest("merkel" , 10);
        examples += " merkel: " + list + ",  ";

        list = word2VecModel.wordsNearest("trump" , 10);
        examples += " trump: " + list + ",  ";

        list = word2VecModel.wordsNearest("germany", 10);
        examples += " germany: " + list + ",  ";

        list = word2VecModel.wordsNearest("usa", 10);
        examples += " usa: " + list + ",  ";

        list = word2VecModel.wordsNearest("nobody", 10);
        examples += " nobody: " + list + " ";

        return new ResponseEntity<String>(examples, responseHeaders,HttpStatus.OK);
    }

    @RequestMapping("/backend/ml/trainnet")
    public ResponseEntity<String> trainNet() {
        classifierService.trainClassifier(languageService.getLanguage("en"));
        HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<String>("training done", responseHeaders,HttpStatus.CREATED);
    }

    @PostMapping("backend/upload")
    public ResponseEntity<String> singleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return  backend("No File found. Please select a file to upload.", HttpStatus.BAD_REQUEST);
        }
        try {
            File target = storageService.getFile(file.getOriginalFilename());
            target.setWritable(true);
            FileUtils.copyInputStreamToFile(file.getInputStream(), target);
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("Exception during Fileupload: " + e.getMessage());
            log.warn("stacktrace: "+ e.getStackTrace());
            return backend("Woahh... it ain't all good. INTERNAL ERROR." + e.getStackTrace(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return backend("You successfully uploaded '" + file.getOriginalFilename() + "'", HttpStatus.CREATED);
    }

    @PostMapping("backend/import/training")
    public ResponseEntity<String> testAndTrainImport(@RequestParam("traindata") MultipartFile trainData,
                                                     @RequestParam("testdata") MultipartFile testData,
                                                     @RequestParam( value = "lang", defaultValue = "") String lang) {
        Language language = languageService.getLanguage(lang);
        if (language == null) {
            backend("language not supported", HttpStatus.NOT_FOUND);
        }
        MultipartFile[] files = {trainData, testData};
        List<File> targetFiles = new LinkedList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                targetFiles.add(null);
                break;
            }
            try {
                File target = new File("resources/training/upload/"+file.getOriginalFilename());
                target.setWritable(true);
                FileUtils.copyInputStreamToFile(file.getInputStream(), target);
                targetFiles.add(target);
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("Exception during Fileupload: " + e.getMessage());
                log.warn("stacktrace: "+ e.getStackTrace());

                return backend("Woahh... it ain't all good. INTERNAL ERROR." + e.getStackTrace(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        basicDataImporter.importTsvTestAndTrain(language, targetFiles.get(0).getPath(), targetFiles.get(1).getPath());
        return backend("success", HttpStatus.OK);
    }
}
