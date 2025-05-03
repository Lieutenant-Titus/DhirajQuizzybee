package com.quizzybee.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user of the QuizzyBee application.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Role {
        STUDENT,
        ADMIN
    }
    
    private String username;
    private String password; // In a real application, this would be hashed
    private String fullName;
    private String email;
    private Role role;
    private List<QuizAttempt> quizAttempts;
    
    public User() {
        this.quizAttempts = new ArrayList<>();
    }
    
    public User(String username, String password, String fullName, String email, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.quizAttempts = new ArrayList<>();
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public List<QuizAttempt> getQuizAttempts() {
        return quizAttempts;
    }
    
    public void setQuizAttempts(List<QuizAttempt> quizAttempts) {
        this.quizAttempts = quizAttempts;
    }
    
    public void addQuizAttempt(QuizAttempt attempt) {
        this.quizAttempts.add(attempt);
    }
    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
} 