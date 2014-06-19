package ch.zhaw.gpstracker.backend.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GPSTrackerSQLiteHelper extends SQLiteOpenHelper {
	public static final String TABLE_TRACKS = "tracks";
	public static final String TR_COLUMN_ID = "_id";
	public static final String TR_COLUMN_NAME = "trackname";
	public static final String TR_COLUMN_STARTTIMESTAMP = "starttimestamp";
	public static final String TR_COLUMN_ENDTIMESTAMP = "endtimestamp";
	public static final String TR_COLUMN_BATTERYSTART = "batterystart";
	public static final String TR_COLUMN_BATTERYEND = "batteryend";
	public static final String TR_COLUMN_CONFIG_DIST = "configdist";
	public static final String TR_COLUMN_CONFIG_TIME = "configtime";

	public static final String TABLE_WAYPOINTS = "waypoints";
	public static final String WP_COLUMN_ID = "_id";
	public static final String WP_COLUMN_TRACK = "track_id";
	public static final String WP_COLUMN_LONGITUDE = "longitude";
	public static final String WP_COLUMN_LATITUDE = "latitude";
	public static final String WP_COLUMN_ALTITUDE = "altitude";
	public static final String WP_COLUMN_TIMESTAMP = "timestamp";

	private static final String DATABASE_NAME = "gpstracker.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String TRACKS_TABLE_CREATE = "create table "
			+ TABLE_TRACKS + "(" + TR_COLUMN_ID
			+ " integer primary key autoincrement, " + TR_COLUMN_NAME
			+ " text not null, " + TR_COLUMN_STARTTIMESTAMP + " int not null, "
			+ TR_COLUMN_ENDTIMESTAMP + " int not null, "
			+ TR_COLUMN_BATTERYSTART + " real not null, "
			+ TR_COLUMN_BATTERYEND + " real not null, " + TR_COLUMN_CONFIG_DIST
			+ " int not null, " + TR_COLUMN_CONFIG_TIME + " int not null);";

	private static final String WAYPOINTS_TABLE_CREATE = "create table "
			+ TABLE_WAYPOINTS + "(" + WP_COLUMN_ID
			+ " integer primary key autoincrement, " + WP_COLUMN_TRACK
			+ " integer not null, " + WP_COLUMN_LONGITUDE + " real not null, "
			+ WP_COLUMN_LATITUDE + " real not null, " + WP_COLUMN_ALTITUDE
			+ " real not null, " + WP_COLUMN_TIMESTAMP + " int not null );";

	public GPSTrackerSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TRACKS_TABLE_CREATE);
		db.execSQL(WAYPOINTS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(GPSTrackerSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAYPOINTS);
		onCreate(db);
	}
}
