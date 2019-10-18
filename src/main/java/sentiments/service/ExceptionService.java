package sentiments.service;

import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Offers utility methods for work with exceptions
 * @author 6runge
 */
@Service
public class ExceptionService {

    /**
     * Extracts the stack trace from an {@link Exception}
     * @param e the exception for which you want a stack trace
     * @return the stack trace as a {@link String}
     */
    public String exceptionToString(Exception e){
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
