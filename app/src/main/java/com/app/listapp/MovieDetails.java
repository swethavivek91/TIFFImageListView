package com.app.listapp;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MovieDetails {

    @SerializedName("TestData")
    @Expose
    private List<DetailedInfo> testData = null;

    public List<DetailedInfo> getTestData() {
        return testData;
    }

    public void setTestData(List<DetailedInfo> testData) {
        this.testData = testData;
    }

}