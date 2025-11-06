package StoreFloor;

public abstract class AbstractPerson implements Actor{
    protected String name;

    public AbstractPerson(String name){
        this.name = name;
    }
    @Override
    public String getName(){
        return name;
    }
}
