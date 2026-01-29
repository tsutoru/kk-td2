package tsutsu.k2_td2.model;

import java.util.ArrayList;
import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishtypeEnum dishType;
    private Double price;
    private List<DishIngredient> ingredients = new ArrayList<>();

    public Dish() {}

    public Dish(List<DishIngredient> ingredients, DishtypeEnum dishType, String name, Double price, Integer id) {
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
        this.dishType = dishType;
        this.name = name;
        this.price = price;
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishtypeEnum getDishType() { return dishType; }
    public void setDishType(DishtypeEnum dishType) { this.dishType = dishType; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public List<DishIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<DishIngredient> ingredients) { this.ingredients = ingredients; }

    // Coût de revient (stock cost)
    public double getStockCost() {
        double cost = 0.0;
        if (ingredients == null) return 0.0;

        for (DishIngredient di : ingredients) {
            if (di.getIngredient() != null && di.getIngredient().getPrice() != null) {
                cost += di.getIngredient().getPrice() * di.getQuantityRequired();
            }
        }
        return cost;
    }

    // Marge brute
    public double getGrossMargin() {
        if (price == null) return 0.0;
        return price - getStockCost();
    }
}
