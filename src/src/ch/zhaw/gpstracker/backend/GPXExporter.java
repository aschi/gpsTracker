package ch.zhaw.gpstracker.backend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class GPXExporter {

	public String exportTrack(Context context, Track track){
		StorageHelper sh = new StorageHelper();
		
		if(sh.isExternalStorageWritable()){
			String filename = track.getName().replaceAll("\\W+", "_");
			filename+=".gpx";
			
			File exportFilePath = sh.getSavePath(context, filename);
			
			OutputStream destination = null;
			
			try {
				Log.i("GPXExporter.exportTrack", "Exporting track to: " + exportFilePath.toString());
	            destination = new FileOutputStream(exportFilePath);
	            destination.write(generateTrackGPX(track).getBytes(Charset.forName("UTF-8")));
	            destination.close();
	            Log.i("GPXExporter.exportTrack", "Export successfully completed.");
	            return exportFilePath.toString();
	        } catch(IOException e) {
	        	e.printStackTrace();
	        	return null;
	        }
		}else{
			Log.w("GPXExporter.exportTrack", "External storage not writable!");
			return null;
		}
		
		

	}

	public String generateTrackGPX(Track track) {
		// ISO 8601 Time formater
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("utf-8", false);
			serializer.startTag("", "gpx");
			serializer.attribute("", "version", "1.0");
			serializer.attribute("", "creator", "ch.zhaw.gpstracker");
			serializer.attribute("", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			serializer.attribute("", "xmlns", "http://www.topografix.com/GPX/1/0");
			serializer.attribute("", "xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd");
			
			double[] boundaries = track.getBoundaries();
			serializer.startTag("", "bounds");
			serializer.attribute("", "maxlon", String.valueOf(boundaries[0]));
			serializer.attribute("", "minlon", String.valueOf(boundaries[1]));
			serializer.attribute("", "maxlat", String.valueOf(boundaries[2]));
			serializer.attribute("", "minlat", String.valueOf(boundaries[3]));
			serializer.endTag("", "bounds");
		
			serializer.startTag("", "trk");
			serializer.startTag("", "name");
			serializer.cdsect(track.getName());
			serializer.endTag("",  "name");
			serializer.startTag("", "trkseg");

			for (Waypoint w : track.getWaypoints()) {
				serializer.startTag("", "trkpt");
				serializer.attribute("", "lat",
						Double.toString(w.getLatitude()));
				serializer.attribute("", "lon",
						Double.toString(w.getLongitude()));
				serializer.startTag("", "ele");
				serializer.text(Double.toString(w.getAltitude()));
				serializer.endTag("", "ele");
				serializer.startTag("", "time");
				serializer.text(df.format(w.getTimestamp()));
				serializer.endTag("", "time");
				serializer.startTag("", "sym");
				serializer.text("Dot");
				serializer.endTag("", "sym");				
				serializer.endTag("", "trkpt");
			}

			serializer.endTag("", "trkseg");
			serializer.endTag("", "trk");
			serializer.endTag("", "gpx");
			serializer.endDocument();
			return writer.toString().replace("'", "\"");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
