package ch.zhaw.gpstracker;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import ch.zhaw.gpstracker.backend.Track;
import ch.zhaw.gpstracker.backend.database.TracksDataSource;

/**
 * A list fragment representing a list of Tracks. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link TrackDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TrackListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * TracksDataSource used to get trackdata
	 */
	private TracksDataSource tds;

	/**
	 * TrackList
	 */
	private List<Track> trackList;
	private ArrayList<Track> openTracks;
	private ArrayList<Track> ongoingTracks;
	private Context context;
	private ArrayAdapter<Track> adapter;
	
	public List<Track> getTrackList(){
		return trackList;
	}
	
	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TrackListFragment() {
	}

	private void updateTracklist(){
		trackList = new ArrayList<Track>();
		trackList.addAll(openTracks);
		trackList.addAll(ongoingTracks);
		
		tds.open();
		trackList.addAll(tds.getAllTracks());
		tds.close();
		
		Log.i("TrackListFragment", "trackList.size(): " + trackList.size());
		
		if(adapter != null){
			adapter.clear();
			adapter.addAll(trackList);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		if(savedInstanceState == null){
			openTracks = new ArrayList<Track>();    
			ongoingTracks = new ArrayList<Track>();
		}else{
			openTracks = savedInstanceState.getParcelableArrayList("openTracks");
			ongoingTracks = savedInstanceState.getParcelableArrayList("ongoingTracks");
		}
		
		tds = new TracksDataSource(getActivity());
		updateTracklist();
		
		adapter = new IconicAdapter();
		
		setListAdapter(adapter);
		
	}

	class IconicAdapter extends ArrayAdapter<Track> {
		IconicAdapter() {
			super(TrackListFragment.this.getActivity(), R.layout.row, R.id.label, trackList);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row=super.getView(position, convertView, parent);
			
			ImageView icon=(ImageView)row.findViewById(R.id.icon);
			TextView status=(TextView)row.findViewById(R.id.status);
						
			if(openTracks.contains(trackList.get(position))){
				row.setBackgroundColor(Color.argb(1, 255, 255, 200));
				icon.setImageResource(R.drawable.ic_action_play);
				status.setText("New track. Click to start tracking.");
			}else if(ongoingTracks.contains(trackList.get(position))){
				row.setBackgroundColor(Color.argb(1, 200, 255, 200));
				icon.setImageResource(R.drawable.ic_action_stop);
				status.setText("Currently tracking. Click to stop tracking.");
			}else{
				icon.setImageResource(android.R.color.transparent);
				status.setText("Tracking finished. Click to view and/or export.");
			}
		
			return(row);
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		Track t = trackList.get(position);
		if(openTracks.contains(t)){
			t.startTrack(getActivity());
			openTracks.remove(t);
			ongoingTracks.add(t);
			updateTracklist();
		}else if(ongoingTracks.contains(t)){
			if(t.endTrack(getActivity())){
				ongoingTracks.remove(t);
				updateTracklist();
			}else{
				ongoingTracks.remove(t);
				openTracks.add(t);
				updateTracklist();
				
				AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
				ad.setCancelable(false); // This blocks the 'BACK' button
				ad.setMessage("No points captured. Reseting track.");
				ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.dismiss();                    
				    }
				});
				ad.show();
			}
			
		}else{
			mCallbacks.onItemSelected(String.valueOf(trackList.get(position).getId()));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
		outState.putParcelableArrayList("openTracks", openTracks);
		outState.putParcelableArrayList("ongoingTracks", ongoingTracks);
	}


	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.gpstracker_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.new_track:
	            newTrack();
	            return true;
	        case R.id.settings:
	        	Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
				startActivity(settingsIntent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void newTrack(){
		context = getActivity();
		
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle("Enter Trackname");

        alert.setMessage("Trackname:");
        // Set an EditText view to get user input 
        final EditText trackname = new EditText(getActivity());
        alert.setView(trackname);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	@Override
        	public void onClick(DialogInterface dialog, int whichButton) {
		          String trname = trackname.getText().toString();
		          Track track = new Track();
		          track.setName(trname);
		          openTracks.add(track);
		          updateTracklist();
	        }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
        	
        });
        alert.show();

	}
}
