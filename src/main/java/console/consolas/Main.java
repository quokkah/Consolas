package console.consolas;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static java.nio.file.Files.readAllLines;

public class Main extends Application { //TODO: Clean up all these variables
    TextArea textArea = new TextArea();
    String preInput = "";
    String[] inputSplit;
    String[] lines = new String[0];
    String state = "sol";
    String correctPass;
    String input;
    Path pathCommands = Paths.get("src/main/resources/console/consolas/commands.txt");
    boolean usernameInUse = false;
    boolean usernameExists = false;
    boolean fullScreen = false;         //turn this off when debugging
    int accountNumber;
    Path userPath = java.nio.file.Paths.get("src/main/resources/console/consolas/userData/usernames.txt");
    Path passPath = java.nio.file.Paths.get("src/main/resources/console/consolas/userData/passwords.txt");

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Font font = Font.loadFont("file:src/main/resources/console/consolas/fonts/CONSOLA.TTF", 20);
        textArea.setFont(font);
        StackPane.setAlignment(textArea, Pos.TOP_LEFT);
        root.getChildren().add(textArea);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("stylesheet.css")).toExternalForm());
        primaryStage.setTitle("Consolas");
        primaryStage.setScene(scene);
        primaryStage.setX(0);
        primaryStage.setY(0);
        primaryStage.setFullScreen(fullScreen);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();
        choice("Do you want to Sign up or Log in?", "Sign Up, Log In");

        textArea.setOnKeyPressed(ke -> {
            if (Objects.requireNonNull(ke.getCode()) == KeyCode.ENTER) {
                preInput = textArea.getText();
                lines = preInput.split("\n");
                if (lines.length >= 1) {
                    input = lines[lines.length - 1];
                    managingInput(primaryStage);
                }
            } else if (Objects.requireNonNull(ke.getCode()) == KeyCode.F11) {
                fullScreen = !fullScreen;
                primaryStage.setFullScreen(fullScreen);
            }
        });
    }
    public void managingInput(Stage primaryStage) {
        try {
            Method method = this.getClass().getMethod(state, Stage.class);
            method.invoke(this, primaryStage);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.out.println("Unknown State!");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    //Main States
    public void home(Stage primaryStage) {
        inputSplit = input.split(" ");
        if (inputSplit.length > 0) {
            switch (inputSplit[0]) {
                case "ex":
                case "exit":
                    state = "exit";
                    choice("Are you sure you want to close the program?", "Yes, No");
                    break;
                case "he":
                case "help":
                    try {
                        for (String line : Files.readAllLines(pathCommands)) {
                            say(line);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "lo":
                case "log":
                case "log out":
                case "logout":
                    state = "sol";
                    clear(false);
                    choice("Do you want to Sign up or Log in?", "Sign Up, Log In");
                    break;
                case "fu":
                case "full":
                case "fullscreen":
                    fullScreen = !fullScreen;
                    primaryStage.setFullScreen(fullScreen);
                    break;
                default:
                    say("Unknown Command!");
            }
        } else {
            say("Unknown Command!");
        }
    }
    public void sol(Stage primaryStage) {
        switch (input) {
            case "1":
                state = "signUpUs";
                say("Username:");
                break;
            case "2":
                state = "logInUs";
                say("Username:");
                break;
            default:
                say("Please type a number from 1-2!");
        }
    }       //SignUp or LogIn

    //Commands (States)
    public void exit(Stage primaryStage) {
        switch (input) {
            case "1":
                System.exit(0);
                break;
            case "2":
                clear(true);
                break;
            default:
                say("Please type a number from 1-2!");
        }
    }
    public void signUpUs(Stage primaryStage) {
        if (dataReqs(input, "Username")) {
            try {
                for (String line : readAllLines(userPath)) {
                    accountNumber++;
                    if (Objects.equals(line, "-")) {
                        List<String> linesUser = Files.readAllLines(userPath, StandardCharsets.UTF_8);
                        linesUser.set(accountNumber - 1, input);
                        Files.write(userPath, linesUser, StandardCharsets.UTF_8);
                        break;
                    }
                }
                state = "signUpPass";
                say("Password:");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            usernameInUse = false;
            say("Username:");
        }
    }
    public void signUpPass(Stage primaryStage) {
        if (dataReqs(input, "Password")) {
            List<String> linesPass;
            try {
                linesPass = Files.readAllLines(passPath, StandardCharsets.UTF_8);
                linesPass.set(accountNumber - 1, input);
                Files.write(passPath, linesPass, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            state = "home";
            clear(true);
        } else {
            signUpPass(primaryStage);
        }
    }
    public void logInUs(Stage primaryStage) {
        usernameExists = false;
        accountNumber = 0;
        try {
            for (String line : readAllLines(userPath)) {
                accountNumber++;
                if (Objects.equals(line, input)) {
                    usernameExists = true;
                    break;
                }
            }
            if (usernameExists) {
                try (Stream<String> lines = Files.lines(passPath)) {
                    correctPass = lines.skip(accountNumber - 1).findFirst().get();
                    state = "logInPass";
                    say("Password:");
                }
            } else {
                say("Username not found");
                say("Username:");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void logInPass(Stage primaryStage) {
        if (Objects.equals(input, correctPass)) {
            state = "home";
            clear(true);
        } else {
            say("Wrong password");
            say("Password:");
        }
    }


    //Utilities
    void say(String text) {
        textArea.appendText(text + "\n");
    }
    void choice(String question, String options) {
        int optionCounter = 0;
        String[] optionsSplit = options.split(", ");
        say(question);
        for (String option : optionsSplit) {
            optionCounter++;
            say("[" + optionCounter + "] " + option);
        }
    }
    void clear(boolean resetState) {
        textArea.clear();
        if (resetState) {
            state = "home";
            say("Type 'help' for commands");
        }
    }
    boolean dataReqs(String input, String passOrUser) {
        if (input.startsWith("-")) {
            say(passOrUser + " cannot start with '-'");
            return false;
        } else if (Objects.equals(input, "Username:")) {
            say(passOrUser + " cannot be empty or be called 'Username:'");
            return false;
        } else if (input.contains(" ")) {
            say(passOrUser + " cannot contain spaces");
            return false;
        } else {
            if (Objects.equals(passOrUser, "Password")) {
                return true;
            } else {
                try {
                    for (String line : readAllLines(userPath)) {
                        if (Objects.equals(line, input)) {
                            say("Username already in use");
                            usernameInUse = true;
                            break;
                        }
                    }
                    return !usernameInUse;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
