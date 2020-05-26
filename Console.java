package bs.code;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Console {
    private Stage consoleWindowStage;
    private ConsoleController controller;

    public void start(String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ConsoleWindow.fxml"));
            Parent parent = loader.load();
            this.controller = loader.getController();
            this.consoleWindowStage = new Stage();
            this.consoleWindowStage.setScene(new Scene(parent));
            this.consoleWindowStage.setTitle(title);
            this.consoleWindowStage.show();
        } catch (Exception ignored) { }
    }

    public void writeLine(String line) {
        controller.writeLine(line);
    }

    public void write(char ch) {
        controller.write(Character.toString(ch));
    }
}
