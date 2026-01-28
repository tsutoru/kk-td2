package tsutsu.k2_td2.model;

import java.time.Instant;

public class StockMovement {

    private Integer id;
    private Integer ingredientId; // on stocke l'id, pas l'objet
    private StockValue value;
    private MovementType type;
    private Instant creationDatetime;

    public StockMovement(Integer id, Integer ingredientId, StockValue value, MovementType type, Instant creationDatetime) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.value = value;
        this.type = type;
        this.creationDatetime = creationDatetime;
    }

    public StockMovement() {
        // constructeur vide pour JDBC mapping
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Integer ingredientId) {
        this.ingredientId = ingredientId;
    }

    public StockValue getValue() {
        return value;
    }

    public void setValue(StockValue value) {
        this.value = value;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }
}
