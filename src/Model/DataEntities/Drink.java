package Model.DataEntities;

import java.io.Serializable;

public class Drink implements Serializable {
    private static final long serialVersionUID=101L;
    private String id;
    private String name;
    private String brand;
    private double price;
    private int initialStock;

    public Drink(String id, String name, String brand, double price, int initialStock){
        this.id=id;
        this.name=name;
        this.brand=brand;
        this.price=price;
        this.initialStock=initialStock;
    }
    // Constructor without initialStock, useful for fetching from DB where initialStock isn't a direct column
    public Drink(String id, String name, String brand, double price) {
        this(id, name, brand, price, 0); // Default initial stock to 0
    }

    //Getters
    public String getId(){return id;}
    public String getName(){return name;}
    public String getBrand(){return brand;}
    public double getPrice(){return price;}
    public int getInitialStock(){return initialStock;}

    //Setters(if needed e.g. for price updates)
    public void setPrice(double price){this.price=price;}

    @Override
    public String toString(){// Used in JList/JComboBox for client display
        return name + " ("+ id + ") -"+ String.format("Ksh %.2f",price);
    }
    @Override
    public boolean equals(Object o){
        if(this==o){return true;}
        if(o==null||getClass()!=o.getClass()){return false;}
        Drink drink=(Drink) o;
        return id.equals(drink.id);
    }
    @Override
    public int hashCode(){
        return id.hashCode();
    }

}
