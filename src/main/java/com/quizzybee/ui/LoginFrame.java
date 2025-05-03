package com.quizzybee.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.quizzybee.auth.UserService;
import com.quizzybee.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login frame for QuizzyBee application
 */
public class LoginFrame extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JPanel mainPanel;
    
    private UserService userService;
    
    public LoginFrame() {
        userService = UserService.getInstance();
        
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF");
        }
        
        initUI();
    }
    
    private void initUI() {
        setTitle("QuizzyBee - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title/Logo panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("QuizzyBee", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JLabel subtitleLabel = new JLabel("MCQ Generator from Syllabus", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Login form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add event listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegistrationDialog();
            }
        });
        
        // Set default button
        getRootPane().setDefaultButton(loginButton);
        
        add(mainPanel);
    }
    
    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and password are required",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (userService.authenticate(username, password)) {
            // Authentication successful
            User currentUser = userService.getCurrentUser();
            openMainApplication(currentUser);
        } else {
            // Authentication failed
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
    
    private void openMainApplication(User user) {
        if (user.isAdmin()) {
            // Open admin panel
            AdminPanel adminPanel = new AdminPanel();
            adminPanel.setVisible(true);
        } else {
            // Open student panel
            try {
                StudentPanel studentPanel = new StudentPanel();
                studentPanel.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error opening Student Panel: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Close the login window
        this.dispose();
    }
    
    private void showRegistrationDialog() {
        // Only admins can add new users, so we'll prompt for admin login first
        if (!userService.isLoggedIn() || !userService.getCurrentUser().isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Only administrators can register new users.\nPlease log in as an administrator first.",
                    "Registration Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "Register New User", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);
        
        // Password
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);
        
        // Full Name
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(nameField, gbc);
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(emailField, gbc);
        
        // Role
        JLabel roleLabel = new JLabel("Role:");
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Student", "Administrator"});
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(roleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(roleComboBox, gbc);
        
        // Register button
        JButton registerButton = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        panel.add(registerButton, gbc);
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String fullName = nameField.getText();
                String email = emailField.getText();
                User.Role role = roleComboBox.getSelectedIndex() == 0 ? User.Role.STUDENT : User.Role.ADMIN;
                
                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "All fields are required",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = userService.registerUser(username, password, fullName, email, role);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            "User registered successfully",
                            "Registration Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Username already exists",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
} 