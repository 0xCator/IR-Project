package oxCator.base;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;

public class UIController {
    private String folderPath = "/files";
    private HashMap<String, PositionalIndex> positionalIndex;
    private HashMap<Integer, String> fileList;
    private Preprocessor preProc;
    private QueryHandler handler;
    private ResultRanker ranker;

    @FXML
    public Label errorLabel;
    public TitledPane preProcPane;
    public TitledPane resultsPane;
    public Accordion accordionPane;
    public TreeView indexTree;
    public TableView tfTable;
    public TableView weightedTFTable;
    public TableView dfTable;
    public TableView idfTable;
    public TableView tfIDFTable;
    public TableView lengthTable;
    public TableView normalizedTF_IDFTable;
    public TextField searchBox;
    public ListView filesList;
    public TableView calTable;
    public Tab filesTab;
    public TabPane resultsTabPane;

    public UIController() {
        preProc = new Preprocessor(folderPath);
        fileList = preProc.getFilenameList();
        positionalIndex = preProc.getPositionalIndex();
        handler = new QueryHandler(positionalIndex, fileList.size());
        ranker = new ResultRanker(preProc.getNormalizedTF_IDF(), preProc.getIdf(), this);
    }

    @FXML
    public void initialize() {
        //Set the preprocessing pane as the starting pane
        accordionPane.setExpandedPane(preProcPane);

        //Positional posting lists
        positionalIndexList();

        //Two-dimensional tables
        setDFTable();
        setIDFTable();
        setLengthTable();

        //Three-dimensional tables
        setThreeDimensionalIntTable(preProc.getTermFrequency(), tfTable);
        setThreeDimensionalTable(preProc.getWeightedTF(), weightedTFTable);
        setThreeDimensionalTable(preProc.getTfIDF(), tfIDFTable);
        setThreeDimensionalTable(preProc.getNormalizedTF_IDF(), normalizedTF_IDFTable);
    }

    //Searching
    public void search() {
        if (!searchBox.getText().isBlank()) {
            errorLabel.setVisible(false);
            filesList.getItems().clear();
            calTable.getColumns().clear();
            calTable.getItems().clear();
            String queryLine = searchBox.getText();

            try {
                ArrayList<Integer> result = handler.makeQuery(queryLine);
                ArrayList<String> tokens = handler.getTokens();

                if (result.size() != 0 && tokens.size() != 0) {
                    result = ranker.rank(result, tokens);
                }

                setResultsBox(result);
            } catch (Exception e) {
                errorLabel.setVisible(true);
                errorLabel.setText("Invalid syntax");
            }
        }
    }

    public void setCalTable(ArrayList<Integer> matchedFiles, double queryLength, HashMap<String, ArrayList<String>> map, ArrayList<Double> sim) {
        ArrayList<String> emptyRow = new ArrayList<>();

        String[] titleList = {"", "TF", "Weighted TF", "IDF", "TF-IDF", "Normalized", ""};
        int i = 0;
        for (; i < titleList.length; i++) {
            TableColumn<calculationRecord, String> column = new TableColumn<>(titleList[i]);
            column.setSortable(false);
            int temp = i;
            column.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().values().get(temp)));
            calTable.getColumns().add(column);
        }

        for (int j = 0; j < titleList.length + matchedFiles.size(); j++) emptyRow.add("");

        HashSet<Integer> files = new HashSet<>(matchedFiles);
        for (int fileID : files) {
            TableColumn<calculationRecord, String> docColumn = new TableColumn<>("Prod(" + fileList.get(fileID) + ")");
            docColumn.setSortable(false);
            int temp = i;
            docColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().values().get(temp)));
            calTable.getColumns().add(docColumn);
            i++;
        }

        SortedSet<String> keys = new TreeSet<>(map.keySet());
        for (String key : keys) {
            calTable.getItems().add(new calculationRecord(map.get(key)));
        }

        calTable.getItems().add(new calculationRecord(emptyRow));

        ArrayList<String> queryLen = new ArrayList<>();
        queryLen.add("Query length: " + queryLength);
        for (i = 0; i < calTable.getColumns().size() - 1; i++) queryLen.add("");
        calTable.getItems().add(new calculationRecord(queryLen));

        calTable.getItems().add(new calculationRecord(emptyRow));

        for (i = 0; i < sim.size(); i++) {
            ArrayList<String> similarityRow = new ArrayList<>();
            similarityRow.add("Sim(q," + fileList.get(matchedFiles.get(i)) + "): " + sim.get(i));
            for (int j = 0; j < calTable.getColumns().size() - 1; j++) similarityRow.add("");
            calTable.getItems().add(new calculationRecord(similarityRow));
        }
    }

    private void setResultsBox(ArrayList<Integer> result) {
        for (int index : result)
            filesList.getItems().add(fileList.get(index));
        accordionPane.setExpandedPane(resultsPane);
        SelectionModel<Tab> selectionModel = resultsTabPane.getSelectionModel();
        selectionModel.select(filesTab);
    }

    //Preprocessing-related tables
    private void positionalIndexList() {
        SortedSet<String> keys = new TreeSet<>(positionalIndex.keySet());
        TreeItem<String> rootItem = new TreeItem<>();
        for (String key : keys) {
            //Term item
            TreeItem<String> keyItem = new TreeItem<>(key);
            PositionalIndex index = positionalIndex.get(key);
            HashMap<Integer, ArrayList<Integer>> postingList = index.getPostingList();
            //Document item for each term
            for (int documentID : postingList.keySet()) {
                TreeItem<String> documentItem = new TreeItem<>(fileList.get(documentID));
                documentItem.getChildren().add(new TreeItem<>(postingList.get(documentID).toString()));
                keyItem.getChildren().add(documentItem);
            }
            rootItem.getChildren().add(keyItem);
        }
        indexTree.setRoot(rootItem);
        indexTree.setShowRoot(false);
    }

    private void setDFTable() {
        TableColumn<dfRecord, String> term = (TableColumn<dfRecord, String>) dfTable.getColumns().get(0);
        term.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().term()));
        TableColumn<dfRecord, Integer> dfValue = (TableColumn<dfRecord, Integer>) dfTable.getColumns().get(1);
        dfValue.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().df()).asObject());

        SortedSet<String> keys = new TreeSet<>(positionalIndex.keySet());
        HashMap<String, Double> dfList = preProc.getDf();
        for (String key : keys) {
            dfTable.getItems().add(new dfRecord(key, (int) dfList.get(key).doubleValue()));
        }
    }

    private void setIDFTable() {
        TableColumn<idfRecord, String> term = (TableColumn<idfRecord, String>) idfTable.getColumns().get(0);
        term.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().term()));
        TableColumn<idfRecord, Double> idfValue = (TableColumn<idfRecord, Double>) idfTable.getColumns().get(1);
        idfValue.setCellValueFactory(p -> new SimpleDoubleProperty(p.getValue().idf()).asObject());

        SortedSet<String> keys = new TreeSet<>(positionalIndex.keySet());
        HashMap<String, Double> idfList = preProc.getIdf();
        for (String key : keys) {
            idfTable.getItems().add(new idfRecord(key, idfList.get(key)));
        }
    }

    private void setLengthTable() {
        TableColumn<lengthRecord, String> document = (TableColumn<lengthRecord, String>) lengthTable.getColumns().get(0);
        document.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().document()));
        TableColumn<lengthRecord, Double> lengthValue = (TableColumn<lengthRecord, Double>) lengthTable.getColumns().get(1);
        lengthValue.setCellValueFactory(p -> new SimpleDoubleProperty(p.getValue().length()).asObject());

        HashMap<Integer, Double> lengthList = preProc.getDocumentLength();
        for (int key : fileList.keySet()) {
            lengthTable.getItems().add(new lengthRecord(fileList.get(key), lengthList.get(key)));
        }
    }

    private void setThreeDimensionalTable(HashMap<String, ArrayList<Double>> values, TableView table) {
        TableColumn<threeDimRecord, String> term = new TableColumn<>("Term");
        term.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().term()));
        table.getColumns().add(term);

        for (int key : fileList.keySet()) {
            TableColumn<threeDimRecord, Double> valueColumn = new TableColumn<>(fileList.get(key));
            valueColumn.setCellValueFactory(p -> new SimpleDoubleProperty(p.getValue().values().get(key)).asObject());
            table.getColumns().add(valueColumn);
        }

        SortedSet<String> keys = new TreeSet<>(positionalIndex.keySet());
        for (String key : keys) {
            table.getItems().add(new threeDimRecord(key, values.get(key)));
        }
    }

    private void setThreeDimensionalIntTable(HashMap<String, ArrayList<Integer>> values, TableView table) {
        TableColumn<threeDimIntRecord, String> term = new TableColumn<>("Term");
        term.setSortable(false);
        term.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().term()));
        table.getColumns().add(term);

        for (int key : fileList.keySet()) {
            TableColumn<threeDimIntRecord, Double> valueColumn = new TableColumn<>(fileList.get(key));
            valueColumn.setSortable(false);
            valueColumn.setCellValueFactory(p -> new SimpleDoubleProperty(p.getValue().values().get(key)).asObject());
            table.getColumns().add(valueColumn);
        }

        SortedSet<String> keys = new TreeSet<>(positionalIndex.keySet());
        for (String key : keys) {
            table.getItems().add(new threeDimIntRecord(key, values.get(key)));
        }
    }
}
