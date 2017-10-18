package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {

    @FXML
    private GridPane gridPane;
    @FXML
    private Slider slider;
    @FXML
    private Label tempoValue;
    @FXML
    private Button stop;
    @FXML
    private ToggleButton mute;
    @FXML
    private Button start;

    private MidiPlayer midiPlayer;

    public void initialize() {
        midiPlayer = new MidiPlayer() {
            @Override
            public void onNextStep(int i) {
                new Thread(() -> {
                    List<Node> nodeList = gridPane.getChildren()
                            .filtered(node -> GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == i + 1)
                            .stream().collect(Collectors.toList());
                        nodeList.forEach(node -> Platform.runLater(() -> node.setStyle("-fx-background-color: #004de6;")));
                        try {
                            Thread.sleep(7500 / getTempo());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nodeList.forEach(node -> Platform.runLater(() -> node.setStyle("-fx-background-color: transparent;")));
                }).start();
            }
        };

        createMidiPlayerButtons();
        setUpSlider();

        stop.setDisable(true);

        start.setOnAction(event -> {
            midiPlayer.start();
            start.setDisable(true);
            stop.setDisable(false);
        });
        stop.setOnAction(event -> {
            midiPlayer.stop();
            start.setDisable(false);
            stop.setDisable(true);
        });
        mute.setOnAction(event -> midiPlayer.setMute(!midiPlayer.getMute()));

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int actualTempo = newValue.intValue();
                midiPlayer.setTempo(actualTempo);
                tempoValue.setText(actualTempo + "");
        });



    }

    private void createMidiPlayerButtons() {
        for (int j = 1; j <= 32; j++) {
            addCheckBox(j, 0);
            addCheckBox(j, 1);
            addCheckBox(j, 2);
            addChoiceBox(j, 3);
        }
    }

    private void addCheckBox(int col, int row) {
        final int index = col - 1;

        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (row == 0) {
                midiPlayer.getHhNotes()[index] = newValue;
            } else if( row == 1) {
                midiPlayer.getKickNotes()[index] = newValue;
            } else if(row == 2) {
                midiPlayer.getSnareNotes()[index] = newValue;
            }
        });
        gridPane.add(checkBox, col, row);
    }

    private void addChoiceBox(int col, int row) {
        final int index = col - 1;

        ObservableList<Integer> observableList = FXCollections.observableList(Arrays.asList(createArray()));
        ChoiceBox<Integer> choiceBox = new ChoiceBox<>();
        choiceBox.setItems(observableList);
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> midiPlayer.getPianoNotes()[index] = newValue);
        gridPane.add(choiceBox, col, row);
    }

    private Integer[] createArray() {
        Integer[] array = new Integer[33];
        for (int i = 0; i < 32; i++) {
            array[i] = i;
        }
        array[32] = -1;
        return array;
    }

    private void setUpSlider() {
        slider.setMin(1);
        slider.setMax(240);
        slider.setValue(120);
        slider.setMajorTickUnit(119);
        slider.setMinorTickCount(10);
        slider.setBlockIncrement(10);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
    }
}
