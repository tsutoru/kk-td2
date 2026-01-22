package tsutsu.k2_td2.model;

import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishtypeEnum dishtype;
    private Double price;
    private List<Ingredient> ingredients;


    public Double getDishCost(){
        return ingredients == null ? null : ingredients.stream()
                .mapToDouble(Ingredient::getPrice)
                .sum();
    }
    public Double getGrossMargin(){
        if (price == null) {
          throw new NullPointerException("price is null");
        }

        return price - getDishCost();
    }

    public Dish(List<Ingredient> ingredients, DishtypeEnum dishtype, String name,Double price ,int id) {
        this.ingredients = ingredients;
        this.dishtype = dishtype;
        this.name = name;
        this.price = price;
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public DishtypeEnum getDishtype() {
        return dishtype;
    }

    public void setDishtype(DishtypeEnum dishtype) {
        this.dishtype = dishtype;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishtype, ingredients);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishtype=" + dishtype +
                ", price=" + price +
                ", ingredients=" + ingredients +
                '}';
    }
}
