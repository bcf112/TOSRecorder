package com.tosrecorder;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PartSpinnerAdapter extends ArrayAdapter<PartInfo> {

	Context mContext;

	public PartSpinnerAdapter(Context context, ArrayList<PartInfo> list) {
		super(context, 0, list);
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();

			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.item_partspinner, null);
			holder.partTitle = (TextView) v
					.findViewById(R.id.partspinner_title);
			holder.partTitle.setTextColor(Color.WHITE);
			holder.partImage = (ImageView) v
					.findViewById(R.id.partspinner_image);
			v.setTag(holder);
		} else {
			v = convertView;
			holder = (ViewHolder) v.getTag();
		}

		PartInfo info = getItem(position);

		holder.partTitle.setText(info.partTitle);
		holder.partImage.setImageDrawable(info.partImage);
		return v;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		View v = null;
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();

			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.item_partspinner, null);
			holder.partTitle = (TextView) v
					.findViewById(R.id.partspinner_title);
			holder.partTitle.setTextColor(Color.BLACK);
			holder.partImage = (ImageView) v
					.findViewById(R.id.partspinner_image);
			v.setTag(holder);
		} else {
			v = convertView;
			holder = (ViewHolder) v.getTag();
		}

		PartInfo info = getItem(position);

		holder.partTitle.setText(info.partTitle);
		holder.partImage.setImageDrawable(info.partImage);

		return v;
	}

	static class ViewHolder {
		TextView partTitle;
		ImageView partImage;
	}
}
