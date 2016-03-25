package dk.lessismore.nojpa.db.model;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.util.Calendar;

public interface MrRich extends ModelObjectInterface {

    public boolean getMyBoolean();
    public void setMyBoolean(boolean a);

    public char getMyChar();
    public void setMyChar(char a);

    public byte getMyByte();
    public void setMyByte(byte a);

    public short getMyShort();
    public void setMyShort(short a);

    public int getMyInt();
    public void setMyInt(int a);

    public long getMyLong();
    public void setMyLong(long a);

    public float getMyFloat();
    public void setMyFloat(float a);

    public double getMyDouble();
    public void setMyDouble(double a);

    public String getString();
    public void setString(String a);

    public Calendar getCalendar();
    public void setCalendar(Calendar a);

    public Boolean getBooleanObject();
    public void setBooleanObject(Boolean a);

    public Double getDoubleObject();
    public void setDoubleObject(Double a);

    public MrRich getMrRich();
    public void setMrRich(MrRich a);

    public MrRich[] getMrRichArray();
    public void setMrRichArray(MrRich[] a);

}