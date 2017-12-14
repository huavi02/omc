package com.omc.service.domain;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class OmcAlertDetail {

	private OmcEvent omcEvent;

	public OmcAlertDetail(OmcEvent omcEvent) {
		this.omcEvent = omcEvent;
	}

	public String toJson(){
		Map<String, Object> json = new HashMap<String, Object>();
		Map<String, Object> data = this.omcEvent.getData();
		json.put("agent", data.get(OmcEventConstant.EVENT_AGENT));
		json.put("class", data.get(OmcEventConstant.EVENT_CLASS));
		json.put("count", data.get(OmcEventConstant.EVENT_COUNT));
		json.put("time", data.get(OmcEventConstant.EVENT_TIME));
		for (Map.Entry<String, Object> entry : this.omcEvent.getData().entrySet()) {
		    String key = entry.getKey();
		    if(key.startsWith(OmcEventConstant.EVENT_TOKEN_PREFIX)) {
		    		json.put(key.replaceFirst(OmcEventConstant.EVENT_PREFIX, ""), entry.getValue());
		    }
		}
	    return new Gson().toJson(json);
	}
}
