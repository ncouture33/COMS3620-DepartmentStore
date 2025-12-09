# High-End Department Store

The High-End Department Store Management System is designed to effectively and efficiently operate a department store in a manner that portrays high-class luxury to consumers. This system addresses the operational needs in a department store setting, enabling the business to run smoothly and provide the best possible experience for consumers. These operational needs encompass a range of roles, from the cashiers working the registers to the board of directors making overarching business decisions. The business functions are also a core part of the application, including inventory management, customer service, employee onboarding and training, and employee offboarding.

## How to Run

Prerequisites: Maven must be installed on your machine

To run the main method:

1. `mvn package` 
2. Format: `java -cp target/my-app-1.0-SNAPSHOT.jar com.mycompany.app.App` \
(which for me was `java -cp .\target\COMS3620-DepartmentStore-1.0-SNAPSHOT.jar SystemManager.Main`)

## Contribution Tips

For reading and writing to the database:
1. Have your class implement the interface `Data`.
2. The database can then just call `getData()`. This will return the object's data as a string so that it can be written to a file. \
    **Note:** You will still need to implement a method to read from the text file and parse it into an object

Products have been approved/rejected and are conceptual versions of inventory