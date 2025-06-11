package Model.DataEntities;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private static final long serialVersionUID=103L;
    private int orderItemId; //Database auto-generated ID
    private String orderIdFk;//Foreign key to Order table
    private String drinkId;
    private String drinkName; //For convenience in display/reports
    private int quantity;
    private double priceAtTimeOfOrder;
    private double itemTotal;

    //Constructor for creating a new item before saving to the database (orderItemId is not known yet)
    public OrderItem(String drinkId, String drinkName,int quantity,double priceAtTimeOfOrder){
        if(quantity<=0){throw new IllegalArgumentException("Quantity must be positive");}
        if(priceAtTimeOfOrder<0){throw new IllegalArgumentException("Price cannot be negative");}
        this.drinkId=drinkId;
        this.drinkName=drinkName;
        this.quantity=quantity;
        this.priceAtTimeOfOrder=priceAtTimeOfOrder;
        this.itemTotal=quantity*priceAtTimeOfOrder;
    }
    // Constructor for mapping from database result (includes orderItemId)
    public OrderItem(int orderItemId, String orderIdFk, String drinkId, String drinkName, int quantity, double priceAtTimeOfOrder, double itemTotal) {
        this(drinkId, drinkName, quantity, priceAtTimeOfOrder); // Calls the other constructor
        this.orderItemId = orderItemId;
        this.orderIdFk = orderIdFk; // Set the foreign key
        this.itemTotal = itemTotal; // Use total from DB to be safe or recalculate
    }

    //Getters
    public int getOrderItemId(){return orderItemId;}
    public String getOrderIdFk(){return orderIdFk;}
    public String getDrinkId(){return drinkId;}
    public String getDrinkName(){return drinkName;}
    public int getQuantity(){return quantity;}
    public double getPriceAtTimeOfOrder(){return priceAtTimeOfOrder;}
    public double getItemTotal(){return itemTotal;}

    //Setters (e.g., for drinkName if populated after fetching Drink object)
    public void setDrinkName(String drinkName){this.drinkName=drinkName;}
    public void setOrderIdFk(String orderIdFk){this.orderIdFk=orderIdFk;}

    @Override
    public String toString(){
        return drinkName + " (ID: " + drinkId + ") - Qty: " + quantity + " @ " + String.format("%.2f", priceAtTimeOfOrder) + " = " + String.format("%.2f", itemTotal);
    }



}
