package com.cit.ids.networking;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlacesHandler {

	private String vAPIKey = "AIzaSyBn2YE7Qaxc9OqpjnetRIBxKderkDLkiq4";
	
	public ArrayList<Place> findPlaces(double vLatitude, double vLongitude, String vPlaceCategory) {
		String vLink = makeURL(vLatitude, vLongitude, vPlaceCategory);
		try {
			String vResult = new HTTPGETQueryHandler().executeQuery(vLink);
			JSONObject vJSONObject = new JSONObject(vResult);
			JSONArray vJSONArray = vJSONObject.getJSONArray("results");

			ArrayList<Place> vPlaceArrayList = new ArrayList<Place>();
			for(int i=0; i<vJSONArray.length(); i++) {
				try {
					Place vPlace = convertJSONToPlace((JSONObject)vJSONArray.get(i));
					vPlaceArrayList.add(vPlace);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			return vPlaceArrayList;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*constructing the URL for google places query*/
	/*https://maps.googleapis.com/maps/api/place/search/json?location=28.632808,77.218276&radius=500&types=atm&sensor=false&key=apikey*/
	private String makeURL(double vLatitude, double vLongitude, String vPlaceCategory) {
		StringBuilder vLink = new StringBuilder("https://maps.googleapis.com/maps/api/place/search/json?");
		vLink.append("&location=");
		vLink.append(Double.toString(vLatitude));
		vLink.append(",");
		vLink.append(Double.toString(vLongitude));
		vLink.append("&radius=1000");
		/*if place category is mentioned*/
		if(!vPlaceCategory.equals("")) {
			vLink.append("&types=" + vPlaceCategory);
		}
		vLink.append("&sensor=false&key=" + vAPIKey);
		return vLink.toString();
	}
	
	/*converting JSON Object to Place Object*/
    private Place convertJSONToPlace(JSONObject vJSONObject) {
        try {
            Place vPlace = new Place();
            JSONObject vGeometry = (JSONObject)vJSONObject.get("geometry");
            JSONObject vLocation = (JSONObject)vGeometry.get("location");
            vPlace.setLatitude((Double)vLocation.get("lat"));
            vPlace.setLongitude((Double)vLocation.get("lng"));
            vPlace.setName(vJSONObject.getString("name"));
            vPlace.setVicinity(vJSONObject.getString("vicinity"));
            return vPlace;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
