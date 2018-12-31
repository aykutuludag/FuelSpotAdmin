package com.fuelspot.admin.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.admin.R;
import com.fuelspot.admin.model.MarkerItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import de.hdodenhof.circleimageview.CircleImageView;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {

    RequestOptions options;
    private Context context;

    public MarkerAdapter(Context ctx) {
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.popup_marker, null);

        MarkerItem infoWindowData = (MarkerItem) marker.getTag();

        TextView sName = view.findViewById(R.id.station_name);
        CircleImageView sLogo = view.findViewById(R.id.station_logo);
        TextView priceOne = view.findViewById(R.id.priceGasoline);
        TextView priceTwo = view.findViewById(R.id.priceDiesel);
        TextView priceThree = view.findViewById(R.id.priceLPG);

        sName.setText(infoWindowData.getStationName());

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_station)
                .error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(context).load(infoWindowData.getPhotoURL()).apply(options).into(sLogo);

        priceOne.setText("" + infoWindowData.getGasolinePrice());
        priceTwo.setText("" + infoWindowData.getDieselPrice());
        priceThree.setText("" + infoWindowData.getLpgPrice());

        return view;
    }
}
