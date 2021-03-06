package com.cit.ids;

import java.util.ArrayList;
import com.cit.ids.navdrawer.NavDrawerItem;
import com.cit.ids.navdrawer.NavDrawerItemListAdapter;
import com.cit.ids.utilities.ThemeHandler;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class AboutActivity extends Activity {
	
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
		setContentView(R.layout.activity_about);
		
		/*applying the saved theme*/
		new ThemeHandler(this).applyThemeActivity(this);
		
		/*initializing the navigation drawer*/
		initializeNavDrawerMenu();
	}
	
	/*returning to the main activity*/
	public void launchCITMain() {
		finish();
	}
	
	/*invoke the google map v2 activity*/
	public void launchGoogleMap() {
		startActivity(new Intent(AboutActivity.this, GoogleMapActivity.class));
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
	
	/*invoke the theme selector activity*/
	public void launchThemeSelector() {
		startActivity(new Intent(AboutActivity.this, ThemeActivity.class));
		finish();
	}
	
	/*invoke the ipport activity*/
	public void launchIPPort() {
		startActivity(new Intent(AboutActivity.this, IPPortActivity.class));
		finish();
	}
	
	/*invoke the about activity*/
	public void launchHelp() {
		startActivity(new Intent(AboutActivity.this, HelpActivity.class));
		finish();
	}
	
	/*initializing navigation drawer*/
	private void initializeNavDrawerMenu() {
		/*getting names of navigation menu items from string resource*/
		vNavMenuItemNames = getResources().getStringArray(R.array.navdrawer_itemsname);
		
		/*getting icons of navigation menu items from string resource*/
		vNavMenuIcons = getResources().obtainTypedArray(R.array.navdrawer_itemsicon);
		
		vDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayoutAbout);
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
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[4], vNavMenuIcons.getResourceId(4, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[5], vNavMenuIcons.getResourceId(5, -1)));
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
			/*perform the action according to selected item i.e. vPosition(start from 0)*/
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
            		launchThemeSelector();
            		break;
            	case 5:
            		launchHelp();
            		break;
            	case 6:
            		launchIPPort();
            		break;
			}
		}
	}
}