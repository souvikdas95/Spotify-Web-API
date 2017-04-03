package com.example.Spotify_Web_API;

import com.google.gson.annotations.SerializedName;

public class TrackModel
{      
    @SerializedName("album")
    public AlbumModel album;
    
    @SerializedName("artists")
    public ArtistModel[] artists;
    
    @SerializedName("duration_ms")
    public long duration_ms;
    
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("popularity")
    public int popularity;
    
    public TrackModel(){}
}
