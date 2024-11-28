package com.dicoding.nutridish.data.api.response;

import com.google.gson.annotations.SerializedName;

public class LoginResult{

	@SerializedName("name")
	private String name;

	@SerializedName("userId")
	private String userId;

	@SerializedName("token")
	private String token;

	public String getName(){
		return name;
	}

	public String getUserId(){
		return userId;
	}

	public String getToken(){
		return token;
	}
}