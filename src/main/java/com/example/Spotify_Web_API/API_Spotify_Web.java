package com.example.Spotify_Web_API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;   // Remove in android
import java.util.Base64;   // Remove in android
// import android.util.Base64;  // Use in android
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class API_Spotify_Web
{
    private static final String ENDPOINT_AUTH = "https://accounts.spotify.com";
    private static final String ENDPOINT_AUTH_POST = "/api/token";
    private static final String ENDPOINT_WEBAPI = "https://api.spotify.com";
    private static final String ENDPOINT_WEBAPI_SEARCH_TRACK_GET = "/v1/search?type=track";
    private static final String ENDPOINT_WEBAPI_AUDIO_FEATURES_GET = "/v1/audio-features/";   // OAuth Necessary
    
    private final String client_id;
    private final String client_secret;
    private String access_token;
    
    public API_Spotify_Web()
    {
        this.client_id = null;
        this.client_secret = null;
        this.access_token = null;
    }
    
    public API_Spotify_Web(String client_id, String client_secret)
    {
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.access_token = null;
    }
    
    public boolean Authenticate(int max_attempt)
    {
        while(--max_attempt >= 0)
        {
            if(this.tryAuthenticate())
                return true;
        }
        return false;
    }
    
    public boolean tryAuthenticate()
    {
        try
        {
            if(this.client_id == null || this.client_secret == null)
                return false;
            String auth_type = "Basic";
            String grant_type = "client_credentials";
            String auth_key = Base64.getEncoder().encodeToString((this.client_id + ":" + this.client_secret).getBytes(StandardCharsets.UTF_8));   // Remove in android
            // String auth_key = Base64.encodeToString((this.client_id + ":" + this.client_secret).getBytes(), Base64.NO_WRAP);    // Use in android
            URL url = new URL(ENDPOINT_AUTH + ENDPOINT_AUTH_POST);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Authorization", auth_type + " " + auth_key);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes("grant_type=" + grant_type);
            wr.flush();
            wr.close();
            conn.connect();
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return false;
            BufferedReader rr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = rr.read(chars)) != -1)
                buffer.append(chars, 0, read);
            String szJSON = buffer.toString(); 
            JsonObject jo = new JsonParser().parse(szJSON).getAsJsonObject();
            if(!jo.has("access_token") || jo.has("error"))
                return false;
            access_token = jo.get("access_token").getAsString();
            return true;
        }
        catch(JsonSyntaxException | IOException ex)
        {
            Logger.getLogger(API_Spotify_Web.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public TrackModel SearchTrackComplete(String szArtist, String szAlbum, String szTrack, long lDuration)
    {
        try
        {
            URL url = new URL(ENDPOINT_WEBAPI + ENDPOINT_WEBAPI_SEARCH_TRACK_GET + "&" +
                                "query=" + URLEncoder.encode(
                                    "artist:'" + szArtist + "'" + " " +
                                    "album:'" + szAlbum + "'" + " " +
                                    "track:'" + szTrack + "'","UTF-8") + "&" +
                                "limit=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if(access_token != null)
            {
                String auth_type = "Bearer";
                conn.addRequestProperty("Authorization", auth_type + " " + access_token);
            }
            conn.connect();
            int respCode = conn.getResponseCode();
            if(access_token != null && respCode == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                conn.disconnect();
                this.access_token = null;
                if(!this.Authenticate(3))
                    return null;
                return this.SearchTrackComplete(szArtist, szAlbum, szTrack, lDuration);
            }
            if(respCode != HttpURLConnection.HTTP_OK)
                return null;
            BufferedReader rr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = rr.read(chars)) != -1)
                buffer.append(chars, 0, read);
            String szJSON = buffer.toString();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            JsonObject jo = new JsonParser().parse(szJSON).getAsJsonObject();
            SearchResponseModel srm = gson.fromJson(jo, SearchResponseModel.class);
            if(srm.tracks.total < 1)
                return null;
            List<TrackModel> ret = srm.tracks.items;
            if(ret.size() < 1)
                return null;
            if(lDuration > 0)   // Passively match duration
            {
                int i, small_i = 0;
                long i_v, small_v = 30000;
                for(i = 0; i < ret.size(); ++i)
                {
                    if(ret.get(i).duration_ms < 1)
                        continue;
                    i_v = Math.abs(ret.get(i).duration_ms - lDuration);
                    if(i_v < small_v)
                    {
                        small_i = i;
                        small_v = i_v;
                    }
                }
                return ret.get(small_i);
            }
            return ret.get(0);
        }
        catch (JsonSyntaxException | IOException ex)
        {
            Logger.getLogger(API_Spotify_Web.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public List<TrackModel> SearchTrackUsingArtist(String szArtist, String szTrack, final long lDuration)
    {
        try
        {
            URL url = new URL(ENDPOINT_WEBAPI + ENDPOINT_WEBAPI_SEARCH_TRACK_GET + "&" +
                                "query=" + URLEncoder.encode(
                                    "artist:'" + szArtist + "'" + " " +
                                    "track:'" + szTrack + "'","UTF-8") + "&" +
                                "limit=10");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if(access_token != null)
            {
                String auth_type = "Bearer";
                conn.addRequestProperty("Authorization", auth_type + " " + access_token);
            }
            conn.connect();
            int respCode = conn.getResponseCode();
            if(access_token != null && respCode == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                conn.disconnect();
                this.access_token = null;
                if(!this.Authenticate(3))
                    return null;
                return this.SearchTrackUsingArtist(szArtist, szTrack, lDuration);
            }
            if(respCode != HttpURLConnection.HTTP_OK)
                return null;
            BufferedReader rr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = rr.read(chars)) != -1)
                buffer.append(chars, 0, read);
            String szJSON = buffer.toString();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            JsonObject jo = new JsonParser().parse(szJSON).getAsJsonObject();
            SearchResponseModel srm = gson.fromJson(jo, SearchResponseModel.class);
            if(srm.tracks.total < 1)
                return null;
            List<TrackModel> ret_raw = srm.tracks.items;
            if(ret_raw.size() < 1)
                return null;
            if(lDuration > 0)   // Passively match duration
            {
                Collections.sort(ret_raw, new Comparator<TrackModel>()
                {
                    @Override
                    public int compare(TrackModel lhs, TrackModel rhs)
                    {
                        long lhs_diff = Math.abs(lhs.duration_ms - lDuration);
                        long rhs_diff = Math.abs(rhs.duration_ms - lDuration);
                        return lhs_diff < rhs_diff ? -1 : (lhs_diff > rhs_diff) ? 1 : 0;
                    }
                });
                int cur = 0;
                long small_v = 30000;
                for(TrackModel t: ret_raw)
                {
                    if(Math.abs(t.duration_ms - lDuration) > small_v)
                        break;
                    cur++;
                }
                List<TrackModel> ret = ret_raw.subList(0, cur);
                return ret;
            }
            return ret_raw;
        }
        catch (JsonSyntaxException | IOException ex)
        {
            Logger.getLogger(API_Spotify_Web.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public List<TrackModel> SearchTrackUsingAlbum(String szAlbum, String szTrack, final long lDuration)
    {
        try
        {
            URL url = new URL(ENDPOINT_WEBAPI + ENDPOINT_WEBAPI_SEARCH_TRACK_GET + "&" +
                                "query=" + URLEncoder.encode(
                                    "album:'" + szAlbum + "'" + " " +
                                    "track:'" + szTrack + "'","UTF-8") + "&" +
                                "limit=10");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if(access_token != null)
            {
                String auth_type = "Bearer";
                conn.addRequestProperty("Authorization", auth_type + " " + access_token);
            }
            conn.connect();
            int respCode = conn.getResponseCode();
            if(access_token != null && respCode == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                conn.disconnect();
                this.access_token = null;
                if(!this.Authenticate(3))
                    return null;
                return this.SearchTrackUsingAlbum(szAlbum, szTrack, lDuration);
            }
            if(respCode != HttpURLConnection.HTTP_OK)
                return null;
            BufferedReader rr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = rr.read(chars)) != -1)
                buffer.append(chars, 0, read);
            String szJSON = buffer.toString();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            JsonObject jo = new JsonParser().parse(szJSON).getAsJsonObject();
            SearchResponseModel srm = gson.fromJson(jo, SearchResponseModel.class);
            if(srm.tracks.total < 1)
                return null;
            List<TrackModel> ret_raw = srm.tracks.items;
            if(ret_raw.size() < 1)
                return null;
            if(lDuration > 0)   // Passively match duration
            {
                Collections.sort(ret_raw, new Comparator<TrackModel>()
                {
                    @Override
                    public int compare(TrackModel lhs, TrackModel rhs)
                    {
                        long lhs_diff = Math.abs(lhs.duration_ms - lDuration);
                        long rhs_diff = Math.abs(rhs.duration_ms - lDuration);
                        return lhs_diff < rhs_diff ? -1 : (lhs_diff > rhs_diff) ? 1 : 0;
                    }
                });
                int cur = 0;
                long small_v = 30000;
                for(TrackModel t: ret_raw)
                {
                    if(Math.abs(t.duration_ms - lDuration) > small_v)
                        break;
                    cur++;
                }
                List<TrackModel> ret = ret_raw.subList(0, cur);
                return ret;
            }
            return ret_raw;
        }
        catch (JsonSyntaxException | IOException ex)
        {
            Logger.getLogger(API_Spotify_Web.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public List<TrackModel> SearchTrackUsingFilename(String szFilename, final long lDuration)
    {
        try
        {
            String szTrack = szFilename.substring(0, szFilename.lastIndexOf('.'));
            URL url = new URL(ENDPOINT_WEBAPI + ENDPOINT_WEBAPI_SEARCH_TRACK_GET + "&" +
                                "query=" + URLEncoder.encode(
                                    "track:'" + szTrack + "'","UTF-8") + "&" +
                                "limit=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if(access_token != null)
            {
                String auth_type = "Bearer";
                conn.addRequestProperty("Authorization", auth_type + " " + access_token);
            }
            conn.connect();
            int respCode = conn.getResponseCode();
            if(access_token != null && respCode == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                conn.disconnect();
                this.access_token = null;
                if(!this.Authenticate(3))
                    return null;
                return this.SearchTrack(szTrack, lDuration);
            }
            if(respCode != HttpURLConnection.HTTP_OK)
                return null;
            BufferedReader rr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = rr.read(chars)) != -1)
                buffer.append(chars, 0, read);
            String szJSON = buffer.toString();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            JsonObject jo = new JsonParser().parse(szJSON).getAsJsonObject();
            SearchResponseModel srm = gson.fromJson(jo, SearchResponseModel.class);
            if(srm.tracks.total < 1)
                return null;
            List<TrackModel> ret_raw = srm.tracks.items;
            if(ret_raw.size() < 1)
                return null;
            if(lDuration > 0)   // Passively match duration
            {
                Collections.sort(ret_raw, new Comparator<TrackModel>()
                {
                    @Override
                    public int compare(TrackModel lhs, TrackModel rhs)
                    {
                        long lhs_diff = Math.abs(lhs.duration_ms - lDuration);
                        long rhs_diff = Math.abs(rhs.duration_ms - lDuration);
                        return lhs_diff < rhs_diff ? -1 : (lhs_diff > rhs_diff) ? 1 : 0;
                    }
                });
                int cur = 0;
                long small_v = 30000;
                for(TrackModel t: ret_raw)
                {
                    if(Math.abs(t.duration_ms - lDuration) > small_v)
                        break;
                    cur++;
                }
                List<TrackModel> ret = ret_raw.subList(0, cur);
                return ret;
            }
            return ret_raw;
        }
        catch (JsonSyntaxException | IOException ex)
        {
            Logger.getLogger(API_Spotify_Web.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public List<TrackModel> SearchTrack(String szTrack, final long lDuration)
    {
        try
        {
            URL url = new URL(ENDPOINT_WEBAPI + ENDPOINT_WEBAPI_SEARCH_TRACK_GET + "&" +
                                "query=" + URLEncoder.encode(
                                    "track:'" + szTrack + "'","UTF-8") + "&" +
                                "limit=10");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if(access_token != null)
            {
                String auth_type = "Bearer";
                conn.addRequestProperty("Authorization", auth_type + " " + access_token);
            }
            conn.connect();
            int respCode = conn.getResponseCode();
            if(access_token != null && respCode == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                conn.disconnect();
                this.access_token = null;
                if(!this.Authenticate(3))
                    return null;
                return this.SearchTrack(szTrack, lDuration);
            }
            if(respCode != HttpURLConnection.HTTP_OK)
                return null;
            BufferedReader rr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = rr.read(chars)) != -1)
                buffer.append(chars, 0, read);
            String szJSON = buffer.toString();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            JsonObject jo = new JsonParser().parse(szJSON).getAsJsonObject();
            SearchResponseModel srm = gson.fromJson(jo, SearchResponseModel.class);
            if(srm.tracks.total < 1)
                return null;
            List<TrackModel> ret_raw = srm.tracks.items;
            if(ret_raw.size() < 1)
                return null;
            if(lDuration > 0)   // Passively match duration
            {
                Collections.sort(ret_raw, new Comparator<TrackModel>()
                {
                    @Override
                    public int compare(TrackModel lhs, TrackModel rhs)
                    {
                        long lhs_diff = Math.abs(lhs.duration_ms - lDuration);
                        long rhs_diff = Math.abs(rhs.duration_ms - lDuration);
                        return lhs_diff < rhs_diff ? -1 : (lhs_diff > rhs_diff) ? 1 : 0;
                    }
                });
                int cur = 0;
                long small_v = 30000;
                for(TrackModel t: ret_raw)
                {
                    if(Math.abs(t.duration_ms - lDuration) > small_v)
                        break;
                    cur++;
                }
                List<TrackModel> ret = ret_raw.subList(0, cur);
                return ret;
            }
            return ret_raw;
        }
        catch (JsonSyntaxException | IOException ex)
        {
            Logger.getLogger(API_Spotify_Web.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public AudioFeaturesModel GetAudioFeatures(String szTrackID)
    {
        try
        {
            if(access_token == null)
                return null;
            String auth_type = "Bearer";
            URL url = new URL(ENDPOINT_WEBAPI + ENDPOINT_WEBAPI_AUDIO_FEATURES_GET + szTrackID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", auth_type + " " + access_token);
            conn.connect();
            int respCode = conn.getResponseCode();
            if(access_token != null && respCode == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                conn.disconnect();
                this.access_token = null;
                if(!this.Authenticate(3))
                    return null;
                return this.GetAudioFeatures(szTrackID);
            }
            if(respCode != HttpURLConnection.HTTP_OK)
                return null;
            BufferedReader rr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = rr.read(chars)) != -1)
                buffer.append(chars, 0, read);
            String szJSON = buffer.toString();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            JsonObject jo = new JsonParser().parse(szJSON).getAsJsonObject();
            AudioFeaturesModel AudioFeatures = gson.fromJson(jo, AudioFeaturesModel.class);
            return AudioFeatures;
        }
        catch (JsonSyntaxException | IOException ex)
        {
            Logger.getLogger(API_Spotify_Web.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
