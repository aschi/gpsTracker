package ch.zhaw.gpstracker.backend.database;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import ch.zhaw.gpstracker.backend.Track;
import ch.zhaw.gpstracker.backend.Waypoint;

public class WaypointsDataSource {
	// Database fields
	private SQLiteDatabase database;
	private GPSTrackerSQLiteHelper dbHelper;
	private String[] allColumns = { GPSTrackerSQLiteHelper.WP_COLUMN_ID,
			GPSTrackerSQLiteHelper.WP_COLUMN_TRACK, 
			GPSTrackerSQLiteHelper.WP_COLUMN_LONGITUDE, 
			GPSTrackerSQLiteHelper.WP_COLUMN_LATITUDE, 
			GPSTrackerSQLiteHelper.WP_COLUMN_ALTITUDE,
			GPSTrackerSQLiteHelper.WP_COLUMN_TIMESTAMP};

	public WaypointsDataSource(Context context) {
		dbHelper = new GPSTrackerSQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Waypoint createWaypoint(Track track, double longitude, double latitude, double altitude, Date timestamp) {
		ContentValues values = new ContentValues();
		values.put(GPSTrackerSQLiteHelper.WP_COLUMN_TRACK, track.getId());
		values.put(GPSTrackerSQLiteHelper.WP_COLUMN_LONGITUDE, longitude);
		values.put(GPSTrackerSQLiteHelper.WP_COLUMN_LATITUDE, latitude);
		values.put(GPSTrackerSQLiteHelper.WP_COLUMN_ALTITUDE, altitude);
		values.put(GPSTrackerSQLiteHelper.WP_COLUMN_TIMESTAMP, timestamp.getTime());
		
		long insertId = database.insert(GPSTrackerSQLiteHelper.TABLE_WAYPOINTS, null,values);
		Cursor cursor = database.query(GPSTrackerSQLiteHelper.TABLE_WAYPOINTS,
				allColumns, GPSTrackerSQLiteHelper.WP_COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Waypoint newWaypoint = cursorToWaypoint(cursor);
		cursor.close();
		return newWaypoint;
	}

	public void deleteWaypoint(Waypoint waypoint) {
		long id = waypoint.getId();
		System.out.println("Waypoint deleted with id: " + id);
		database.delete(GPSTrackerSQLiteHelper.TABLE_WAYPOINTS,
				GPSTrackerSQLiteHelper.WP_COLUMN_ID + " = " + id, null);
	}

	public List<Waypoint> getWaypointsForTrack(long trackid) {
		List<Waypoint> waypoints = new LinkedList<Waypoint>();

		Cursor cursor = database.query(GPSTrackerSQLiteHelper.TABLE_WAYPOINTS,
				allColumns, GPSTrackerSQLiteHelper.WP_COLUMN_TRACK +"=?", new String[]{Long.toString(trackid)}, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Waypoint waypoint = cursorToWaypoint(cursor);
			waypoints.add(waypoint);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return waypoints;
	}
	
	public List<Waypoint> getAllWaypoints() {
		List<Waypoint> waypoints = new LinkedList<Waypoint>();

		Cursor cursor = database.query(GPSTrackerSQLiteHelper.TABLE_WAYPOINTS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Waypoint waypoint = cursorToWaypoint(cursor);
			waypoints.add(waypoint);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return waypoints;
	}

	private Waypoint cursorToWaypoint(Cursor cursor) {
		long id = cursor.getLong(0);
		long track_id = cursor.getLong(1);
		double longitude = cursor.getDouble(2);
		double latitude = cursor.getDouble(3);
		double altitude = cursor.getDouble(4);
		Date timestamp = new Date(cursor.getLong(5));
						
		Waypoint waypoint = new Waypoint(id, track_id, longitude, latitude, altitude, timestamp);
		return waypoint;
	}
}
