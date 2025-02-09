package com.cloud.aws.model;

public class ApiResponse {
	    private boolean success;
	    private String message;
	    private User data;

	    public ApiResponse(boolean success, String message, User data) {
	        this.success = success;
	        this.message = message;
	        this.data = data;
	    }

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public User getData() {
			return data;
		}

		public void setData(User data) {
			this.data = data;
		}

	    // Getters and setters...
	}
