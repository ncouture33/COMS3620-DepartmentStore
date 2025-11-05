To run the main method

1. mvn package 
2. Format: java -cp target/my-app-1.0-SNAPSHOT.jar com.mycompany.app.App 
which for me was java -cp .\target\COMS3620-DepartmentStore-1.0-SNAPSHOT.jar SystemManager.Main

For writing and reading to the database:
- Have your class implement the interface Data
- The database can then just call getData(), which will return the object's data as string so that it can be written to a file
- You'll still need to implement a method to read from the text file and parse into an object
