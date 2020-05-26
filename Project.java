package bs.code;

import java.io.File;

/**
 * Class that defines and holds all the file objects for the project directory.
 * @author James-Toan Le
 * @author Alvin Huynh
 */
public class Project {

    /**
     * File object to hold the project folder.
     */
    private File projectFolder;


    /**
     * Constructor of the Project object.
     * @param projectFolder File object pointing to the directory of the project.
     */
    public Project(File projectFolder) {
        this.projectFolder = projectFolder;
    }
}
