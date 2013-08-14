package dk.lessismore.reusable_v4.db.model;

import dk.lessismore.reusable_v4.properties.Default;
import dk.lessismore.reusable_v4.reflection.db.annotations.ModelObjectLifeCycleListener;
import dk.lessismore.reusable_v4.reflection.db.annotations.ModelObjectMethodListener;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import dk.lessismore.reusable_v4.reflection.db.annotations.DbStrip;

import javax.persistence.Column;

@ModelObjectLifeCycleListener(lifeCycleListener = Company.CompanyLifeCycleListener.class)
public interface Company extends ModelObjectInterface {
//
//    public enum CompanyType { A_S, ApS, IpS };
//
    @Default("MyFunnyName")
    @ModelObjectMethodListener(methodListener = CompanyMethodListener.class)
    @Column(length = 1000)
    public String getName();

    @ModelObjectMethodListener(methodListener = CompanyMethodListener.class)
    public void setName(String value);

    public Person[] getEmployees();
    public void setEmployees(Person[] value);


    public CompanyType getMyT();
    public void setMyT(CompanyType type);


//    public CompanyType getMyType();
//    public void setMyType(CompanyType cType);




    public enum CompanyType {
        RD, IT, SALES
    }




    public static class CompanyMethodListener implements ModelObjectMethodListener.MethodListener {

        @Override
        public void preRun(Object mother, String methodName, Object[] args) {
            System.out.println("Company$CompanyMethodListener.preRun");
            if(args != null && args.length == 1){
                args[0] = args[0] + ((Company) mother).getName() ;
            }
        }

        @Override
        public Object postRun(Object mother, String methodName, Object preResult, Object[] args) {
            System.out.println("Company$CompanyMethodListener.postRun mother("+ mother +") methodName("+ methodName +") preResult("+ preResult +") args("+ args +")");
            if(preResult != null && preResult.getClass().equals(String.class)) {
                return preResult + " " + ((Company) mother).getName() ;
            } else {
                return preResult;
            }
        }
    }

    public static class CompanyLifeCycleListener implements ModelObjectLifeCycleListener.LifeCycleListener {

        @Override
        public void onNew(Object mother) {
            System.out.println("Company$CompanyLifeCycleListener.onNew");
        }

        @Override
        public void onDelete(Object mother) {
            System.out.println("Company$CompanyLifeCycleListener.onDelete");
        }

        @Override
        public void preUpdate(Object mother) {
            System.out.println("Company$CompanyLifeCycleListener.preUpdate");
        }

        @Override
        public void postUpdate(Object mother) {
            System.out.println("Company$CompanyLifeCycleListener.postUpdate");
        }
    }



}
