package SystemManager;



import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Department Management System is running...");
        Scanner scanner = new Scanner(System.in);
        
        while (true){
            System.out.print("1: HR related actions\n");
            System.out.print("2: Store Floor actions\n");
            System.out.print("5: Inventory actions\n");
            
            String command = scanner.nextLine();

            if (command.equals("1")){
                HR.Util.runHR(scanner);
            }
            else if (command.equals("5")){
                inventory.Util.runInventory(scanner);
            }else if(command.equals("2")){
                StoreFloor.Util.runSales(scanner);
            }
            //More options go here

            else if (command.equals("exit")){
                System.out.println("Exiting Department Management System.");
                break;
            }
        }
        scanner.close();
    }
}

