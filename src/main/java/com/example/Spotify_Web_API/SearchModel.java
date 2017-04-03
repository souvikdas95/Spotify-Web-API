package com.example.Spotify_Web_API;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SearchModel<E>
{
    @SerializedName("items")
    public List<E> items = new ArrayList<E>();  // Array can be of AlbumModel, ArtistModel or TrackModel
    
    @SerializedName("limit")
    public int limit;
    
    @SerializedName("offset")
    public int offset;
    
    @SerializedName("total")
    public int total;
    
    public SearchModel(){}
}
