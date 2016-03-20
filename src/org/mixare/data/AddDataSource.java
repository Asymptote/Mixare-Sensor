/*
 * Copyright (C) 2012- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.data;

import org.mixare.R;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionProvider;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to add new DataSources
 * @author KlemensE
 */
public class AddDataSource extends SherlockActivity {
	
	private static final int MENU_SAVE_ID = Menu.FIRST;
	
	EditText nameField;
	EditText urlField;
	Spinner typeSpinner;
	Spinner displaySpinner;
	Spinner blurSpinner;
	
	Bundle extras;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datasourcedetails);
		
		nameField = (EditText) findViewById(R.id.name);
		urlField = (EditText) findViewById(R.id.url);
		typeSpinner = (Spinner) findViewById(R.id.type);
		displaySpinner = (Spinner) findViewById(R.id.displaytype);
		blurSpinner = (Spinner) findViewById(R.id.blurtype);
		
		extras = getIntent().getExtras();
		if (extras != null) {
			// Get DataSource
			if (extras.containsKey("DataSourceId")) {
				DataSource ds = DataSourceStorage.getInstance().getDataSource(
						extras.getInt("DataSourceId"));
				nameField.setText(ds.getName(), TextView.BufferType.EDITABLE);
				urlField.setText(ds.getUrl(), TextView.BufferType.EDITABLE);
				typeSpinner.setSelection(ds.getTypeId());
				displaySpinner.setSelection(ds.getDisplayId());
				blurSpinner.setSelection(ds.getBlurId());
			}
			
			// Check whether DataSource can be edited or not
			if (extras.containsKey("isEditable")) {
				boolean activated = extras.getBoolean("isEditable");
//				nameField.setActivated(activated);
				nameField.setFocusable(activated);
//				urlField.setActivated(activated);
				urlField.setFocusable(activated);
//				typeSpinner.setActivated(activated);
				typeSpinner.setClickable(activated);
//				displaySpinner.setActivated(activated);
				displaySpinner.setClickable(activated);
			}
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	/**
	 * Creates a new DataSource and Saves it to the SharedPreferences
	 */
	private boolean saveNewDataSource() {
		String name = nameField.getText().toString();
		String url = urlField.getText().toString();
		int typeId = (int) typeSpinner.getItemIdAtPosition(typeSpinner
				.getSelectedItemPosition());
		int displayId = (int) displaySpinner.getItemIdAtPosition(displaySpinner
				.getSelectedItemPosition());
		int blurId = (int) blurSpinner.getItemIdAtPosition(blurSpinner
				.getSelectedItemPosition());
		
		if (!name.isEmpty() && !url.isEmpty()) {
			if (extras != null) {
				if (extras.containsKey("DataSourceId")) {
					// DataSource allready exists
					DataSource ds = DataSourceStorage.getInstance().getDataSource(
							extras.getInt("DataSourceId"));
					ds.setName(name);
					ds.setUrl(url);
					ds.setType(typeId);
					ds.setDisplay(displayId);
					ds.setBlur(blurId);
					
					DataSourceStorage.getInstance(getApplicationContext()).save();
					return true;
				}
			}
			// New DataSource
			DataSource ds = new DataSource(name, url,
					DataSource.TYPE.values()[typeId],
					DataSource.DISPLAY.values()[displayId], true);
			ds.setBlur(DataSource.BLUR.values()[blurId]);
			DataSourceStorage.getInstance().add(ds);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case MENU_SAVE_ID: 
			if (saveNewDataSource()) {
				finish();
			} else {
				Toast.makeText(this, "Error saving DataSource", Toast.LENGTH_LONG).show();
			}
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {	
		menu.add(0, MENU_SAVE_ID, Menu.NONE, "Save").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	/**
	 * Creates a new Dialog to describe the different Types of DataSources
	 * @param v
	 */
	public void onDataSourceInfoClick(View v) {
		Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setMessage("This option tells mixare what informations your DataSource needs to process the request and send mixare the marker data. Some examples:" 
				+ "Wikipedia: \n" + "?lat=0.0&lng=0.0&radius=20.0&maxRows=50&lang=de&username=mixare \n\n"
				+ "Twitter: \n" + "?geocode=0.0,0.0,20.0km \n\n"
				+ "Arena: \n" + "&lat=0.0&lng=0.0 \n\n"
				+ "OSM: \n" + "[bbox=-1.0,1.0,-2.0,2.0] \n\n"
				+ "Panoramio \n" + "?set=public&from=0&to=20&minx=-180&miny=-90&maxx=180&maxy=90&size=medium&mapfilter=true \n\n"
				);
		builder.setNegativeButton(getString(R.string.close_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert1 = builder.create();
		alert1.setTitle("DataSource Info");
		alert1.show();
	}
}