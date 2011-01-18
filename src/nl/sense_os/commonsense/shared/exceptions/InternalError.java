package nl.sense_os.commonsense.shared.exceptions;

import java.io.Serializable;

public class InternalError extends Exception implements Serializable {
    private static final long serialVersionUID = 1L;
    private String msg;
    
    public InternalError() {
        // empty constructor for serialization
    }
    
    public InternalError(String msg) {
        this.msg = msg;
    }
    
    @Override
    public String getMessage() {
        return this.msg;
    }
}
