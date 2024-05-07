package console.consolas;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static java.nio.file.Files.readAllLines;

public class Main extends Application { //TODO: Clean up all these variables
    TextArea textArea = new TextArea();
    String preInput = "";
    String noteContent;
    String[] noteContentSplit;
    String[] inputSplit;
    String[] lines = new String[0];
    String state = "sol";
    String savedState;  //Used to remember previous state
    String correctPass;
    String input;
    String usernameInput;
    String currentUsername;
    String currentPass;
    String unconfirmedUser;
    String unconfirmedPass;
    String notesFolderPath = "src/main/resources/console/consolas/userData/account";
    String currentNotePath;
    String userFilePath = "src/main/resources/console/consolas/userData/account";
    String[] noteOptions = new String[8];       //TODO: Increase this random number
    String[] noteOptionPaths = new String [noteOptions.length];
    String noteOptionsFused = "";
    Path pathCommands = Paths.get("src/main/resources/console/consolas/commands.txt");
    Path pathTitle = Paths.get("src/main/resources/console/consolas/title.txt");
    boolean usernameInUse = false;
    boolean usernameExists = false;
    boolean firstNoteOption = true;
    boolean signedIn = false;
    boolean fullScreen = true;         //turn this off when debugging
    boolean firstNoteLine = true;
    boolean notesExist = false;
    boolean noteNameUnavailable = false;
    int accountNumber;
    int noteEdited;
    Path userPath = java.nio.file.Paths.get("src/main/resources/console/consolas/accountData/usernames.txt");
    Path passPath = java.nio.file.Paths.get("src/main/resources/console/consolas/accountData/passwords.txt");

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
        choice("Do you want to Sign up or Log in?", "Sign Up; Log In");

        textArea.setOnKeyPressed(ke -> {            //TODO: replace else-ifs with switch
            if (Objects.requireNonNull(ke.getCode()) == KeyCode.ENTER) {
                preInput = textArea.getText();
                lines = preInput.split("\n");
                if (lines.length >= 1) {
                    input = lines[lines.length - 1];
                    managingState(primaryStage);
                }
            } else if (Objects.requireNonNull(ke.getCode()) == KeyCode.F11) {
                fullScreen = !fullScreen;
                primaryStage.setFullScreen(fullScreen);
            } else if (Objects.requireNonNull(ke.getCode()) == KeyCode.ALT_GRAPH) {
                System.out.println(preInput);
            } else if (Objects.requireNonNull(ke.getCode()) == KeyCode.ALT) {
                if (Objects.equals(state, "editingNote")) {
                    firstNoteLine = true;
                    try {
                        var noteWriter = Files.newBufferedWriter(Paths.get(currentNotePath));
                        noteContent = textArea.getText();
                        noteContentSplit = noteContent.split("\n");
                        for (String noteContentSplitLine : noteContentSplit) {
                            if (!firstNoteLine) {
                                noteWriter.write("\n");
                            }
                            if (!Objects.equals(noteContentSplitLine, "-back")) {
                                noteWriter.write(noteContentSplitLine);
                            }
                            firstNoteLine = false;
                        }
                        noteWriter.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    clear(true);
                }
            }
        });
    }
    public void createFiles() {
        userFilePath += accountNumber;
        new File(userFilePath).mkdirs();
        userFilePath += "/notes";
        new File(userFilePath).mkdirs();
        userFilePath = "src/main/resources/console/consolas/userData/account";
        state = "home";
        signedIn = true;
        clear(true);
    }
    public void managingState(Stage primaryStage) {
        switch (input) {
            case "-ba":
            case "-back":
                if (signedIn) {
                    savedState = "home";
                    clear(true);
                } else {
                    savedState = "sol";
                    clear(false);
                    choice("Do you want to Sign up or Log in?", "Sign Up; Log In");
                }
                state = "none";
                break;
        }
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
                    choice("Are you sure you want to close the program?", "Close it; Go back");
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
                    state = "logout";
                    choice("Are you sure you want to log out?", "Yes; No");
                    break;
                case "fu":
                case "full":
                case "fullscreen":
                    fullScreen = !fullScreen;
                    primaryStage.setFullScreen(fullScreen);
                    break;
                case "us":
                case "user":
                case "username":
                    try (Stream<String> lines = Files.lines(userPath)) {
                        currentUsername = lines.skip(accountNumber - 1).findFirst().get();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (inputSplit.length > 1) {
                        switch (inputSplit[1]) {
                            case "vi":
                            case "view":
                                say("Your current username is: '" + currentUsername + "'");
                                break;
                            case "ch":
                            case "change":
                                say("New Username:");
                                state = "userChange";
                                break;
                            default:
                                say("Unknown Command!");
                        }
                    } else {
                        state = "userDefault";
                        currentUsername = "Your current username is: '" + currentUsername + "', do you want to change it?";
                        choice(currentUsername, "Change it; Go back");
                    }
                    break;
                case "pa":
                case "pass":
                case "password":
                    try (Stream<String> lines = Files.lines(passPath)) {
                        currentPass = lines.skip(accountNumber - 1).findFirst().get();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (inputSplit.length > 1) {
                        switch (inputSplit[1]) {
                            case "vi":
                            case "view":
                                say("Your current password is: '" + currentPass + "'");
                                break;
                            case "ch":
                            case "change":
                                say("New Password:");
                                state = "passChange";
                                break;
                            default:
                                say("Unknown Command!");
                        }
                    } else {
                        state = "passDefault";
                        choice("Do you want to view or edit your password?", "View; Change; Go back");
                    }
                    break;
                case "cl":
                case "clear":
                    clear(true);
                    break;
                case "no":
                case "note":
                case "notes":
                    Arrays.fill(noteOptions, null);
                    notesFolderPath = "src/main/resources/console/consolas/userData/account";
                    notesFolderPath += accountNumber;
                    notesFolderPath += "/notes";
                    File folder = new File(notesFolderPath);
                    File[] listOfFiles = folder.listFiles();
                    if (listOfFiles != null) {
                        for (int x = 0; x < listOfFiles.length; x++) {
                            if (listOfFiles[x].isFile()) {
                                noteOptions[x] = (listOfFiles[x].getName());
                            }
                        }
                    }
                    for (int x = 0; x < noteOptions.length; x++) {
                        noteOptionPaths[x] = notesFolderPath + "/";
                        noteOptionPaths[x] += noteOptions[x];
                    }
                    for (int x = 0; x < noteOptions.length; x++) {
                        if (Objects.equals(noteOptions[x], null)) {
                            noteOptions[x] = "-Empty Slot-";
                        } else {
                            notesExist = true;
                        }
                    }
                    if (inputSplit.length > 1) {
                        switch (inputSplit[1]) {
                            case "ed":
                            case "edit":
                                for (int x = 0; x < noteOptions.length; x++) {
                                    if (Objects.equals(noteOptions[x], null)) {
                                        noteOptions[x] = "-Create New Note-";
                                    }
                                }
                                firstNoteOption = true;
                                noteOptionsFused = "";
                                for (String option : noteOptions) {
                                    if (!firstNoteOption) {
                                        noteOptionsFused += "; ";
                                    }
                                    noteOptionsFused += option;
                                    firstNoteOption = false;
                                }
                                noteOptionsFused += "; Go back";
                                choice("What note do you want to edit?", noteOptionsFused);
                                say("Tip: In order to stop editing the file, press 'ALT'!");
                                notesFolderPath = "src/main/resources/console/consolas/userData/account";
                                state = "notesOptions";
                                break;
                            case "de":
                            case "del":
                            case "delete":
                                if (notesExist) {
                                    firstNoteOption = true;
                                    noteOptionsFused = "";
                                    for (String option : noteOptions) {
                                        if (!firstNoteOption) {
                                            noteOptionsFused += "; ";
                                        }
                                        noteOptionsFused += option;
                                        firstNoteOption = false;
                                    }
                                    noteOptionsFused += "; Go back";
                                    choice("What note do you want to delete?", noteOptionsFused);
                                    state = "deleteNote";
                                    notesExist = false;
                                } else {
                                    say("No notes have been found");
                                    state = "home";
                                }
                                break;
                            default:
                                say("Unknown Command!");
                        }
                    } else {
                        choice("Do you want to edit or delete a note?", "Edit; Delete; Go back");
                        state = "noteDefault";
                    }
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
    public void none(Stage primaryStage) {
        state = savedState;
    }

    //Commands (States)
    public void exit(Stage primaryStage) {
        switch (input) {
            case "1":
                System.exit(0);
                break;
            case "2":
                state = "home";
                break;
            default:
                say("Please type a number from 1-2!");
        }
    }           //TODO: check if Stage primaryStage is really necessary
    public void logout(Stage primaryStage) {
        switch (input) {
            case "1":
                state = "sol";
                signedIn = false;
                clear(false);
                choice("Do you want to Sign up or Log in?", "Sign Up; Log In");
                break;
            case "2":
                state = "home";
                break;
            default:
                say("Please type a number from 1-2!");
        }
    }
    public void signUpUs(Stage primaryStage) {
        if (dataReqs(input, "Username")) {
            state = "signUpPass";
            usernameInput = input;
            say("Password:");
        } else {
            usernameInUse = false;
            say("Username:");
        }
    }
    public void signUpPass(Stage primaryStage) {
        if (dataReqs(input, "Password")) {
            List<String> linesPass;
            try {
                accountNumber = 0;
                for (String line : readAllLines(userPath)) {
                    accountNumber++;
                    if (Objects.equals(line, "-")) {
                        List<String> linesUser = Files.readAllLines(userPath, StandardCharsets.UTF_8);
                        linesUser.set(accountNumber - 1, usernameInput);
                        Files.write(userPath, linesUser, StandardCharsets.UTF_8);
                        break;
                    }
                }
                linesPass = Files.readAllLines(passPath, StandardCharsets.UTF_8);
                linesPass.set(accountNumber - 1, input);
                Files.write(passPath, linesPass, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            createFiles();
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
            signedIn = true;
            clear(true);
        } else {
            say("Wrong password");
            say("Password:");
        }
    }
    public void userDefault(Stage primaryStage) {
        switch (input) {
            case "1":
                say("New Username:");
                state = "userChange";
                break;
            case "2":
                state = "home";
                break;
            default:
                say("Please type a number from 1-2!");
        }
    }
    public void userChange(Stage primaryStage) {
        unconfirmedUser = input;
        if (!Objects.equals(unconfirmedUser, currentUsername)) {
            if (dataReqs(unconfirmedUser, "Username")) {
                choice("Do you want to change your username to '" + unconfirmedUser + "'?", "Yes; No");
                state = "userConfirm";
            } else {
                usernameInUse = false;
                say("New Username:");
            }
        } else {
            say("New Username cannot be your old Username" + "\nNew Username:");
        }
    }
    public void userConfirm(Stage primaryStage) {
        switch (input) {
            case "1":
                try {
                    List<String> linesUser = Files.readAllLines(userPath, StandardCharsets.UTF_8);
                    linesUser.set(accountNumber - 1, unconfirmedUser);
                    Files.write(userPath, linesUser, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                clear(false);
                say("Username successfully changed" + "\nType 'help' for commands");
                state = "home";
                break;
            case "2":
                state = "home";
                clear(true);
                break;
            default:
                say("Please type a number from 1-2!");
        }
    }
    public void passDefault(Stage primaryStage) {
        switch (input) {
            case "1":
                say("Your current password is: '" + currentPass + "'");
                state = "home";
                break;
            case "2":
                say("New Password:");
                state = "passChange";
                break;
            case "3":
                state = "home";
                break;
            default:
                say("Please type a number from 1-3!");
        }
    }
    public void passChange(Stage primaryStage) {
        unconfirmedPass = input;
        if (!Objects.equals(unconfirmedPass, currentPass)) {
            if (dataReqs(unconfirmedPass, "Password")) {
                choice("Do you want to change your password to '" + unconfirmedPass + "'?", "Yes; No");
                state = "passConfirm";
            } else {
                say("New Password:");
            }
        } else {
            say("New Password cannot be your old Password" + "\nNew Password:");
        }
    }
    public void passConfirm(Stage primaryStage) {
        switch (input) {
            case "1":
                try {
                    List<String> linesPass = Files.readAllLines(passPath, StandardCharsets.UTF_8);
                    linesPass.set(accountNumber - 1, unconfirmedPass);
                    Files.write(passPath, linesPass, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                clear(false);
                title();
                say("Password successfully changed" + "\nType 'help' for commands");
                state = "home";
                break;
            case "2":
                state = "home";
                clear(true);
                break;
            default:
                say("Please type a number from 1-2!");
        }
    }
    public void noteDefault(Stage primaryStage) {
        switch (input) {
            case "1":
                for (int x = 0; x < noteOptions.length; x++) {
                    if (Objects.equals(noteOptions[x], null)) {
                        noteOptions[x] = "-Create New Note-";
                    }
                }
                firstNoteOption = true;
                noteOptionsFused = "";
                for (String option : noteOptions) {
                    if (!firstNoteOption) {
                        noteOptionsFused += "; ";
                    }
                    noteOptionsFused += option;
                    firstNoteOption = false;
                }
                noteOptionsFused += "; Go back";
                choice("What note do you want to edit?", noteOptionsFused);
                say("Tip: In order to stop editing the file, press 'ALT'!");
                notesFolderPath = "src/main/resources/console/consolas/userData/account";
                state = "notesOptions";
                break;
            case "2":
                if (notesExist) {
                    firstNoteOption = true;
                    noteOptionsFused = "";
                    for (String option : noteOptions) {
                        if (!firstNoteOption) {
                            noteOptionsFused += "; ";
                        }
                        noteOptionsFused += option;
                        firstNoteOption = false;
                    }
                    noteOptionsFused += "; Go back";
                    choice("What note do you want to delete?", noteOptionsFused);
                    state = "deleteNote";
                    notesExist = false;
                } else {
                    say("No notes have been found");
                    state = "home";
                }
                break;
            case "3":
                state = "home";
                break;
            default:
                say("Please type a number from 1-3!");
        }
    }
    public void notesOptions(Stage primaryStage) {
        if (input.matches("[0-9]+")) {
            if (Integer.parseInt(input) >= 1 && Integer.parseInt(input) <= noteOptions.length + 1) {
                if (Integer.parseInt(input) == 9) {
                    state = "home";
                } else {
                    noteEdited = Integer.parseInt(input) - 1;
                    currentNotePath = noteOptionPaths[noteEdited];
                    if (new File(currentNotePath).exists()) {
                        clear(false);
                        try {
                            for (String line : readAllLines(Paths.get(currentNotePath))) {
                                say(line);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        state = "editingNote";
                    } else {
                        say("Choose a name for the new note:\n(Tip: Exclude .txt)");
                        state = "newNote";
                    }
                }
            } else {
                say("Please type a number from 1-" + (noteOptions.length + 1) + "!");
            }
        } else {
            say("Please type a number from 1-" + (noteOptions.length + 1) + "!");
        }
    }
    public void editingNote(Stage primaryStage) {}      //TODO: change this to universal method or check if necessary
    public void deleteNote(Stage primaryStage) {
        if (input.matches("[0-9]+")) {
            if (Integer.parseInt(input) >= 1 && Integer.parseInt(input) <= noteOptions.length + 1) {
                if (Integer.parseInt(input) == 9) {
                    state = "home";
                } else {
                    noteEdited = Integer.parseInt(input) - 1;
                    currentNotePath = noteOptionPaths[noteEdited];
                    File deleteNoteFile = new File(currentNotePath);
                    clear(false);
                    state = "home";
                    title();
                    if (deleteNoteFile.delete()) {
                        say("Note successfully deleted!");
                    } else {
                        say("You cannot delete non-existent notes!");
                    }
                    notesFolderPath = "src/main/resources/console/consolas/userData/account";
                }
            } else {
                say("Please type a number from 1-" + (noteOptions.length + 1) + "!");
            }
        } else {
            say("Please type a number from 1-" + (noteOptions.length + 1) + "!");
        }
    }
    public void newNote(Stage primaryStage) {
        noteNameUnavailable = false;
        if (!Objects.equals(input, "(Tip: Exclude .txt)") && !Objects.equals(input, "(Tip: Exclude .txt)")) {
            if (!input.contains(" ")) {
                for (String noteOption : noteOptions) {
                    if (Objects.equals(input + ".txt", noteOption)) {
                        noteNameUnavailable = true;
                        break;
                    }
                }
                if (noteNameUnavailable) {
                    say("This name is already in use! Choose a different one:");
                } else {
                    notesFolderPath += accountNumber;
                    notesFolderPath += "/notes/";
                    currentNotePath = notesFolderPath += input;
                    currentNotePath += ".txt";
                    File noteFile = new File(currentNotePath);
                    try {
                        noteFile.createNewFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    clear(false);
                    try {
                        for (String line : readAllLines(Paths.get(currentNotePath))) {
                            say(line);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    state = "editingNote";
                    notesFolderPath = "src/main/resources/console/consolas/userData/account";
                }
            } else {
                say("Name cannot contain spaces!");
                say("Choose a name for the new note:\n(Tip: Exclude .txt)");
            }
        } else {
            say("Name cannot be empty!");
            say("Choose a name for the new note:\n(Tip: Exclude .txt)");
        }
    }

    //Utilities
    void say(String text) {
        textArea.appendText(text + "\n");
    }
    void choice(String question, String options) {
        int optionCounter = 0;
        String[] optionsSplit = options.split("; ");
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
            title();
            say("Type 'help' for commands");
        }
    }
    void title() {
        try {
            for (String line : Files.readAllLines(pathTitle)) {
                say(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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


