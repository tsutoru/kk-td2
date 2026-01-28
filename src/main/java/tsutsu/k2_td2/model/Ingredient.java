package tsutsu.k2_td2.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ingredient {

    private Integer id; // Integer pour permettre null si pas encore en base
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList = new ArrayList<>();

    public Ingredient(Integer id, String name, Double price, CategoryEnum category, List<StockMovement> stockMovementList) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        if (stockMovementList != null) {
            this.stockMovementList = stockMovementList;
        }
    }

    public Ingredient(int id, String name, Double price, CategoryEnum category) {
        // constructeur vide utile pour JDBC mapping
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public StockValue getStockValueAt(Instant t) {
        double q = 0.0;

        if (stockMovementList != null) {
            for (StockMovement m : stockMovementList) {
                if (m.getCreationDatetime() != null && !m.getCreationDatetime().isAfter(t)) {
                    double delta = m.getValue().getQuantity();
                    q += (m.getType() == MovementType.IN) ? delta : -delta;
                }
            }
        }

        return new StockValue(q, UnitType.KG);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                '}';
    }
}
