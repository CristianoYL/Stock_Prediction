package cristiano.webapp.database;

/**
 * Created by Administrator on 2016/4/27.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Stock.db";
    public static final String STOCK_TABLE = "stock";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + STOCK_TABLE + " (ID TEXT,stock TEXT,value TEXT,volume TEXT,open TEXT,high TEXT,low TEXT,date TEXT,time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + STOCK_TABLE);
        onCreate(db);
    }
    public boolean insertData(String tableName, String[] columnValue){
        if(tableName.equals(STOCK_TABLE) && columnValue.length == 9){ // STOCK_TABLE has 9 column
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ID",columnValue[0]);
            values.put("stock",columnValue[1]);
            values.put("value",columnValue[2]);
            values.put("volume",columnValue[3]);
            values.put("open",columnValue[4]);
            values.put("high",columnValue[5]);
            values.put("low",columnValue[6]);
            values.put("date",columnValue[7]);
            values.put("time",columnValue[8]);
            if (db.insert(STOCK_TABLE, null, values) == -1){
                return false;
            } else {
                return true;
            }
        } else{
            return false;
        }
    }
    public Cursor getStockData(String stockName){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select date,time,value from "+STOCK_TABLE+" where stock = ?",new String[]{stockName});

    }
}

