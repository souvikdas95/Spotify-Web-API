package com.example.Spotify_Web_API;

import com.google.gson.annotations.SerializedName;

public class SearchResponseModel
{
    @SerializedName("albums")
    public SearchModel<AlbumModel> albums;
    
    @SerializedName("artists")
    public SearchModel<ArtistModel> artists;
    
    @SerializedName("tracks")
    public SearchModel<TrackModel> tracks;
    
    public SearchResponseModel(){}
}
