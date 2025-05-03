package com.quizzybee.ui;

import com.quizzybee.service.PdfService;
import com.quizzybee.service.QuizService;
import com.quizzybee.model.Quiz;
import com.quizzybee.model.Question;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

public class MainFrame extends JFrame {
    
    private static final String API_KEY_PREF = "gemini_api_key";
    private static final String DEFAULT_API_KEY = "AIzaSyBhYEMf7pjCjAKq343BLfYOXqbFpQzqjG4";
    
    private JTextField pdfPathField;
    private JButton browseButton;
    private JButton generateButton;
    private JTextArea syllabusTextArea;
    private JPanel quizPanel;
    private JScrollPane quizScrollPane;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    private PdfService pdfService;
    private QuizService quizService;
    private Preferences prefs;
    
    public MainFrame() {
        pdfService = new PdfService();
        quizService = new QuizService();
        prefs = Preferences.userNodeForPackage(MainFrame.class);
        
        // Ensure the API key is saved
        if (prefs.get(API_KEY_PREF, null) == null) {
            prefs.put(API_KEY_PREF, DEFAULT_API_KEY);
        }
        
        initUI();
    }
    
    private String getApiKey() {
        return prefs.get(API_KEY_PREF, DEFAULT_API_KEY);
    }
    
    private void initUI() {
        setTitle("QuizzyBee - MCQ Generator from Syllabus");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Create main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Center panel with split pane
        JSplitPane splitPane = createCenterPanel();
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Bottom panel for status and progress
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        addEventListeners();
    }
    
    private JSplitPane createCenterPanel() {
        // Left panel for PDF selection and preview
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        
        // PDF selection panel
        JPanel pdfSelectionPanel = new JPanel(new BorderLayout(5, 0));
        JLabel pdfLabel = new JLabel("Syllabus PDF:");
        pdfPathField = new JTextField();
        pdfPathField.setEditable(false);
        browseButton = new JButton("Browse");
        
        pdfSelectionPanel.add(pdfLabel, BorderLayout.WEST);
        pdfSelectionPanel.add(pdfPathField, BorderLayout.CENTER);
        pdfSelectionPanel.add(browseButton, BorderLayout.EAST);
        
        // PDF content preview
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Syllabus Preview"));
        
        syllabusTextArea = new JTextArea();
        syllabusTextArea.setEditable(false);
        syllabusTextArea.setLineWrap(true);
        syllabusTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(syllabusTextArea);
        previewPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Generate button
        generateButton = new JButton("Generate MCQs");
        generateButton.setEnabled(false);
        
        leftPanel.add(pdfSelectionPanel, BorderLayout.NORTH);
        leftPanel.add(previewPanel, BorderLayout.CENTER);
        leftPanel.add(generateButton, BorderLayout.SOUTH);
        
        // Right panel for quiz display
        quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
        quizScrollPane = new JScrollPane(quizPanel);
        quizScrollPane.setBorder(BorderFactory.createTitledBorder("Generated MCQs"));
        
        // Create and configure split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, quizScrollPane);
        splitPane.setResizeWeight(0.5);
        return splitPane;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        statusLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addEventListeners() {
        browseButton.addActionListener(e -> selectPdfFile());
        generateButton.addActionListener(e -> generateQuiz());
    }
    
    private void selectPdfFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            pdfPathField.setText(selectedFile.getAbsolutePath());
            
            try {
                statusLabel.setText("Loading PDF...");
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
                
                // Extract text from PDF in a background thread
                SwingWorker<String, Void> worker = new SwingWorker<>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return pdfService.extractTextFromPdf(selectedFile);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            String pdfText = get();
                            syllabusTextArea.setText(pdfText);
                            generateButton.setEnabled(true);
                            statusLabel.setText("PDF loaded successfully");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "Error loading PDF: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            statusLabel.setText("Error loading PDF");
                        } finally {
                            progressBar.setVisible(false);
                        }
                    }
                };
                worker.execute();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading PDF: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error loading PDF");
                progressBar.setVisible(false);
            }
        }
    }
    
    private void generateQuiz() {
        String apiKey = getApiKey();
        
        String syllabus = syllabusTextArea.getText();
        if (syllabus.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No syllabus content available",
                    "Missing Content", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Disable UI controls
        generateButton.setEnabled(false);
        browseButton.setEnabled(false);
        
        // Update status
        statusLabel.setText("Generating MCQs...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        
        // Clear previous quiz
        quizPanel.removeAll();
        quizPanel.revalidate();
        quizPanel.repaint();
        
        // Generate quiz in background
        SwingWorker<Quiz, Void> worker = new SwingWorker<>() {
            @Override
            protected Quiz doInBackground() throws Exception {
                return quizService.generateQuiz(syllabus, apiKey);
            }
            
            @Override
            protected void done() {
                try {
                    Quiz quiz = get();
                    displayQuiz(quiz);
                    statusLabel.setText("MCQs generated successfully");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Error generating MCQs: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error generating MCQs");
                } finally {
                    // Re-enable UI controls
                    generateButton.setEnabled(true);
                    browseButton.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        };
        worker.execute();
    }
    
    private void displayQuiz(Quiz quiz) {
        if (quiz == null || quiz.getQuestions().isEmpty()) {
            JPanel messagePanel = new JPanel(new BorderLayout());
            messagePanel.add(new JLabel("No questions were generated. Try with a different syllabus."), BorderLayout.CENTER);
            quizPanel.add(messagePanel);
            quizPanel.revalidate();
            quizPanel.repaint();
            return;
        }
        
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
        
        List<Question> questions = quiz.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            QuestionPanel questionPanel = new QuestionPanel(i + 1, question);
            quizPanel.add(questionPanel);
            quizPanel.add(Box.createVerticalStrut(10));
        }
        
        // Add save button at the bottom
        JButton saveButton = new JButton("Save Quiz Results");
        saveButton.addActionListener(e -> saveQuizResults(quiz));
        JPanel saveButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButtonPanel.add(saveButton);
        quizPanel.add(saveButtonPanel);
        
        quizPanel.revalidate();
        quizPanel.repaint();
    }
    
    private void saveQuizResults(Quiz quiz) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".txt")) {
                filePath += ".txt";
            }
            
            try {
                quizService.saveQuizToFile(quiz, filePath);
                JOptionPane.showMessageDialog(this,
                        "Quiz saved successfully",
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving quiz: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 