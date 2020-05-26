package bs.code;

import org.fxmisc.richtext.CodeArea;

import java.io.File;

/**
 * Document class object that is responsible for holding the metadata for files, including content, filepath, filename,
 * file extension, and state.
 *
 * @author Toan-James Le
 * @author Alvin Huynh
 */
public class Document {
    /**
     * JavaFX Editable Code Area object.
     */
    public CodeArea codeArea;

    /**
     * Number of keywords.
     */
    private int keywords;

    /**
     * Boolean state flag. True if state if file has been modified, false if otherwise.
     */
    private boolean needSave;

    /**
     * String member variable to hold the filepath of the file.
     */
    private String filePath;

    /**
     * String member variable to hold the current parent directory.
     */
    private String fileDirectory;

    /**
     * String member variable to hold the filename of the file.
     */
    private String fileName;

    /**
     * String member variable to hold the extension of the file (e.g. .java).
     */
    private String fileExtension;

    /**
     * String member variable to hold the current text inside of the text area.
     */
    private String fileContent;

    /**
     * Default constructor for new files.
     */
    public Document() {
        this.needSave = false;
        this.filePath = "";
        this.fileDirectory = "";
        this.fileExtension = "";
        this.fileContent = "";
        this.codeArea = new CodeArea();
    }

    /**
     * Constructor for file object that takes a <code>name</code>, <code>path</code>, and <code>content</code>.
     *
     * @param path Filepath of the file.
     * @param name Filename of the file.
     * @param content Text content of the file.
     */
    public Document(String path, String name, String content) {
        this.needSave = false;
        this.filePath = path;
        this.fileDirectory = new File(this.filePath).getParent();
        this.fileName = name;
        this.fileContent = content;
        this.codeArea = new CodeArea();
        this.fileExtension = "";

        int lastIndexOf = this.fileName.lastIndexOf(".");
        if (lastIndexOf != -1) {
            this.fileExtension = this.fileName.substring(lastIndexOf);
        }
    }

    /**
     * Saves the contents of the file to the file.
     */
    public void save() {
        if (FileHelper.saveFile(this.filePath, this.fileContent)) {
            setFileContent(this.codeArea.getText());
        }
    }

    /**
     * Sets number of keywords.
     *
     * @param keywords number of keywords to set (from lexer).
     */
    public void setKeywordCount(int keywords) { this.keywords = keywords; }

    /**
     * Gets number of keywords.
     *
     * @return Number of keywords.
     */
    public int getKeywordCount() {
        return this.keywords;
    }

    /**
     * Sets the current file content of the document.
     *
     * @param content Full content of file as a string.
     */
    public void setFileContent(String content) {
        this.fileContent = content;
    }

    /**
     * Gets the current state of the document, i.e. whether or not the document has writes it needs to save.
     *
     * @return True if file has writes it needs to save, false otherwise.
     */
    public boolean getNeedsSave() {
        return this.needSave;
    }

    /**
     * Gets the current filepath of the document.
     *
     * @return String of the filepath.
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Gets the current file directory of the document.
     *
     * @return String of the file directory.
     */
    public String getFileDirectory() {
        return this.fileDirectory;
    }

    /**
     * Gets the current filename of the document.
     *
     * @return String of the file name.
     */
    public String getFileName() { return this.fileName; }

    /**
     * Gets the current file content of the document.
     *
     * @return String of the file content.
     */
    public String getFileContent() {
        return this.fileContent;
    }

    /**
     * Gets the current file extension of the document.
     *
     * @return String of the file extension.
     */
    public String getFileExtension() {
        return this.fileExtension;
    }

    /**
     * Sets whether or not the document has writes it needs to save.
     *
     * @param state State to set the document to.
     */
    public void setNeedsSave(boolean state) { this.needSave = state; }

    public String toString() { return fileName; }
}
