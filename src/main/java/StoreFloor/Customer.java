package StoreFloor;

public class Customer extends AbstractPerson{
    private boolean rewardsMember;
    private String email;
    private String phone;

    public Customer(String name, boolean rewardsMember){
        super(name);
        this.rewardsMember = rewardsMember;
    }
    public boolean isRewardsMember(){
        return rewardsMember;
    }
    public void setContantInfo(String email, String phone){
        this.email = email;
        this.phone = phone;

    }

    public String getPhone(){
        return phone;
    }
    public String getEmail(){
        return email;
    }
}