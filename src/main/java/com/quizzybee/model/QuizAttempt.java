package com.quizzybee.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user's attempt at a quiz
 */
public class QuizAttempt implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String attemptId;
    private Quiz quiz;
    private Date attemptDate;
    private Map<Integer, Integer> userAnswers; // Maps question index to selected answer index
    private int score;
    private boolean completed;
    
    public QuizAttempt() {
        this.attemptDate = new Date();
        this.userAnswers = new HashMap<>();
        this.completed = false;
    }
    
    public QuizAttempt(String attemptId, Quiz quiz) {
        this.attemptId = attemptId;
        this.quiz = quiz;
        this.attemptDate = new Date();
        this.userAnswers = new HashMap<>();
        this.completed = false;
    }
    
    public String getAttemptId() {
        return attemptId;
    }
    
    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }
    
    public Quiz getQuiz() {
        return quiz;
    }
    
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
    
    public Date getAttemptDate() {
        return attemptDate;
    }
    
    public void setAttemptDate(Date attemptDate) {
        this.attemptDate = attemptDate;
    }
    
    public Map<Integer, Integer> getUserAnswers() {
        return userAnswers;
    }
    
    public void setUserAnswers(Map<Integer, Integer> userAnswers) {
        this.userAnswers = userAnswers;
    }
    
    public void addAnswer(int questionIndex, int selectedAnswerIndex) {
        this.userAnswers.put(questionIndex, selectedAnswerIndex);
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    /**
     * Calculates the score based on correct answers
     */
    public void calculateScore() {
        if (quiz == null || quiz.getQuestions().isEmpty()) {
            score = 0;
            return;
        }
        
        int correctAnswers = 0;
        
        for (Map.Entry<Integer, Integer> entry : userAnswers.entrySet()) {
            int questionIndex = entry.getKey();
            int selectedAnswerIndex = entry.getValue();
            
            if (questionIndex < quiz.getQuestions().size()) {
                Question question = quiz.getQuestions().get(questionIndex);
                if (question.getCorrectOptionIndex() == selectedAnswerIndex) {
                    correctAnswers++;
                }
            }
        }
        
        // Calculate percentage score
        score = (int) (((double) correctAnswers / quiz.getQuestions().size()) * 100);
    }
    
    /**
     * Returns the percentage score as a string
     */
    public String getScoreAsString() {
        return score + "%";
    }
    
    /**
     * Gets the number of questions that were answered correctly
     */
    public int getCorrectAnswersCount() {
        if (quiz == null || quiz.getQuestions().isEmpty()) {
            return 0;
        }
        
        int correctAnswers = 0;
        
        for (Map.Entry<Integer, Integer> entry : userAnswers.entrySet()) {
            int questionIndex = entry.getKey();
            int selectedAnswerIndex = entry.getValue();
            
            if (questionIndex < quiz.getQuestions().size()) {
                Question question = quiz.getQuestions().get(questionIndex);
                if (question.getCorrectOptionIndex() == selectedAnswerIndex) {
                    correctAnswers++;
                }
            }
        }
        
        return correctAnswers;
    }
    
    @Override
    public String toString() {
        return "Quiz: " + quiz.getTitle() + ", Score: " + getScoreAsString() + 
               ", Date: " + attemptDate.toString();
    }
} 