package com.cit.ids;

import java.util.ArrayList;
import com.cit.ids.navdrawer.NavDrawerItem;
import com.cit.ids.navdrawer.NavDrawerItemListAdapter;
import com.cit.ids.networking.SignupHandler;
import com.cit.ids.utilities.ThemeHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SignupActivity extends Activity {

	private String vFName = null, vLName = null, vPass = null, vPassConf = null, vEmail = null,
			vFContact = null, vSContact = null, vSecretCode = null, vVehicleNo = null, vAddress = null;
	private SharedPreferences vPref = null;
	
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
		setContentView(R.layout.activity_signup);
		
		/*applying the saved theme*/
		new ThemeHandler(this).applyThemeActivity(this);
		
		vPref = getSharedPreferences("CITIDSData", MODE_PRIVATE);
		
		/*initializing the navigation drawer*/
		initializeNavDrawerMenu();
	}
	
	private void getUserData() {
		vFName = ((EditText)findViewById(R.id.editTextNameFirst)).getText().toString();
		vLName = ((EditText)findViewById(R.id.editTextNameLast)).getText().toString();
		vPass = ((EditText)findViewById(R.id.editTextPassword)).getText().toString();
		vPassConf = ((EditText)findViewById(R.id.editTextPasswordConfirm)).getText().toString();
		vEmail = ((EditText)findViewById(R.id.editTextEmailLogin)).getText().toString();
		vFContact = ((EditText)findViewById(R.id.editTextContactFirst)).getText().toString();
		vSContact = ((EditText)findViewById(R.id.editTextContactSecond)).getText().toString();
		vSecretCode = ((EditText)findViewById(R.id.editTextSecretCode)).getText().toString();
		vVehicleNo = ((EditText)findViewById(R.id.editTextVehicleNo)).getText().toString();
		vAddress = "XYZ";
	}
	
	public void saveButtonPressed(View view) {
		if(validateUserInfo()) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    	alert.setTitle("Confirmation").setCancelable(false);
	    	alert.setMessage("Do you want to continue?");
	    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface vDialog, int vWhich) {
					createNewUser();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface vDialog, int vWhich) {
					vDialog.cancel();
				}
			});
	    	AlertDialog alertDialog = alert.create();
	    	alertDialog.show();
		}
	}
	
	/*calling async task*/
	private void createNewUser() {
		new SignupUser(this).execute();
	}
	
	/*validating the data entered by user*/
	private Boolean validateUserInfo() {
		getUserData();
		
		/*checking for empty boxes*/
		if(vFName.isEmpty() || vLName.isEmpty() || vPass.isEmpty() || vPassConf.isEmpty() 
							|| vEmail.isEmpty() || vFContact.isEmpty() || vSContact.isEmpty() 
							|| vSecretCode.isEmpty() || vVehicleNo.isEmpty() || vAddress.isEmpty()) {
			Toast.makeText(this, "Error! Please fill all your info", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		/*checking for spaces in data*/
		if(vFName.contains(" ") || vLName.contains(" ") || vPass.contains(" ") || vPassConf.contains(" ") 
							|| vEmail.contains(" ") || vFContact.contains(" ") || vSContact.contains(" ") 
							|| vSecretCode.contains(" ") || vVehicleNo.contains(" ") || vAddress.contains(" ")) {
			Toast.makeText(this, "Error! Space is not allowed", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		/*if pass & confirm pass doesn't match*/
		if(!vPassConf.equals(vPass)) {
			Toast.makeText(this, "Error! Password not matched", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	
	/*returning to the main activity*/
	public void launchCITMain() {
		finish();
	}
	
	/*invoke the google map v2 activity*/
	public void launchGoogleMap() {
		startActivity(new Intent(SignupActivity.this, GoogleMapActivity.class));
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
		startActivity(new Intent(SignupActivity.this, ThemeActivity.class));
		finish();
	}
	
	/*invoke the ip port activity*/
	public void launchIPPort() {
		startActivity(new Intent(SignupActivity.this, IPPortActivity.class));
		finish();
	}
	
	/*invoke the help activity*/
	public void launchHelp() {
		startActivity(new Intent(SignupActivity.this, HelpActivity.class));
		finish();
	}
	
	/*invoke the about activity*/
	public void launchAbout() {
		startActivity(new Intent(SignupActivity.this, AboutActivity.class));
		finish();
	}
	
	/*initializing navigation drawer*/
	private void initializeNavDrawerMenu() {
		/*getting names of navigation menu items from string resource*/
		vNavMenuItemNames = getResources().getStringArray(R.array.navdrawer_itemsname);
		
		/*getting icons of navigation menu items from string resource*/
		vNavMenuIcons = getResources().obtainTypedArray(R.array.navdrawer_itemsicon);
		
		vDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayoutSignup);
		vDrawerList = (ListView)findViewById(R.id.listViewNavMenu);
		
		vNavDrawerItems = new ArrayList<NavDrawerItem>();
		
		/*getting the full name of user from shared preferences*/
		String vUserName = vPref.getString("fname", "User") + " " + vPref.getString("lname", "");
		
		/*adding the header to the navigation menu*/
		vNavDrawerItems.add(new NavDrawerItem(vUserName, R.drawable.ic_navuserpic));
		/*adding the items to the navigation menu*/
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[0], vNavMenuIcons.getResourceId(0, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[1], vNavMenuIcons.getResourceId(1, -1)));
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
            		launchAbout();
            		break;
            	case 7:
            		launchIPPort();
            		break;
			}
		}
	}
	
	/*async task to create a new user*/
	private class SignupUser extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog vDialog;
		private Context vContext;
		private SharedPreferences vIPPortPref = null;
		
		public SignupUser(Context vContext) {
			this.vContext = vContext;
			/*initializing the shared preference database for host ip & port*/
			vIPPortPref = vContext.getSharedPreferences("CITIDSIPPort", Context.MODE_PRIVATE);
		}
		
		/*showing the wait dialog and suspending GUI*/
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
		protected Integer doInBackground(Void... vArgs) {
			try {
				return new SignupHandler(vContext).signUpUser(vIPPortPref.getString("ipport", "localhost:8085"),
						vFName, vLName, vPass, vEmail, vFContact, vSContact, vSecretCode, vVehicleNo, vAddress);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return -1;
		}
		
		@Override
		protected void onPostExecute(Integer vResult) {	
			
			/*hiding the wait dialog if enabled*/
			if(vDialog.isShowing()) {
				vDialog.dismiss();
			}
			if(vResult == 1) {
				Toast.makeText(vContext, "Successfully registered your request.", Toast.LENGTH_SHORT).show();
				Toast.makeText(vContext, "Please verify your email to use our services.", Toast.LENGTH_SHORT).show();
			} else if(vResult == 0) {
				Toast.makeText(vContext, "Error! unnable to create new account", Toast.LENGTH_SHORT).show();
			} else {
				/*time or host unreachable*/
				Toast.makeText(vContext, "Error! host unreachable.", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(vResult);
		}
	}
}