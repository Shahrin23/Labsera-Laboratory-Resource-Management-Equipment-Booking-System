package com.labresa.model;

public class User {

    public enum Role {
        UNDERGRAD, GRAD, FACULTY, TECHNICIAN
    }

    private final int id;
    private final String name;
    private final Role role;

    public User(int id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public int getId() {
        return id; }
    public String getName() {
        return name; }
    public Role getRole() {
        return role; }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
