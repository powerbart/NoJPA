package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

public interface Car extends ModelObjectInterface {

    String getBrand();
    void setBrand(String brand);


    double getVolume();
    void setVolume(double volume);

    FuelType getFuelType();
    void setFuelType(FuelType fuelType);

    Address getAddress();
    void setAddress(Address address);

    public static enum FuelType {
        LPG, DIESEL, PETROL, ELECTRIC
    }
}