package org.mixare;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.mixare.lib.marker.Marker;
import org.mixare.marker.LocalMarker;
import org.mixare.sectionedlist.Item;
import org.mixare.sectionedlist.SectionItem;

import java.util.List;

/**
 * This class extends the ArrayAdapter to be able to create our own View and
 * OnClickListeners
 *
 * @author KlemensE
 */
class SectionedListAdapter extends ArrayAdapter<Item> {

    private Activity parentActivity;
    private List<Item> items;

    public SectionedListAdapter(Activity parentActivity, Context context, int textViewResourceId, List<Item> objects) {
        super(context, textViewResourceId, objects);
        this.parentActivity = parentActivity;
        this.items = objects;
    }

    public void changeList(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public Item getItem(int position) {
        if (position > items.size()) {
            return null;
        }
        return items.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item i = getItem(position);
//        Log.d(Config.TAG, "getView: " + position);
        if (i != null) {
            if (i.isSection()) {
                SectionViewHolder sectionViewHolder;
                Object tag = null;

                if(convertView!=null) {
                    try {
                        tag = convertView.getTag(R.string.list_view_section);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
//					Log.d(Config.TAG, "getView: " + position + " tag: " + tag + " section");
                if (tag == null) {
                    convertView = parentActivity.getLayoutInflater().inflate(R.layout.list_item_section, parent, false);

                    sectionViewHolder = new SectionViewHolder();
                    sectionViewHolder.title = (TextView) convertView.findViewById(R.id.section_title);
                    sectionViewHolder.markerCount = (TextView) convertView.findViewById(R.id.section_marker_count);

                    convertView.setTag(R.string.list_view_section, sectionViewHolder);
                } else {
                    sectionViewHolder = (SectionViewHolder) tag;
                }

                convertView.setOnClickListener(null);
                convertView.setOnLongClickListener(null);
                convertView.setLongClickable(false);

                sectionViewHolder.title.setText(((SectionItem) i).getTitle());
                sectionViewHolder.markerCount.setText(
                        parentActivity.getString(R.string.list_view_marker_in_section,((SectionItem) i).getMarkerCount() )
                );
            } else {
                ViewHolder holder;
                Object tag = null;
                if(convertView!=null) {
                    try {
                        tag = convertView.getTag(R.string.list_view_entry);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
//					Log.d("test", "getView: " + position + " tag: " + tag + " entry");
                if (tag == null) {
                    convertView = parentActivity.getLayoutInflater().inflate(R.layout.marker_list, parent, false);

                    holder = new ViewHolder();

                    holder.sideBar = convertView.findViewById(R.id.side_bar);
                    holder.title = (TextView) convertView.findViewById(R.id.marker_list_title);
                    holder.desc = (TextView) convertView.findViewById(R.id.marker_list_summary);
                    holder.moreButton = (ImageButton) convertView.findViewById(R.id.marker_list_morebutton);

                    convertView.setTag(R.string.list_view_entry, holder);
                } else {
                    holder = (ViewHolder) tag;
                }

                MarkerListFragment.EntryItem item = (MarkerListFragment.EntryItem) i;

                Marker marker = item.getMarker();
                SpannableString spannableString = new SpannableString(marker.getTitle());

                if (marker.getURL() != null) {
                    spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
                    convertView.setOnClickListener(new OnClickListenerWebView(position));
                } else {
                    convertView.setOnClickListener(null);
                }

                holder.sideBar.setBackgroundColor(marker.getColor());
                holder.moreButton.setOnClickListener(onClickListenerMoreActions);
                holder.moreButton.setTag(position);


                holder.title.setText(spannableString);
                holder.desc.setText(getContext().getString(R.string.distance_format, marker.getDistance()));
            }
        }

        return convertView;
    }

    private class SectionViewHolder {
        TextView title;
        TextView markerCount;
    }

    private class ViewHolder {
        View sideBar;
        TextView title;
        TextView desc;
        ImageButton moreButton;
    }

    public int getCount() {
        return items.size();
    }

    View.OnClickListener onClickListenerMoreActions = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LocalMarker marker = (LocalMarker) ((MarkerListFragment.EntryItem) getItem((Integer) v.getTag())).getMarker();

            marker.retrieveActionPopupMenu(parentActivity, v).show();
        }
    };

    /**
     * Handles the click on the list row to open the WebView
     */
    private class OnClickListenerWebView implements View.OnClickListener {
        private int position;
        public OnClickListenerWebView (int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Marker marker = ((MarkerListFragment.EntryItem) getItem(position)).getMarker();
            marker.doClick(0,0,MixContext.getInstance(),MixViewActivity.getMarkerRendererStatically().getState());
        }
    }
}