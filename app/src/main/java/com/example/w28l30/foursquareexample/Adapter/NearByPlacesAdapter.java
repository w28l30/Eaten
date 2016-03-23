package com.example.w28l30.foursquareexample.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.w28l30.foursquareexample.AppController;
import com.example.w28l30.foursquareexample.Model.FoursquareVenue;
import com.example.w28l30.foursquareexample.R;

import java.util.ArrayList;

/**
 * Created by W28L30 on 15/11/1.
 */
public class NearByPlacesAdapter extends BaseAdapter {
    ArrayList<FoursquareVenue> mFourSqureList;
    private Context mContext;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public NearByPlacesAdapter(Context context, ArrayList<FoursquareVenue> foursquareVenuesList) {
        mContext = context;
        mFourSqureList = foursquareVenuesList;
    }

    @Override
    public int getCount() {
        return mFourSqureList.size();
    }

    @Override
    public FoursquareVenue getItem(int position) {
        return mFourSqureList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.item_list_nearby, null, true);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        viewHolder.thumbNail.setImageUrl(mFourSqureList.get(position).getThumbnailUrl(), imageLoader);
        viewHolder.tv_address.setText(mFourSqureList.get(position).getAddress());
        viewHolder.tv_name.setText(mFourSqureList.get(position).getName());
        if (mFourSqureList.get(position).getDistance() != 0.0) {
            viewHolder.tv_distance.setText(String.valueOf(mFourSqureList.get(position).getDistance() / 1000) + " KM");
        }
        viewHolder.tv_rating.setText(mFourSqureList.get(position).getRating() + "");
        viewHolder.tv_price.setText(mFourSqureList.get(position).getPrice());

        return v;
    }

    class ViewHolder {
        TextView tv_address;
        TextView tv_name;
        TextView tv_rating;
        TextView tv_price;
        TextView tv_distance;
        NetworkImageView thumbNail;

        public ViewHolder(View view) {
            tv_address = (TextView) view.findViewById(R.id.address);
            tv_name = (TextView) view.findViewById(R.id.name);
            tv_rating = (TextView) view.findViewById(R.id.rating);
            tv_price = (TextView) view.findViewById(R.id.price);
            tv_distance = (TextView) view.findViewById(R.id.distance);
            thumbNail = (NetworkImageView) view
                    .findViewById(R.id.thumbnail);
        }
    }
}
