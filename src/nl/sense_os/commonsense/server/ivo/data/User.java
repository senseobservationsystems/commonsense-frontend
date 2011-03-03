package nl.sense_os.commonsense.server.ivo.data;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private String email;
    private int id;
    private String mobile;
    private String name;
    private String surname;
    private String username;
    private String uuid;

    public User() {

    }

    public User(int id, String username, String email, String name, String surname, String mobile,
            String uuid) {
        setId(id);
        setUsername(username);
        setEmail(email);
        setName(name);
        setSurname(surname);
        setMobile(mobile);
        setUuid(uuid);
    }

    public String getEmail() {
        return email;
    }

    public int getId() {
        return id;
    }

    public String getMobile() {
        return mobile;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    private User setEmail(String email) {
        this.email = email;
        return this;
    }

    public User setId(int id) {
        this.id = id;
        return this;
    }

    private User setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    private User setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    private User setUsername(String username) {
        this.username = username;
        return this;
    }

    private User setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
}
