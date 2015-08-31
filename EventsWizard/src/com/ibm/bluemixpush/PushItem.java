package com.ibm.bluemixpush;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

class PushItem implements Serializable {

	private  String title;
	private  String time;
	private  String message;
	private HashMap<String,String> payload;


	public PushItem(String pushMessage) throws JSONException {
		JSONObject jsonObject = new JSONObject(pushMessage);

		if(jsonObject.has("title")){
			title = jsonObject.getString("title");
		}else{
			title = "Default Title";
		}

		if(jsonObject.has("message")){
			message = jsonObject.getString("message");
		}else{
			message = "Default Title";
		}

		if(jsonObject.has("time")){
			time = jsonObject.getString("time");
		}else{
			time = Calendar.getInstance().getTime().toString();
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public HashMap<String, String> getPayload() {
		return payload;
	}

	public void setPayload(HashMap<String, String> payload) {
		this.payload = payload;
	}

	public String toJson(){
		return "{\"title\":\""+title+"\",\"message\":\""+message+"\",\"time\":\""+time+"\"}";
	}
}
