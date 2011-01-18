package nl.sense_os.commonsense.shared.exceptions;

import java.io.Serializable;

public class WrongResponseException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;
    private String msg;
    
    public WrongResponseException() {
        // empty constructor for serialization
    }
    
    public WrongResponseException(String msg) {
        this.msg = msg;
    }
    
    @Override
    public String getMessage() {
        return this.msg;
    }
}
