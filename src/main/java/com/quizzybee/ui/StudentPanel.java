package com.quizzybee.ui;

import com.quizzybee.auth.UserService;
import com.quizzybee.model.Question;
import com.quizzybee.model.Quiz;
import com.quizzybee.model.QuizAttempt;
import com.quizzybee.model.User;
import com.quizzybee.service.PdfService;
import com.quizzybee.service.QuizService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Main panel for student users
 */
public class StudentPanel extends JFrame {
    
    private JTabbedPane tabbedPane;
    private JPanel uploadPanel;
    private JPanel historyPanel;
    private JPanel profilePanel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JButton logoutButton;
    private JButton uploadButton;
    private JButton startQuizButton;
    private JLabel statusLabel;
    
    private UserService userService;
    private PdfService pdfService;
    private QuizService quizService;
    
    private File selectedFile;
    private String extractedText;
    
    public StudentPanel() {
        userService = UserService.getInstance();
        pdfService = new PdfService();
        quizService = new QuizService();
        
        initUI();
        loadData();
    }
    
    private void initUI() {
        setTitle("QuizzyBee - Student Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Upload Syllabus tab
        uploadPanel = createUploadPanel();
        tabbedPane.addTab("Upload Syllabus", uploadPanel);
        
        // Quiz History tab
        historyPanel = createHistoryPanel();
        tabbedPane.addTab("Quiz History", historyPanel);
        
        // Profile tab
        profilePanel = createProfilePanel();
        tabbedPane.addTab("My Profile", profilePanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(logoutButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createUploadPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Instructions
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("Upload Your Syllabus PDF");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel instructionLabel = new JLabel("<html><p>Upload your syllabus PDF to generate quiz questions. " +
                "The AI will analyze the content and create multiple-choice questions based on the material.</p></html>");
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        instructionPanel.add(titleLabel);
        instructionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        instructionPanel.add(instructionLabel);
        
        panel.add(instructionPanel, BorderLayout.NORTH);
        
        // Upload controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField filePathField = new JTextField(30);
        filePathField.setEditable(false);
        JButton browseButton = new JButton("Browse");
        
        filePanel.add(filePathField);
        filePanel.add(browseButton);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Status label
        statusLabel = new JLabel("Select a PDF file to upload");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Button panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        uploadButton = new JButton("Upload & Extract Text");
        startQuizButton = new JButton("Generate Quiz");
        startQuizButton.setEnabled(false);
        
        actionPanel.add(uploadButton);
        actionPanel.add(startQuizButton);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        controlPanel.add(filePanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(actionPanel);
        
        panel.add(controlPanel, BorderLayout.CENTER);
        
        // Add action listeners
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(StudentPanel.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                    statusLabel.setText("File selected: " + selectedFile.getName());
                    uploadButton.setEnabled(true);
                }
            }
        });
        
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    statusLabel.setText("Extracting text from PDF...");
                    uploadButton.setEnabled(false);
                    
                    // Use SwingWorker to prevent UI freezing
                    SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            return pdfService.extractTextFromPdf(selectedFile);
                        }
                        
                        @Override
                        protected void done() {
                            try {
                                extractedText = get();
                                if (extractedText != null && !extractedText.isEmpty()) {
                                    statusLabel.setText("Text extracted successfully! Ready to generate quiz.");
                                    startQuizButton.setEnabled(true);
                                } else {
                                    statusLabel.setText("Failed to extract text. Please try another PDF.");
                                    uploadButton.setEnabled(true);
                                }
                            } catch (Exception ex) {
                                statusLabel.setText("Error: " + ex.getMessage());
                                uploadButton.setEnabled(true);
                            }
                        }
                    };
                    
                    worker.execute();
                }
            }
        });
        
        startQuizButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (extractedText != null && !extractedText.isEmpty()) {
                    // Get API key from preferences or prompt user
                    Preferences prefs = Preferences.userNodeForPackage(StudentPanel.class);
                    String savedApiKey = prefs.get("gemini.api.key", "");
                    
                    final String apiKey;
                    if (savedApiKey.isEmpty()) {
                        // No saved key, prompt user
                        String inputApiKey = JOptionPane.showInputDialog(StudentPanel.this, 
                                "Enter your Gemini API key:", 
                                "API Key Required", 
                                JOptionPane.QUESTION_MESSAGE);
                        
                        if (inputApiKey == null || inputApiKey.trim().isEmpty()) {
                            // User cancelled or didn't provide an API key
                            statusLabel.setText("API key is required. Please try again.");
                            return;
                        }
                        
                        // Ask if user wants to save the key
                        int saveOption = JOptionPane.showConfirmDialog(StudentPanel.this,
                                "Do you want to save this API key for future use?",
                                "Save API Key",
                                JOptionPane.YES_NO_OPTION);
                        
                        if (saveOption == JOptionPane.YES_OPTION) {
                            prefs.put("gemini.api.key", inputApiKey);
                        }
                        
                        apiKey = inputApiKey;
                    } else {
                        // Use saved key
                        apiKey = savedApiKey;
                    }
                    
                    statusLabel.setText("Generating quiz questions...");
                    startQuizButton.setEnabled(false);
                    
                    // Use SwingWorker to prevent UI freezing
                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            // Generate the quiz
                            Quiz generatedQuiz = quizService.generateQuiz(extractedText, apiKey);
                            
                            // Store the quiz in a QuizAttempt
                            QuizAttempt attempt = new QuizAttempt(
                                "attempt-" + System.currentTimeMillis(), 
                                generatedQuiz
                            );
                            
                            // Add the attempt to the current user
                            userService.addQuizAttempt(attempt);
                            
                            return null;
                        }
                        
                        @Override
                        protected void done() {
                            try {
                                get(); // Check for exceptions
                                statusLabel.setText("Quiz generated successfully!");
                                
                                // Get the most recent quiz attempt
                                List<QuizAttempt> attempts = userService.getCurrentUser().getQuizAttempts();
                                if (!attempts.isEmpty()) {
                                    QuizAttempt latestAttempt = attempts.get(attempts.size() - 1);
                                    
                                    // Show the quiz taking frame
                                    SwingUtilities.invokeLater(() -> {
                                        QuizTakingFrame quizFrame = new QuizTakingFrame(latestAttempt);
                                        quizFrame.setVisible(true);
                                        
                                        // Reset for next upload
                                        uploadButton.setEnabled(true);
                                        startQuizButton.setEnabled(false);
                                        filePathField.setText("");
                                        selectedFile = null;
                                        
                                        // Refresh the history tab
                                        loadData();
                                    });
                                } else {
                                    JOptionPane.showMessageDialog(StudentPanel.this,
                                            "Error: Could not find the generated quiz.",
                                            "Quiz Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                                
                            } catch (Exception ex) {
                                statusLabel.setText("Error generating quiz: " + ex.getMessage());
                                startQuizButton.setEnabled(true);
                            }
                        }
                    };
                    
                    worker.execute();
                }
            }
        });
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Create table model with column names
        historyTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        historyTableModel.addColumn("Quiz Title");
        historyTableModel.addColumn("Date");
        historyTableModel.addColumn("Score");
        historyTableModel.addColumn("Status");
        
        // Create table
        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setReorderingAllowed(false);
        
        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewAttemptButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh");
        
        buttonPanel.add(viewAttemptButton);
        buttonPanel.add(refreshButton);
        
        // Add action listeners
        viewAttemptButton.addActionListener(e -> viewSelectedAttempt());
        refreshButton.addActionListener(e -> loadData());
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        
        User currentUser = userService.getCurrentUser();
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username (read-only)
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(currentUser.getUsername());
        usernameField.setEditable(false);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        formPanel.add(usernameField, gbc);
        
        // Full Name
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField(currentUser.getFullName());
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        formPanel.add(nameField, gbc);
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(currentUser.getEmail());
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        formPanel.add(emailField, gbc);
        
        // Password Change
        JLabel passwordLabel = new JLabel("New Password:");
        JPasswordField passwordField = new JPasswordField();
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        formPanel.add(passwordField, gbc);
        
        // Buttons
        JButton saveButton = new JButton("Save Changes");
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(saveButton, gbc);
        
        // Add action listener
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fullName = nameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                
                if (fullName.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(panel,
                            "Name and email are required",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Update user info
                currentUser.setFullName(fullName);
                currentUser.setEmail(email);
                
                // Only update password if new one is provided
                if (!password.isEmpty()) {
                    currentUser.setPassword(password);
                }
                
                userService.updateUser(currentUser);
                
                JOptionPane.showMessageDialog(panel,
                        "Profile updated successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                
                // Clear password field
                passwordField.setText("");
            }
        });
        
        // Add some padding
        JPanel paddedPanel = new JPanel(new BorderLayout());
        paddedPanel.add(formPanel, BorderLayout.NORTH);
        paddedPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        panel.add(paddedPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadData() {
        // Clear existing data in the history table
        historyTableModel.setRowCount(0);
        
        // Get current user
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            // Load quiz attempts for the current user
            List<QuizAttempt> attempts = currentUser.getQuizAttempts();
            for (QuizAttempt attempt : attempts) {
                Object[] row = {
                        attempt.getQuiz().getTitle(),
                        attempt.getAttemptDate().toString(),
                        attempt.getScoreAsString(),
                        attempt.isCompleted() ? "Completed" : "In Progress"
                };
                historyTableModel.addRow(row);
            }
        }
    }
    
    private void viewSelectedAttempt() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a quiz attempt to view",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // In a real application, we would retrieve the actual QuizAttempt object
        // and display its details. For now, we'll just show a message.
        JOptionPane.showMessageDialog(this,
                "Viewing details for selected quiz attempt",
                "Quiz Attempt Details",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        userService.logout();
        
        // Show login screen
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        
        // Close this window
        this.dispose();
    }
} 