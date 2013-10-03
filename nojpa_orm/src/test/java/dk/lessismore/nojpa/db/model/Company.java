package dk.lessismore.nojpa.db.model;

import dk.lessismore.nojpa.properties.Default;
import dk.lessismore.nojpa.reflection.db.annotations.ModelObjectLifeCycleListener;
import dk.lessismore.nojpa.reflection.db.annotations.ModelObjectMethodListener;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import javax.persistence.Column;

//@ModelObjectLifeCycleListener(lifeCycleListener = Company.CompanyLifeCycleListener.class)
public interface Company extends ModelObjectInterface {
//
//    public enum CompanyType { A_S, ApS, IpS };
//
    @Default("MyFunnyName")
//    @ModelObjectMethodListener(methodListener = CompanyMethodListener.class)
    @Column(length = 1000)
    public String getName();

    @ModelObjectMethodListener(methodListener = CompanyMethodListener.class)
    public void setName(String value);

    public Person[] getEmployees();
    @ModelObjectMethodListener(methodListener = CompanySetEmployeesMethodListener.class)
    public void setEmployees(Person[] value);

    public int getNumberOfEmployees();
    public void setNumberOfEmployees(int numberOfEmployees);

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
                ((Company) mother).setNumberOfEmployees(((String) args[0]).length());
            }
        }

        @Override
        public Object postRun(Object mother, String methodName, Object preResult, Object[] args) {
            System.out.println("Company$CompanyMethodListener.postRun mother("+ mother +") methodName("+ methodName +") preResult("+ preResult +") args("+ args +")");
            if(args != null && args.length == 1){
                ((Company) mother).setNumberOfEmployees(10 + ((String) args[0]).length());
            }
            return preResult;
        }
    }

    public static class CompanySetEmployeesMethodListener implements ModelObjectMethodListener.MethodListener {

        @Override
        public void preRun(Object mother, String methodName, Object[] args) {
        }

        @Override
        public Object postRun(Object mother, String methodName, Object preResult, Object[] args) {
            System.out.println("CompanySetEmployeesMethodListener.postRun mother("+ mother +") methodName("+ methodName +") preResult("+ preResult +") args("+ args +")");

            Company t = (Company) mother;
            Person[] employees = t.getEmployees();
            t.setNumberOfEmployees(employees == null ? 0 : employees.length);
            return preResult;
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
