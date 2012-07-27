package com.tosrecorder;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayListAdapter extends ArrayAdapter<RecordingInfo> {
	private Context mContext;

	public PlayListAdapter(Context context, ArrayList<RecordingInfo> list) {
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
			v = inflater.inflate(R.layout.item_playlist, null);
			holder.rowTitle = (TextView) v.findViewById(R.id.row_title);
			holder.rowDuration = (TextView) v.findViewById(R.id.row_duration);
			holder.rowPartImg = (ImageView) v.findViewById(R.id.row_partimage);
			v.setTag(holder);
		} else {
			v = convertView;
			holder = (ViewHolder) v.getTag();
		}

		RecordingInfo info = getItem(position);

		if (info.title.regionMatches(1, "P1", 0, 2)) {
			holder.rowPartImg.setImageResource(R.drawable.icon_part1);
		} else if (info.title.regionMatches(1, "P2", 0, 2)) {
			holder.rowPartImg.setImageResource(R.drawable.icon_part2);
		} else if (info.title.regionMatches(1, "P3", 0, 2)) {
			holder.rowPartImg.setImageResource(R.drawable.icon_part3);
		} else if (info.title.regionMatches(1, "P4", 0, 2)) {
			holder.rowPartImg.setImageResource(R.drawable.icon_part4);
		} else if (info.title.regionMatches(1, "P5", 0, 2)) {
			holder.rowPartImg.setImageResource(R.drawable.icon_part5);
		} else if (info.title.regionMatches(1, "P6", 0, 2)) {
			holder.rowPartImg.setImageResource(R.drawable.icon_part6);
		}

		String removedPartTitle = info.title.substring(4, info.title.length());
		holder.rowTitle.setText(removedPartTitle);
		holder.rowDuration.setText(info.duration);

		return v;
	}

	static class ViewHolder {
		TextView rowTitle;
		TextView rowDuration;
		ImageView rowPartImg;
	}
}
