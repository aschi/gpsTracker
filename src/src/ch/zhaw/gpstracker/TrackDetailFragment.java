package ch.zhaw.gpstracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.zhaw.gpstracker.backend.GPXExporter;
import ch.zhaw.gpstracker.backend.TimeUnits;
import ch.zhaw.gpstracker.backend.Track;
import ch.zhaw.gpstracker.backend.database.TracksDataSource;

/**
 * A fragment representing a single Track detail screen. This fragment is either
 * contained in a {@link TrackListActivity} in two-pane mode (on tablets) or a
 * {@link TrackDetailActivity} on handsets.
 */
public class TrackDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "track_id";

	/**
	 * The GPX Exporter
	 * 
	 */
	GPXExporter gex = new GPXExporter();
	
	/**
	 * The currently opened track
	 */
	private Track track;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TrackDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			long track_id = Long.parseLong(getArguments().getString(ARG_ITEM_ID));
			
			if(track_id != 0){
				TracksDataSource tds = new TracksDataSource(getActivity());
				tds.open();
				track = tds.getTrackById(track_id);
				tds.close();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_track_detail,
				container, false);

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);
		
		setHasOptionsMenu(true);
		
		// Show the dummy content as text in a TextView.
		if (track != null) {
			((TextView) rootView.findViewById(R.id.track_name)).setText(track.getName());
			((TextView) rootView.findViewById(R.id.startdate)).setText("Start: " + df.format(track.getStartTimestamp()));
			((TextView) rootView.findViewById(R.id.enddate)).setText("Stop: " + df.format(track.getEndTimestamp()));
			((TextView) rootView.findViewById(R.id.timeused)).setText("Time used (min): " + String.valueOf(track.getTimeUsed(TimeUnits.MINUTES)));
			((TextView) rootView.findViewById(R.id.startbattery)).setText("Start Batterypercentage: " + String.valueOf(track.getStartBatteryPercentage()));
			((TextView) rootView.findViewById(R.id.endbattery)).setText("End Batterypercentage: " + String.valueOf(track.getEndBatteryPercentage()));
			((TextView) rootView.findViewById(R.id.batteryusage)).setText("Batteryusage: " + String.valueOf(track.getBatteryusage()));
			((TextView) rootView.findViewById(R.id.configdist)).setText("Config: dist between wp (m): " + String.valueOf(track.getConfigDist()));
			((TextView) rootView.findViewById(R.id.configtime)).setText("Config: time between wp (ms): " + String.valueOf(track.getConfigTime()));
			((TextView) rootView.findViewById(R.id.nowaypoints)).setText("# Waypoints: " + String.valueOf(track.getWaypoints().size()));
			((TextView) rootView.findViewById(R.id.distance)).setText("Distance (km): " + String.valueOf(track.getDistance()));
			((TextView) rootView.findViewById(R.id.heightDiffPos)).setText("Height difference positive (m):" + String.valueOf(track.getHeightDifference("positive")));
			((TextView) rootView.findViewById(R.id.heightDiffNeg)).setText("Height difference negative (m):" + String.valueOf(track.getHeightDifference("negative")));
			((TextView) rootView.findViewById(R.id.heightDiffTot)).setText("Height difference total (m): " + String.valueOf(track.getHeightDifference("total")));
		}else{
			((TextView) rootView.findViewById(R.id.track_name)).setText("Unable to open track");
		}

		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.track_detail_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.export_track:
	            if(track != null){
	            	String filename = gex.exportTrack(getActivity(), track);
	            	if(filename == null){
	            		AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
	    				ad.setCancelable(false); // This blocks the 'BACK' button
	    				ad.setMessage("Unable to export track!");
	    				ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
	    				    @Override
	    				    public void onClick(DialogInterface dialog, int which) {
	    				        dialog.dismiss();                    
	    				    }
	    				});
	    				ad.show();
	            	}else{
	            		AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
	    				ad.setCancelable(false); // This blocks the 'BACK' button
	    				ad.setMessage("Track successfully exported ("+filename+")");
	    				ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
	    				    @Override
	    				    public void onClick(DialogInterface dialog, int which) {
	    				        dialog.dismiss();                    
	    				    }
	    				});
	    				ad.show();
	            	}
		            return true;
	            }else{
	            	return false;
	            }
	        case R.id.delete_track:
	        	if(track != null){
	        		TracksDataSource tds = new TracksDataSource(getActivity());
	        		tds.open();
	        		tds.deleteTrack(track);
	        		tds.close();
	        	}
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
