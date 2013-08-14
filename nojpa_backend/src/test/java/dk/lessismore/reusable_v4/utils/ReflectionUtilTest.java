package dk.lessismore.reusable_v4.utils;

import dk.lessismore.reusable_v4.db.model.Woman;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import org.junit.Test;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectService;
import dk.lessismore.reusable_v4.reflection.util.ReflectionUtil;

import java.util.LinkedList;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 19-04-2010
 * Time: 16:20:41
*/
public class ReflectionUtilTest {


    public static class ManPojo {

        private String name = null;
        private WomanPojo woman = null;

        public WomanPojo getWoman() {
            return woman;
        }

        public void setWoman(WomanPojo woman) {
            this.woman = woman;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class WomanPojo {

        private String age = null;

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

    }




    @Test
     public void testCopyNotNulls() throws Exception {
        ManPojo original = new ManPojo();
        original.setName("original");
        WomanPojo woman = new WomanPojo();
        woman.setAge("33");
        original.setWoman(woman);
        ManPojo newCopy = new ManPojo();
        newCopy.setName( null );
        ArrayList<String> listOfAttributes = new ArrayList<String>();
        listOfAttributes.add("name");
        ReflectionUtil.copyNotNulls(original, newCopy, listOfAttributes);


        System.out.println("newCopy.getName() = " + newCopy.getName());
        System.out.println("newCopy.getName() = " + newCopy.getWoman().getAge());
        System.out.println("222");

     }




    @Test
     public void testCopyNotNulls2() throws Exception {
        ManPojo original = new ManPojo();
        ManPojo newCopy = new ManPojo();
        newCopy.setName( null );

        ReflectionUtil.copyNotNulls(original, newCopy);


        System.out.println("newCopy.getName() = " + newCopy.getName());


     }


    @Test
     public void testList() throws Exception {
        LinkedList list = new LinkedList();
        for(int i = 0; i < 5; i++){
            list.add("" + i);
        }
        list.add(0, ""+ 10);

        for(int i = 0; i < list.size(); i++){
            System.out.println("list.get("+i+") = " + list.get(i));
        }


    }

    @Test
    public void testCopyNotNulls3() {
        ModelObjectInterface moi = ModelObjectService.create(Woman.class);
        Woman woman = (Woman)moi;
        woman.setFavouriteColor("a color");
        //ModelObjectService.save(woman);

        Woman newMoi = ModelObjectService.create(Woman.class);
        ReflectionUtil.copyNotNulls(woman, newMoi);

        System.out.println("newMoi.get = " + newMoi.getFavouriteColor());


        //ModelObjectService.save(newMoi);

    }












}
