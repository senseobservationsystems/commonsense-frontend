package nl.sense_os.commonsense.shared.exceptions;

import java.io.Serializable;

public class TooMuchDataException extends Exception implements Serializable {

    private String msg;
    
    private static final long serialVersionUID = 1L;

    public TooMuchDataException() {
        // empty constructor for serialization
    }
    
    public TooMuchDataException(String msg) {
        this.msg = msg;
    }
    
    @Override
    public String getMessage() {
        return this.msg;
    }
}
