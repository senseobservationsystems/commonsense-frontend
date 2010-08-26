package nl.sense_os.commonsense.dto.exceptions;

import java.io.Serializable;

public class DbConnectionException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;
    private String msg;
    
    public DbConnectionException() {
        // empty constructor for serialization
    }
    
    public DbConnectionException(String msg) {
        this.msg = msg;
    }
    
    @Override
    public String getMessage() {
        return this.msg;
    }

}
