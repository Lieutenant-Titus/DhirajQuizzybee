# QuizzyBee - MCQ Generator from Syllabus PDFs

QuizzyBee is a Java Swing application that allows students to upload PDF syllabi and automatically generates multiple-choice questions (MCQs) for exam practice using Google's Gemini AI.

## Feature

- PDF syllabus upload and text extraction
- Automatic MCQ generation using (Gemini AI)
- Interactive quiz interface with correct answer checking
- Explanation for each question
- Save quiz results to a text file.

## Requirements

- Java 11 or higher
- Maven for dependency management
- Google Gemini API key

## How to Use

1. Clone this repository
2. Run `mvn clean package` to build the application
3. Run the application with `java -jar target/quizzybee-1.0-SNAPSHOT-jar-with-dependencies.jar`
4. Enter your Gemini API key in the application
5. Upload a syllabus PDF
6. Click "Generate MCQs" to create questions

## Getting a Gemini API Key

1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Create an API key
4. Copy the API key and paste it into the application

## Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/quizzybee.git
cd quizzybee

# Build with Maven
mvn clean package

# Run the application
java -jar target/quizzybee-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Technologies Used

- Java Swing for the user interface
- Apache PDFBox for PDF processing
- OkHttp for API communication
- Jackson for JSON processing
- FlatLaf for modern UI look and feel
- Google Gemini AI for MCQ generation

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
