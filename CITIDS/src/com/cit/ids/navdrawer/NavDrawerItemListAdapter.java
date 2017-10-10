package com.cit.ids.navdrawer;

import com.cit.ids.R;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavDrawerItemListAdapter extends BaseAdapter {
	
	private Context vContext;
	private ArrayList<NavDrawerItem> vNavDrawerItems;
	private ImageView vImageIcon = null;
	private TextView vTextItemName =  null;
	private Boolean vIsHeader = null;
	
	public NavDrawerItemListAdapter(Context vContext, ArrayList<NavDrawerItem> vNavDrawerItems){
		this.vContext = vContext;
		this.vNavDrawerItems = vNavDrawerItems;
		this.vIsHeader = true;
	}

	@Override
	public int getCount() {
		return vNavDrawerItems.size();
	}

	@Override
	public Object getItem(int vPosition) {		
		return vNavDrawerItems.get(vPosition);
	}

	@Override
	public long getItemId(int vPosition) {
		return vPosition;
	}

	@Override
	public View getView(int vPosition, View vConvertView, ViewGroup vParent) {
		if (vConvertView == null) {
            LayoutInflater vInflater = (LayoutInflater)
                    vContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if(vIsHeader) {
            	vConvertView = vInflater.inflate(R.layout.navdrawer_header, null);
            	vImageIcon = (ImageView)vConvertView.findViewById(R.id.imageViewUserPic);
                vTextItemName = (TextView)vConvertView.findViewById(R.id.textViewUserName);
                vIsHeader = false;
            } else {
            	vConvertView = vInflater.inflate(R.layout.navdrawer_listitem, null);
            	vImageIcon = (ImageView)vConvertView.findViewById(R.id.imageViewListItemIcon);
                vTextItemName = (TextView)vConvertView.findViewById(R.id.textViewListItemName);
            }
        }
        
        /*setting the icon & name*/
        vImageIcon.setImageResource(vNavDrawerItems.get(vPosition).getIcon());
        vTextItemName.setText(vNavDrawerItems.get(vPosition).getItemName());
        
        return vConvertView;
	}
}
