package com.fuelspot.admin.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.fuelspot.admin.R;
import com.fuelspot.admin.model.StationItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import de.hdodenhof.circleimageview.CircleImageView;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private Context mContext;

    public MarkerAdapter(Context ctx) {
        mContext = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.popup_marker, null);

        StationItem infoWindowData = (StationItem) marker.getTag();

        TextView sName = view.findViewById(R.id.station_name);
        CircleImageView sLogo = view.findViewById(R.id.station_logo);
        TextView priceOne = view.findViewById(R.id.priceGasoline);
        TextView priceTwo = view.findViewById(R.id.priceDiesel);
        TextView priceThree = view.findViewById(R.id.priceLPG);
        TextView id = view.findViewById(R.id.stationId);

        sName.setText(infoWindowData.getStationName());
        priceOne.setText("" + infoWindowData.getGasolinePrice());
        priceTwo.setText("" + infoWindowData.getDieselPrice());
        priceThree.setText("" + infoWindowData.getLpgPrice());
        id.setText("ID: " + infoWindowData.getID());
        sLogo.setImageDrawable(infoWindowData.getStationLogoDrawable());

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
