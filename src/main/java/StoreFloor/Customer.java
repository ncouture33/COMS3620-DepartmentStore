package StoreFloor;

public class Customer extends AbstractPerson{
    private boolean rewardsMember;
    private Rewards rewards;

    public Customer(String name, boolean rewardsMember){
        super(name);
        this.rewardsMember = rewardsMember;
    }

    public void setRewards(Rewards rewards){
        this.rewards = rewards;
    }

    public boolean isRewardsMember(){
        return rewardsMember;
    }
    public Rewards getRewards(){
        return rewards;
    }

    public String getData(){
        return "\n" + getName() + " " + rewardsMember + " ";
    }   
}