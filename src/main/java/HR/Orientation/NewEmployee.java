package HR.Orientation;

import HR.OrientableEmployee;

public class NewEmployee extends OrientableEmployee {

    public NewEmployee (int id, String fName, String lName, int DOB, int social) {
        super(id, fName, lName, DOB, social);
    }
        @Override
        public void resetTimeCard() {
            System.out.println("Time card has been reset for " + fName + " " + lName);
        }
        
        @Override
        public double renderPayment() {
            return 300.00;
        }
    

    public void startOrientation() {
        System.out.println(fName + " has started orientation.");
    }

}
