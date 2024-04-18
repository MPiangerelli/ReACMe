package grafo.view;

import grafo.comparators.DictionaryComparator;
import grafo.comparators.JaccardSimilarity;
import grafo.comparators.RepeatingSimilarity;
import grafo.config.RunProperties;
import grafo.controllers.LogsDictionaryController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class ViewController implements Initializable {


    @FXML
    public TextArea _consoleOutput;
    @FXML
    private Label _xesFiles;
    @FXML
    private TextField _gammaID;
    @FXML
    private ChoiceBox<String> _changeScoreID = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> _changeComparator = new ChoiceBox<>();


    @FXML
    private TextField _activityEqualScoreID;
    @FXML
    private TextField _activityNotEqualScoreID;
    @FXML
    private TextField _activitySemiEqualScoreID;
    @FXML
    private TextField _gramsEqualScoreID;
    @FXML
    private TextField _gramsNotEqualScoreID;
    @FXML
    private TextField _gramsSemiEqualScoreID;
    @FXML
    private TextField _nGramID;


    private final File outputDir = new File("./output/");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initData();
        PrintStream out = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                _consoleOutput.appendText(String.valueOf((char) b));
            }
        });
        System.setOut(out);
    }

    private void initData() {
        _changeScoreID.getItems().addAll("No", "Yes");
        _changeComparator.getItems().addAll("Repeating", "Jaccard");
        _changeScoreID.setValue("No");
        _changeComparator.setValue("Jaccard");
        _consoleOutput.setEditable(false);
    }

    public static List<Path> checkExtension(Path path, String xesExtension) {
        List<Path> result = null;
        try (Stream<Path> files = Files.walk(path)) {
            result = files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(xesExtension))
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* REMINDER --> QUESTA ANNOTAZIONE, PERMETTE DI IGNORARE GLI WARNINGS. IN QUESTO CASO ESSI SONO RELATIVI AL
     *          Dereference of '_xesDirectory.listFiles()' may produce 'NullPointerException'
     */
    @SuppressWarnings("ConstantConditions")
    public void loadDirectory() {
        File _xesDirectory;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select XES Files Directory");
        directoryChooser.setInitialDirectory(new java.io.File("."));

        _xesDirectory = directoryChooser.showDialog(null);


        List<Path> allXesFiles = checkExtension(Paths.get(_xesDirectory.getAbsolutePath()), ".xes");
        if (allXesFiles.size() <= 2) {
            System.exit(99);
        } else {
            _xesFiles.setText(_xesDirectory.getAbsolutePath());
        }

    }

    public void changeScore() {
        if (_changeScoreID.getValue().equals("Yes")) {
            _activityEqualScoreID.setDisable(false);
            _activityNotEqualScoreID.setDisable(false);
            _activitySemiEqualScoreID.setDisable(false);
            _gramsEqualScoreID.setDisable(false);
            _gramsNotEqualScoreID.setDisable(false);
            _gramsSemiEqualScoreID.setDisable(false);
            _nGramID.setDisable(false);
        } else {
            _activityEqualScoreID.setDisable(true);
            _activityNotEqualScoreID.setDisable(true);
            _activitySemiEqualScoreID.setDisable(true);
            _gramsEqualScoreID.setDisable(true);
            _gramsNotEqualScoreID.setDisable(true);
            _gramsSemiEqualScoreID.setDisable(true);
            _nGramID.setDisable(true);
        }
    }

    private void resetOutputDir() {
        try {
            if (!isOutputDirEmpty()) {
                deleteFiles();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isOutputDirEmpty() throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(outputDir.getPath()))) {
            return !dirStream.iterator().hasNext();
        }
    }

    private void deleteFiles() throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(outputDir.getPath()))) {
            dirStream.forEach(p -> p.toFile().delete());
        }
        isOutputDirEmpty();
    }

    public void runMining() throws Exception {
        if (outputDir.exists()) {
            resetOutputDir();
            if (!isOutputDirEmpty()) {
                deleteFiles();
            }
        }
        if (checkInputValues()) {
            startMining();
        } else {
            System.err.println("Invalid inputs");
        }
    }


    /**
     * Questo metodo permette di controllare la validità di tutti i valori inseriti: gamma, score, ngram.
     *
     * @return true se tutti i valori in input sono validi.
     */
    private boolean checkInputValues() {
        if (_changeScoreID.getValue().equals("Yes")) {
            return visualValidate(_xesFiles) && visualValidate(_gammaID) && visualValidate(_activityEqualScoreID) && visualValidate(_activityNotEqualScoreID) && visualValidate(_activitySemiEqualScoreID)
                    && visualValidate(_gramsEqualScoreID) && visualValidate(_gramsNotEqualScoreID) && visualValidate(_gramsSemiEqualScoreID) && validateVisualNGram(_nGramID);
        } else {
            return visualValidate(_gammaID) && visualValidate(_xesFiles);
        }
    }


    /**
     * Questo metodo permette di validare l'input di gamma ed eventualmente degli score.
     * Un campo è valido se è un numero reale compreso tra 0 e 1.
     *
     * @param textField il campo da validare
     */
    private boolean visualValidate(TextField textField) {
        if (isValidNumber(textField) && !isNumberOutOfRange(textField)) {
            setValidStyle(textField);
            return true;
        } else {
            setInvalidStyle(textField);
            return false;
        }
    }

    /**
     * Questo metodo permette di validare l'input di gamma ed eventualmente degli score.
     * Un campo è valido se è un numero reale compreso tra 0 e 1.
     *
     * @param label il campo da validare
     */
    private boolean visualValidate(Label label) {
        if (!label.getText().equals("No .xes files loaded")) {
            setValidStyle(label);
            return true;
        } else {
            setInvalidStyle(label);
            return false;
        }
    }

    /**
     * Questo metodo serve per togliere le propriet&agrave; applicate con il metodo <code>setInvalidStyle</code>
     *
     * @param textField field al quale applicare lo stile
     */
    private void setValidStyle(TextField textField) {
        textField.setStyle("-fx-border-color: transparent ; -fx-border-width: 0px ;");
    }

    /**
     * Questo metodo serve per applicare uno stile di errore in modo da aiutare l'utente a capire qual'&egrave; il
     * field che &egrave; risultato invalido
     *
     * @param textField field al quale applicare lo stile
     */
    private void setInvalidStyle(TextField textField) {
        textField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
    }

    /**
     * Questo metodo serve per togliere le propriet&agrave; applicate con il metodo <code>setInvalidStyle</code>
     *
     * @param label field al quale applicare lo stile
     */
    private void setValidStyle(Label label) {
        label.setStyle("-fx-text-fill: black;");
    }

    /**
     * Questo metodo serve per applicare uno stile di errore in modo da aiutare l'utente a capire qual'&egrave; il
     * field che &egrave; risultato invalido
     *
     * @param label al quale applicare lo stile
     */
    private void setInvalidStyle(Label label) {
        label.setStyle("-fx-text-fill: black;");
    }


    /**
     * Questo metodo controlla se il contenuto del Field rispetta il formato numerico
     *
     * @param textField il field da controllare
     * @return <code>true</code> se valido, <code>false</code> altrimenti
     */
    private static boolean isValidNumber(TextField textField) {
        return textField.getText().matches("^[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?$");
    }

    /**
     * Questo metodo controlla che il numero rispetti il range.
     * Il range &egrave; definito da [0.0 - 1.0]
     *
     * @param textField field al quale controllare il valore
     * @return <code>true</code> se valido, <code>false</code> altrimenti
     */
    private static boolean isNumberOutOfRange(TextField textField) {
        return Double.parseDouble(textField.getText()) < 0 || Double.parseDouble(textField.getText()) > 1;
    }

    private boolean validateVisualNGram(TextField textField) {
        if (_nGramID.getText().matches("\\d*") && !_nGramID.getText().equals("")) {
            setValidStyle(textField);
            return true;
        } else {
            setInvalidStyle(textField);
            return false;
        }
    }

    private void startMining() {
        Locale.setDefault(Locale.ITALIAN);
        LogsDictionaryController logsDictionaryController = new LogsDictionaryController(setProcessMiningRunProperties(), _xesFiles.getText());
        logsDictionaryController.run();
    }

    private RunProperties setProcessMiningRunProperties() {

        var gamma = Double.parseDouble(_gammaID.getText());

        if (_changeScoreID.getValue().equals("Yes")) {
            var activityEqualScoreID = Double.parseDouble(_activityEqualScoreID.getText());
            var activityNotEqualScoreID = Double.parseDouble(_activityNotEqualScoreID.getText());
            var activitySemiEqualScoreID = Double.parseDouble(_activitySemiEqualScoreID.getText());
            var gramsEqualScoreID = Double.parseDouble(_gramsEqualScoreID.getText());
            var gramsNotEqualScoreID = Double.parseDouble(_gramsNotEqualScoreID.getText());
            var gramsSemiEqualScoreID = Double.parseDouble(_gramsSemiEqualScoreID.getText());
            var nGramID = Integer.parseInt(_nGramID.getText());
            RunProperties runProperties = new RunProperties(activityEqualScoreID, activityNotEqualScoreID, activitySemiEqualScoreID, gramsEqualScoreID, gramsNotEqualScoreID, gramsSemiEqualScoreID, gamma, nGramID);
            DictionaryComparator comparator;
            if (_changeComparator.getValue().equals("Jaccard")) {
                comparator = new JaccardSimilarity(runProperties);
            } else {
                comparator = new RepeatingSimilarity(runProperties);
            }
            runProperties.setComparator(comparator);

            return runProperties;
        } else {
            RunProperties runProperties = new RunProperties();
            DictionaryComparator comparator;
            if (_changeComparator.getValue().equals("Jaccard")) {
                comparator = new JaccardSimilarity(runProperties);
            } else {
                comparator = new RepeatingSimilarity(runProperties);
            }
            runProperties.setComparator(comparator);
            return runProperties;
        }
    }


    public void closeApplication() throws IOException {
        if (isOutputDirEmpty()) {
            Platform.exit();
        } else {
            try {
                Desktop.getDesktop().open(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Platform.exit();
        }
    }


    public void changeComparator(ActionEvent actionEvent) {
        // Ancora da fare
    }
}

