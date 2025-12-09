package StoreOperations;

import HR.BaseEmployee;

/**
 * auth for employees
 * - registerEmployee.txt ensures unique username and PIN across registered employees
 * - login verifies username/password/pin and returns the employee on success
 *
 */
public class AuthService {

    private String[] userKeys = new String[16];
    private BaseEmployee[] userVals = new BaseEmployee[16];
    private int userCount = 0;

    private String[] pinKeys = new String[16];
    private BaseEmployee[] pinVals = new BaseEmployee[16];
    private int pinCount = 0;

    private static final AuthService INSTANCE = new AuthService();

    private AuthService(){}

    public static AuthService getInstance(){ return INSTANCE; }

    private int findUserIndex(String username){
        if(username == null) return -1;
        for(int i=0;i<userCount;i++){
            if(username.equals(userKeys[i])) return i;
        }
        return -1;
    }

    private int findPinIndex(String pin){
        if(pin == null) return -1;
        for(int i=0;i<pinCount;i++){
            if(pin.equals(pinKeys[i])) return i;
        }
        return -1;
    }

    private void ensureUserCapacity(){
        if(userCount < userKeys.length) return;
        int newSize = userKeys.length * 2;
        String[] nk = new String[newSize];
        BaseEmployee[] nv = new BaseEmployee[newSize];
        System.arraycopy(userKeys,0,nk,0,userKeys.length);
        System.arraycopy(userVals,0,nv,0,userVals.length);
        userKeys = nk; userVals = nv;
    }

    private void ensurePinCapacity(){
        if(pinCount < pinKeys.length) return;
        int newSize = pinKeys.length * 2;
        String[] nk = new String[newSize];
        BaseEmployee[] nv = new BaseEmployee[newSize];
        System.arraycopy(pinKeys,0,nk,0,pinKeys.length);
        System.arraycopy(pinVals,0,nv,0,pinVals.length);
        pinKeys = nk; pinVals = nv;
    }

    /**
     * Register an employee with credentials. Returns true on success; false if username or pin already in use.
     */
    public synchronized boolean registerEmployee(BaseEmployee employee, String username, String password, String pin){
        if(employee == null || username == null || password == null || pin == null) return false;
        if(findUserIndex(username) != -1) return false;
        if(findPinIndex(pin) != -1) return false;
        
        // set credentials on employee
        employee.setUsername(username);
        employee.setPassword(password);
        employee.setPin(pin);
        ensureUserCapacity();
        userKeys[userCount] = username;
        userVals[userCount] = employee;
        userCount++;
        ensurePinCapacity();
        pinKeys[pinCount] = pin;
        pinVals[pinCount] = employee;
        pinCount++;
        return true;
    }

    /**
     * Attempt login — returns employee when username/password/pin match, otherwise null.
     */
    public synchronized BaseEmployee login(String username, String password, String pin){
        if(username == null || password == null || pin == null) return null;
        int idx = findUserIndex(username);
        if(idx == -1) return null;
        BaseEmployee emp = userVals[idx];
        if(emp == null) return null;
        if(!emp.verifyPassword(password)) return null;
        if(!emp.verifyPin(pin)) return null;
        return emp;
    }

    public synchronized BaseEmployee findByUsername(String username){
        int idx = findUserIndex(username);
        return (idx == -1) ? null : userVals[idx];
    }
}
