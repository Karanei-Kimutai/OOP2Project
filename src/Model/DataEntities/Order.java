package Model.DataEntities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID=104L;
    private String orderId;
    private String customerId;
    private String branchId;
    private LocalDateTime orderTimestamp;
    private List<OrderItem> items;
    private double totalAmount;

    public Order(String orderId,String customerId,String branchId,LocalDateTime orderTimestamp, List<OrderItem> items,double totalAmount){
        this.orderId=orderId;
        this.customerId=customerId;
        this.branchId=branchId;
        this.orderTimestamp=orderTimestamp;
        this.items=items!=null? items:new ArrayList<>();
        this.totalAmount=totalAmount;
        // Recalculate totalAmount if items are provided and totalAmount is zero (or not set)
        if (!this.items.isEmpty() && this.totalAmount == 0) {
            this.totalAmount = this.items.stream().mapToDouble(OrderItem::getItemTotal).sum();
        }
    }

    //Getters
    public String getOrderId(){return orderId;}
    public String getCustomerId(){return customerId;}
    public String getBranchId(){return branchId;}
    public LocalDateTime getOrderTimestamp(){return orderTimestamp;}
    public List<OrderItem> getItems(){return Collections.unmodifiableList(items);}
    public double getTotalAmount(){return totalAmount;}


    // Setter for items (e.g., when DAO populates them after fetching the order header)
    public void setItems(List<OrderItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId + ", Customer: " + customerId + ", Branch: " + branchId +
                ", Timestamp: " + orderTimestamp + ", Total: " + String.format("%.2f", totalAmount) +
                ", Items: " + (items != null ? items.size() : 0);
    }
}
