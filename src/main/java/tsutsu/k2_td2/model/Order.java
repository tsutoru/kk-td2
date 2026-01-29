package tsutsu.k2_td2.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders = new ArrayList<>();

    private static final double VAT_RATE = 0.20;

    public Order() {}

    public Order(Integer id, String reference, Instant creationDatetime, List<DishOrder> dishOrders) {
        this.id = id;
        this.reference = reference;
        this.creationDatetime = creationDatetime;
        if (dishOrders != null) this.dishOrders = dishOrders;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Instant getCreationDatetime() { return creationDatetime; }
    public void setCreationDatetime(Instant creationDatetime) { this.creationDatetime = creationDatetime; }

    public List<DishOrder> getDishOrders() { return dishOrders; }
    public void setDishOrders(List<DishOrder> dishOrders) { this.dishOrders = dishOrders; }


    public double getTotalAmountWithoutVAT() {
        double total = 0.0;
        if (dishOrders == null) return 0.0;

        for (DishOrder d : dishOrders) {
            if (d.getDish() != null && d.getDish().getPrice() != null) {
                total += d.getDish().getPrice() * d.getQuantity();
            }
        }
        return total;
    }


    public double getTotalAmountWithVAT() {
        return getTotalAmountWithoutVAT() * (1.0 + VAT_RATE);
    }
}
