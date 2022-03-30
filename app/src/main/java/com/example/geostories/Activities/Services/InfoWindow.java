package com.example.geostories.Activities.Services;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.geostories.Activities.ViewStorie;
import com.example.geostories.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindow implements GoogleMap.InfoWindowAdapter{
    Context context;
    View mWindow;

    public InfoWindow(Context context){
        this.context = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.info_window, null);
    }

    public View rendowInfoWindow(Marker marker, View view){
        TextView txtStorieTittle = mWindow.findViewById(R.id.infoStorieTitle);
        txtStorieTittle.setText(marker.getTitle());
        TextView txtStorieSnippet = mWindow.findViewById(R.id.infoStorieSnippet);
        txtStorieSnippet.setText(marker.getSnippet());
        View imgButton = mWindow.findViewById(R.id.infoWindowButton);
        return mWindow;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        rendowInfoWindow(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowInfoWindow(marker, mWindow);
        return mWindow;
    }
}
