package tsutsu.k2_td2.DataRetriever;

import tsutsu.k2_td2.DbConnection.DbConnection;
import tsutsu.k2_td2.model.Dish;
import tsutsu.k2_td2.model.DishtypeEnum;
import tsutsu.k2_td2.model.Ingredient;
import tsutsu.k2_td2.model.CategoryEnum;
import tsutsu.k2_td2.model.DishIngredient;
import tsutsu.k2_td2.model.UnitType;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DataRetriever {


//    Dish findDishById (Integer id){
//        Connection connection = DbConnection.getdbConnection();
//        PreparedStatement preparedStatement = connection.prepareStatement("?");
//        PreparedStatement.setInt(1, id);
//        ResultSet resultSet = preparedStatement.executeQuery();
//        if(resultSet.next()){
//            DbConnection.closeConnection(connection)
//        }
//    }

    public Dish findDishById(Integer id) throws Exception {

        String query = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?\n";

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();


            if (rs.next()) {

                Double price = rs.getBigDecimal("price") == null
                        ? null
                        : rs.getBigDecimal("price").doubleValue();

                Dish dish = new Dish(
                                        new ArrayList<>(),
                                        DishtypeEnum.valueOf(rs.getString("dish_type")),
                                        rs.getString("name"),
                        price,
                        rs.getInt("id")
                                );




                dish.setIngredients(findIngredientByDishId(dish.getId(),10,0));
                return dish;
            } else {
                return null;
            }
        }
    }

    public List<Ingredient> findIngredientByDishId(Integer dishId, int limit, int offset) throws Exception {
        List<Ingredient> ingredients = new ArrayList<>();

        String query =
                "SELECT i.id, i.name, i.price, i.category " +
                        "FROM dish_ingredient di " +
                        "JOIN ingredient i ON i.id = di.id_ingredient " +
                        "WHERE di.id_dish = ? " +
                        "LIMIT ? OFFSET ?";

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, dishId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            CategoryEnum.valueOf(rs.getString("category")),
                            null // plus de "new Dish(...)" ici, car ingredient n'a plus id_dish
                    );
                    ingredients.add(ingredient);
                }
            }
        }
        return ingredients;
    }


    public Ingredient createIngredient(Ingredient ingredient) throws Exception {
        String checkQuery = "SELECT id FROM ingredient WHERE name = ?";
        String insertQuery = "INSERT INTO ingredient (name, price, category) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DbConnection.getDbConnection()) {

            try (PreparedStatement checkPs = connection.prepareStatement(checkQuery)) {
                checkPs.setString(1, ingredient.getName());
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    ingredient.setId(rs.getInt("id"));
                    return ingredient;
                }
            }

            try (PreparedStatement insertPs = connection.prepareStatement(insertQuery)) {
                insertPs.setString(1, ingredient.getName());
                insertPs.setDouble(2, ingredient.getPrice());
                insertPs.setString(3, ingredient.getCategory().name());

                ResultSet rs = insertPs.executeQuery();
                if (rs.next()) {
                    ingredient.setId(rs.getInt("id"));
                }
            }
            return ingredient;
        }
    }


    public Dish saveDish(Dish dish) throws Exception {
        String insertQuery = "INSERT INTO dish (name, dish_type, selling_price) VALUES (?, ?, ?) RETURNING id";
        String updateQuery = "UPDATE dish SET name = ?, dish_type = ?, selling_price = ? WHERE id = ?";

        try (Connection connection = DbConnection.getDbConnection()) {
            if (dish.getId() != 0 && dish.getId() > 0) {
                try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
                    ps.setString(1, dish.getName());
                    ps.setString(2, dish.getDishtype().name());

                    if (dish.getPrice() == null) ps.setNull(3, Types.NUMERIC);
                    else ps.setDouble(3, dish.getPrice());

                    ps.setInt(4, dish.getId());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
                    ps.setString(1, dish.getName());
                    ps.setString(2, dish.getDishtype().name());

                    if (dish.getPrice() == null) ps.setNull(3, Types.NUMERIC);
                    else ps.setDouble(3, dish.getPrice());

                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) dish.setId(rs.getInt("id"));
                }
            }
        }
        return dish;
    }

    public List<Dish> findDishByIngredientName(String ingredientName) throws Exception {
        List<Dish> dishes = new ArrayList<>();

        String query =
                "SELECT DISTINCT d.id, d.name, d.dish_type, d.selling_price " +
                        "FROM dish d " +
                        "JOIN dish_ingredient di ON di.id_dish = d.id " +
                        "JOIN ingredient i ON i.id = di.id_ingredient " +
                        "WHERE i.name ILIKE ?";

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, "%" + ingredientName + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Double sellingPrice = rs.getBigDecimal("selling_price") == null
                            ? null
                            : rs.getBigDecimal("selling_price").doubleValue();

                    Dish dish = new Dish(
                            new ArrayList<>(),
                            DishtypeEnum.valueOf(rs.getString("dish_type")),
                            rs.getString("name"),
                            sellingPrice,
                            rs.getInt("id")
                    );

                    dish.setIngredients(findIngredientByDishId(dish.getId(), 10, 0));
                    dishes.add(dish);
                }
            }
        }
        return dishes;
    }

    public void updateDishPrice(int dishId, Double price) throws Exception {

        String sql = "UPDATE dish SET price = ? WHERE id = ?";

        try (Connection conn = DbConnection.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (price == null) {
                ps.setNull(1, Types.NUMERIC);
            } else {
                ps.setDouble(1, price);
            }

            ps.setInt(2, dishId);
            ps.executeUpdate();
        }
    }

    public List<DishIngredient> findDishIngredientsByDishId(Integer dishId, int limit, int offset) throws Exception {
        List<DishIngredient> out = new ArrayList<>();

        String query =
                "SELECT di.id AS di_id, di.quantity_required, di.unit, " +
                        "       i.id AS i_id, i.name AS i_name, i.price AS i_price, i.category AS i_category " +
                        "FROM dish_ingredient di " +
                        "JOIN ingredient i ON i.id = di.id_ingredient " +
                        "WHERE di.id_dish = ? " +
                        "LIMIT ? OFFSET ?";

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, dishId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient(
                            rs.getInt("i_id"),
                            rs.getString("i_name"),
                            rs.getDouble("i_price"),
                            CategoryEnum.valueOf(rs.getString("i_category")),
                            null // plus besoin de dish dans Ingredient
                    );

                    Dish dishRef = new Dish(null, null, null, null, dishId);

                    DishIngredient di = new DishIngredient(
                            rs.getInt("di_id"),
                            dishRef,
                            ingredient,
                            rs.getBigDecimal("quantity_required").doubleValue(),
                            UnitType.valueOf(rs.getString("unit"))
                    );

                    out.add(di);
                }
            }
        }

        return out;
    }




}

