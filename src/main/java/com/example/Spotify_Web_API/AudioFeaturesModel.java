package com.example.Spotify_Web_API;

import com.google.gson.annotations.SerializedName;

public class AudioFeaturesModel
{
    @SerializedName("acousticness")
    public float acousticness;
    
    @SerializedName("danceability")
    public float danceability;
    
    @SerializedName("energy")
    public float energy;
    
    @SerializedName("id")
    public String id;
    
    @SerializedName("instrumentalness")
    public float instrumentalness;
    
    @SerializedName("key")
    public int key;
    
    @SerializedName("liveness")
    public float liveness;
    
    @SerializedName("loudness")
    public float loudness;
    
    @SerializedName("mode")
    public int mode;
    
    @SerializedName("speechiness")
    public float speechiness;
    
    @SerializedName("tempo")
    public float tempo;
    
    @SerializedName("time_signature")
    public int time_signature;
    
    @SerializedName("valence")
    public float valence;
    
    public AudioFeaturesModel(){}
}
