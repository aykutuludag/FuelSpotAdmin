package com.fuelspot.admin.model;

public class StationItem {

    private int ID;
    private String stationName;
    private String vicinity;
    private String countryCode;
    private String locStation;
    private String googleMapID;
    private String facilities;
    private String licenseNo;
    private String owner;
    private String photoURL;
    private float gasolinePrice;
    private float dieselPrice;
    private float lpgPrice;
    private float electricityPrice;
    private int isVerified;
    private int hasSupportMobilePayment;
    private int hasFuelDelivery;
    private int isActive;
    private String lastUpdated;
    private int distance;

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLocation() {
        return locStation;
    }

    public void setLocation(String locStation) {
        this.locStation = locStation;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getGoogleMapID() {
        return googleMapID;
    }

    public void setGoogleMapID(String googleMapID) {
        this.googleMapID = googleMapID;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
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

    public float getElectricityPrice() {
        return electricityPrice;
    }

    public void setElectricityPrice(float electricityPrice) {
        this.electricityPrice = electricityPrice;
    }

    public int getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(int isVerified) {
        this.isVerified = isVerified;
    }

    public int getHasSupportMobilePayment() {
        return hasSupportMobilePayment;
    }

    public void setHasSupportMobilePayment(int hasSupportMobilePayment) {
        this.hasSupportMobilePayment = hasSupportMobilePayment;
    }

    public int getHasFuelDelivery() {
        return hasFuelDelivery;
    }

    public void setHasFuelDelivery(int hasFuelDelivery) {
        this.hasFuelDelivery = hasFuelDelivery;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}