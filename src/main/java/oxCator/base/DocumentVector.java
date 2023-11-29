package oxCator.base;

import java.util.*;

public class DocumentVector {
    private int dimensions, documentNumber;
    private HashMap<String, Double> weightValues;

    DocumentVector(ArrayList<String> dimensions, int docNum) {
        this.dimensions = dimensions.size();
        documentNumber = docNum;
        weightValues = new HashMap<>();
        for (String term : dimensions)
            weightValues.put(term, 0.0);
    }

    public int getDimensions() {
        return dimensions;
    }

    //Sets the weight (tf-idf) value for a specific dimension
    public void setDimension(String term, int tfRaw, int df) {
        double tfWeight, idf, weight;
        if (tfRaw == 0)
            tfWeight = 0;
        else
            tfWeight = 1 + Math.log10(tfRaw);

        idf = Math.log10(documentNumber / (double) df);

        weight = tfWeight * idf;

        weightValues.replace(term, weight);
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
            unitDimension = Math.ceil(unitDimension * 100.0) / 100.0;
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
