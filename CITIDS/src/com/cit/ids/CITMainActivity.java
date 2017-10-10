package com.cit.ids;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import com.cit.ids.navdrawer.NavDrawerItem;
import com.cit.ids.navdrawer.NavDrawerItemListAdapter;
import com.cit.ids.networking.LoginHandler;
import com.cit.ids.networking.PassResetHandler;
import com.cit.ids.networking.ConnectivityChecker;
import com.cit.ids.utilities.LocationHandler;
import com.cit.ids.utilities.TTSHandler;
import com.cit.ids.utilities.ThemeHandler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.TypedArray;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class CITMainActivity extends Activity implements OnClickListener {

	/*static variables*/
	private static final int LOC_UPDATE_INTERVAL = 1 * 10 * 1000;
	private static final int INITIAL_LOC_UPDATE_DELAY = 7 * 1000;
	
	private static CITMainActivity vActivity = null;
	
	/*non static variables*/
	private TTSHandler vTTSHandler = null;
	private SharedPreferences vPref = null;
	private Context vContext = null;
	private ThemeHandler vThemeHandler = null;
	private Dialog vDialogLogin = null;
	private Dialog vDialogResetPass = null;
	private String vEmail = null;
	private String vPass = null; 
	private Handler vHandler = null;
	private Runnable vRefresher = null;
	private Boolean vIsLocationUpdating = false;
	private LocationHandler vLocHandler = null;
	
	/*variables for creating navigation drawer*/
	private DrawerLayout vDrawerLayout;
	private ListView vDrawerList;
	private String[] vNavMenuItemNames;
	private TypedArray vNavMenuIcons;
	private ArrayList<NavDrawerItem> vNavDrawerItems;
	private NavDrawerItemListAdapter vNavDrawerItemAdapter;
	
	/*variable for bluetooth handling*/
	private static final int MESSAGE_WRITE = 1;
	private static final int MESSAGE_READ = 2;
	
	private BluetoothAdapter vBluetoothAdapter = null;
	private BroadcastReceiver vDiscoverBTDevice = null;
	private BroadcastReceiver vSocketConnector = null;
	private BluetoothSocket vGlobalBTSocket = null;
	private Boolean vIsBTConnected = null;
	
	private static int vCurrentGear = 6;
	private static ImageView vImageViewGear1 = null;
	private static ImageView vImageViewGear2 = null;
	private static ImageView vImageViewGear3 = null;
	private static ImageView vImageViewGear4 = null;
	private static ImageView vImageViewGearNeutral = null;
	private static ImageView vImageViewGearReverse = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_citmain);
		
		/*applying the saved theme*/
		vThemeHandler = new ThemeHandler(this);
		vThemeHandler.applyThemeActivity(this);
		
		/*getting the context of this activity*/
		vContext = this;
		vActivity = this;
		
		/*initializing TTS service and adding initial voice message*/
		Log.d("CITMain", "Initializing TTS Service");
		vTTSHandler = new TTSHandler(vContext);
		
		/*initializing the shared preference database*/
		vPref = getSharedPreferences("CITIDSData", MODE_PRIVATE);
		
		/*initializing the navigation drawer*/
		initializeNavDrawerMenu();
		
		/*initialize background components*/
		new initialiseCompoAsync().execute();
		
		/*initializing gear status image view*/
		initializeImageView();
		
		/*if user is logged in*/
		if(vPref.getBoolean("loginstatus", false)) {
			vIsLocationUpdating = true;
			/*initializing location handler*/
			vLocHandler = new LocationHandler(this, false);
			/*initializing handler for repeated timer task*/
			initializeHandlerTask();
		}
	}
	
	/*initializing handler for repeated timer task*/
	private void initializeHandlerTask() {
		vHandler = new Handler();
		vRefresher = new Runnable() {
			public void run() {
				Log.d("CITMain","in runnable");
				
				if(new ConnectivityChecker(vContext).isConnectedToInternet()) {
					//new LocationHandler(vContext, true);
					while(!vLocHandler.isLocClientConnected());
					vLocHandler.updateLocOnServer();
				}
				vHandler.postDelayed(this, LOC_UPDATE_INTERVAL);
			}
		};
		vHandler.postDelayed(vRefresher, INITIAL_LOC_UPDATE_DELAY);
	} 
	
	/*creating custom dialog for login*/
	private void createLoginDialog() {
		vDialogLogin = new Dialog(this);
        vDialogLogin.setContentView(R.layout.dialog_login);
        vDialogLogin.setTitle("Login");
        vDialogLogin.setCanceledOnTouchOutside(false);
        
        /*attaching on click action listener*/
        (vDialogLogin.findViewById(R.id.buttonLogin)).setOnClickListener(this);
        (vDialogLogin.findViewById(R.id.textViewSignupLogin)).setOnClickListener(this);
        (vDialogLogin.findViewById(R.id.textViewForgotPassLogin)).setOnClickListener(this);
	}
	
	/*creating custom dialog for password reset*/
	private void createResetPassDialog() {
		vDialogResetPass = new Dialog(this);
        vDialogResetPass.setContentView(R.layout.dialog_forgotpass);
        vDialogResetPass.setTitle("Reset Password");
        vDialogResetPass.setCanceledOnTouchOutside(false);
        
        /*attaching on click action listener*/
        (vDialogResetPass.findViewById(R.id.buttonResetPass)).setOnClickListener(this);
	}
	
	/*onclick listener for login passreset dialog boxes*/
	@Override
	public void onClick(View vView) {
		switch(vView.getId()) {
			/*login*/
			case R.id.buttonLogin:
				vEmail = ((EditText)vDialogLogin.findViewById(R.id.editTextEmailLogin)).getText().toString();
				vPass = ((EditText)vDialogLogin.findViewById(R.id.editTextPassLogin)).getText().toString();
				/*if email & password is not empty*/
				if(!vEmail.isEmpty() && !vPass.isEmpty()) {
					vDialogLogin.dismiss();
					new LoginUserAsync(vContext).execute();
				} else {
					Toast.makeText(this, "Error! Email or password can't be blank", Toast.LENGTH_SHORT).show();
				}
				break;
			/*calling signup Activity*/
			case R.id.textViewSignupLogin:
				vDialogLogin.dismiss();
				startActivity(new Intent(CITMainActivity.this, SignupActivity.class));
				break;
			/*calling forgot pass dialog*/
			case R.id.textViewForgotPassLogin:
				//vDialogLogin.dismiss();
				createResetPassDialog();
				vDialogResetPass.show();
				break;
			/*calling reset pass async task*/
			case R.id.buttonResetPass:
				vEmail = ((EditText)vDialogResetPass.findViewById(R.id.editTextEmailFPass)).getText().toString();
				/*if email is not empty*/
				if(!vEmail.isEmpty() && vEmail.contains(" ")) {
					vDialogResetPass.dismiss();
					new ResetPassAsync(vContext).execute();
				} else {
					vTTSHandler.speakIt("Error! Email can't be blank or have spaces");
					//Toast.makeText(this, "Error! Email can't be blank or have spaces", Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}
	
	/*invoke the google map v2 activity*/
	public void launchGoogleMap() {
		startActivity(new Intent(CITMainActivity.this, GoogleMapActivity.class));
	}
	
	/*invoke the native google map navigation activity*/
	public void launchGoogleNavigation() {
		try {
			Intent vIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="));
			vIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			startActivity(vIntent);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*invoke car tracker signup activity*/
	public void launchCarTracker() {
		if(vPref.getBoolean("loginstatus", false)) {
			Toast.makeText(this, "You are already logged in.", Toast.LENGTH_SHORT).show();
		} else { 
			/*creating custom dialog box*/
	    	//createLoginDialog();
			//vDialogLogin.show();
		}
		/*temporary added the code for easy debugging*/
		/*creating custom dialog box*/
		if(new ConnectivityChecker(vContext).isConnectedToInternet()) {
			createLoginDialog();
			vDialogLogin.show();
		} else {
			/*internet is not available*/
			vTTSHandler.speakIt("Error! Not connected to the internet");
		}
	}
	
	/*invoke the theme selector activity*/
	public void launchThemeSelector() {
		startActivity(new Intent(CITMainActivity.this, ThemeActivity.class));
	}
	
	/*invoke the ip port activity*/
	public void launchIPPort() {
		startActivity(new Intent(CITMainActivity.this, IPPortActivity.class));
	}
	
	/*invoke the help activity*/
	public void launchHelp() {
		startActivity(new Intent(CITMainActivity.this, HelpActivity.class));
	}
	
	/*invoke the about activity*/
	public void launchAbout() {
		startActivity(new Intent(CITMainActivity.this, AboutActivity.class));
	}
	
	/*initializing navigation drawer*/
    private void initializeNavDrawerMenu() {
		/*getting names of navigation menu items from string resource*/
		vNavMenuItemNames = getResources().getStringArray(R.array.navdrawer_itemsname);
		
		/*getting icons of navigation menu items from string resource*/
		vNavMenuIcons = getResources().obtainTypedArray(R.array.navdrawer_itemsicon);
		
		vDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayoutCITMain);
		vDrawerList = (ListView)findViewById(R.id.listViewNavMenu);
		
		vNavDrawerItems = new ArrayList<NavDrawerItem>();
		
		/*getting the full name of user from shared preferences*/
		String vUserName = vPref.getString("fname", "User") + " " + vPref.getString("lname", "");
		
		/*adding the header to the navigation menu*/
		vNavDrawerItems.add(new NavDrawerItem(vUserName, R.drawable.ic_navuserpic));
		/*adding the items to the navigation menu*/
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[1], vNavMenuIcons.getResourceId(1, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[2], vNavMenuIcons.getResourceId(2, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[3], vNavMenuIcons.getResourceId(3, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[4], vNavMenuIcons.getResourceId(4, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[5], vNavMenuIcons.getResourceId(5, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[6], vNavMenuIcons.getResourceId(6, -1)));
		vNavDrawerItems.add(new NavDrawerItem(vNavMenuItemNames[8], vNavMenuIcons.getResourceId(8, -1)));
		/*recycling the typed array*/
		vNavMenuIcons.recycle();
		
		/*adding the onitemclick action listener*/
		vDrawerList.setOnItemClickListener(new NavItemOnClickListener());
		
		/*setting the navdrawerlist adapter*/
		vNavDrawerItemAdapter = new NavDrawerItemListAdapter(getApplicationContext(), vNavDrawerItems);
		vDrawerList.setAdapter(vNavDrawerItemAdapter);
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
            		launchGoogleMap();
            		break;
            	case 2:
            		launchGoogleNavigation();
            		break;
            	case 3:
            		launchCarTracker();
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
	
	/*async task to login user*/
	private class LoginUserAsync extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog vDialog;
		private Context vContext;
		private SharedPreferences vIPPortPref = null;

		public LoginUserAsync(Context vContext) {
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
		
		/*executing network task*/
		@Override
		protected Integer doInBackground(Void... vArgs) {
			try {
				return new LoginHandler(vContext).loginUser(vIPPortPref.getString
											("ipport", "localhost:8085"), vEmail, vPass);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return -1;
		}
		
		/*processing the result*/
		@Override
		protected void onPostExecute(Integer vResult) {	
			/*hiding the wait dialog if enabled*/
			if(vDialog.isShowing()) {
				vDialog.dismiss();
			}
			if(vResult == 1) {
				/*success*/
				//Toast.makeText(vContext, "Successfully logged in", Toast.LENGTH_SHORT).show();
				vTTSHandler.speakIt("Successfully logged in");
			} else if(vResult == 0) {
				/*invalid user credentials*/
				vTTSHandler.speakIt("Error! invalid credentials or email is not verified");
				//Toast.makeText(vContext, "Error! user doesn't exists or " 
				//                 + "email is not verified", Toast.LENGTH_LONG).show();
			} else {
				/*timeout or host unreachable*/
				vTTSHandler.speakIt("Error! Host unreachable");
				Toast.makeText(vContext, "Error! host unreachable", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(vResult);
		}
	}
	
	/*async task to reset user password*/
	private class ResetPassAsync extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog vDialog;
		private Context vContext;
		private SharedPreferences vIPPortPref = null;

		public ResetPassAsync(Context vContext) {
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

		/*executing network task*/
		@Override
		protected Integer doInBackground(Void... vArgs) {
			try {
				return new PassResetHandler().resetPassword(vIPPortPref.getString
										("ipport", "localhost:8085"), vEmail);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return -1;
		}
		
		/*processing the result*/
		@Override
		protected void onPostExecute(Integer vResult) {	
			/*hiding the wait dialog if enabled*/
			if(vDialog.isShowing()) {
				vDialog.dismiss();
			}
			if(vResult == 1) {
				/*success*/
				Toast.makeText(vContext, "Password successfully reseted", Toast.LENGTH_SHORT).show();
			} else if(vResult == 0) {
				/*invalid user credentials*/
				Toast.makeText(vContext, "Error! user doesn't exists", Toast.LENGTH_SHORT).show();
			} else {
				/*timeout or host unreachable*/
				Toast.makeText(vContext, "Error! host unreachable", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(vResult);
		}
	}
	
	/*async task to initialize background components*/
	private class initialiseCompoAsync extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog vDialog;
		
		/*showing the wait dialog and suspending GUI*/
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			vDialog = new ProgressDialog(vContext);
			vDialog.setCancelable(false);
			vDialog.setMessage("Initializing Components ...");
			vDialog.isIndeterminate();
			vDialog.show();
		}

		/*executing network task*/
		@Override
		protected Boolean doInBackground(Void... vArgs) {
			try {				
				/*initializing the Bluetooth device*/
				initializeBluetooth();
				
				/*waiting until bluetooth device got connected && connected to TTS service*/
				//while(!vIsBTConnected||!vTTSHandler.isConnected());
				
				return true;
			} catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		
		/*processing the result*/
		@Override
		protected void onPostExecute(Boolean vResult) {	
			if(vResult) {
				vTTSHandler.speakIt("Hello " + vPref.getString("fname", "User") + ", your system is ready.");
			} else {
				vTTSHandler.speakIt("Error! Please restart the application.");
			}
			
			/*hiding the wait dialog if enabled*/
			if(vDialog.isShowing()) {
				vDialog.dismiss();
			}
			super.onPostExecute(vResult);
		}
	}
	
	/*initializing imageview variables to handle gear status*/
	public void initializeImageView() {
		vImageViewGear1 = ((ImageView)findViewById(R.id.imageViewGearStatus1));
		vImageViewGear2 = ((ImageView)findViewById(R.id.imageViewGearStatus2));
		vImageViewGear3 = ((ImageView)findViewById(R.id.imageViewGearStatus3));
		vImageViewGear4 = ((ImageView)findViewById(R.id.imageViewGearStatus4));
		vImageViewGearNeutral = ((ImageView)findViewById(R.id.imageViewGearStatusNeutral));
		vImageViewGearReverse = ((ImageView)findViewById(R.id.imageViewGearStatusReverse));
	}
	
	/*changing the new gear from unselected to selected*/
	private static void engageGear(int vGearNo) {
		int vImageResource = R.drawable.ic_selected;
		String vMessageToSpeak = "";
		switch(vGearNo) {
			case 1:
				vImageViewGear1.setImageResource(vImageResource);
				vMessageToSpeak = vMessageToSpeak + "Gear one has been activated.";
				break;
			case 2:
				vImageViewGear2.setImageResource(vImageResource);
				vMessageToSpeak = vMessageToSpeak + "Gear two has been activated.";
				break;
			case 3:
				vImageViewGear3.setImageResource(vImageResource);
				vMessageToSpeak = vMessageToSpeak + "Gear three has been activated.";
				break;
			case 4:
				vImageViewGear4.setImageResource(vImageResource);
				vMessageToSpeak = vMessageToSpeak + "Gear four has been activated.";
				break;
			case 5:
				vImageViewGearReverse.setImageResource(vImageResource);
				vMessageToSpeak = vMessageToSpeak + "Reverse gear has been activated.";
				break;
			case 6:
				vImageViewGearNeutral.setImageResource(vImageResource);
				vMessageToSpeak = vMessageToSpeak + "All gears has been deactivated.";
				break;
		}
		vActivity.vTTSHandler.speakIt(vMessageToSpeak);
	}
	
	/*changing the previous gear from selected to unselected*/
	private static void disengageGear(int vGearNo, Boolean vIsCompleted) {
		int vImageResource = 0;
		if(vIsCompleted) {
			vImageResource = R.drawable.ic_unselected;
		} else {
			vImageResource = R.drawable.ic_disengage;
		}
		switch(vGearNo) {
			case 1:
				vImageViewGear1.setImageResource(vImageResource);
				break;
			case 2:
				vImageViewGear2.setImageResource(vImageResource);
				break;
			case 3:
				vImageViewGear3.setImageResource(vImageResource);
				break;
			case 4:
				vImageViewGear4.setImageResource(vImageResource);
				break;
			case 5:
				vImageViewGearReverse.setImageResource(vImageResource);
				break;
			case 6:
				vImageViewGearNeutral.setImageResource(vImageResource);
				break;
 		}
	}
	
	/*terminating all the connected services*/
    private void terminatingApp() {
    	try {
			if(vTTSHandler != null) {
				vTTSHandler.setFinalMessage("Bye " + vPref.getString("fname", "User") 
										   			+ ", System is shutting down.");
				vTTSHandler.stopTTSHandler();
			}
			/*closing bluetooth sockets*/
			if(!vIsBTConnected) {
				unregisterReceiver(vDiscoverBTDevice);
				unregisterReceiver(vSocketConnector);
			}	
			/*removing periodic location update
			 *calls for handler if enabled
			 */
			if(vIsLocationUpdating) {
				vHandler.removeCallbacks(vRefresher);
			}
			/*waiting for TTS service*/
			while(vTTSHandler.isConnected());
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
	
    @Override
	protected void onResume() {
    	Log.d("CITMain", "OnResume");
    	/*setting the theme to current activity*/
    	vThemeHandler.applyThemeActivity(this);
    	/*closing the drawer menu if opened*/
    	if(vDrawerLayout.isDrawerOpen(vDrawerList)) {
    		vDrawerLayout.closeDrawers();
    	}
    	super.onResume();
	}
    
    @Override
	protected void onDestroy() {
    	Log.d("CITMain", "OnDestroy");
    	terminatingApp();
		super.onDestroy();
	}
    
    /*
     * ========================
     * Bluetooth Handler
     * ========================
     * */
	
    /*handler to update gui components*/
	private static final Handler GUIUpdater = new Handler() {
		 @Override
		 public void handleMessage(Message vMessage) {
			 try {
				 switch (vMessage.what) {
				 	/*when writing on the output stream*/
				 	case MESSAGE_WRITE:
				 		
				 		break;
				 	/*when reading from the input stream, 1 byte at a time*/
				 	case MESSAGE_READ:
				 		/*Get the bytes from the message*/
				 		byte[] vReadBuffer = (byte[])vMessage.obj;
				 		/*construct a string from the valid bytes in the buffer*/
				 		//vReadMessage = new String(vReadBuffer, 0, vMessage.arg1);
				 		int vGearNo = Integer.parseInt(new String(vReadBuffer, 0, vMessage.arg1));
				 		Log.d("CITMain", vCurrentGear + " " + vGearNo);
				 		if(vGearNo != vCurrentGear) {
				 			if(vGearNo == 7) {
				 				disengageGear(vCurrentGear, false);
				 			} else {
				 				engageGear(vGearNo);
				 				disengageGear(vCurrentGear, true);
				 				vCurrentGear = vGearNo;
				 			}
				 		}
				 		break;
				 }
			 } catch(Exception e) {
				 e.printStackTrace();
			 }
		 }
	};
    
    /*initializing the bluetooth device*/
	private void initializeBluetooth() {
		vBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		vBluetoothAdapter.enable();
		vDiscoverBTDevice = new BluetoothBroadcastReceiver();
		vSocketConnector = new BluetoothBroadcastReceiver();
		registerReceiver(vDiscoverBTDevice, new IntentFilter(BluetoothDevice.ACTION_FOUND));
	    registerReceiver(vSocketConnector, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
	    
	    /*waiting until bluetooth adapter got enabled*/
		while(!vBluetoothAdapter.isEnabled());
		
		Log.d("CITMain", "Bluetooth is enabled");
		if(!vBluetoothAdapter.isDiscovering()) {
	    	vBluetoothAdapter.startDiscovery();
	    }
		vIsBTConnected = false;
	}
	
	/*bluetooth broadcast receiver*/
	private class BluetoothBroadcastReceiver extends BroadcastReceiver {
		@SuppressWarnings("unused")
		@Override
		public void onReceive(Context vContext, Intent vIntent) {
			try {
				String vAction = vIntent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(vAction)) {
					String vDiscoveredDeviceName = vIntent.getStringExtra(BluetoothDevice.EXTRA_NAME);
					BluetoothDevice vDiscoveredDevice = vIntent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					int vBondState = vDiscoveredDevice.getBondState();
					
					/*naming the different bond state*/
					String vBondStateName;
					switch(vBondState) {
						case 10: vBondStateName="BOND_NONE";
					 		break;
					 	case 11: vBondStateName="BOND_BONDING";
					 		break;
					 	case 12: vBondStateName="BOND_BONDED";
					 		break;
					 	default: vBondStateName="INVALID BOND STATE";
					 		break;
					}
					/*if HC-05 found*/
					if(vDiscoveredDeviceName.equals("HC-05")) {
						unregisterReceiver(vDiscoverBTDevice);
						ConnectToBluetooth connectBT = new ConnectToBluetooth(vDiscoveredDevice);
						
						/*creating the socket connection to the the device in a new thread*/
						new Thread(connectBT).start();
					}
				}
				if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(vAction)){
					
					/*wait until BTSocket got connected*/
					while(vGlobalBTSocket == null) {}
					
					/*successfully connected to the HC-05 Bluetooth device*/
					vIsBTConnected = true;
					
					if(vGlobalBTSocket != null) {
						SendReceiveBytes sendReceiveBT = new SendReceiveBytes(vGlobalBTSocket);
						new Thread(sendReceiveBT).start();
					}
				}
		 	} catch(Exception e) {
		 		e.printStackTrace();
		 	}
		 }
	}
	
	/*to connect to the bluetooth device*/
	public class ConnectToBluetooth implements Runnable {
		 private BluetoothDevice vBTDevice;
		 private BluetoothSocket vBTSocket = null;
		 private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		 
		 public ConnectToBluetooth(BluetoothDevice vBluetoothShield) {
			 vBTDevice = vBluetoothShield;
			 try{
				 vBTSocket = vBTDevice.createRfcommSocketToServiceRecord(uuid);
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		 }
		 
		 /*creating socket connection to bluetooth device*/
		 @Override
		 public void run() {
		 /*Cancel discovery on Bluetooth Adapter to prevent slow connection*/
			 vBluetoothAdapter.cancelDiscovery();
			 try{
				 vBTSocket.connect();
				 vGlobalBTSocket = vBTSocket;
				 Log.d("ConnectToBluetooth", vGlobalBTSocket.toString());
			 } catch (Exception e){
				 Log.e("ConnectToBluetooth", "Error with Socket Connection");
				 try {
					vBTSocket.close();
				 } catch(Exception e1) {
					e1.printStackTrace();
				 }
				 return;
			 }
		 }
		 
		 /*Will cancel an in-progress connection, and close the socket*/
		 public void cancel() {
			 try {
				 vBTSocket.close();
			 } catch(IOException e) {
				 e.printStackTrace();
			 }
		 }
	}
	
	/*runnable thread to send or receives the data from device bluetooth*/
	private class SendReceiveBytes implements Runnable {
		 private BluetoothSocket vBluetoothSocket;
		 private InputStream vByteInputStream = null;
		 private OutputStream vByteOutputStream = null;
		 String TAG = "SendReceiveBytes";

		 public SendReceiveBytes(BluetoothSocket vBTSocket) {
			 vBluetoothSocket = vBTSocket;
			 try {
				 vByteInputStream = vBluetoothSocket.getInputStream();
				 vByteOutputStream = vBluetoothSocket.getOutputStream();
			 } catch (IOException streamError) { 
				 Log.e(TAG, "Error when getting input or output Stream");
			 }
		 }

		 /*read the data from the input stream*/
		 public void run() {
			 /*buffer store for the stream*/
			 byte[] vBuffer = new byte[1];
			 /*bytes returned from read function*/
			 int vBytes;
			 /*Keep listening to the InputStream until an exception occurs*/
			 while(true) {
				 try {
					 /*Read from the InputStream*/
					 vBytes = vByteInputStream.read(vBuffer);
					 /*Send the obtained bytes to the UI activity*/
					 GUIUpdater.obtainMessage(MESSAGE_READ, vBytes, -1, vBuffer).sendToTarget();
				 } catch(IOException e) {
					 Log.e(TAG, "Error reading from BTInputStream");
					 break;
				 }
			 }
		 }
		 
		 /*Call this from the main activity to send data to the remote device */
		 @SuppressWarnings("unused")
		 public void write(byte[] bytes) {
			 try {
				 vByteOutputStream.write(bytes); 
			 } catch(Exception e) { 
				 Log.e(TAG, "Error when writing to BTOutputStream");
			 }
		 }
		 
		 /*Call this from the main activity to shutdown the connection*/
		 @SuppressWarnings("unused")
		 public void cancel() {
			 try {
				 vBluetoothSocket.close();
			 } catch (Exception e) { 
				 e.printStackTrace();
			 }
		}
	}
	
	/*convert string in to the byte array*/
	public static byte[] stringToBytesUTFCustom(String vText) {
		 char[] vCharArray = vText.toCharArray();
		 byte[] vByteArray = new byte[vCharArray.length << 1];
		 for (int i=0; i<vCharArray.length; i++) {
			 int vBitPosition = i<<1;
			 vByteArray[vBitPosition] = (byte)((vCharArray[i]&0xFF00)>>8);
			 vByteArray[vBitPosition + 1] = (byte)(vCharArray[i]&0x00FF);
		 }
		 return vByteArray;
	}
}