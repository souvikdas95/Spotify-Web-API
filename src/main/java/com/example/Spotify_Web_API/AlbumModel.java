package com.example.Spotify_Web_API;

import com.google.gson.annotations.SerializedName;

public class AlbumModel
{
    @SerializedName("genres")
    public String[] genres;
    
    @SerializedName("id")
    public String id;
    
    @SerializedName("images")
    public ImageModel[] images;
    
    @SerializedName("name")
    public String name;
    
    public AlbumModel(){}
}
