package wallet.rate.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.BigDecimal;

import pivtrum.UloNodeData;

/**
 * Created by furszy on 7/5/17.
 */

public class NodeDb extends AbstractSqliteDb<UloNodeData>  {


    private static final String DATABASE_NAME = "db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "nodes";

    private static final String KEY_ID = "id";
    private static final String KEY_HOST = "host";
    private static final String KEY_TCPPORT = "tcpPort";
    private static final String KEY_TIMESTAMP = "timestamp";

    private static final int KEY_POS_ID = 0;
    private static final int KEY_POS_HOST = 1;
    private static final int KEY_POS_TCPPORT = 2;
    private static final int KEY_POS_TIMESTAMP = 3;


    public NodeDb(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " +TABLE_NAME+
                        "(" +
                        KEY_ID + " INTEGER primary key autoincrement, "+
                        KEY_HOST + " TEXT, "+
                        KEY_TCPPORT + " INTEGER, "+
                        KEY_TIMESTAMP + " INTEGER "
                        +")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // todo: this is just for now..
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected ContentValues buildContent(UloNodeData obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_HOST,obj.getHost());
        contentValues.put(KEY_TCPPORT,obj.getTcpPort());
        contentValues.put(KEY_TIMESTAMP,System.currentTimeMillis());
        return contentValues;
    }

    @Override
    protected UloNodeData buildFrom(Cursor cursor) {
        String host = cursor.getString(KEY_POS_HOST);
        int post = cursor.getInt(KEY_POS_TCPPORT);
        return new UloNodeData(host,post);
    }

    public UloNodeData getNode(String host){
        return get(KEY_HOST,host);
    }


    public void insertOrUpdateIfExist(UloNodeData pivxRate) {
        if (getNode(pivxRate.getHost())==null){
            insert(pivxRate);
        }else {
            updateByKey(KEY_HOST,pivxRate.getHost(),pivxRate);
        }
    }

}
