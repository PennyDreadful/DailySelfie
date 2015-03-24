package course.dailyselfie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class DailySelfieActivity extends ActionBarActivity {

	private static final int ACTION_TAKE_PHOTO_B = 1;

	private PictureAdapter mAdapter;
	private ListView mSelfieList;
	private AlarmManager mAlarmManager;
	private static final int ALARM_INTERVAL = 1000*60*2;
	

	private String mCurrentPhotoPath;
	private static final String SELFIE_LOCATIONS = "SelfieData.txt";
	
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	
	public static final String IMAGE_PATH_KEY = "imagepathkey";
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.daily_selfie_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_selfie:
	        	dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	

	@Override
	protected void onPause() {
		super.onPause();
		try{
			storeSelfies();
		} catch (IOException iOEx){
			iOEx.printStackTrace();
		}
		
	}  
	
	private void storeSelfies() throws FileNotFoundException {

		FileOutputStream fos = openFileOutput(SELFIE_LOCATIONS, MODE_PRIVATE);

		PrintWriter pw = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(fos)));

		for(String s : mAdapter.getList()){
			pw.println(s);
		}

		pw.close();

	}
	
	private ArrayList<String> retrieveSelfies() throws IOException{
		ArrayList<String> selfies = new ArrayList<>();
		
		try (FileInputStream fis = openFileInput(SELFIE_LOCATIONS);
			 BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
			
			String line = "";		
			while (null != (line = br.readLine())) {
				selfies.add(line);	
			}
		} catch (FileNotFoundException ex){
			// no need to do anything here, since this happens on the first
			// run of the app, when there is no selfie catalog.
		}
	
		
		return selfies;

	}

	
	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();		
		return f;
	}





	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		switch(actionCode) {
		case ACTION_TAKE_PHOTO_B:
			File f = null;
			
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
			break;

		default:
			break;			
		} // switch

		startActivityForResult(takePictureIntent, actionCode);
	}


	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			mAdapter.add(mCurrentPhotoPath);
			mAdapter.notifyDataSetChanged();
			mCurrentPhotoPath = null;
		} 
		
	

	}



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		
	
		
		mAdapter = new PictureAdapter(this);
		
		
		// Load the previously taken selfies, if there are any
	    ArrayList<String> selfies = null;
	    try {
	    	 selfies = retrieveSelfies();
	    } catch (IOException iOEx){
	    	iOEx.printStackTrace();
	    }
	    if(selfies != null){
	    	mAdapter.setList(selfies);
	    } 
 
		
		mSelfieList = (ListView) findViewById(R.id.selfie_list);		
		mSelfieList.setAdapter(mAdapter);	
		mSelfieList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String imagePath = (String) parent.getItemAtPosition(position);
				Intent i = new Intent(DailySelfieActivity.this, SelfieDisplayActivity.class);
				i.putExtra(IMAGE_PATH_KEY, imagePath);
				startActivity(i);
			}
		}); 
		
		
		// Get the AlarmManager Service
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		// Create an Intent to broadcast to the AlarmNotificationReceiver
		Intent notificationReceiverIntent = new Intent(DailySelfieActivity.this,
				AlarmNotificationReceiver.class);

		// Create an PendingIntent that holds the NotificationReceiverIntent
		PendingIntent notificationReceiverPendingIntent = PendingIntent.getBroadcast(
				DailySelfieActivity.this, 0, notificationReceiverIntent, 0);
		
		
		// Set repeating alarm to inform the user that it's time to take a selfie
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + ALARM_INTERVAL, ALARM_INTERVAL,
				notificationReceiverPendingIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO_B: {
			if (resultCode == RESULT_OK) {
				handleBigCameraPhoto();
			}
			break;
		} // ACTION_TAKE_PHOTO_B
		} // switch
	}


	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}



}