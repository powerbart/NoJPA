package dk.lessismore.reusable_v4.db.testmodel;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

public interface Car extends ModelObjectInterface {

    String getBrand();
    void setBrand(String brand);


    double getVolume();
    void setVolume(double volume);

    FuelType getFuelType();
    void setFuelType(FuelType fuelType);

    public static enum FuelType {
        LPG, DIESEL, PETROL, ELECTRIC
    }
}