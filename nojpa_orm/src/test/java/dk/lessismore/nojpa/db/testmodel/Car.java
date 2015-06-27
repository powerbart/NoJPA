package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import javax.persistence.Column;

public interface Car extends ModelObjectInterface {

    @Column(unique = true)
    String getBrand();
    void setBrand(String brand);


    double getVolume();
    void setVolume(double volume);

    FuelType getFuelType();
    void setFuelType(FuelType fuelType);

    @Column(unique = true)
    Address getAddress();
    void setAddress(Address address);

    public static enum FuelType {
        LPG, DIESEL, PETROL, ELECTRIC
    }
}