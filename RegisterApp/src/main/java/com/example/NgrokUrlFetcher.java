package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class NgrokUrlFetcher
{
    public static String getNgrokHttpsUrl()
    {
        try 
        {
            URL url = new URL("http://127.0.0.1:4040/api/tunnels");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) 
                content.append(inputLine);
            in.close();
            con.disconnect();
            JSONObject json = new JSONObject(content.toString());
            JSONArray tunnels = json.getJSONArray("tunnels");	
            for (int i = 0; i < tunnels.length(); i++) 
            {
                JSONObject tunnel = tunnels.getJSONObject(i);
                String publicUrl = tunnel.getString("public_url");
                if (publicUrl.startsWith("https://")) 
                    return publicUrl;
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return null;
    }
}
