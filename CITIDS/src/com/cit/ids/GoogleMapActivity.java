package com.cit.ids;

import java.util.ArrayList;
import com.cit.ids.navdrawer.NavDrawerItem;
import com.cit.ids.navdrawer.NavDrawerItemListAdapter;
import com.cit.ids.networking.Place;
import com.cit.ids.networking.PlacesHandler;
import com.cit.ids.networking.ConnectivityChecker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class GoogleMapActivity extends Activity implements ConnectionCallbacks, OnCameraChangeListener, 
									OnConnectionFailedListener, LocationListener, OnMapLongClickListener, OnClickListener {
	
	private static final int MIN_TIME_FOR_LOC_UPDATE = 2000;
	private static final int DEFAULT_ZOOM = 17;
	
	private LocationClient vLocationClient = null;
	private GoogleMap vGoogleMap = null;
	private Location vLocation = null;
	private Boolean vIsInitialLocation = null;
	private Boolean vIsNormalMap = null;
	private Boolean vIsLocationMarkerVisible = null;
	private Boolean vIsUserChangedCamera = null;
	private String[] vPlacesCategories = null;
	private Dialog vDialogPlace = null;
	private LatLng vPlaceLatLng = null;
	
	/*variables for creating navigation drawer*/
	private DrawerLayout vDrawerLayout;
	private ListView vDrawerList;
	private String[] vNavMenuItemNames;
	private TypedArray vNavMenuIcons;
	private ArrayList<NavDrawerItem> vNavDrawerItems;
	private NavDrawerItemListAdapter vAdapter;

	/*Location updates request for high accuracy updates*/
	private static final LocationRequest LOCATION_REQUEST_HIGH_ACCURACY = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(MIN_TIME_FOR_LOC_UPDATE);
	
	/*Location updates request with no power/power saver mode*/
	private static final LocationRequest LOCATION_REQUEST_NO_POWER = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_NO_POWER);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_googlemap);
		
		/*initializing flags*/
		vIsNormalMap = true;
		vIsInitialLocation = true;
		vIsLocationMarkerVisible = true;
		vIsUserChangedCamera = true;
		
		/*initializing the map*/
		initializeMap();
		
		/*initializing the navigation drawer*/
		initializeNavDrawerMenu();
		
		/*getting the list of places category*/
    	vPlacesCategories = getResources().getStringArray(R.array.places_categories);
    	
    	/*creating custom dialog box*/
    	createPlaceDialog();
	}
	
	/*initialize google map*/
	private void initializeMap() {
		Log.d("GoogleMap", "Initiliazing map");
		vGoogleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.fragmentGoogleMap)).getMap();
		if(vGoogleMap == null) {
            Toast.makeText(this, "Error! unable to create maps", Toast.LENGTH_SHORT).show();
        } else {
        	vGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        	vGoogleMap.getUiSettings().setCompassEnabled(true);
            vGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        	vGoogleMap.setMyLocationEnabled(true);
        	vGoogleMap.setBuildingsEnabled(true);
        	vGoogleMap.setIndoorEnabled(true);
        	vGoogleMap.setTrafficEnabled(true);
        	
        	/*setting various map listeners*/
        	vGoogleMap.setOnMapLongClickListener(this);
        	vGoogleMap.setOnCameraChangeListener(this);
        	
        	vLocationClient = new LocationClient(getApplicationContext(), this, this);
        	if(vLocationClient != null) {
        		vLocationClient.connect();
        	}
        }
    }
	
	/*update camera position with smooth animation as per the new location*/
	private void updateLocation() {
		if(vIsInitialLocation) {
			vGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
				.target(new LatLng(vLocation.getLatitude(), vLocation.getLongitude()))
				.bearing(vGoogleMap.getCameraPosition().bearing)
				.tilt(vGoogleMap.getCameraPosition().tilt)
				.zoom(DEFAULT_ZOOM)
				.build())
			);
			vIsInitialLocation = false;
		} else {
			vGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
				.target(new LatLng(vLocation.getLatitude(), vLocation.getLongitude()))
				.bearing(vGoogleMap.getCameraPosition().bearing)
				.tilt(vGoogleMap.getCameraPosition().tilt)
				.zoom(vGoogleMap.getCameraPosition().zoom)
				.build())
			);
		}
		vIsUserChangedCamera = false;
	}
	
	/*check every time camera change that user location marker is visible or not*/
	@Override
	public void onCameraChange(CameraPosition vCameraPosition) {
		if(vIsUserChangedCamera && vLocation != null) {
			LatLng vLatLng = new LatLng(vLocation.getLatitude(), vLocation.getLongitude());
			if(vGoogleMap.getProjection().getVisibleRegion().latLngBounds.contains(vLatLng)) {
				/*if earlier hidden but now visible then move location marker*/
				if(!vIsLocationMarkerVisible) {
					updateLocation();
				}
				vIsLocationMarkerVisible = true;
			} else {
				vIsLocationMarkerVisible = false;
			}
		}
		vIsUserChangedCamera = true;
	}
	
	/*creating custom dialog for places*/
	private void createPlaceDialog() {
		vDialogPlace = new Dialog(this);

        vDialogPlace.setContentView(R.layout.dialog_places);
        vDialogPlace.setTitle("Please select an item");
        vDialogPlace.setCanceledOnTouchOutside(true);
        
        /*adding on click listener*/
        vDialogPlace.findViewById(R.id.textViewPlaceATM).setOnClickListener(this);
        vDialogPlace.findViewById(R.id.textViewPlaceBank).setOnClickListener(this);
        vDialogPlace.findViewById(R.id.textViewPlaceCafe).setOnClickListener(this);
        vDialogPlace.findViewById(R.id.textViewPlaceFireStation).setOnClickListener(this);
        vDialogPlace.findViewById(R.id.textViewPlaceHospital).setOnClickListener(this);
        vDialogPlace.findViewById(R.id.textViewPlacePharmacy).setOnClickListener(this);
        vDialogPlace.findViewById(R.id.textViewPlacePolice).setOnClickListener(this);
        vDialogPlace.findViewById(R.id.textViewPlaceRestaurant).setOnClickListener(this);
        vDialogPlace.findViewById(R.id.textViewPlaceShopMall).setOnClickListener(this);
	}
	
	/*map long click listener for changing map type*/
	@Override
	public void onMapLongClick(LatLng vLatLng) {
		if(new ConnectivityChecker(this).isConnectedToInternet()) {
			vPlaceLatLng = vLatLng;
			vDialogPlace.show();
		} else {
			Toast.makeText(this, "Error! not connected to the internet", Toast.LENGTH_SHORT).show();
		}
	}
	
	/*calling async task with selected place category*/
	@Override
	public void onClick(View vView) {
		Log.e("GoogleMap", "Dialog method called");
		vDialogPlace.dismiss();
		vGoogleMap.clear();
		int vIndexPlace = 0;
		switch(vView.getId()) {
			case R.id.textViewPlaceATM:
				vIndexPlace = 0;
				break;
			case R.id.textViewPlaceBank:
				vIndexPlace = 1;
				break;
			case R.id.textViewPlaceCafe:
				vIndexPlace = 2;
				break;
			case R.id.textViewPlaceFireStation:
				vIndexPlace = 3;
				break;
			case R.id.textViewPlaceHospital:
				vIndexPlace = 4;
				break;
			case R.id.textViewPlacePharmacy:
				vIndexPlace = 5;
				break;
			case R.id.textViewPlacePolice:
				vIndexPlace = 6;
				break;
			case R.id.textViewPlaceRestaurant:
				vIndexPlace = 7;
				break;
			case R.id.textViewPlaceShopMall:
				vIndexPlace = 8;
				break;
		}
		new ShowPlacesOnMapAsync(this, vPlacesCategories[vIndexPlace]).execute();
	}
	
	/*location changed action listener*/
	@Override
	public void onLocationChanged(Location vLocation) {
		Log.e("GoogleMap", "Location Changed");
		this.vLocation = vLocation;
		/*update camera if user location marker is visible*/
		if(vIsLocationMarkerVisible) {
			updateLocation();
		}
	}
	
	/*location client connected and ready*/
	@Override
	public void onConnected(Bundle vBundle) {
		Log.e("GoogleMap", "LocClient Connected");
		vLocationClient.requestLocationUpdates(LOCATION_REQUEST_HIGH_ACCURACY, this);
	}
	
	/*returning to the main activity*/
	private void launchCITMain() {
		finish();
	}
	
	/*change map view between normal & satellite*/
	private void changeMapView() {
		if(vIsNormalMap) {
			vGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			Toast.makeText(this, "Map type changed to SATELLITE", Toast.LENGTH_SHORT).show();
			vIsNormalMap = false;
		} else {
			vGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			Toast.makeText(this, "Map type changed to NORMAL", Toast.LENGTH_SHORT).show();
			vIsNormalMap = true;
		}
	}
	
	/*invoke the native google map navigation activity*/
	private void launchGoogleNavigation() {
		try {
			Intent vIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="));
			vIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			startActivity(vIntent);
			finish();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*invoke the theme selector activity*/
	private void launchThemeSelector() {
		startActivity(new Intent(GoogleMapActivity.this, ThemeActivity.class));
		finish();
	}
	
	/*invoke the ip port activity*/
	private void launchIPPort() {
		startActivity(new Intent(GoogleMapActivity.this, IPPortActivity.class));
		finish();
	}
	
	/*invoke the help activity*/
	private void launchHelp() {
		startActivity(new Intent(GoogleMapActivity.this, HelpActivity.class));
		finish();
	}
	
	/*invoke the about activity*/
	private void launchAbout() {
		startActivity(new Intent(GoogleMapActivity.this, AboutActivity.class));
		finish();
	}
	
	/*initializing navigation drawer*/
	private void initializeNavDrawerMenu() {
		/*getting names of navigation menu items from string resource*/
		vNavMenuItemNames = getResources().getStringArray(R.array.navdrawer_itemsname);
		
		/*getting icons of navigation menu items from string resource*/
		vNavMenuIcons = getResources().obtainTypedArray(R.array.navdrawer_itemsicon);
		
		vDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayoutGoogleMap);
		vDrawerList = (ListView)findViewById(R.id.listViewNavMenu);
		
		vNavDrawerItems = new ArrayList<NavDrawerItem>();
		
		/*getting the full name of user from shared preferences*/
		SharedPreferences vPref = getSharedPreferences("CITIDSData", MODE_PRIVATE);
		String vUserName = vPref.getString("fname", "User") + " " + vPref.getString("lname", "");
		
		/*adding the header to the navigation menu*/
		vNavDrawerItems.add(new NavDrawerItem(vUserName, R.drawable.ic_navuserpic));
		/*adding the items to the navigation menu*/
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[0], vNavMenuIcons.getResourceId(0, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[7], vNavMenuIcons.getResourceId(7, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[2], vNavMenuIcons.getResourceId(2, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[4], vNavMenuIcons.getResourceId(4, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[5], vNavMenuIcons.getResourceId(5, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[6], vNavMenuIcons.getResourceId(6, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[8], vNavMenuIcons.getResourceId(8, -1)));
		/*recycling the typed array*/
		vNavMenuIcons.recycle();
		
		/*adding the onitemclick action listener*/
		vDrawerList.setOnItemClickListener(new NavItemOnClickListener());

		/*setting the navdrawerlist adapter*/
		vAdapter = new NavDrawerItemListAdapter(getApplicationContext(), vNavDrawerItems);
		vDrawerList.setAdapter(vAdapter);
	}
	
	/*navigation menu item click listener*/
	private class NavItemOnClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> vParent, View vView, int vPosition, long vId) {
			/*closing the drawer menu if not clicked on header*/
			if(vPosition != 0) {
				vDrawerLayout.closeDrawers();
			}
			/*perform the action according to selected item i.e. vPosition*/
			switch (vPosition) {
            	case 1:
            		launchCITMain();
            		break;
            	case 2:
            		changeMapView();
            		break;
            	case 3:
            		launchGoogleNavigation();
            		break;
            	case 4:
            		launchThemeSelector();
            		break;
            	case 5:
            		launchHelp();
            		break;
            	case 6:
            		launchAbout();
            		break;
            	case 7:
            		launchIPPort();
            		break;
			}
		}
	}
	
	/*async task to show places on google map*/
	private class ShowPlacesOnMapAsync extends AsyncTask<Void, Void, ArrayList<Place>> {

		private ProgressDialog vDialog;
		private Context vContext;
		private String vPlaceCategory;

		public ShowPlacesOnMapAsync(Context vContext, String vPlaceCategory) {
			this.vContext = vContext;
			this.vPlaceCategory = vPlaceCategory;
		}
		
		/*showing the wait dialog and suspend gui*/
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			vDialog = new ProgressDialog(vContext);
			vDialog.setCancelable(false);
			vDialog.setMessage("Loading ...");
			vDialog.isIndeterminate();
			vDialog.show();
		}

		@Override
		protected ArrayList<Place> doInBackground(Void... vArgs) {
			ArrayList<Place> vFoundPlaces = null;
			try {
				vFoundPlaces = new PlacesHandler().findPlaces(vPlaceLatLng.latitude,
													vPlaceLatLng.longitude, vPlaceCategory);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return vFoundPlaces;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Place> vPlacesResult) {	
			
			/*hiding the wait dialog if enabled*/
			if(vDialog.isShowing()) {
				vDialog.dismiss();
			}
			if(vPlacesResult == null) {
				/*time or host unreachable*/
				Toast.makeText(vContext, "Error! host unreachable", Toast.LENGTH_SHORT).show();
			} else if(vPlacesResult.size() == 0) {
				/*if no place found*/
				Toast.makeText(vContext, "Sorry! no place found", Toast.LENGTH_SHORT).show();
			} else {
				/*Placing the markers on the map*/
				for(int i=0; i<vPlacesResult.size(); i++) {
					vGoogleMap.addMarker(new MarkerOptions()
							.title(vPlacesResult.get(i).getName())
							.snippet(vPlacesResult.get(i).getVicinity())
							.position(new LatLng(vPlacesResult.get(i).getLatitude(),
												vPlacesResult.get(i).getLongitude()))
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
				}	
				/*Sets the center of the map to Mountain View*/
				CameraPosition vCameraPosition = new CameraPosition.Builder()
						.target(new LatLng(vPlacesResult.get(0).getLatitude(), vPlacesResult.get(0).getLongitude())) 
						.zoom(14)
						.tilt(30)
						.build(); 
				vGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(vCameraPosition));
			}
			super.onPostExecute(vPlacesResult);
		}
	}
	
	@Override
	protected void onResume() {
		Log.d("GoogleMap", "OnResume");
		if(vLocationClient.isConnected()) {
			vLocationClient.requestLocationUpdates(
    			LOCATION_REQUEST_HIGH_ACCURACY, this);
    	}
		/*closing the drawer menu if opened*/
    	if(vDrawerLayout.isDrawerOpen(vDrawerList)) {
    		vDrawerLayout.closeDrawers();
    	}
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.d("GoogleMap", "OnPause");
		if(vLocationClient.isConnected()) {
			vLocationClient.requestLocationUpdates(
				LOCATION_REQUEST_NO_POWER, this);
		}
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		Log.d("GoogleMap", "OnDestroy");
		if(vLocationClient.isConnected()) {
    		vLocationClient.disconnect();
    	}
		super.onDestroy();
	}
	
	/*other unused methods*/
	@Override
	public void onConnectionFailed(ConnectionResult vConnectionResult) {}
	@Override
	public void onDisconnected() {}
}