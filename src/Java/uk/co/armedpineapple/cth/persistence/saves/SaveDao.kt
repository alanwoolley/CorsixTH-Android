package uk.co.armedpineapple.cth.persistence.saves

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

/**
 * DAO for save game metadata
 */
@Dao
interface SaveDao {
    /**
     * Upsert a SaveData
     *
     * @param save The SaveData
     */
    @Upsert
    fun upsert(save: SaveData)

    /**
     * Deletes a SaveData
     *
     * @param save The SaveData
     */
    @Delete
    fun delete(save: SaveData)

    /**
     * Deletes a SaveData identified by SaveName
     *
     * @param save The SaveName
     */
    @Delete(entity = SaveData::class)
    fun delete(save: SaveName)

    /**
     * Gets all SaveData
     *
     * @return All SaveData
     */
    @Query("SELECT * from saves")
    fun getAll(): List<SaveData>

    /**
     * Gets a single SaveData identified by its name
     *
     * @param name The save name
     * @return The SaveData corresponding to name
     */
    @Query("SELECT * from saves WHERE save_name = :name")
    fun get(name: String) : SaveData
}