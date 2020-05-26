package bs.code;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class responsible for building, compiling, and executing Java code on the console.
 *
 * TODO: Run commands based on what operating systems the user is using.
 * TODO: Figure out how to use dependencies.
 * TODO: Add ClassLoader and memory diagnostics.
 *
 * @author Alvin Huynh
 */
public class Builder {

    /**
     * Default constructor for builder class.
     */
    Builder() { }

    /**
     * Compiles the entire project directory into a production folder. If the production folder does not exist, it will
     * automatically create new directories. It will use command and assume that the user has a JDK installed and is
     * using Windows.
     *
     * @param projectDirectory The directory of the project.
     * @param mainFile Path of the main file relative to the project directory.
     *
     * @return Returns true if compiled successfully, otherwise false.
     */
    public boolean compileProject(File projectDirectory, File mainFile) {
        // if project directory is a directory
        if (projectDirectory.isDirectory()) {
            // CREATE PRODUCTION FOLDER

            // creates a build path
            File buildPath = new File(
                projectDirectory.getAbsolutePath() +
                    "\\out\\production\\" +
                    projectDirectory.getName()
            );

            // if build path does not exist, creates a new directory
            if (!buildPath.exists()) {
                if (buildPath.mkdirs()) {
                    System.out.println("Build directory created at: " + buildPath.toString());
                } else {
                    System.out.println("Failed to create build path at: " + buildPath.toString());
                    return false;
                }
            }
            else {
                System.out.println("Build path already exists at: " + buildPath.toString());
            }

            // CREATE CONFIG FILE

            // gets relative path of the main file
            String relativeMainPath = new File(projectDirectory.getAbsolutePath()).toURI().relativize(
                new File(mainFile.getParent()).toURI()
            ).getPath();

            int lastIndexOf = mainFile.getName().lastIndexOf(".");
            String pureFileName = "";
            if (lastIndexOf != -1) {
                pureFileName = mainFile.getName().substring(0, lastIndexOf);
            }

            System.out.println(mainFile.getName());

            // creates a config file to point to the main file
            try {
                // creates file writer object using file path
                FileWriter fileWriter = new FileWriter(buildPath.getAbsolutePath() + "\\main.config");
                // creates print writer with file writer
                PrintWriter printWriter = new PrintWriter(fileWriter);
                // writes new content to print writer
                printWriter.print(pureFileName);
                // closes the print writer
                printWriter.close();
                // closes the file writer
                fileWriter.close();
                System.out.println("Created main.config file with relative main path: " + relativeMainPath);

                fileWriter = new FileWriter(buildPath.getAbsolutePath() + "\\classpath.config");
                printWriter = new PrintWriter(fileWriter);
                if (!"".equals(relativeMainPath)) {
                    // reverses the classpath to remove the last "/" character from the classpath
                    String classPath = relativeMainPath.replace("/", "\\");
                    String reverseClassPath = new StringBuffer(classPath).reverse().toString();
                    reverseClassPath = reverseClassPath.replaceFirst("\\\\", "");
                    classPath = new StringBuffer(reverseClassPath).reverse().toString();
                    printWriter.print(classPath);
                }
                printWriter.close();
                fileWriter.close();
                System.out.println("Created classpath.config file with relative main path: " + relativeMainPath);
            }
            catch (Exception ignored) { }

            // COMPILE PROJECT

            // begin recursively compiling entire project structure into build path production folder
            javacProject(buildPath, "", Objects.requireNonNull(projectDirectory.listFiles()));

        } else {
            System.out.println("Project directory is not a directory.");
            return false;
        }

        return true;
    }

    /**
     * Recursively compiles entire project structure into production folder.
     * @param currentDirectory
     * @param fileList
     */
    private void javacProject(File buildPath, String currentDirectory, File[] fileList) {
        assert fileList != null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                if (currentDirectory.equals("") && file.getName().equals("out")) { continue; }
                File buildDir = new File(buildPath.toString() + "\\" + file.getName());
                System.out.println("mkdir to: " + buildDir);
                buildDir.mkdirs();
                javacProject(buildPath, "\\" + file.getName(), Objects.requireNonNull(file.listFiles()));
            }
            else {
                String fileExtension = "";
                int lastIndexOf = file.getName().lastIndexOf(".");
                if (lastIndexOf != -1) {
                    fileExtension = file.getName().substring(lastIndexOf).toLowerCase();
                }

                String destination = buildPath.toString() + currentDirectory + "\\" + file.getName();

                try {
                    // if file is a java file, compile java file into production folder
                    if (fileExtension.equals(".java")) {
                        String command = "javac " +
                            "\"" + file.getAbsolutePath() + "\"" +
                            " -d \"" + buildPath.toString() + currentDirectory + "\"";
                        System.out.println("javac to: " + destination);
                        System.out.println("\tUsing command: " + command);
                        Runtime.getRuntime().exec(command);
                    } else {
                        System.out.println("copy to: " + destination);
                        File fileDestination = new File(destination);
                        Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(fileDestination.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                    }

                }
                catch (Exception ignored) { }
            }
        }
    }

    /**
     * Attempts to go into the production folder of the current directory and then executes the code using command
     * prompt. If production folder does not exist, it will call <code>compileProject(...)</code> and then attempt to
     * execute the compiled project again.
     *
     * @return Returns true if project was able to be executed, false otherwise.
     */
    public boolean executeProject(File projectDirectory, boolean debug) {

        File productionFolder = new File(projectDirectory.getAbsolutePath() + "\\out\\production\\" + projectDirectory.getName());
        File mainConfigFile = new File(projectDirectory.getAbsolutePath() + "\\out\\production\\" + projectDirectory.getName() + "\\main.config");
        String mainClass = FileHelper.readFile(mainConfigFile);
        File classpathFile = new File(projectDirectory.getAbsolutePath() + "\\out\\production\\" + projectDirectory.getName() + "\\classpath.config");
        String classpath = FileHelper.readFile(classpathFile);

        System.out.println("Executing project in: " + productionFolder.getAbsolutePath());
        System.out.println("Loading main.config at: " + mainConfigFile.getAbsolutePath());
        System.out.println("Main class: " + mainClass);
        System.out.println("Class path: " + classpath);

        System.out.println("Copy to: " + productionFolder.getAbsolutePath() + "\\" + classpath + "\\" + "CustomClassLoader.class");
        System.out.println("Copy to: " + productionFolder.getAbsolutePath() + "\\" + classpath + "\\" + "ClassLoaderRunner.class");

        try {
            Path CCLPath = Paths.get(productionFolder.getAbsolutePath() + "\\" + classpath + "\\" + "CustomClassLoader.class");
            Path CLRunnerPath = Paths.get(productionFolder.getAbsolutePath() + "\\" + classpath + "\\" + "ClassLoaderRunner.class");
            InputStream iStreamCCL = this.getClass().getClassLoader().getResourceAsStream("CustomClassLoader.class");
            InputStream iStreamCLRunner = this.getClass().getClassLoader().getResourceAsStream("ClassLoaderRunner.class");
            Files.copy(iStreamCCL, CCLPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(iStreamCLRunner, CLRunnerPath, StandardCopyOption.REPLACE_EXISTING);

            String[] command = {
                "cmd.exe", "/k", "start", "cmd", "/k",
                "java", "-cp",
                ("\"" + productionFolder.getAbsolutePath() + "\\" + classpath + "\""), mainClass
            };
            if (debug) {
                command = new String[]{
                    "cmd.exe", "/k", "start", "cmd", "/k",
                    "java", "-cp",
                    ("\"" + productionFolder.getAbsolutePath() + "\\" + classpath + "\""),
                    "ClassLoaderRunner", mainClass
                };
            }
            System.out.println("Executing with command: " + Arrays.toString(command));

            // creates a new console window
            //Console console = new Console();
            //console.start("Console Output");

            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            //console.writeLine(Arrays.toString(command));
            /*
            BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bReader.readLine()) != null) {
                //console.writeLine(line);
            }
            bReader.close();
            */
            return true;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
