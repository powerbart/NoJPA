package dk.lessismore.nojpa.reflection.attributeconverters;
import java.awt.*;
import java.util.*;
/**
 * This class can convert a Color to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class ColorConverter extends AttributeConverter {

    public Object stringToObject(String str) {
        StringTokenizer tokens = new StringTokenizer(str, ",");
        if(tokens.countTokens() == 3) {
            try {
                int red = Integer.parseInt(tokens.nextToken().trim());
                int green = Integer.parseInt(tokens.nextToken().trim());
                int blue = Integer.parseInt(tokens.nextToken().trim());
                return new Color(red, green, blue);
            }catch(Exception e) {

            }
        }
        else if(tokens.countTokens() == 4) {
            try {
                int red = Integer.parseInt(tokens.nextToken().trim());
                int green = Integer.parseInt(tokens.nextToken().trim());
                int blue = Integer.parseInt(tokens.nextToken().trim());
                int alpha = Integer.parseInt(tokens.nextToken().trim());
                return new Color(red, green, blue, alpha);
            }catch(Exception e) {

            }
        }
        return null;
    }

    public String objectToString(Object object) {
        Color color = (Color)object;
        return ""+color.getRed()+","+color.getGreen()+","+color.getBlue();
    }
    protected Class getObjectClass() {
        return Color.class;
    }
}
