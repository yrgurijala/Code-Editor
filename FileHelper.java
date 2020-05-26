package bs.code;

import javafx.scene.control.Alert;

import java.io.*;

public class FileHelper {
    /**
     * Saves file content to a file path. Returns true if successful, false otherwise.
     * @param filePath
     * @param fileContent
     * @return
     */
    public static boolean saveFile(String filePath, String fileContent) {
        // attempt to write to file
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            printWriter.print(fileContent);

            printWriter.close();
            fileWriter.close();
            return true;
        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Error");
            alert.setHeaderText("There was an error saving the document.");
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
            return false;
        }
    }

    /**
     * Reads the file content and returns the file content as a string.
     *
     * @param file File object with file opened.
     * @return String of text content inside of the file.
     * @author Toan-James Le
     */
    public static String readFile(File file) {
        StringBuilder stringBuffer = new StringBuilder();
        BufferedReader bufferedReader;

        // attempts to read the file
        try {
            // reads the file into buffered reader
            bufferedReader = new BufferedReader(new FileReader(file));

            // holds the text of the file content
            int ch;
            // while buffered reader is able to read a line
            while ((ch = bufferedReader.read()) != -1) {
                stringBuffer.append((char)ch);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("Error: Unable to read file: " + file.getAbsolutePath());
        }

        return stringBuffer.toString();
    }

}
