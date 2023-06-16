package uk.co.armedpineapple.cth.persistence


import android.content.Context
import android.database.sqlite.SQLiteDatabase

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException

import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.Reporting

class PersistenceHelper(context: Context) : OrmLiteSqliteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    override fun onCreate(database: SQLiteDatabase, connectionSource: ConnectionSource) {
        try {
            TableUtils.createTable(connectionSource, SaveData::class.java)
        } catch (e: SQLException) {
            Reporting.report(e)
        }

    }

    override fun onUpgrade(database: SQLiteDatabase, connectionSource: ConnectionSource, oldVersion: Int, newVersion: Int) {
        try {
            TableUtils.dropTable<SaveData, Any>(connectionSource, SaveData::class.java, false)
        } catch (e: SQLException) {
            Reporting.report(e)
        }

        onCreate(database, connectionSource)
    }

    companion object {

        private const val DATABASE_NAME = "CorsixTH"
        private const val DATABASE_VERSION = 1
    }
}
