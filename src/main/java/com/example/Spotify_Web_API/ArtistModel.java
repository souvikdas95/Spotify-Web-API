package com.example.Spotify_Web_API;

import com.google.gson.annotations.SerializedName;

public class ArtistModel
{
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    public ArtistModel(){}
}
