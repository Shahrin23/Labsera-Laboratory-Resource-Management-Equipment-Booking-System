package com.labresa.model;

public class User {

    public enum Role {
        UNDERGRAD, GRAD, FACULTY, TECHNICIAN, ADMIN
    }

    private int id;
    private final String firstName;
    private final String lastName;
    private final String session;
    private final String classRoll;
    private final Role role;

    public User(int id, String firstName, String lastName, String session, String classRoll, Role role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.session = session;
        this.classRoll = classRoll;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSession() { return session; }
    public String getClassRoll() { return classRoll; }
    public Role getRole() { return role; }

    /** Combined display name, kept so existing UI code that expects a single name string keeps working. */
    public String getName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }

    @Override
    public String toString() {
        return getName() + " (" + role + ")";
    }
}
