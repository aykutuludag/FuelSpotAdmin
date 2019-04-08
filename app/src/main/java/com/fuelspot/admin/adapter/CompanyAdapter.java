package com.fuelspot.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.admin.R;
import com.fuelspot.admin.model.CompanyItem;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompanyAdapter extends BaseAdapter {
    private LayoutInflater inflter;
    private List<CompanyItem> feedItemList;
    private Context mContext;

    public CompanyAdapter(Context context, List<CompanyItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        inflter = (LayoutInflater.from(mContext));
    }

    @Override
    public int getCount() {
        return feedItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflter.inflate(R.layout.card_company, null);
        CircleImageView circleImageViewCompanyLogo = convertView.findViewById(R.id.company_logo);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_station)
                .error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        Glide.with(mContext).load(feedItemList.get(position).getLogo()).apply(options).into(circleImageViewCompanyLogo);

        TextView textViewCompanyName = convertView.findViewById(R.id.company_name);
        textViewCompanyName.setText(feedItemList.get(position).getName());
        return convertView;
    }
}
