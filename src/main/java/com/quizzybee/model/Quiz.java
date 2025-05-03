package com.quizzybee.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of questions forming a quiz
 */
public class Quiz {
    private List<Question> questions;
    private String title;
    private String description;

    public Quiz() {
        this.questions = new ArrayList<>();
        this.title = "Syllabus MCQ Quiz";
        this.description = "Generated from syllabus content";
    }

    public Quiz(String title, String description) {
        this.questions = new ArrayList<>();
        this.title = title;
        this.description = description;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getQuestionCount() {
        return questions.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        sb.append(description).append("\n\n");
        
        for (int i = 0; i < questions.size(); i++) {
            sb.append("Question ").append(i + 1).append(":\n");
            sb.append(questions.get(i).toString()).append("\n");
        }
        
        return sb.toString();
    }
} 