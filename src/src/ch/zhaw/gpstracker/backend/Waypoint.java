package ch.zhaw.gpstracker.backend;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Waypoint implements Parcelable {
	private long id;
	private long track_id;
	private Date timestamp;
	private double longitude;
	private double latitude;
	private double altitude;

	private final static int R = 6371;
	
	public Waypoint(long id, long track_id, double longitude, double latitude,
			double altitude, Date timestamp) {
		super();
		this.id = id;
		this.track_id = track_id;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.timestamp = timestamp;
	}

	public Waypoint(Parcel in){
		super();
		long[] longs = new long[3];
		double[] doubles = new double[3];
		
		in.readLongArray(longs);
		in.readDoubleArray(doubles);
		
		this.id = longs[0];
		this.track_id = longs[1];
		this.timestamp = new Date(longs[2]);
		this.longitude = doubles[0];
		this.latitude = doubles[1];
		this.altitude = doubles[2];
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTrackid() {
		return track_id;
	}

	public void setTrackid(long track_id) {
		this.track_id = track_id;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double altitude) {
		this.latitude = altitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public double getDistanceTo(Waypoint other){
		double loThis = Math.toRadians(this.getLongitude());
		double laThis = Math.toRadians(this.getLatitude());
		double loOther = Math.toRadians(other.getLongitude());
		double laOther = Math.toRadians(other.getLatitude());
		
		double deltaLa = laThis-laOther;
		double deltaLo = loThis-loOther;
		
		double a = Math.pow(Math.sin(deltaLa/2), 2) + Math.cos(laThis) * Math.cos(laOther) * Math.pow(Math.sin(deltaLo/2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = R * c;
		
		return d;
	}
	
	@Override
	public int describeContents() {
		return 1; //Track = 0; Waypoint = 1
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDoubleArray(new double[] { longitude, latitude, altitude });
		dest.writeLongArray(new long[] {id, track_id, timestamp.getTime()});
	}

	 public static final Parcelable.Creator<Waypoint> CREATOR = new Parcelable.Creator<Waypoint>() {
         public Waypoint createFromParcel(Parcel in) {
             return new Waypoint(in); 
         }

         public Waypoint[] newArray(int size) {
             return new Waypoint[size];
         }
     };
	
}
