package uk.co.armedpineapple.cth.persistence;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "saves")
public class SaveData {

    @DatabaseField(id=true)
    public String saveName;

    @DatabaseField
    public String screenshotPath;

    @DatabaseField
    public int rep;

    @DatabaseField
    public long money;

    @DatabaseField
    public String levelName;

    @DatabaseField(version = true, dataType = DataType.DATE_LONG)
    public Date lastModified;


}
