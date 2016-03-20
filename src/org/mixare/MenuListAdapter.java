package org.mixare;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Created by MelanieW on 29.12.2015.
 */
public class MenuListAdapter extends BaseAdapter {

        // Declare Variables
        Context context;
        String[] mTitles;
        String[] mSubTitle;
        TypedArray mIcons;
        LayoutInflater inflater;


    public MenuListAdapter(Context context, String[] titles, TypedArray icons) {
            this.context = context;
            this.mTitles = titles;
            this.mIcons = icons;
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public Object getItem(int position) {
            return mTitles[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Declare Variables
            TextView txtTitle;
            ImageView imgIcon;

            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.drawer_list_item, parent,
                    false);

            // Locate the TextViews in drawer_list_item.xml
            txtTitle = (TextView) itemView.findViewById(R.id.title);

            // Locate the ImageView in drawer_list_item.xml
            imgIcon = (ImageView) itemView.findViewById(R.id.icon);

            // Set the results into TextViews
            txtTitle.setText(mTitles[position]);

            // Set the results into ImageView
            imgIcon.setImageDrawable(mIcons.getDrawable(position));

            return itemView;
        }


}
