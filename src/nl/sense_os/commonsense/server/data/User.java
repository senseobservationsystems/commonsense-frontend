package nl.sense_os.commonsense.server.data;

import java.io.Serializable;

public class User implements Serializable {
	
    private static final long serialVersionUID = 1L;
    private int id;
	private String name;
	private String password;
	
	public User() {
	    
	}
	
	public User(int id, String name, String pass) {
        setId(id);
	    setName(name);
	    setPassword(pass);
	}
	
	public int getId() {
	    return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}
	public User setId(int id) {
	    this.id = id;
	    return this;
	}

	public User setName(String name) {
		this.name = name;
		return this;
	}
	
	public User setPassword(String password) {
		this.password = password;
		return this;
	}

}
