package com.cit.ids.networking;

public class Place {
	
    private String vName = null;
    private Double vLatitude = null;
    private Double vLongitude = null;
    private String vVicinity = null;
    
    /*getters*/
    public String getName() {
        return vName;
    }
    public Double getLatitude() {
        return vLatitude;
    }
    public Double getLongitude() {
        return vLongitude;
    }
    public String getVicinity() {
        return vVicinity;
    }
    
    /*setters*/
    public void setName(String vName) {
        this.vName = vName;
    }
    public void setLatitude(Double vLatitude) {
        this.vLatitude = vLatitude;
    }
    public void setLongitude(Double vLongitude) {
        this.vLongitude = vLongitude;
    }
    public void setVicinity(String vVicinity) {
        this.vVicinity = vVicinity;
    }
}