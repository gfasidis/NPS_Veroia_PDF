package sample;

import com.gembox.spreadsheet.*;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"Duplicates", "SpellCheckingInspection", "FieldCanBeLocal"})
public class Main extends Application {

    private TableView endekadaTableView;
    private TableView anapliromatikoiTableView;

    private Button importPlayersBtn;
    private Button clearTablesBtn;

    private Button endekadaBtn;
    private Button anapliromatikoiBtn;

    private Label endekadaSelected;
    private int endekadaSelectedInt;

    private Label anapliromatikoiSelected;
    private int anapliromatikoiSelectedInt;

    private ArrayList<Player> endekadaPlayers;
    private ArrayList<Player> anapliroamtikoiPlayers;

    private ArrayList<Player> players;

    private TextField captainNumber;
    private String captainNo;

    private CheckBox homeCheckBox;
    private CheckBox awayCheckBox;
    private Button writeToPDF;

    private String[] columnNames;

    static {
        SpreadsheetInfo.setLicense("FREE-LIMITED-KEY");
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("NPS Veria 2019");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        columnNames = new String[]{"NUM","NAME","POS","CODE"};
        endekadaSelectedInt = 0;
        anapliromatikoiSelectedInt = 0;

        endekadaPlayers = new ArrayList<>();
        anapliroamtikoiPlayers = new ArrayList<>();

        homeCheckBox = new CheckBox();
        homeCheckBox = (CheckBox) primaryStage.getScene().lookup("#homeCheckBox");

        awayCheckBox = new CheckBox();
        awayCheckBox = (CheckBox) primaryStage.getScene().lookup("#awayCheckBox");

        writeToPDF = new Button();
        writeToPDF = (Button) primaryStage.getScene().lookup("#writeToPdf");
        writeToPDF.setDisable(true);
        writeToPDF.setOnAction(event -> {
            try {
                if (homeCheckBox.isSelected() && awayCheckBox.isSelected()){
                    alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Wrong Data","Δεν γίνεται να είναι επιλεγμένα και το home και το away. Παρακαλώ δοκιμάστε ξανά");
                    return;
                } else if (!homeCheckBox.isSelected() && !awayCheckBox.isSelected()){
                    alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Wrong Data","Πρέπει να είναι επιλεγμένα το home ή το away. Παρακαλώ δοκιμάστε ξανά");
                    return;
                }

                if (homeCheckBox.isSelected())
                    writeDataPDF("players_home.pdf");
                else if (awayCheckBox.isSelected())
                    writeDataPDF("players_away.pdf");

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        captainNumber = new TextField();
        captainNumber = (TextField) primaryStage.getScene().lookup("#captainNumber");
        captainNumber.setDisable(true);

        endekadaSelected = new Label();
        endekadaSelected = (Label) primaryStage.getScene().lookup("#endekadaSelected") ;
        endekadaTableView = new TableView();
        endekadaTableView = (TableView) primaryStage.getScene().lookup("#endekadaTable");
        endekadaTableView.setDisable(true);
        endekadaTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        endekadaTableView.setOnMousePressed(event -> {
            ObservableList<ObservableList<String>> selectedItems = endekadaTableView.getSelectionModel().getSelectedItems();

            ArrayList<String> selectedIDs = new ArrayList<>();
            for (ObservableList<String> row : selectedItems) {
                if (row.get(0) != null)
                    selectedIDs.add(row.get(0));
            }
            endekadaSelectedInt = selectedIDs.size();
            endekadaSelected.setText("Selected " + endekadaSelectedInt + "/11");

        });

        anapliromatikoiSelected = new Label();
        anapliromatikoiSelected = (Label) primaryStage.getScene().lookup("#anapliromatikoiSelected");
        anapliromatikoiTableView = new TableView();
        anapliromatikoiTableView = (TableView) primaryStage.getScene().lookup("#anaplaromatikoiTable");
        anapliromatikoiTableView.setDisable(true);
        anapliromatikoiTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        anapliromatikoiTableView.setOnMousePressed(event -> {
            ObservableList<ObservableList<String>> selectedItems = anapliromatikoiTableView.getSelectionModel().getSelectedItems();

            ArrayList<String> selectedIDs = new ArrayList<>();
            for (ObservableList<String> row : selectedItems) {
                if (row.get(0) != null)
                    selectedIDs.add(row.get(0));
            }
            anapliromatikoiSelectedInt = selectedIDs.size();
            anapliromatikoiSelected.setText("Selected " + anapliromatikoiSelectedInt + "/7");
        });

        importPlayersBtn = new Button();
        importPlayersBtn = (Button) primaryStage.getScene().lookup("#importPlayers");
        importPlayersBtn.setOnAction(event -> {
            try {
                loadData(primaryStage);
            } catch (IOException e) {
                alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Something goes wrong",e.getMessage());
            }
        });

        clearTablesBtn = new Button();
        clearTablesBtn = (Button) primaryStage.getScene().lookup("#clearTables");
        clearTablesBtn.setOnAction(event -> {
            clearTableData();
        });

        endekadaBtn = new Button();
        endekadaBtn = (Button) primaryStage.getScene().lookup("#entekadaBtn");
        endekadaBtn.setDisable(true);
        endekadaBtn.setOnAction(event -> {
            if (endekadaTableView.getItems() != null && endekadaSelectedInt == 11 && !captainNumber.getText().isEmpty()) {
                if (alertConfirm(endekadaTableView,"Έλεγχος 11αδας","Η 11αδα αποτελείται απο τους εξής:")){
                    captainNo = captainNumber.getText();
                    saveDataEndekada(endekadaTableView);
                }
            }
            else if (endekadaSelectedInt != 11)
                alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Empty Selection","Η βασική ενδεκάδα πρέπει να αποτελείται απο 11 ακριβώς παίκτες! Προσπάθησε ξανά!");
            else if (captainNumber.getText().isEmpty())
                alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Empty Selection","Δεν έχει δηλωθεί αρχηγός! Προσπάθησε ξανά!");
            else
                alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Empty Table","Δεν υπάρχουν παίκτες! Προσπάθησε ξανά εισάγωντας το αρχείο του excel!");
        });

        anapliromatikoiBtn = new Button();
        anapliromatikoiBtn = (Button) primaryStage.getScene().lookup("#anapliromatikoiBtn");
        anapliromatikoiBtn.setDisable(true);
        anapliromatikoiBtn.setOnAction(event -> {
            if (anapliromatikoiTableView.getItems() != null && anapliromatikoiSelectedInt != 0) {
                if (alertConfirm(anapliromatikoiTableView,"Έλεγχος αναπληρωματικών","Ο πάγκος αποτελείται απο τους εξής:"))
                    saveDataAnapliromatikoi(anapliromatikoiTableView);
            }
            else {
                alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Empty Table","Δεν υπάρχουν παίκτες! Προσπάθησε ξανά εισάγωντας το αρχείο του excel!");
            }
        });
    }

    private void clearTableData() {
        endekadaTableView.getColumns().clear();
        anapliromatikoiTableView.getColumns().clear();

        endekadaPlayers.clear();
        anapliroamtikoiPlayers.clear();

        anapliromatikoiTableView.setDisable(true);
        anapliromatikoiBtn.setDisable(true);

        endekadaTableView.setDisable(true);
        endekadaBtn.setDisable(true);
        captainNumber.setDisable(true);
    }

    private boolean alertConfirm(TableView tableLocal, String header, String labelText){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(header);
        alert.setContentText("Αν είστε σίγουροι για τις επιλογές σας συνεχίστε!");

        StringBuilder tempString = new StringBuilder();
        ObservableList<ObservableList<String>> selectedItems = tableLocal.getSelectionModel().getSelectedItems();
        ArrayList<Player> tempArray = new ArrayList<>();

        for (ObservableList<String> row : selectedItems) {
            tempArray.add(new Player(row.get(0),row.get(1),row.get(2),row.get(3)));
//            tempString.append(row.get(0)).append(" ")
//                    .append(row.get(1)).append(" ")
//                    .append(row.get(2) != null ? row.get(2):" ").append(" ")
//                    .append(row.get(3)).append("\n");
        }
        TableStringBuilder<Player> t = new TableStringBuilder<Player>();
        t.addColumn("Αριθμός", Player::getNumber);
        t.addColumn("Όνομα", Player::getName);
        t.addColumn("Θέση", Player::getPosition);
        t.addColumn("Αριθμός Δελτίου", Player::getCode);

        tempString.append(t.createString(tempArray));
        if (header.equals("Έλεγχος 11αδας"))
            tempString.append("Captain = ").append(captainNumber.getText());

        Label label = new Label(labelText);

        TextArea textArea = new TextArea(tempString.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();

        return alert.getResult() == ButtonType.OK;

    }

    // MARK : WRITE DATA TO PDF

    private void writeDataPDF(String pdfName) throws IOException {
        PDDocument pdfDocument;
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Άνοιγμα της Default φόρμας...");
//        File file = fileChooser.showOpenDialog(writeToPDF.getScene().getWindow());
        InputStream inputStream = Main.class.getResourceAsStream("/sample/" + pdfName);

        if (inputStream != null) {
            pdfDocument = PDDocument.load(inputStream);
            PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            InputStream fontStream = Main.class.getResourceAsStream("/sample/Arial.ttf");
            PDFont font = PDType0Font.load(pdfDocument, fontStream);
            PDResources resources = new PDResources();
            COSName fontName = resources.add(font);
            acroForm.setDefaultResources(resources);
            String da = "/" + fontName.getName() + " 9 Tf 0 g";
            acroForm.setDefaultAppearance(da);

            for (PDField field : acroForm.getFields()) {
                if (field instanceof PDTextField) {
                    PDTextField textField = (PDTextField) field;
                    textField.setDefaultAppearance(da);
                }
            }

            //Fill endekada
            Player gkPlayer = Player.getGK(endekadaPlayers);
            if (gkPlayer != null) {
                acroForm.getField("playerNo1").setValue(gkPlayer.getNumber());
                acroForm.getField("playerName1").setValue(gkPlayer.getName());
                acroForm.getField("playerCode1").setValue(gkPlayer.getCode());

                Iterator itr = endekadaPlayers.iterator();
                while (itr.hasNext()) {
                    Player player = (Player) itr.next();
                    if (player.getNumber().equals(gkPlayer.getNumber())) {
                        itr.remove();
                    }
                }
                endekadaPlayers.sort(Player.playersComparator);

                int i = 2;
                for (Player player : endekadaPlayers) {
                    acroForm.getField("playerNo" + i).setValue(player.getNumber());
                    acroForm.getField("playerName" + i).setValue(player.getName());
                    acroForm.getField("playerCode" + i).setValue(player.getCode());
                    i++;
                }
                acroForm.getField("captainNo").setValue(captainNo);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning Dialog");
                alert.setHeaderText("Wrong Data");
                alert.setContentText("Δεν υπάρχει τερματοφύλακας");

                alert.showAndWait();
                return;
            }

            //Fill anapliromatikoi
            Player gkAnapPlayer = Player.getGK(anapliroamtikoiPlayers);
            if (gkAnapPlayer != null) {
                acroForm.getField("anaplNo1").setValue(gkAnapPlayer.getNumber());
                acroForm.getField("anaplName1").setValue(gkAnapPlayer.getName());
                acroForm.getField("anaplCode1").setValue(gkAnapPlayer.getCode());

                Iterator itr = anapliroamtikoiPlayers.iterator();
                while (itr.hasNext()) {
                    Player player = (Player) itr.next();
                    if (player.getNumber().equals(gkAnapPlayer.getNumber())) {
                        itr.remove();
                    }
                }
            }
            anapliroamtikoiPlayers.sort(Player.playersComparator);

            int i = 2;
            for (Player player : anapliroamtikoiPlayers) {
//            System.out.println(i);
                acroForm.getField("anaplNo" + i).setValue(player.getNumber());
                acroForm.getField("anaplName" + i).setValue(player.getName());
                acroForm.getField("anaplCode" + i).setValue(player.getCode());
                i++;
            }

            alertDialog(Alert.AlertType.INFORMATION,"Information Dialog","Information","Η διαδικασία εγγραφής του αρχείου PDF ολοκληρώθηκε! Παρακαλώ στο επόμενο παράθυρο που θα εμφανιστεί διαλέξετε το όνομα και το μέρος που θέλετε να γίνει η αποθήκευση!");

            FileChooser saveDialog = new FileChooser();
            saveDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File saveFile = saveDialog.showSaveDialog(writeToPDF.getScene().getWindow());
            if (saveFile != null) {
                pdfDocument.save(saveFile.getAbsoluteFile());
                pdfDocument.close();
                alertDialog(Alert.AlertType.INFORMATION,"Confirmation Dialog","Done","Η διαδικασία ολοκληρώθηκε!");

                System.exit(0);
            } else {
                alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Error","Η διαδικασία δεν ολοκληρώθηκε! Παρακαλώ δοκιμάστε ξανά");
                writeToPDF.setDisable(true);
                clearTableData();
            }
        } else {
            alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Error","Η διαδικασία δεν ολοκληρώθηκε! Παρακαλώ δοκιμάστε ξανά");
        }
    }

    private void alertDialog(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // MARK : ENABLE SECOND TABLE VIEW

    private void enableAnapliromatikoi() {
        ArrayList<Player> tempData = new ArrayList<>(players);

        Iterator itr = tempData.iterator();
        while (itr.hasNext())
        {
            Player player = (Player) itr.next();
            if (Player.containPlayer(endekadaPlayers,player.getNumber())) {
                itr.remove();
            }
        }

        String[][] list = new String[tempData.size()][4];
        for (int i = 0; i < tempData.size(); i++){
            list[i][0] = tempData.get(i).getNumber();
            list[i][1] = tempData.get(i).getName();
            list[i][2] = tempData.get(i).getPosition();
            list[i][3] = tempData.get(i).getCode();
        }

        fillAnapliromatikoi(list);
        anapliromatikoiTableView.setDisable(false);
        anapliromatikoiBtn.setDisable(false);

        endekadaTableView.setDisable(true);
        endekadaBtn.setDisable(true);
        captainNumber.setDisable(true);

    }

    // MARK : SAVE DATA TO ARRAYS

    private void saveDataEndekada(TableView tableLocal) {
        ObservableList<ObservableList<String>> selectedItems = tableLocal.getSelectionModel().getSelectedItems();

        endekadaPlayers.clear();
        for (ObservableList<String> row : selectedItems) {
            endekadaPlayers.add(new Player(row.get(0),row.get(1),row.get(2),row.get(3)));
        }

        if (endekadaPlayers.size() == 11) {
            if (Player.containCaptain(endekadaPlayers,captainNo) && Player.oneGoalKeeper(endekadaPlayers)) {
                endekadaTableView.getColumns().clear();
                captainNumber.clear();
                for (Player selectedID : endekadaPlayers) {
                    System.out.println(selectedID.toString());
                }
                System.out.println("Captain No: " + captainNo);
                enableAnapliromatikoi();
            } else if (!Player.containCaptain(endekadaPlayers,captainNumber.getText())){
                alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Wrong Data","Ο αρχηγός δεν υπάρχει στην ενδεκάδα! Προσπάθησε ξανά!");
                endekadaPlayers.clear();
            } else {
                alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Wrong Data","Δεν υπάρχει τερματοφύλακας ή έχουν δηλωθεί παραπάνω απο ένας! Προσπάθησε ξανά!");
                endekadaPlayers.clear();
            }
        } else {
            alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Wrong Data","Η βασική ενδεκάδα πρέπει να αποτελείται απο 11 ακριβώς παίκτες! Προσπάθησε ξανά!");
        }
    }

    private void saveDataAnapliromatikoi(TableView tableLocal) {
        ObservableList<ObservableList<String>> selectedItems = tableLocal.getSelectionModel().getSelectedItems();

        anapliroamtikoiPlayers.clear();
        for (ObservableList<String> row : selectedItems) {
            anapliroamtikoiPlayers.add(new Player(row.get(0),row.get(1),row.get(2),row.get(3)));
        }

        if (anapliroamtikoiPlayers.size() < 8) {
            for (Player selectedID : anapliroamtikoiPlayers) {
                System.out.println(selectedID.toString());
            }
            writeToPDF.setDisable(false);
            anapliromatikoiTableView.setDisable(true);
            anapliromatikoiBtn.setDisable(true);
        } else {
            alertDialog(Alert.AlertType.WARNING,"Warning Dialog","Wrong Data","Ο μέγιστος αριθμός αναπληρωματικών μπορεί να είναι 7 άτομα. Προσπάθησε ξανά!");
            anapliroamtikoiPlayers.clear();
        }
    }


    // MARK : LOAD DATA

    private void loadData(Stage primaryStage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Άνοιγμα του excel με τους παίκτες...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel File","*.xlsx", "*.xls"));
        File file = fileChooser.showOpenDialog(primaryStage.getScene().getWindow());

        if (file != null) {
            ExcelFile excelFile = ExcelFile.load(file.getAbsolutePath());
            ExcelWorksheet excelWorksheet = excelFile.getWorksheet(0);

            String[][] sourceData = new String[excelWorksheet.getRows().size()][4];
            for (int row = 0; row < sourceData.length; row++) {
                for (int column = 0; column < sourceData[row].length; column++) {
                    ExcelCell cell = excelWorksheet.getCell(row, column);
                    if (cell.getValueType() != CellValueType.NULL)
                        sourceData[row][column] = cell.getValue().toString();
                }
            }
            fillEndekada(sourceData);
        }
    }


    // MARK : FILL TABLES

    private void fillAnapliromatikoi(String[][] list) {
        anapliromatikoiTableView.getColumns().clear();


        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (String[] row : list)
            data.add(FXCollections.observableArrayList(row));
        anapliromatikoiTableView.setItems(data);

        for (int i = 0; i < list[0].length; i++) {
            final int tempInt = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnNames[tempInt]);
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(tempInt)));
            column.setEditable(true);
            column.setSortable(false);
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(
                    (TableColumn.CellEditEvent<ObservableList<String>, String> t) -> {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).set(t.getTablePosition().getColumn(), t.getNewValue());
                    });
            anapliromatikoiTableView.getColumns().add(column);
        }
    }

    private void fillEndekada(String[][] dataSource) {
        endekadaTableView.getColumns().clear();
        endekadaTableView.setDisable(false);
        endekadaBtn.setDisable(false);
        captainNumber.setDisable(false);

        anapliromatikoiTableView.setDisable(true);
        anapliromatikoiBtn.setDisable(true);

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (String[] row : dataSource)
            data.add(FXCollections.observableArrayList(row));
        endekadaTableView.setItems(data);

        for (int i = 0; i < dataSource[0].length; i++) {
            final int tempInt = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnNames[tempInt]);
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(tempInt)));
            column.setEditable(true);
            column.setSortable(false);
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(
                    (TableColumn.CellEditEvent<ObservableList<String>, String> t) -> {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).set(t.getTablePosition().getColumn(), t.getNewValue());
                    });
            endekadaTableView.getColumns().add(column);
        }

        ObservableList<ObservableList<String>> allPlayers = endekadaTableView.getItems();
        players = new ArrayList<>();
        for (ObservableList<String> row : allPlayers) {
            players.add(new Player(row.get(0),row.get(1),row.get(2),row.get(3)));
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}

class TableStringBuilder<T>
{
    private final List<String> columnNames;
    private final List<Function<? super T, String>> stringFunctions;

    TableStringBuilder()
    {
        columnNames = new ArrayList<String>();
        stringFunctions = new ArrayList<Function<? super T, String>>();
    }

    void addColumn(String columnName, Function<? super T, ?> fieldFunction)
    {
        columnNames.add(columnName);
        stringFunctions.add((p) -> (String.valueOf(fieldFunction.apply(p))));
    }

    private int computeMaxWidth(int column, Iterable<? extends T> elements)
    {
        int n = columnNames.get(column).length();
        Function<? super T, String> f = stringFunctions.get(column);
        for (T element : elements)
        {
            String s = f.apply(element);
            n = Math.max(n, s.length());
        }
        return n;
    }

    private static String padLeft(String s, char c, int length)
    {
        while (s.length() < length)
        {
            s = c + s;
        }
        return s;
    }

    private List<Integer> computeColumnWidths(Iterable<? extends T> elements)
    {
        List<Integer> columnWidths = new ArrayList<Integer>();
        for (int c=0; c<columnNames.size(); c++)
        {
            int maxWidth = computeMaxWidth(c, elements);
            columnWidths.add(maxWidth);
        }
        return columnWidths;
    }

    public String createString(Iterable<? extends T> elements)
    {
        List<Integer> columnWidths = computeColumnWidths(elements);

        StringBuilder sb = new StringBuilder();
        for (int c=0; c<columnNames.size(); c++)
        {
            if (c > 0)
            {
                sb.append("|");
            }
            String format = "%"+columnWidths.get(c)+"s";
            sb.append(String.format(format, columnNames.get(c)));
        }
        sb.append("\n");
        for (int c=0; c<columnNames.size(); c++)
        {
            if (c > 0)
            {
                sb.append("+");
            }
            sb.append(padLeft("", '-', columnWidths.get(c)));
        }
        sb.append("\n");

        for (T element : elements)
        {
            for (int c=0; c<columnNames.size(); c++)
            {
                if (c > 0)
                {
                    sb.append("|");
                }
                String format = "%"+columnWidths.get(c)+"s";
                Function<? super T, String> f = stringFunctions.get(c);
                String s = f.apply(element);
                sb.append(String.format(format, s));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}