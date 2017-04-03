package com.example.Spotify_Web_API;

import com.google.gson.annotations.SerializedName;

public class ImageModel
{
    @SerializedName("height")
    public int height;
    
    @SerializedName("url")
    public String url;
    
    @SerializedName("width")
    public int width;
    
    public ImageModel(){}
}
