package com.quizzybee.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

/**
 * Service for handling PDF-related operations
 */
public class PdfService {
    
    /**
     * Extracts text from a PDF file
     * 
     * @param pdfFile The PDF file to extract text from
     * @return The extracted text
     * @throws IOException If there's an error reading the PDF
     */
    public String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            if (document.isEncrypted()) {
                throw new IOException("Cannot process encrypted PDF");
            }
            
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    /**
     * Extracts a summary of the PDF for preview purposes
     * Limits the text to about 500 words
     * 
     * @param pdfFile The PDF file to extract a summary from
     * @return A summary of the PDF content
     * @throws IOException If there's an error reading the PDF
     */
    public String extractSummaryFromPdf(File pdfFile) throws IOException {
        String fullText = extractTextFromPdf(pdfFile);
        
        // Limit to approximately 500 words (or 3000 characters)
        if (fullText.length() > 3000) {
            return fullText.substring(0, 3000) + "... (content truncated for preview)";
        }
        
        return fullText;
    }
} 