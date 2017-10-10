package com.cit.ids.utilities;

import com.cit.ids.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

public class ThemeHandler {
	
	private SharedPreferences vPref = null;
	private Boolean vIsSolidColorTheme = null;
	
	/*default Constructor*/
	public ThemeHandler(Context vContext) {
		/*initializing the shared preference database*/
		vPref = vContext.getSharedPreferences("CITIDSThemeData", Context.MODE_PRIVATE);
		vIsSolidColorTheme = false;
	}
	
	/*save the theme resource to the shared preferences*/
	private void saveThemeResource(int vThemeResource) {
		SharedPreferences.Editor vSPEdit = vPref.edit();
		vSPEdit.putBoolean("isthemesaved", true);
		vSPEdit.putBoolean("issolidcolor", vIsSolidColorTheme);
		vSPEdit.putInt("themeresource", vThemeResource);
		vSPEdit.commit();
	}
	
	/*return the saved theme resource from the shared preferences*/
	public int getThemeResource() {
		vIsSolidColorTheme = vPref.getBoolean("issolidcolor", false);
		int vThemeResource = vPref.getInt("themeresource", -1);
		/*set the default theme & save it*/
		if(!vPref.getBoolean("isthemesaved", false)) {
			vThemeResource = R.drawable.back01;
			saveThemeResource(vThemeResource);
		}
		return vThemeResource;
	}
	
	/*applying the saved theme on Activity*/
	public void applyThemeActivity(Activity vActivity) {
		int vThemeResource = getThemeResource();
		if(vIsSolidColorTheme) {
			vActivity.getWindow().getDecorView().setBackgroundColor(vThemeResource);
		} else {
			vActivity.getWindow().getDecorView().setBackgroundResource(vThemeResource);
		}
	}
	
	/*applying the saved theme on View*/
	public void applyThemeView(View vView) {
		int vThemeResource = getThemeResource();
		if(vIsSolidColorTheme) {
			vView.setBackgroundColor(vThemeResource);
		} else {
			vView.setBackgroundResource(vThemeResource);
		}
	}
	
	/*used when a new theme is selected for Activity*/
	public void applyThemeActivity(Activity vActivity,
					Boolean vIsSolidColorTheme, int vThemeResource, Boolean vIsSave) {		
		if(vIsSolidColorTheme) {
			vActivity.getWindow().getDecorView().setBackgroundColor(vThemeResource);
		} else {
			vActivity.getWindow().getDecorView().setBackgroundResource(vThemeResource);
		}
		
		/*saving the theme*/
		if(vIsSave) {
			this.vIsSolidColorTheme = vIsSolidColorTheme;
			saveThemeResource(vThemeResource);
		}
	}
	
	/*used when a new theme is selected for View*/
	public void applyThemeView(View vView, Boolean vIsSolidColorTheme, int vThemeResource, Boolean vIsSave) {
		if(vIsSolidColorTheme) {
			vView.setBackgroundColor(vThemeResource);
		} else {
			vView.setBackgroundResource(vThemeResource);
		}
		
		/*saving the theme*/
		if(vIsSave) {
			this.vIsSolidColorTheme = vIsSolidColorTheme;
			saveThemeResource(vThemeResource);
		}
	}
	
	public Boolean isSavedSCTheme() {
		return vPref.getBoolean("issolidcolor", false);
	}
}