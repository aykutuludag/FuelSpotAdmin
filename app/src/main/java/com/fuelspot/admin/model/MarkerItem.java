package com.fuelspot.admin.model;

public class MarkerItem {

    private int ID;
    private String stationName;
    private String photoURL;
    private float gasolinePrice;
    private float dieselPrice;
    private float lpgPrice;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public float getGasolinePrice() {
        return gasolinePrice;
    }

    public void setGasolinePrice(float gasolinePrice) {
        this.gasolinePrice = gasolinePrice;
    }

    public float getDieselPrice() {
        return dieselPrice;
    }

    public void setDieselPrice(float dieselPrice) {
        this.dieselPrice = dieselPrice;
    }

    public float getLpgPrice() {
        return lpgPrice;
    }

    public void setLpgPrice(float lpgPrice) {
        this.lpgPrice = lpgPrice;
    }
}
