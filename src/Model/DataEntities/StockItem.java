package Model.DataEntities;

// Internal representation for stock data, mainly for DAO layer
public class StockItem {
    String branchId;
    String drinkId;
    int quantity;
    int minimumThreshold;

    public StockItem(String branchId, String drinkId, int quantity, int minimumThreshold){
        this.branchId=branchId;
        this.drinkId=drinkId;
        this.quantity=quantity;
        this.minimumThreshold=minimumThreshold;
    }

    public String getBranchId(){return branchId;}
    public String getDrinkId(){return drinkId;}
    public int getQuantity(){return quantity;}
    public int getMinimumThreshold(){return minimumThreshold;}

    public void setQuantity(int quantity){this.quantity=quantity;}
    public void setMinimumThreshold(int minimumThreshold){this.minimumThreshold=minimumThreshold;}


}
