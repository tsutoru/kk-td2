package tsutsu.k2_td2.model;


public class DishIngredient {
    private Integer id; // optionnel si tu as un id dans dish_ingredient
    private Dish dish;
    private Ingredient ingredient;
    private Double quantityRequired;
    private UnitType unit; // PCS / KG / L

    public DishIngredient(Integer id, Dish dish, Ingredient ingredient, Double quantityRequired, UnitType unit) {
        this.id = id;
        this.dish = dish;
        this.ingredient = ingredient;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public Integer getId() { return id; }
    public Dish getDish() { return dish; }
    public Ingredient getIngredient() { return ingredient; }
    public Double getQuantityRequired() { return quantityRequired; }
    public UnitType getUnit() { return unit; }
}

