package tsutsu.k2_td2.model;

import java.util.List;

public class Dish {
    int id;
    String name;
    DishtypeEnum dishtype;
    List<Ingredient> ingredients;

    public Dish(List<Ingredient> ingredients, DishtypeEnum dishtype, String name, int id) {
        this.ingredients = ingredients;
        this.dishtype = dishtype;
        this.name = name;
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
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishtype=" + dishtype +
                ", ingredients=" + ingredients +
                '}';
    }
}
