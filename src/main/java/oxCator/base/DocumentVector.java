package oxCator.base;

import java.util.*;

public class DocumentVector {
    private int dimensions, documentNumber;
    private ArrayList<Double> weightValues;

    DocumentVector(int dim, int docNum) {
        dimensions = dim;
        documentNumber = docNum;
        weightValues = new ArrayList<>();
        for (int i = 0; i < dim; i++)
            weightValues.add(0.0);
    }

    public int getDimensions() {
        return dimensions;
    }

    //Sets the weight (tf-idf) value for a specific dimension
    public void setDimension(int index, int tfRaw, int df) {
        double tfWeight, idf, weight;
        if (tfRaw == 0)
            tfWeight = 0;
        else
            tfWeight = 1 + Math.log10(tfRaw);

        idf = Math.log10(documentNumber / (double) df);

        weight = tfWeight * idf;

        weightValues.set(index, weight);
    }

    //Calculates the unit vector (dimension / length) and returns in an arraylist
    public ArrayList<Double> getUnitVector() {
        ArrayList<Double> unitVector = (ArrayList<Double>) weightValues.clone();
        double length = 0.0;
        for (double value : weightValues)
            length += Math.pow(value, 2);
        length = Math.sqrt(length);
        for (int i = 0; i < dimensions; i++) {
            double unitDimension = unitVector.get(i) / length;
            unitDimension = Math.ceil(unitDimension * 100.0) / 100.0;
            unitVector.set(i, unitDimension);
        }

        return unitVector;
    }
}
