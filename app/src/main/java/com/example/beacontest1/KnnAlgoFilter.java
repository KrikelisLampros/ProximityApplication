package com.example.beacontest1;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;




// Class to perform KNN prediction
public class KnnAlgoFilter {

    private DbManager dbManager;

    // Constructor to initialize DbManager instance
    public KnnAlgoFilter(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    // Function to calculate the absolute difference between RSSI values
    private double calculateDistance(int rssi1, int rssi2) {
        return Math.abs(rssi1 - rssi2); // Absolute difference
    }

    // Function to classify a new RSSI value using KNN
    public String classify(int targetRssi, int k) {
        // Retrieve the dataset from the database
        List<DbManager.DataPoint> dataset = dbManager.getRecordsForKNN();

        // Ensure there are enough records for KNN
        if (dataset.size() < k) {
            System.out.println("Not enough data for KNN.");
            return null; // Return an error value or handle it appropriately
        }

        // Calculate distances between target RSSI and each record in the dataset
        List<Neighbor> neighbors = new ArrayList<>();
        for (DbManager.DataPoint dataPoint : dataset) {

            Log.i("KnnAlgo", "This is the targeted rssi  " + dataPoint.getRssi());
            double distance = calculateDistance(targetRssi, dataPoint.getRssi());
            neighbors.add(new Neighbor(dataPoint.getRssi(), distance, dataPoint.actualdistance()));
            Log.i("KnnAlgo", "This is the calculated distance " + dataPoint.actualdistance());

        }

        // Sort the neighbors by distance (ascending)
        neighbors.sort(Comparator.comparingDouble(Neighbor::getDistance));

        // Count the number of occurrences of each class in the k nearest neighbors
        int countA = 0, countB = 0, countC = 0;
        for (int i = 0; i < k; i++) {
            double classification = neighbors.get(i).getActualDistance();
            Log.i("KnnAlgo", "This is the result from db "+classification);
            if (classification == 50.0) {
                countA++;
            } else if (classification == 70.71) {
                countB++;
            } else if (classification == 100.12) {
                countC++;
            }
        }

        Log.i("KnnAlgo", "This is the Count A "+countA);
        Log.i("KnnAlgo", "This is the Count B "+countB);
        Log.i("KnnAlgo", "This is the Count C "+countC);


        // Determine the class with the most votes
        if (countA >= countB && countA >= countC) {
            return "A";
        } else if (countB >= countA && countB >= countC) {
            return "B";
        } else {
            return "C";
        }
    }

    // Class to represent a neighbor with distance and class
    private static class Neighbor {
        private final int rssi;              // RSSI value of the neighbor
        private final double actualDistance; // Actual distance from the beacon
        private final double distance;       // Calculated distance between target RSSI and this neighbor's RSSI


        public Neighbor(int rssi,  double distance,double actualDistance) {
            this.rssi = rssi;
            this.actualDistance = actualDistance;
            this.distance = distance;

        }

        public int getRssi() {
            return rssi;
        }

        public double getActualDistance() {
            return actualDistance;
        }

        public double getDistance() {
            return distance;
        }

    }

}