package com.example.beacontest1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbManager extends SQLiteOpenHelper {



    // Constants for the database name, version, table name, and column names
    private static final String DbNote = "DbNote";

    private static final String db_Table = "Records";
    private static final String db_counter = "Counter";
    private static final String distanceFlag = "distance_flag";
    private static final String Distance = "distance";
    private static final String Uuid = "uuid";
    private static final String RSSI = "rssi";
    private static final String MEAN = "mean";

    private static DbManager dbManager;

    // Private constructor to ensure singleton pattern
    private DbManager(Context context) {
        super(context, DbNote, null, 1);
    }

    // Singleton pattern: returns an instance of the database manager
    public static DbManager instanceOfDataBase(Context context) {
        if (dbManager == null) {
            dbManager = new DbManager(context);
        }
        return dbManager;
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create the database table
        String sql = "CREATE TABLE " + db_Table + "(" +
                db_counter + " INTEGER PRIMARY KEY, " +
                Uuid + " TEXT, " +
                RSSI + " INTEGER,"+
                Distance + " DOUBLE,"+
                MEAN + " DOUBLE,"+
                distanceFlag + " DOUBLE)";

        // Execute the SQL statement to create the table
        db.execSQL(sql);
    }

    // Called when the database needs to be upgraded (not implemented in your code)
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int ii) {

    }

    // Adds a record (MAC address and RSSI) to the database
    public void addRecordToDb(String uuid, int rssi, double distance, double mean, double distance_flag) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(Uuid, uuid);
        contentValues.put(RSSI, rssi);
        contentValues.put(Distance, distance);
        contentValues.put(MEAN, mean);
        contentValues.put(distanceFlag,distance_flag);

        // Insert the data into the database table
        db.insert(db_Table, null, contentValues);
    }

    public List<DataPoint> getRecordsForKNN() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<DataPoint> dataPoints = new ArrayList<>();

        String query = "SELECT " + Uuid + " , " + RSSI + ", " + distanceFlag + "  FROM " + db_Table + " ORDER BY counter DESC LIMIT 20 ";

        try (Cursor result = db.rawQuery(query, null)) {

            if (result.getCount()!=0 ) {
                while (result.moveToNext()) {
                    String uuid = result.getString(result.getColumnIndex(Uuid));
                    int rssi = result.getInt(result.getColumnIndex(RSSI));
                    double actualdistance = result.getDouble(result.getColumnIndex(distanceFlag));

                    dataPoints.add(new DataPoint(rssi, actualdistance));

                }
            }
        }
        return dataPoints;
    }

    // Class to represent a data point (rssi, distance) for KNN
    public class DataPoint {
        int rssi;
        double actualdistance;

        public DataPoint(int rssi, double actualdistance) {
            this.rssi = rssi;
            this.actualdistance = actualdistance;
        }

        public int getRssi() {
            return rssi;
        }

        public double actualdistance() {
            return actualdistance;
        }
    }
}