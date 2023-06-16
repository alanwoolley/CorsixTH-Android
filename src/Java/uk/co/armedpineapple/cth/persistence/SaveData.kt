package uk.co.armedpineapple.cth.persistence

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.util.Date

@DatabaseTable(tableName = "saves")
class SaveData {

    @DatabaseField(id = true)
    var saveName: String? = null

    @DatabaseField
    var screenshotPath: String? = null

    @DatabaseField
    var rep: Int = 0

    @DatabaseField
    var money: Long = 0

    @DatabaseField
    var levelName: String? = null

    @DatabaseField(version = true, dataType = DataType.DATE_LONG)
    var lastModified: Date? = null
}
