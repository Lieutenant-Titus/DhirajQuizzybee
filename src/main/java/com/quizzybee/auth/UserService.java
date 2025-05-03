package com.quizzybee.auth;

import com.quizzybee.model.QuizAttempt;
import com.quizzybee.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing users and authentication
 */
public class UserService {
    private static final String USER_DATA_FILE = "users.dat";
    private static UserService instance;
    
    private Map<String, User> users; // username -> User
    private User currentUser;
    
    private UserService() {
        users = new HashMap<>();
        loadUsers();
        
        // Create a default admin user if no users exist
        if (users.isEmpty()) {
            createDefaultUsers();
        }
    }
    
    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    /**
     * Creates default admin and student accounts
     */
    private void createDefaultUsers() {
        // Create admin user
        User admin = new User("admin", "admin123", "Administrator", "admin@quizzybee.com", User.Role.ADMIN);
        users.put(admin.getUsername(), admin);
        
        // Create a test student user
        User student = new User("student", "student123", "Test Student", "student@quizzybee.com", User.Role.STUDENT);
        users.put(student.getUsername(), student);
        
        saveUsers();
    }
    
    /**
     * Attempts to authenticate a user with the provided credentials
     * 
     * @param username The username
     * @param password The password
     * @return True if authentication was successful
     */
    public boolean authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Registers a new user
     * 
     * @param username Username
     * @param password Password
     * @param fullName Full name
     * @param email Email address
     * @param role User role
     * @return True if registration was successful
     */
    public boolean registerUser(String username, String password, String fullName, String email, User.Role role) {
        if (users.containsKey(username)) {
            return false; // Username already exists
        }
        
        User newUser = new User(username, password, fullName, email, role);
        users.put(username, newUser);
        saveUsers();
        return true;
    }
    
    /**
     * Gets the current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Gets all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Gets all students (non-admin users)
     */
    public List<User> getAllStudents() {
        List<User> students = new ArrayList<>();
        for (User user : users.values()) {
            if (!user.isAdmin()) {
                students.add(user);
            }
        }
        return students;
    }
    
    /**
     * Gets a user by username
     */
    public User getUserByUsername(String username) {
        return users.get(username);
    }
    
    /**
     * Adds a quiz attempt for the current user
     */
    public void addQuizAttempt(QuizAttempt attempt) {
        if (currentUser != null) {
            currentUser.addQuizAttempt(attempt);
            saveUsers();
        }
    }
    
    /**
     * Loads users from file
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USER_DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (Map<String, User>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Error loading user data: " + e.getMessage());
                users = new HashMap<>();
            }
        }
    }
    
    /**
     * Saves users to file
     */
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(users);
        } catch (Exception e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }
    
    /**
     * Updates a user
     */
    public void updateUser(User user) {
        if (user != null && users.containsKey(user.getUsername())) {
            users.put(user.getUsername(), user);
            saveUsers();
        }
    }
    
    /**
     * Deletes a user by username
     */
    public boolean deleteUser(String username) {
        if (users.containsKey(username)) {
            users.remove(username);
            saveUsers();
            return true;
        }
        return false;
    }
} 