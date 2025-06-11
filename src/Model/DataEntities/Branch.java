package Model.DataEntities;

import java.io.Serializable;

public class Branch implements Serializable {
    private static final long serialVersionUID=102L;
    private String id;
    private String name;
    private String location;

    public Branch(String id, String name, String location){
        this.id=id;
        this.name=name;
        this.location=location;
    }

    public String getId(){return id;}
    public String getName(){return name;}
    public String getLocation(){return location;}

    @Override
    public String toString(){
        return name+" ("+ id +")";
    }
    @Override
    public boolean equals(Object o){
        if(this==o){return true;}
        if(o==null|| getClass()!=o.getClass()){return false;}
        Branch branch=(Branch) o;
        return id.equals(branch.id);
    }
    @Override
    public int hashCode(){
        return id.hashCode();
    }

}
