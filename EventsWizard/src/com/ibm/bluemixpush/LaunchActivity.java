/*
 * Copyright 2014 IBM Corp. All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.bluemixpush;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.ibm.mobile.services.cloudcode.IBMCloudCode;
import com.ibm.mobile.services.core.http.IBMHttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class LaunchActivity extends Activity {

	List<PushItem> itemList;
	PushApplication blApplication;
	ArrayAdapter<PushItem> lvArrayAdapter;
	ActionMode mActionMode = null;
	int listItemPosition;
	public static final String CLASS_NAME = "LaunchActivity";
	RefreshReceiver refreshReceiver;
	Boolean refreshReceiverIsRegistered = false;
	
	@Override
	/**
	 * onCreate called when main activity is created.
	 * 
	 * Sets up the itemList, application, and sets listeners.
	 *
	 * @param savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.ibm.bluemixpush.R.layout.activity_main);
		
		/* Use application class to maintain global state. */
		blApplication = (PushApplication) getApplication();
		itemList = blApplication.getPushList();

		
		/* Set up the array adapter for items list view. */
		ListView itemsLV = (ListView)findViewById(com.ibm.bluemixpush.R.id.itemsList);
		lvArrayAdapter = new PushItemAdapter(this, com.ibm.bluemixpush.R.layout.list_item, itemList);
		itemsLV.setAdapter(lvArrayAdapter);
		
		/* Refresh the list. */
		listItems(); 

		/* Set long click listener. */
		itemsLV.setOnItemLongClickListener(new OnItemLongClickListener() {
		    /* Called when the user long clicks on the textview in the list. */
		    public boolean onItemLongClick(AdapterView<?> adapter, View view, int position,
	                long rowId) {
		    	listItemPosition = position;
				if (mActionMode != null) {
		            return false;
		        }
		        /* Start the contextual action bar using the ActionMode.Callback. */
		        //mActionMode = LaunchActivity.this.startActionMode(mActionModeCallback);
		        return true;
		    }
		});

		refreshReceiver =  new RefreshReceiver();
		if (!refreshReceiverIsRegistered) {
			registerReceiver(refreshReceiver, new IntentFilter("com.ibm.bluemix.push.REFRESH"));
			refreshReceiverIsRegistered = true;
		}
	}

	public void refreshList(){
		try {
			lvArrayAdapter.clear();
			lvArrayAdapter.addAll(blApplication.getPushList());
			lvArrayAdapter.notifyDataSetChanged();
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * Refreshes itemList from data service.
	 *
	 * An IBMQuery is used to find all the list items.
	 */
	public void listItems() {

	}

	/**
	 * Send a notification to all devices whenever the BlueList is modified (create, update, or delete).
	 */
	private void updateOtherDevices() {

		// Initialize and retrieve an instance of the IBM CloudCode service.
		IBMCloudCode.initializeService();
		IBMCloudCode myCloudCodeService = IBMCloudCode.getService();
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("key1", "value1");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		/*
		 * Call the node.js application hosted in the IBM Cloud Code service
		 * with a POST call, passing in a non-essential JSONObject.
		 * The URI is relative to/appended to the BlueMix context root.
		 */
		
		myCloudCodeService.post("notifyOtherDevices", jsonObj).continueWith(new Continuation<IBMHttpResponse, Void>() {

            @Override
            public Void then(Task<IBMHttpResponse> task) throws Exception {
                if (task.isCancelled()) {
                    Log.e(CLASS_NAME, "Exception : Task" + task.isCancelled() + "was cancelled.");
                } else if (task.isFaulted()) {
                    Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                } else {
                    InputStream is = task.getResult().getInputStream();
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(is));
                        String responseString = "";
                        String myString = "";
                        while ((myString = in.readLine()) != null)
                            responseString += myString;

                        in.close();
                        Log.i(CLASS_NAME, "Response Body: " + responseString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Log.i(CLASS_NAME, "Response Status from notifyOtherDevices: " + task.getResult().getHttpResponseCode());
                }

                return null;
            }

        });

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.pushactions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// action with ID action_refresh was selected
			case R.id.action_clear:

				SharedPreferences settings = getApplicationContext().getSharedPreferences("PUST_WIZARD", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("push.wizard.list","");
				editor.commit();


				try {
					lvArrayAdapter.clear();
					lvArrayAdapter.notifyDataSetChanged();
				}catch (Exception e){
					e.printStackTrace();
				}
				break;
			default:
				break;
		}

		return true;
	}

	class RefreshReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshList();
		}
	}
}
