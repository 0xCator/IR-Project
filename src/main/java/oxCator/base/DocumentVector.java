package oxCator.base;

import java.util.*;

public class DocumentVector {
    private int dimensions;
    private HashMap<String, Double> weightValues;

    DocumentVector(ArrayList<String> dimensions) {
        this.dimensions = dimensions.size();
        weightValues = new HashMap<>();
        for (String term : dimensions)
            weightValues.put(term, 0.0);
    }

    public int getDimensions() {
        return dimensions;
    }

    public HashMap<String, Double> getWeightValues() {
        return weightValues;
    }

    public ArrayList<Double> getVector() {
        return parseValue(weightValues);
    }

    //Sets the weight (tf-idf) value for a specific dimension
    public void setDimension(String term, double tfIDF) {
        weightValues.replace(term, tfIDF);
    }

    public double getLength() {
        ArrayList<Double> vector = parseValue(weightValues);
        double length = 0.0;

        for (double value : vector)
            length += Math.pow(value, 2);
        length = Math.sqrt(length);

        return length;
    }

    //Calculates the unit vector (dimension / length) and returns in an arraylist
    public ArrayList<Double> getUnitVector() {
        ArrayList<Double> unitVector = parseValue(weightValues);
        double length = 0.0;
        for (double value : unitVector)
            length += Math.pow(value, 2);
        length = Math.sqrt(length);
        for (int i = 0; i < dimensions; i++) {
            double unitDimension = unitVector.get(i) / length;
            unitVector.set(i, unitDimension);
        }

        return unitVector;
    }

    private ArrayList<Double> parseValue(HashMap<String, Double> hashMap) {
        ArrayList<Double> result = new ArrayList<>();
        for (double value : hashMap.values())
            result.add(value);
        return result;
    }
}
