package com.fuelspot.admin.model;

public class CompanyItem {

    private int ID;
    private String name;
    private String logo;
    private String website;
    private String phone;
    private String address;
    private int numOfVerifieds;
    private int numOfStations;

    public Integer getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getNumOfVerifieds() {
        return numOfVerifieds;
    }

    public void setNumOfVerifieds(int numOfVerifieds) {
        this.numOfVerifieds = numOfVerifieds;
    }

    public int getNumOfStations() {
        return numOfStations;
    }

    public void setNumOfStations(int numOfStations) {
        this.numOfStations = numOfStations;
    }
}