package oxCator.base;

import java.util.ArrayList;

record dfRecord(String term, int df) {}

record idfRecord(String term, double idf) {}

record lengthRecord(String document, double length) {}

record threeDimRecord(String term, ArrayList<Double> values) {}

record threeDimIntRecord(String term, ArrayList<Integer> values) {}