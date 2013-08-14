package dk.lessismore.reusable_v4.db.model;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

import java.util.Calendar;

public interface MrRich extends ModelObjectInterface {

    public boolean getMyBoolean();
    public void setMyBoolean(boolean _);

    public char getMyChar();
    public void setMyChar(char _);

    public byte getMyByte();
    public void setMyByte(byte _);

    public short getMyShort();
    public void setMyShort(short _);

    public int getMyInt();
    public void setMyInt(int _);

    public long getMyLong();
    public void setMyLong(long _);

    public float getMyFloat();
    public void setMyFloat(float _);

    public double getMyDouble();
    public void setMyDouble(double _);

    public String getString();
    public void setString(String _);

    public Calendar getCalendar();
    public void setCalendar(Calendar _);

    public Boolean getBooleanObject();
    public void setBooleanObject(Boolean _);

    public Double getDoubleObject();
    public void setDoubleObject(Double _);

    public MrRich getMrRich();
    public void setMrRich(MrRich _);

    public MrRich[] getMrRichArray();
    public void setMrRichArray(MrRich[] _);

}