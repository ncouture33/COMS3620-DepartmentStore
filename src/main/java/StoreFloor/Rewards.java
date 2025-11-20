package StoreFloor;
public class Rewards implements Data{
    private int id;
    private int points;
    private String email;
    private String phoneNumber;
    
    public Rewards(int id, int points, String email, String phoneNumber){
        this.id = id;
        this.points = points;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    public void setID(int id){
        this.id = id;
    }   
    public int getId(){
        return id;
    }
    public int getPoints(){
        return points;
    }

    public void setPoints(int points){
        this.points = points;
    }

    public void addPoints(int points){
        this.points += points;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    public String getEmail(){
        return email;
    }

    public String getData()
    {
        return  id + " " + points + " " + email + " " + phoneNumber + " ";
    }
}