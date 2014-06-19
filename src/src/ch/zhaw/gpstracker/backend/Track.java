package ch.zhaw.gpstracker.backend;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import ch.zhaw.gpstracker.backend.TimeUnits;
import ch.zhaw.gpstracker.backend.database.TracksDataSource;

public class Track implements Parcelable {
	private long id;
	private List<Waypoint> waypoints;
	private String name;
	private Date startTimestamp;
	private Date endTimestamp;
	private double endBatteryPercentage;
	private double startBatteryPercentage;
	private long configDist;
	private long configTime;

	public Track() {
		super();
		waypoints = new LinkedList<Waypoint>();
	}

	public Track(long id, List<Waypoint> waypoints, String name,
			Date startTimestamp, Date endTimestamp,
			double startBatteryPercentage, double endBatteryPercentage,
			long configDist, long configTime) {
		super();
		this.id = id;
		
		if(waypoints != null){
			this.waypoints = waypoints;
		}else{
			this.waypoints = new LinkedList<Waypoint>();
		}
		
		this.name = name;
		this.startBatteryPercentage = startBatteryPercentage;
		this.endBatteryPercentage = endBatteryPercentage;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
		this.configDist = configDist;
		this.configTime = configTime;
	}

	public Track(Parcel in) {
		long[] longs = new long[5];
		double[] doubles = new double[2];

		in.readLongArray(longs);
		in.readDoubleArray(doubles);

		this.id = longs[0];
		this.startTimestamp = (longs[1] == -1 ? null : new Date(longs[1]));
		this.endTimestamp = (longs[2] == -1 ? null : new Date(longs[2]));
		this.configDist = longs[3];
		this.configTime = longs[4];

		this.startBatteryPercentage = doubles[0];
		this.endBatteryPercentage = doubles[1];

		this.name = in.readString();

		in.readTypedList(this.waypoints, Waypoint.CREATOR);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<Waypoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(List<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getBatteryusage() {
		return this.startBatteryPercentage - this.endBatteryPercentage;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public Date getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	
	public double getEndBatteryPercentage() {
		return endBatteryPercentage;
	}

	public void setEndBatteryPercentage(double endBatteryPercentage) {
		this.endBatteryPercentage = endBatteryPercentage;
	}

	public double getStartBatteryPercentage() {
		return startBatteryPercentage;
	}

	public void setStartBatteryPercentage(double startBatteryPercentage) {
		this.startBatteryPercentage = startBatteryPercentage;
	}

	public long getConfigDist() {
		return configDist;
	}

	public void setConfigDist(long configDist) {
		this.configDist = configDist;
	}

	public long getConfigTime() {
		return configTime;
	}

	public void setConfigTime(long configTime) {
		this.configTime = configTime;
	}

	public double getTimeUsed(TimeUnits unit){
		long timeUsed = (startTimestamp == null || endTimestamp == null ? 0 : endTimestamp.getTime()-startTimestamp.getTime());
		switch(unit){
			case MILLISECONDS:
				return (double)timeUsed;
			case SECONDS:
				return (double)timeUsed/1000;
			case MINUTES:
				return (double)timeUsed/(60*1000);
			case HOURS:
				return (double)timeUsed/(60*60*1000);				
			case DAYS:
				return (double)timeUsed/(24*60*60*1000);				
			default:
				return (double)timeUsed;
		}
	}
	
	public double getDistance() {
		if (waypoints.size() == 0 || waypoints.size() == 1) {
			return 0;
		} else {
			double sum = 0;

			for (int i = 1; i <= waypoints.size() - 1; i++) {
				sum += waypoints.get(i - 1).getDistanceTo(waypoints.get(i));
			}
			return sum;
		}
	}

	public double getHeightDifference(String mode) {
		if (waypoints.size() == 0 || waypoints.size() == 1) {
			return 0;
		} else {
			double sum = 0;
			for (int i = 1; i <= waypoints.size() - 1; i++) {
				double div = waypoints.get(i).getAltitude()
						- waypoints.get(i - 1).getAltitude();

				switch (mode) {
					case "positive":
						sum += div > 0 ? div : 0; break;
					case "negative":
						sum += div < 0 ? (-1) * div : 0; break;
					case "total":
						sum += div; break;
				}
			}
			return sum;
		}
	}

	public double[] getBoundaries() {
		double[] boundaries = new double[4]; // 0 = max lon; 1 = min lon;2 = max
												// lat; 3 = min lat

		for (Waypoint w : waypoints) {
			boundaries[0] = (w.getLongitude() > boundaries[0]) ? w
					.getLongitude() : boundaries[0];
			boundaries[1] = (w.getLongitude() < boundaries[0]) ? w
					.getLongitude() : boundaries[1];
			boundaries[2] = (w.getLatitude() > boundaries[0]) ? w
					.getLongitude() : boundaries[2];
			boundaries[3] = (w.getLatitude() < boundaries[0]) ? w
					.getLongitude() : boundaries[3];
		}

		return boundaries;
	}

	// Define a listener that responds to location updates
	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location
			// provider.
			trackLocation(location);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

	public void startTrack(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		Log.i("Track","sharedPref dist: "+ sharedPref.getString("max_dist_between_tp", "10"));
		Log.i("Track","sharedPref time: "+ sharedPref.getString("max_time_between_tp", "10000"));

		long minDistance = Long.parseLong(sharedPref.getString("max_dist_between_tp", "10"));
		long maxTime = Long.parseLong(sharedPref.getString("max_time_between_tp", "10000"));

		this.configDist = minDistance;
		this.configTime = maxTime;
		
		startTimestamp = new Date();
		startBatteryPercentage = getBatteryStatus(context);

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, maxTime, minDistance, locationListener);
	}

	private double getBatteryStatus(Context context) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		double batteryPct = level / (double) scale;

		Log.i("getBatteryStatus()", "level: " + level + "; scale: " + scale
				+ "; pct: " + batteryPct);

		return batteryPct;
	}

	private void trackLocation(Location location) {
		Date timestamp = new Date();

		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		double altitude = location.getAltitude();

		waypoints.add(new Waypoint(0, this.id, longitude, latitude, altitude,
				timestamp));
	}

	public boolean endTrack(Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(locationListener);

		if (waypoints == null || waypoints.size() == 0) {

			return false;
		} else {
			endTimestamp = new Date();
			endBatteryPercentage = getBatteryStatus(context);

			TracksDataSource tds = new TracksDataSource(context);
			tds.open();
			tds.createTrack(this);
			tds.close();

			return true;
		}
	}

	public String toString() {
		return name;
	}

	@Override
	public int describeContents() {
		return 0; // Track = 0; Waypoint = 1
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		long sts = (startTimestamp == null ? -1 : startTimestamp.getTime());
		long ets = (endTimestamp == null ? -1 : endTimestamp.getTime());
		
		dest.writeLongArray(new long[] { id, sts, ets, configDist, configTime });
		dest.writeString(name);
		dest.writeDoubleArray(new double[] { startBatteryPercentage,
				endBatteryPercentage });
		dest.writeTypedList(waypoints);
	}

	public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
		public Track createFromParcel(Parcel in) {
			return new Track(in);
		}

		public Track[] newArray(int size) {
			return new Track[size];
		}
	};

}
