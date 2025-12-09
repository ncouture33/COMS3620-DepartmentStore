package HR;

public class BasicValidator implements  InformationValidator{
    @Override
    public boolean validate(String role, double salary, String department){
        if(role == null || role.isEmpty()){ return false;}
        else if(salary < 0) {return false;}
        else if (department == null|| department.isEmpty()){ return false;}
        return true;
    }
    
}
