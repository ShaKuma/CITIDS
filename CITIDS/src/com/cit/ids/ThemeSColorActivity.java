package com.cit.ids;

import java.util.ArrayList;
import com.cit.ids.navdrawer.NavDrawerItem;
import com.cit.ids.navdrawer.NavDrawerItemListAdapter;
import com.cit.ids.utilities.ThemeHandler;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ThemeSColorActivity extends Activity  {

	private int vColor = 0;
	private Boolean vIsSolidColorTheme = null;
	private ThemeHandler vThemeHandler = null;
	private TextView vTxtViewRed = null, vTxtViewGreen = null, vTxtViewBlue = null;
	private SeekBar vSeekBarRed = null, vSeekBarGreen = null, vSeekBarBlue = null;
	private Boolean vIsThemeApplied = null;
	
	/*variables for creating navigation drawer*/
	private DrawerLayout vDrawerLayout;
	private ListView vDrawerList;
	private String[] vNavMenuItemNames;
	private TypedArray vNavMenuIcons;
	private ArrayList<NavDrawerItem> vNavDrawerItems;
	private NavDrawerItemListAdapter vAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_themescolor);
		
		initialiseCompo();
	}
	
	private void initialiseCompo() {
		
		/*set the saved theme*/
		vThemeHandler = new ThemeHandler(this);
		vThemeHandler.applyThemeActivity(this);
		vIsThemeApplied = false;
		
		/*set the value for vIsSolidColorTheme*/
		vIsSolidColorTheme = vThemeHandler.isSavedSCTheme();
		
		/*get the value of saved color*/
		if(vIsSolidColorTheme) {
			vColor = vThemeHandler.getThemeResource();
		}
		
		/*Initializing the reference variables*/
		vSeekBarRed = (SeekBar)findViewById(R.id.seekBarRed);
		vSeekBarGreen = (SeekBar)findViewById(R.id.seekBarGreen);
		vSeekBarBlue = (SeekBar)findViewById(R.id.seekBarBlue);
		vTxtViewRed = (TextView)findViewById(R.id.textViewRedSBValue);
		vTxtViewGreen = (TextView)findViewById(R.id.textViewGreenSBValue);
		vTxtViewBlue = (TextView)findViewById(R.id.textViewBlueSBValue);
		
		/*Initializing progress of the seek bars as per the current theme*/
		setSeekBarsProgress(vColor);
		
		/*action listener for all the three seek bars*/
		vSeekBarRed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				seekBarChanged();
			}
		});
		vSeekBarGreen.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				seekBarChanged();
			}
		});
		vSeekBarBlue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				seekBarChanged();
			}
		});
		
		/*initializing the navigation drawer*/
		initializeNavDrawerMenu();
	}

	/*change the values of the seek bars*/
	private void setSeekBarsValue() {
		vTxtViewRed.setText(vSeekBarRed.getProgress() + "");
		vTxtViewGreen.setText(vSeekBarGreen.getProgress() + "");
		vTxtViewBlue.setText(vSeekBarBlue.getProgress() + "");
	}
	
	/*change the progress bar of the seek bars according to the current theme*/
	private void setSeekBarsProgress(int vColor) {
		vSeekBarRed.setProgress(Color.red(vColor));
		vSeekBarGreen.setProgress(Color.green(vColor));
		vSeekBarBlue.setProgress(Color.blue(vColor));
		vIsSolidColorTheme = true;
		setSeekBarsValue();
	}
	
	/*called when any one the three seek bars changed*/
	private void seekBarChanged() {
		setSeekBarsValue();
		int vColor = Color.rgb(vSeekBarRed.getProgress(),
								vSeekBarGreen.getProgress(),
								vSeekBarBlue.getProgress());
		setPreviewBackgroundColor(vColor);
	}
	
	/*applying background color to preview screen*/
	public void applyThemePSPredefined(View vView) {
		int vTempColor = 0;
		switch(vView.getId()) {
			case R.id.buttonRedIcon:
				vTempColor = Color.rgb(200, 0, 0);
				break;
			case R.id.buttonGreenIcon:
				vTempColor = Color.rgb(0, 128, 0);
				break;
			case R.id.buttonBlueIcon:
				vTempColor = Color.rgb(0, 0, 150);
				break;
			case R.id.buttonPurpleIcon:
				vTempColor = Color.rgb(128, 0, 128);
				break;
				
		}
		setPreviewBackgroundColor(vTempColor);
	}
	
	/*applying the preview theme to the application*/
	public void applyButtonPressed(View view) {
		//setActivityBackgroundColor(vTempColor, true);
		vThemeHandler.applyThemeActivity(this, vIsSolidColorTheme, vColor, true);
		vIsThemeApplied = true;
	}
	
	/*To change the background color of the preview screen without saving*/
	private void setPreviewBackgroundColor(int vTempColor) {
		setSeekBarsProgress(vTempColor);
		vColor = vTempColor;
		vThemeHandler.applyThemeView(findViewById(R.id.scrollViewPreview), vIsSolidColorTheme, vTempColor, false);
	}
	
	/*returning to the main activity*/
	public void launchCITMain() {
		finish();
	}
	
	/*invoke the google map v2 activity*/
	public void launchGoogleMap() {
		startActivity(new Intent(ThemeSColorActivity.this, GoogleMapActivity.class));
		finish();
	}
	
	/*invoke the native google map navigation activity*/
	public void launchGoogleNavigation() {
		try {
			Intent vIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="));
			vIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			startActivity(vIntent);
			finish();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*invoke the ip port activity*/
	public void launchIPPort() {
		startActivity(new Intent(ThemeSColorActivity.this, IPPortActivity.class));
		finish();
	}
	
	/*invoke the help activity*/
	public void launchHelp() {
		startActivity(new Intent(ThemeSColorActivity.this, HelpActivity.class));
		finish();
	}
	
	/*invoke the about activity*/
	public void launchAbout() {
		startActivity(new Intent(ThemeSColorActivity.this, AboutActivity.class));
		finish();
	}
	
	/*initializing navigation drawer*/
	private void initializeNavDrawerMenu() {
		/*getting names of navigation menu items from string resource*/
		vNavMenuItemNames = getResources().getStringArray(R.array.navdrawer_itemsname);
		
		/*getting icons of navigation menu items from string resource*/
		vNavMenuIcons = getResources().obtainTypedArray(R.array.navdrawer_itemsicon);
		
		vDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayoutThemeSColor);
		vDrawerList = (ListView)findViewById(R.id.listViewNavMenu);
		
		vNavDrawerItems = new ArrayList<NavDrawerItem>();
		
		/*getting the full name of user from shared preferences*/
		SharedPreferences vPref = getSharedPreferences("CITIDSData", MODE_PRIVATE);
		String vUserName = vPref.getString("fname", "User") + " " + vPref.getString("lname", "");
		
		/*adding the header to the navigation menu*/
		vNavDrawerItems.add(new NavDrawerItem(vUserName, R.drawable.ic_navuserpic));
		/*adding the items to the navigation menu*/
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[0], vNavMenuIcons.getResourceId(0, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[1], vNavMenuIcons.getResourceId(1, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[2], vNavMenuIcons.getResourceId(2, -1)));
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
            		launchGoogleMap();
            		break;
            	case 3:
            		launchGoogleNavigation();
            		break;
            	case 4:
            		launchHelp();
            		break;
            	case 5:
            		launchAbout();
            		break;
            	case 6:
            		launchIPPort();
            		break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d("ThemeSColor", "OnResume");
		/*printing  the theme changed operation*/
		if(vIsThemeApplied) {
			vIsThemeApplied = false;
			Toast.makeText(this, "Your theme has been changed " +
								"successfully.", Toast.LENGTH_SHORT).show();
		}
		super.onDestroy();
	}
}