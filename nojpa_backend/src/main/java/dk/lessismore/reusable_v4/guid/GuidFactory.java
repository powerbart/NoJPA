package dk.lessismore.reusable_v4.guid;

import java.util.*;
/**
 * This class can make an Global Unique Identifier (GUID). Translated - the class can
 * produce an unique id string that can be used as primary key, in forinstance an
 * database tupel. The class allso contains its own singelton/factory.
 *
 * @author Raymond
 * @version 1.0
 */
public class GuidFactory {

    static public GuidFactory factory = null;
    Random random = new Random();

  static public GuidFactory getInstance() {
    if (factory == null)
      factory = new GuidFactory();
    return factory;
  }

  public String returnConvertedGuid() {
    return makeGuid();
  }

  static final private char[] digits = new char[] {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
  static final private int shift = 4;
  static final private long mask = (1 << 4) - 1;

  static final public int guidLenght = 32;

  public String makeGuid() {
    char[] buf = new char[ guidLenght ];
    int charPos = guidLenght - 1;

    long rnd = random.nextLong();
    while ( charPos >= 16 ) {
        buf[charPos--] = digits[(int)(rnd & mask)];
        rnd >>>= shift;
    }

      long time = System.nanoTime();
    while ( charPos >= 0 ) {
        buf[charPos--] = digits[(int)(time & mask)];
        time >>>= shift;
    }

      return new String( buf );
  }

    public static void main(String[] args){
        for(int i = 0; i < 100; i++){
            System.out.println(GuidFactory.getInstance().makeGuid());

        }

    }
}
