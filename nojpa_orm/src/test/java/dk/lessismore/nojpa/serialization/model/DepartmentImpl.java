package dk.lessismore.nojpa.serialization.model;

public class DepartmentImpl {

    private String name;
    private EmployeeImpl chef;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmployeeImpl getChef() {
        return chef;
    }

    public void setChef(EmployeeImpl chef) {
        this.chef = chef;
    }
}
