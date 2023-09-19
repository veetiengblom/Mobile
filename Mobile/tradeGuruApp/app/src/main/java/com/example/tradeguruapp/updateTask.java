package com.example.tradeguruapp;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class updateTask extends AsyncTask<String, String, JSONObject> {

    String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=TSLA&interval=1min&apikey=DV0ZUWVK94S3TOCY";

    private onTaskComplete listener;

    public updateTask(onTaskComplete listener){
        this.listener=listener;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            int responseCode = con.getResponseCode();
            System.out.println("\n sending 'get' request to URL:" + url);
            System.out.println("Response code:" + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject myResponse = new JSONObject(response.toString());
            return myResponse;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(JSONObject result) {
        listener.onTaskComplete(result);
    }
}