package tsutsu.k2_td2.model;

public class RestaurantTable {
    private Integer id;
    private String label;
    private int seats;

    public RestaurantTable(Integer id, String label, int seats) {
        this.id = id; this.label = label; this.seats = seats;
    }
    public Integer getId() { return id; }
    public String getLabel() { return label; }
    public int getSeats() { return seats; }

    @Override
    public String toString() {
        return "RestaurantTable{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", seats=" + seats +
                '}';
    }
}
