package com.dicoding.nutridish.data.api.response;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

	@SerializedName("error")
	private boolean error;

	@SerializedName("message")
	private String message;

	public boolean isError(){
		return error;
	}

	public String getMessage(){
		return message;
	}
}