package com.example.Sample;

import com.example.Spotify_Web_API.*;
import java.util.List;

public class Sample
{
    public static void main(String args[]) throws Exception
    {
        String szClientID = "<ClientID>";
        String szClientSecret = "<ClientSecret>";
        
        // Create API Object
        API_Spotify_Web obj = new API_Spotify_Web(szClientID, szClientSecret);
        
        // Authneticate
        if(!obj.Authenticate(10))
            return;
        
        // Search for Track
        List<TrackModel> TrackL = obj.SearchTrack("Be Intehaan", 300000);
        if(TrackL == null || TrackL.isEmpty())
            return;
        TrackModel Track = TrackL.get(0);
        if(Track == null)
            return;
        System.out.println(Track.id + " : " + Track.name + " : " + Track.album.name + " : " + Track.artists[0].name + " : " + Track.popularity);
        
        // Retrieve AudioFeatures
        AudioFeaturesModel AudioFeatures = obj.GetAudioFeatures(Track.id);
        if(AudioFeatures == null)
            return;
        System.out.println("acousticness: " + AudioFeatures.acousticness + "\n" +
                           "danceability: " + AudioFeatures.danceability + "\n" +
                           "energy: " + AudioFeatures.energy + "\n" +
                           "instrumentalness: " + AudioFeatures.instrumentalness + "\n" +
                           "key: " + AudioFeatures.key + "\n" +
                           "liveness: " + AudioFeatures.liveness + "\n" +
                           "loudness: " + AudioFeatures.loudness + "\n" +
                           "mode: " + AudioFeatures.mode + "\n" +
                           "speechiness: " + AudioFeatures.speechiness + "\n" +
                           "tempo: " + AudioFeatures.tempo + "\n" +
                           "time_signature: " + AudioFeatures.time_signature + "\n" +
                           "valence: " + AudioFeatures.valence + "\n" + 
                           "duration:" + Track.duration_ms);
    }
}
