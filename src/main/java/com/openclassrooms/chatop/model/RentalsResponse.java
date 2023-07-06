package com.openclassrooms.chatop.model;

import java.util.List;

import com.openclassrooms.chatop.model.Rentals;

public class RentalsResponse {
    private List<Rentals> rentals;

    public RentalsResponse(List<Rentals> rentals) {
        this.rentals = rentals;
    }

    public List<Rentals> getRentals() {
        return rentals;
    }

    public void setRentals(List<Rentals> rentals) {
        this.rentals = rentals;
    }
}