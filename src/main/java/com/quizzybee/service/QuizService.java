package com.quizzybee.service;

import com.quizzybee.model.Question;
import com.quizzybee.model.Quiz;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating MCQs using Gemini API
 */
public class QuizService {
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Generates a quiz with MCQs based on the provided syllabus text
     * 
     * @param syllabusText The syllabus content
     * @param apiKey Gemini API key
     * @return A Quiz object containing generated questions
     * @throws IOException If there's an error communicating with the API
     */
    public Quiz generateQuiz(String syllabusText, String apiKey) throws IOException {
        // Trim the syllabus text if it's too long
        if (syllabusText.length() > 10000) {
            syllabusText = syllabusText.substring(0, 10000);
        }
        
        // Create prompt for Gemini
        String prompt = createGeminiPrompt(syllabusText);
        
        // Call Gemini API
        String response = callGeminiApi(prompt, apiKey);
        
        // Parse the response to create quiz
        return parseGeminiResponse(response);
    }
    
    /**
     * Creates a prompt for the Gemini API requesting MCQ generation
     */
    private String createGeminiPrompt(String syllabusText) {
        return "Based on the following syllabus content, generate 10 multiple-choice questions (MCQs) " +
               "that would be suitable for a student practice exam. For each question, provide 4 options, " +
               "clearly indicate the correct answer, and include a brief explanation of why that answer is correct.\n\n" +
               "Format your response as a JSON array of questions with the following structure for each question:\n" +
               "{\n" +
               "  \"question\": \"The question text\",\n" +
               "  \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
               "  \"correctOptionIndex\": 0,  // 0-based index of the correct option\n" +
               "  \"explanation\": \"Why this answer is correct\"\n" +
               "}\n\n" +
               "Ensure questions cover different concepts from the syllabus and vary in difficulty.\n\n" +
               "Here's the syllabus content:\n\n" + syllabusText;
    }
    
    /**
     * Calls the Gemini API with the provided prompt
     */
    private String callGeminiApi(String prompt, String apiKey) throws IOException {
        // Create an OkHttpClient with increased timeout values
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();
        
        // Prepare request body
        String requestBody = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": \"" + escapeJson(prompt) + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"generationConfig\": {\n" +
                "    \"temperature\": 0.7,\n" +
                "    \"topK\": 40,\n" +
                "    \"topP\": 0.95,\n" +
                "    \"maxOutputTokens\": 8192\n" +
                "  }\n" +
                "}";
        
        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + apiKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code() + 
                        "\nResponse: " + (response.body() != null ? response.body().string() : "null"));
            }
            
            return parseApiResponse(response);
        }
    }
    
    /**
     * Parses the raw API response
     */
    private String parseApiResponse(Response response) throws IOException {
        String responseBody = response.body().string();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        
        if (!rootNode.has("candidates") || rootNode.get("candidates").isEmpty() ||
                !rootNode.get("candidates").get(0).has("content") ||
                !rootNode.get("candidates").get(0).get("content").has("parts") ||
                rootNode.get("candidates").get(0).get("content").get("parts").isEmpty()) {
            throw new IOException("Invalid API response format");
        }
        
        return rootNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
    }
    
    /**
     * Parses the response from Gemini API to create a Quiz object
     */
    private Quiz parseGeminiResponse(String response) {
        Quiz quiz = new Quiz("Syllabus MCQ Quiz", "Generated from syllabus content");
        
        try {
            // Extract JSON array from the response
            int jsonStart = response.indexOf('[');
            int jsonEnd = response.lastIndexOf(']') + 1;
            
            if (jsonStart == -1 || jsonEnd == 0 || jsonEnd <= jsonStart) {
                // Try alternative format (if not a direct JSON array)
                jsonStart = response.indexOf('{');
                jsonEnd = response.lastIndexOf('}') + 1;
                
                if (jsonStart == -1 || jsonEnd == 0 || jsonEnd <= jsonStart) {
                    // If still not found, handle as a formatting error
                    fallbackParseResponse(response, quiz);
                    return quiz;
                }
                
                // If we found a single object, wrap it in an array
                String json = "[" + response.substring(jsonStart, jsonEnd) + "]";
                parseJsonQuestions(json, quiz);
                return quiz;
            }
            
            String json = response.substring(jsonStart, jsonEnd);
            parseJsonQuestions(json, quiz);
            
        } catch (Exception e) {
            // If JSON parsing fails, try fallback parsing
            fallbackParseResponse(response, quiz);
        }
        
        return quiz;
    }
    
    /**
     * Parses JSON questions and adds them to the quiz
     */
    private void parseJsonQuestions(String json, Quiz quiz) {
        try {
            JsonNode questionsArray = objectMapper.readTree(json);
            
            for (JsonNode questionNode : questionsArray) {
                String questionText = questionNode.has("question") ? 
                        questionNode.get("question").asText() : "";
                        
                List<String> options = new ArrayList<>();
                if (questionNode.has("options") && questionNode.get("options").isArray()) {
                    JsonNode optionsArray = questionNode.get("options");
                    for (JsonNode option : optionsArray) {
                        options.add(option.asText());
                    }
                }
                
                int correctIndex = questionNode.has("correctOptionIndex") ? 
                        questionNode.get("correctOptionIndex").asInt() : 0;
                        
                String explanation = questionNode.has("explanation") ? 
                        questionNode.get("explanation").asText() : "";
                
                // If we have a valid question with options, add it to the quiz
                if (!questionText.isEmpty() && !options.isEmpty()) {
                    Question question = new Question(questionText, options, correctIndex, explanation);
                    quiz.addQuestion(question);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON questions: " + e.getMessage());
        }
    }
    
    /**
     * Fallback parser for when the API response doesn't contain proper JSON
     */
    private void fallbackParseResponse(String response, Quiz quiz) {
        // Split by question number patterns like "1.", "Question 1:", etc.
        String[] questionBlocks = response.split("(?m)^\\s*(?:Question\\s*)?\\d+[\\.:\\)]\\s*");
        
        for (String block : questionBlocks) {
            if (block.trim().isEmpty()) continue;
            
            try {
                // Try to parse question and options
                String[] lines = block.split("\n");
                if (lines.length < 5) continue; // Need at least question + 4 options
                
                String questionText = lines[0].trim();
                List<String> options = new ArrayList<>();
                int correctIndex = -1;
                StringBuilder explanation = new StringBuilder();
                
                // Look for options (A, B, C, D)
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i].trim();
                    
                    // Match option patterns like "A) text" or "A. text"
                    if (line.matches("^[A-D][).:]\\s*.+")) {
                        char optionLetter = line.charAt(0);
                        String optionText = line.substring(line.indexOf(' ')).trim();
                        options.add(optionText);
                        
                        // Check if this is the correct answer
                        if (line.toLowerCase().contains("correct") || 
                            (i+1 < lines.length && lines[i+1].toLowerCase().contains("correct"))) {
                            correctIndex = optionLetter - 'A';
                        }
                    }
                    
                    // Look for the correct answer indication or explanation
                    if (line.toLowerCase().contains("correct answer") || 
                        line.toLowerCase().contains("explanation")) {
                        // If we find the explanation, use the rest of the text
                        for (int j = i; j < lines.length; j++) {
                            if (lines[j].toLowerCase().contains("explanation")) {
                                // Add all remaining lines to the explanation
                                for (int k = j+1; k < lines.length; k++) {
                                    explanation.append(lines[k].trim()).append("\n");
                                }
                                break;
                            }
                            
                            // Check for answer like "Correct answer: A"
                            if (lines[j].toLowerCase().contains("correct answer")) {
                                String answerLine = lines[j];
                                char correctLetter = ' ';
                                
                                for (char letter : new char[]{'A', 'B', 'C', 'D'}) {
                                    if (answerLine.contains("" + letter)) {
                                        correctLetter = letter;
                                        break;
                                    }
                                }
                                
                                if (correctLetter != ' ') {
                                    correctIndex = correctLetter - 'A';
                                }
                            }
                        }
                        break;
                    }
                }
                
                // If we couldn't find the correct answer, default to the first option
                if (correctIndex == -1 && !options.isEmpty()) {
                    correctIndex = 0;
                }
                
                // If we have a valid question with options, add it to the quiz
                if (!questionText.isEmpty() && options.size() >= 2) {
                    Question question = new Question(
                            questionText, 
                            options, 
                            correctIndex, 
                            explanation.toString().isEmpty() ? "No explanation provided" : explanation.toString());
                            
                    quiz.addQuestion(question);
                }
                
            } catch (Exception e) {
                // Skip this question block if there's an error
                continue;
            }
        }
    }
    
    /**
     * Saves the quiz to a text file
     */
    public void saveQuizToFile(Quiz quiz, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(new File(filePath))) {
            writer.write(quiz.toString());
        }
    }
    
    /**
     * Escapes special characters in a string for JSON
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
} 