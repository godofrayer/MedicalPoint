package com.project.medicalpoint.entity;

import android.support.annotation.NonNull;

public class MedicalPoint implements Comparable<MedicalPoint> {
    private Integer medicalId;
    private String name;
    private String category;
    private String doctors;
    private String diseases;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private Float distance;

    public MedicalPoint() {
        medicalId = 0;
    }

    public Integer getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(Integer medicalId) {
        this.medicalId = medicalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDoctors() {
        return doctors;
    }

    public void setDoctors(String doctors) {
        this.doctors = doctors;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDiseases() {
        return diseases;
    }

    public void setDiseases(String diseases) {
        this.diseases = diseases;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(@NonNull MedicalPoint medicalPoint) {
        return distance.compareTo(medicalPoint.distance);
    }
}
