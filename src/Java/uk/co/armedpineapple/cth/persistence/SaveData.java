package uk.co.armedpineapple.cth.persistence;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "saves")
public class SaveData {

    @DatabaseField(id=true)
    private String saveName;

    @DatabaseField
    private String screenshotPath;

    @DatabaseField
    private int rep;

    @DatabaseField
    private long money;

    @DatabaseField
    private String levelName;

    @DatabaseField(version = true, dataType = DataType.DATE_LONG)
    private Date lastModified;


}
