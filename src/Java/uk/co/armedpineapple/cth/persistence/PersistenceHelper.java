package uk.co.armedpineapple.cth.persistence;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.Reporting;

public class PersistenceHelper extends OrmLiteSqliteOpenHelper{

    private static final String DATABASE_NAME = "CorsixTH";
    private static final int DATABASE_VERSION = 1;


    public PersistenceHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, SaveData.class);
        } catch (SQLException e) {
            Reporting.report(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, SaveData.class, false);
        } catch (SQLException e) {
            Reporting.report(e);
        }
        onCreate(database, connectionSource);
    }
}
