package bs.code;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ConsoleController {
    @FXML
    private TextArea outputText;

    public void writeLine(String line) {
        outputText.appendText(line + "\n");
    }

    public void write(String string) {
        outputText.appendText(string);
    }
}
