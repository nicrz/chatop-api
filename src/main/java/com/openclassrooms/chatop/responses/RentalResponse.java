package com.openclassrooms.chatop.responses;

public class RentalResponse {
    private String message;

    public RentalResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
