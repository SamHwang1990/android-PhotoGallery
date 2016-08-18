package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 16/8/15.
 */
public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";

    // 暂时,PhotoGallery 这个练手项目只支持模拟器中运行,为了方便访问flickr,开发机本地做了socks5 代理
    private static final String HOST_PROXY = "http://10.0.2.2:3000/proxy";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public byte[] fetchThumbnail(String urlSpec) throws IOException {
        String url = Uri.parse(HOST_PROXY).buildUpon()
                .appendPath("photos")
                .appendPath("fetchUrl")
                .appendQueryParameter("uri", urlSpec)
                .build().toString();

        return getUrlBytes(url);
    }

    public List<GalleryItem> fetchItems() {
        return fetchItems(0);
    }

    public List<GalleryItem> fetchItems(int page) {
        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse(HOST_PROXY).buildUpon()
                    .appendPath("photos")
                    .appendPath("getRecent")
                    .appendPath("" + page)
                    .build().toString();

            String jsonString = getUrlString(url);
            Log.e(TAG, "Received JSON: " + jsonString);

            parseItems(items, new JSONObject(jsonString));
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse json", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); ++i) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (photoJsonObject.has("url_s")) {
                item.setUrl(photoJsonObject.getString("url_s"));
            }

            items.add(item);
        }
    }
}
