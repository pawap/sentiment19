package sentiments.service;

import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author 6runge
 */
@Service
public class ExceptionService {

    public String exceptionToString(Exception e){
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
