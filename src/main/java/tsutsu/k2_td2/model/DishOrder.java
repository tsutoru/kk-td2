package tsutsu.k2_td2.model;

public class DishOrder {
    private Integer id;
    private Dish dish;
    private int quantity;

    public DishOrder() {}

    public DishOrder(Integer id, Dish dish, int quantity) {
        this.id = id;
        this.dish = dish;
        this.quantity = quantity;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
