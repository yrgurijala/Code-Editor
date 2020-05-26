package bs.code;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * MainController object that controls the action events of the FXML window.
 */
public class MainController {
    @FXML
    public MenuItem openMenuItem;
    @FXML
    public TitledPane projectExplorerTitle;
    @FXML
    private TreeView<String> projectExplorer;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane editorAnchorPane;
    @FXML
    private TitledPane statsPanel;

    /**
     * Document object to hold the content and meta-data of the file.
     */
    private Document document;
    private List<Document> docList;
    private List<Tab> tabList;
    private List<TextArea> tabText;
    private File currDirectory;

    public MainController() {
        document = new Document();
        docList = new ArrayList<>();
        tabList = new ArrayList<>();
        tabText = new ArrayList<>();
        currDirectory = new File("");
    }

    public Tab newTab(Document document) {
        Tab tab = new Tab();
        editorAnchorPane.setTopAnchor(document.codeArea, 0.0);
        editorAnchorPane.setBottomAnchor(document.codeArea, 0.0);
        editorAnchorPane.setLeftAnchor(document.codeArea, 0.0);
        editorAnchorPane.setRightAnchor(document.codeArea, 0.0);
        VirtualizedScrollPane scrollPane = new VirtualizedScrollPane(document.codeArea);
        document.codeArea.replaceText(document.getFileContent());

        tab.setContent(scrollPane);

        document.codeArea.multiPlainChanges().subscribe((irrelevant) -> {
            syntaxCheck(document);
            statsPanel.setText(document.getKeywordCount() + " keyword(s)");
            document.setNeedsSave(true);
        });

        return tab;
    }

    @FXML
    public void initialize() {
        tabPane.getSelectionModel().selectedIndexProperty().addListener((irrelevant, oldV, newV) -> {
            if (docList.size() >= 1) {
                document = docList.get((Integer) newV);
                document.codeArea.replaceText(document.getFileContent());
                statsPanel.setText(document.getKeywordCount() + " keyword(s)");
            } else {
                // editField.replaceText("");
            }
        });

        // set on mouse click
        projectExplorer.setOnMouseClicked((MouseEvent mouseEvent) -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                // if double clicked
                if (mouseEvent.getClickCount() == 2) {
                    // set tree item to current selected item
                    TreeItem<String> item = projectExplorer.getSelectionModel().getSelectedItem();
                    // creates a string builder and sets value to current item selected
                    StringBuilder stringBuilder = new StringBuilder(item.getValue());

                    // build the relative path of the selected file
                    while (true) {
                        item = item.getParent();
                        if (item.getParent() instanceof TreeItem) {
                            stringBuilder.insert(0, item.getValue() + "\\");
                        } else {
                            break;
                        }
                    }

                    try {
                        String path = currDirectory.getAbsolutePath() + "\\" + stringBuilder.toString();
                        File openingFile = new File(path);


                        // creates a new document object for the file
                        Document currDoc = new Document(
                                openingFile.getAbsolutePath(),
                                openingFile.getName(),
                                FileHelper.readFile(openingFile)
                        );

                        Tab currTab = newTab(currDoc);

                        currTab.setOnCloseRequest((Event event) -> {
                            currDoc.save();

                            int idx = tabPane.getSelectionModel().getSelectedIndex();
                            docList.remove(idx);
                            tabList.remove(idx);
                        });

                        // boolean flag to indicate if there's already an existing tab
                        boolean found = false;
                        for (Tab temp : tabList) {
                            if (temp.getText().equals(openingFile.getAbsolutePath())) {
                                found = true;
                                break;
                            }
                        }

                        // if file is not a directory and no existing tab is found
                        if (!openingFile.isDirectory() && !found) {
                            tabList.add(currTab);
                            docList.add(currDoc);
                            // TODO: Tabs needs to be identified in another unique way instead of by absolute path.
                            currTab.setText(openingFile.getAbsolutePath());
                            tabPane.getTabs().add(currTab);
                            syntaxCheck(currDoc);
                        }
                    }
                    catch (NullPointerException ex)
                    {
                        System.out.println("Opening the File");
                    }
                }
            }
        });
    }

    /**
     * Opens the application.
     *
     * @param event Action event that triggered the function.
     * @author Sergio Galera
     * @author Yashwantth Gurijala
     */
    public void openApp(ActionEvent event) {
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(null);

        String nameOfFile = selectedFile.getName();

        System.out.println(nameOfFile);

        fc.showOpenDialog(null);
    }

    /**
     * Function that closes the application.
     *
     * @param event
     * @author Sergio Galera
     * @author Yashwantth Gurijala
     */
    public void closeApp(ActionEvent event) {
        boolean saved = this.checkDocumentState();
        if (saved) {
            System.exit(0);
        }
    }

    /**
     * Prompts an open window to the user.
     *
     * @param event Action event parameter.
     * @author Aaron Rohan
     * @author James-Toan Le
     * @author Alvin Huynh
     */
    public void openPrompt(ActionEvent event) {
        // checks document state and prompts to save
        this.checkDocumentState();

        // creates a new directory chooser window
        DirectoryChooser chooser = new DirectoryChooser();
        try {
            File folder = chooser.showDialog(null);
            currDirectory = folder;

            TreeItem<String> projectRoot = new TreeItem<>(folder.getName());
            // sets project root to not show
            projectExplorer.setShowRoot(false);
            projectExplorer.setRoot(projectRoot);
            // sets explorer title to the name of the folder
            projectExplorerTitle.setText(folder.getName());

            // recursively opens and builds tree view
            this.buildTreeView(projectRoot, Objects.requireNonNull(folder.listFiles()));
            //this.displayFiles(folder.listFiles());
        } catch (Exception ignored) {

        }
    }

    /**
     * Recursively finds all folders and files inside of selected folder and adds them into the tree view.
     * <p>
     * TODO: Clear tree view before creating new tabs and tree view items.
     *
     * @param root     Root tree item.
     * @param fileList File list to iterate through.
     * @author Alvin Huynh
     */
    private void buildTreeView(TreeItem<String> root, File[] fileList) {
        List<TreeItem<String>> directories = new ArrayList<TreeItem<String>>();
        List<TreeItem<String>> files = new ArrayList<>();

        // assert list of files to not be null
        assert fileList != null;
        // for each file in files list
        for (File file : fileList) {
            // if a directory, recursively call build tree view on directory then add root folder
            if (file.isDirectory()) {
                TreeItem<String> rootFolder = new TreeItem<>(file.getName());
                new TreeItem<>();
                // recursively builds tree on directory
                this.buildTreeView(rootFolder, Objects.requireNonNull(file.listFiles()));
                directories.add(rootFolder);
            } else {
                files.add(new TreeItem<>(file.getName()));
            }
        }

        // adds all the directories to the root tree item
        for (TreeItem<String> item : directories) {
            root.getChildren().add(item);
        }

        // adds all the files to the root tree item
        for (TreeItem<String> item : files) {
            root.getChildren().add(item);
        }
    }

    /**
     * Displays the file contents in the project folder onto the tab pane.
     *
     * @param fileList list of relevant files to display to tabpane.
     * @author Toan-James Le
     */
    private void displayFiles(File[] fileList) {
        // loops through each file in the file list
        for (File file : fileList) {
            // creates a new tab
            Tab currTab = new Tab();
            // creates a new document object for the file
            Document currDoc = new Document(
                file.getAbsolutePath(),
                file.getName(),
                FileHelper.readFile(file)
            );

            // boolean flag to indicate if there's already an existing tab
            boolean found = false;
            for (Tab temp : tabList) {
                if (temp.getText().equals(currDoc.getFileName())) {
                    found = true;
                    break;
                }
            }

            // if file is not a directory and no existing tab is found
            if (!file.isDirectory() && !found) {
                tabList.add(currTab);
                docList.add(currDoc);
                currTab.setText(currDoc.getFileName());
                tabPane.getTabs().add(currTab);
            }
        }
    }

    /**
     * Checks the document state and prompts the user to save if a change has been made.
     *
     * @return Returns true if save prompt was acknowledged. False otherwise.
     * @author Aaron Rohan
     * @author Alvin Huynh
     */
    private boolean checkDocumentState() {
        // if document state is true (e.g. file is ready)
        if (this.document.getNeedsSave()) {
            // creates a new confirmation window
            Alert confirmer = new Alert(Alert.AlertType.CONFIRMATION);
            confirmer.setTitle("Save?");
            confirmer.setHeaderText("You have changes that you haven't saved.");
            confirmer.setContentText("Would you like to save them?");

            ButtonType bSave = new ButtonType("Save");
            ButtonType bSaveAs = new ButtonType("Save As");
            ButtonType bDontSave = new ButtonType("Don't Save");
            ButtonType bCancel = new ButtonType("Cancel",
                    ButtonData.CANCEL_CLOSE);
            confirmer.getButtonTypes().setAll(bSave, bSaveAs, bDontSave, bCancel);

            // wait for result of confirmation window
            Optional<ButtonType> result = confirmer.showAndWait();

            if (result.isPresent()) {
                if (result.get() == bSave) {
                    // this.document.save(this.editField.getText());
                    return true;
                } else if (result.get() == bSaveAs) {
                    this.saveAsPrompt(null);
                    return true;
                } else if (result.get() == bDontSave) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Checks to see if the file path exists. If it does, it saves the file and if not, it pulls up the
     * save as prompt.
     * <p>
     *
     * @param actionEvent
     * @author Alvin Huynh
     * @author Toan-James Le
     */
    public void savePrompt(ActionEvent actionEvent) {
        document.save();
    }

    /**
     * Prompts the user with a FileChooser window dialog with a save option.
     *
     * @param actionEvent
     * @author Alvin Huynh
     * @author Toan-James Le
     * @editor Yashwanth Gurijala
     */
    public void saveAsPrompt(ActionEvent actionEvent) {
        // creates new FileChooser
        FileChooser chooser = new FileChooser();
        // sets title of file chooser
        chooser.setTitle("Save As");

        // adds extension filters
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Source Files",
                        "*.txt", "*.json", "*.java", "*.c", "*.py"),
                new FileChooser.ExtensionFilter("All File", "*.*"));

        // if file path does not exist
        if (this.document.getFilePath().length() == 0) {
            // sets initial directory of file chooser user's home directory
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        } else {
            // sets initial directory of file chooser to file's parent directory
            chooser.setInitialDirectory(new File(this.document.getFileDirectory()));
        }

        // shows save dialog window
        File file = chooser.showSaveDialog(null);

        // if file is not null
        if (file != null) {
            // creates a new document object
            this.document = new Document(
                file.getAbsolutePath(),
                file.getName(),
                ""
            );
        }
    }

    public void newFilePrompt(ActionEvent actionEvent) {
        // creates new FileChooser
        FileChooser chooser = new FileChooser();
        // sets title of file chooser
        chooser.setTitle("Save As");

        // adds extension filters
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Source Files",
                        "*.txt", "*.json", "*.java", "*.c", "*.py"),
                new FileChooser.ExtensionFilter("All File", "*.*"));

        // if file path does not exist
        if (this.document.getFilePath().length() == 0) {
            // sets initial directory of file chooser user's home directory
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        } else {
            // sets initial directory of file chooser to file's parent directory
            chooser.setInitialDirectory(new File(this.document.getFileDirectory()));
        }

        // shows save dialog window
        File file = chooser.showSaveDialog(null);

        if (file != null) {
            // creates a new document object
            Document newDoc = new Document(
                file.getAbsolutePath(),
                file.getName(),
                ""
            );

            docList.add(newDoc);
            var tab = newTab(newDoc);
            tabList.add(tab);
            tabPane.getTabs().add(tab);

            //catches the nullpointerexception
            try
            {
                TreeItem<String> projectRoot = new TreeItem<>(currDirectory.getName());

                // sets project root to not show
                projectExplorer.setShowRoot(false);
                projectExplorer.setRoot(projectRoot);
                projectExplorerTitle.setText(currDirectory.getName());

                // recursively opens and builds tree view
                buildTreeView(projectRoot, Objects.requireNonNull(currDirectory.listFiles()));
            }
            catch (NullPointerException ex)
            {
                System.out.println("Created a New File");
            }
        }

    }

    /**
     * Function that calls lexer and highlights the document.
     *
     * @author Aaron Rohan
     * @editor Yashwanth Gurijala
     */
    private void syntaxCheck(Document document) {
        document.codeArea.clearStyle(0, document.codeArea.getLength());
        ArrayList<LexerToken> tokens = Lexer.lex(document.codeArea.getText());

        int keywords = 0;
        for (LexerToken token : tokens) {
            int from = token.getOffset();
            int to = token.getOffset() + token.getLength();
            switch (token.getKind()) {
                case Keyword:
                    document.codeArea.setStyleClass(from, to, "keyword");
                    keywords += 1;
                    break;
                case Arithmetic:
                    document.codeArea.setStyleClass(from, to, "arithmetic");
                    break;
                case Boolean:
                    document.codeArea.setStyleClass(from, to, "boolean");
                    break;
                case String:
                    document.codeArea.setStyleClass(from, to, "string");
                    break;
            }
        }

        document.setKeywordCount(keywords);
    }


    public void buildPrompt(ActionEvent actionEvent) {
        // creates new FileChooser
        FileChooser chooser = new FileChooser();
        // sets title of file chooser
        chooser.setTitle("Choose Main File");
        // adds extension filters
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JAVA Source Files", "*.java"));
        // sets initial directory of file chooser to file's parent directory
        chooser.setInitialDirectory(new File(this.currDirectory.getAbsolutePath()));

        // shows save dialog window
        File file = chooser.showOpenDialog(null);

        if (file != null) {
            Builder builder = new Builder();
            boolean compileSuccess = builder.compileProject(currDirectory, file);

            System.out.println("Build success: " + compileSuccess);
            if (compileSuccess) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Build Success");
                alert.setHeaderText("Project build successful.");
                alert.showAndWait();

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Build Error");
                alert.setHeaderText("Unable to build project.");
                alert.showAndWait();
            }
        }
    }

    /**
     * Builds the project into a production folder and then attempts to execute the compiled project.
     * @param actionEvent
     */
    public void runPrompt(ActionEvent actionEvent) {
        executeProject(false);
    }

    public void debugPrompt(ActionEvent actionEvent) {
        executeProject(true);
    }

    public void executeProject(boolean debug) {
        // creates new FileChooser
        FileChooser chooser = new FileChooser();
        // sets title of file chooser
        chooser.setTitle("Choose Main File");
        // adds extension filters
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JAVA Source Files", "*.java"));
        // sets initial directory of file chooser to file's parent directory
        chooser.setInitialDirectory(new File(this.currDirectory.getAbsolutePath()));

        // shows save dialog window
        File file = chooser.showOpenDialog(null);

        if (file != null) {
            Builder builder = new Builder();
            boolean buildSuccess = builder.compileProject(currDirectory, file);
            System.out.println("Build success: " + buildSuccess);
            if (buildSuccess) {
                builder.executeProject(currDirectory, debug);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Build Error");
                alert.setHeaderText("Unable to build project.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    MenuItem undoButton;
    @FXML
    MenuItem redoButton;
    @FXML
    MenuItem cutButton;
    @FXML
    MenuItem copyButton;
    @FXML
    MenuItem pasteButton;
    @FXML
    MenuItem deleteButton;
    @FXML
    MenuItem selectAllButton;
    @FXML
    MenuItem deselectAllButton;

    /**
     * When the menu is opened, this function gets called in order to check if the menu items should be grayed-out or
     * not by calling their respective functions.
     *
     * @author Aaron Rohan
     */
    public void checkMenu() {
        undoButton.setDisable(!canUndo());
        redoButton.setDisable(!canRedo());
        cutButton.setDisable(!hasSelection());
        copyButton.setDisable(!hasSelection());
        pasteButton.setDisable(!canPaste());
        deleteButton.setDisable(!hasSelection());
        selectAllButton.setDisable(!validCodeArea());
        deselectAllButton.setDisable(!validCodeArea());
    }

    /**
     * Returns whether there is a valid code area we can run code against.
     *
     * @author Aaron Rohan
     */
    private boolean validCodeArea() {
        return document != null && document.codeArea != null;
    }

    /**
     * Returns whether undo is available.
     *
     * @author Aaron Rohan
     */
    private boolean canUndo() {
        return validCodeArea() && document.codeArea.isUndoAvailable();
    }

    /**
     * Returns whether redo is available.
     *
     * @author Aaron Rohan
     */
    private boolean canRedo() {
        return validCodeArea() && document.codeArea.isRedoAvailable();
    }

    /**
     * Returns whether a selection exists in the code area.
     *
     * @author Aaron Rohan
     */
    private boolean hasSelection() {
        return validCodeArea() && document.codeArea.getSelection().getLength() != 0;
    }

    /**
     * Returns whether we can paste in the code area.
     *
     * @author Aaron Rohan
     */
    private boolean canPaste() {
        return validCodeArea() && Clipboard.getSystemClipboard().hasString();
    }

    /**
     * Function that undos the lasts action.
     *
     * @author Aaron Rohan
     */
    public void undo() {
        document.codeArea.undo();
        // TODO: Check if we need syntax check
    }

    /**
     * Function that redos the lasts action.
     *
     * @author Aaron Rohan
     */
    public void redo() {
        document.codeArea.redo();
        // TODO: Check if we need syntax check
    }

    /**
     * Function that cuts current selection to the clipboard.
     *
     * @author Aaron Rohan
     */
    public void cut() {
        document.codeArea.cut();
        syntaxCheck(document);

    }

    /**
     * Function that copies current selection to the clipboard.
     *
     * @author Aaron Rohan
     */
    public void copy() {
        document.codeArea.copy();
        // As text is not yet "modified," we can forego a syntax check.
    }

    /**
     * Function that pastes current selection from the clipboard.
     *
     * @author Aaron Rohan
     */
    public void paste() {
        document.codeArea.paste();
        syntaxCheck(document);
    }

    /**
     * Function that deletes current selection without modifying clipboard.
     *
     * @author Aaron Rohan
     */
    public void delete() {
        IndexRange range = document.codeArea.getSelection();
        document.codeArea.deleteText(range);
        syntaxCheck(document);
        // TODO: Check if this is undoable.
    }

    /**
     * Selects the entire document.
     *
     * @author Aaron Rohan
     */
    public void selectAll() {
        document.codeArea.selectAll();
        // As text is not yet "modified," we can forego a syntax check.
    }

    /**
     * Deselects everything in the document.
     *
     * @author Aaron Rohan
     */
    public void deselectAll() {
        document.codeArea.deselect();
        // As text is not yet "modified," we can forego a syntax check.
    }
}
