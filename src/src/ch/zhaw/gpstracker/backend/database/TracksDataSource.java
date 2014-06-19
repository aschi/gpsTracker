package ch.zhaw.gpstracker.backend.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import ch.zhaw.gpstracker.backend.Track;
import ch.zhaw.gpstracker.backend.Waypoint;

public class TracksDataSource {
	// Database fields
	private Context context;
	private SQLiteDatabase database;
	private GPSTrackerSQLiteHelper dbHelper;
	private String[] allColumns = { GPSTrackerSQLiteHelper.TR_COLUMN_ID,
			GPSTrackerSQLiteHelper.TR_COLUMN_NAME, 
			GPSTrackerSQLiteHelper.TR_COLUMN_STARTTIMESTAMP, 
			GPSTrackerSQLiteHelper.TR_COLUMN_ENDTIMESTAMP, 
			GPSTrackerSQLiteHelper.TR_COLUMN_BATTERYSTART,
			GPSTrackerSQLiteHelper.TR_COLUMN_BATTERYEND,
			GPSTrackerSQLiteHelper.TR_COLUMN_CONFIG_DIST,
			GPSTrackerSQLiteHelper.TR_COLUMN_CONFIG_TIME
			};

	public TracksDataSource(Context context) {
		this.context = context;
		dbHelper = new GPSTrackerSQLiteHelper(context);
	}
	
	public void setContext(Context context){
		this.context = context;
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/*//Used to fix faulty data caused by a bug. is still in here in case I need something similar again :-)
	public void fixStartEndTimestamp(){
		List<Track> tracks = getAllTracks();
		for(Track track : tracks){
			ContentValues values = new ContentValues();
			values.put(GPSTrackerSQLiteHelper.TR_COLUMN_STARTTIMESTAMP, track.getEndTimestamp().getTime());
			values.put(GPSTrackerSQLiteHelper.TR_COLUMN_ENDTIMESTAMP, track.getStartTimestamp().getTime());
			
			database.update(GPSTrackerSQLiteHelper.TABLE_TRACKS, 
					values, 
					GPSTrackerSQLiteHelper.TR_COLUMN_ID+"=?", 
					new String[]{String.valueOf(track.getId())});
		}
	}
	*/
	
	public Track createTrack(Track track) {
		ContentValues values = new ContentValues();
		values.put(GPSTrackerSQLiteHelper.TR_COLUMN_NAME, track.getName());
		values.put(GPSTrackerSQLiteHelper.TR_COLUMN_STARTTIMESTAMP, track.getStartTimestamp().getTime());
		values.put(GPSTrackerSQLiteHelper.TR_COLUMN_ENDTIMESTAMP, track.getEndTimestamp().getTime());
		values.put(GPSTrackerSQLiteHelper.TR_COLUMN_BATTERYSTART, track.getStartBatteryPercentage());
		values.put(GPSTrackerSQLiteHelper.TR_COLUMN_BATTERYEND, track.getEndBatteryPercentage());
		values.put(GPSTrackerSQLiteHelper.TR_COLUMN_CONFIG_DIST, track.getConfigDist());
		values.put(GPSTrackerSQLiteHelper.TR_COLUMN_CONFIG_TIME, track.getConfigTime());
		
		long insertId = database.insert(GPSTrackerSQLiteHelper.TABLE_TRACKS, null,values);
		
		track.setId(insertId);
		
		//save waypoints
		WaypointsDataSource wpds = new WaypointsDataSource(context);
		wpds.open();
		
		for(Waypoint waypoint : track.getWaypoints()){
			wpds.createWaypoint(track, waypoint.getLongitude(), waypoint.getLatitude(), waypoint.getAltitude(), waypoint.getTimestamp());
		}
		wpds.close();
		
		return track;
	}

	public void deleteTrack(Track track) {
		long id = track.getId();
		Log.i("deleteTrack", "Track deleted with id: " + id);
		database.delete(GPSTrackerSQLiteHelper.TABLE_TRACKS,
				GPSTrackerSQLiteHelper.TR_COLUMN_ID + " = " + id, null);
	}

	public Track getTrackById(long trackid){
		Cursor cursor = database.query(GPSTrackerSQLiteHelper.TABLE_TRACKS,
				allColumns, GPSTrackerSQLiteHelper.TR_COLUMN_ID+"=?", new String[]{Long.toString(trackid)}, null, null, null);

		cursor.moveToFirst();
		Track track = null;
		if(!cursor.isAfterLast()){
			track = cursorToTrack(cursor);
		}
		// make sure to close the cursor
		cursor.close();
		return track;
	}
	
	public List<Track> getAllTracks() {
		List<Track> tracks = new ArrayList<Track>();

		Cursor cursor = database.query(GPSTrackerSQLiteHelper.TABLE_TRACKS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Track track = cursorToTrack(cursor);
			tracks.add(track);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return tracks;
	}

	private Track cursorToTrack(Cursor cursor) {
		WaypointsDataSource wpds = new WaypointsDataSource(context);
		
		long id = cursor.getLong(0);
		String name = cursor.getString(1);
		Date startTimestamp = new Date(cursor.getLong(2));
		Date endTimestamp = new Date(cursor.getLong(3));
		double batteryStart = cursor.getDouble(4);
		double batteryEnd = cursor.getDouble(5);
		long configDist = cursor.getLong(6);
		long configTime = cursor.getLong(7);
		
		
		wpds.open();
		List<Waypoint> waypoints = wpds.getWaypointsForTrack(id);
		wpds.close();
				
		Track track = new Track(id, waypoints, name, startTimestamp, endTimestamp, batteryStart, batteryEnd, configDist, configTime);
		return track;
	}
}
