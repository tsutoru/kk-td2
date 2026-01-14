package tsutsu.k2_td2.DataRetriever;

import tsutsu.k2_td2.DbConnection.DbConnection;
import tsutsu.k2_td2.model.Dish;
import tsutsu.k2_td2.model.DishtypeEnum;
import tsutsu.k2_td2.model.Ingredient;
import tsutsu.k2_td2.model.CategoryEnum;

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

        String query = "SELECT id, name, dish_type, price FROM dish WHERE id = ?";

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
        String query = "SELECT id, name, price, category FROM Ingredient WHERE id_dish = ? LIMIT ? OFFSET ?";

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, dishId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        new Dish(null, null,null,null, dishId)
                );
                ingredients.add(ingredient);
            }
        }

        return ingredients;
    }

    public Ingredient createIngredient(Ingredient ingredient) throws Exception {
        String checkQuery = "SELECT id FROM Ingredient WHERE name = ? AND id_dish = ?";
        String insertQuery = "INSERT INTO dish (name, dish_type, price) VALUES (?, ?, ?) RETURNING id\n";

        try (Connection connection = DbConnection.getDbConnection()) {

            // Vérifier si existe
            try (PreparedStatement checkPs = connection.prepareStatement(checkQuery)) {
                checkPs.setString(1, ingredient.getName());
                checkPs.setInt(2, ingredient.getDish().getId());
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
                insertPs.setInt(4, ingredient.getDish().getId());

                ResultSet rs = insertPs.executeQuery();
                if (rs.next()) {
                    ingredient.setId(rs.getInt("id"));
                }
            }

            return ingredient;
        }
    }

    public  Dish saveDish(Dish dish) throws Exception {
        String checkQuery = "SELECT id FROM Dish WHERE id = ?";
        String insertQuery = "INSERT INTO Dish (name, dish_type) VALUES (?, ?) RETURNING id";
        String updateQuery = "UPDATE dish SET name = ?, dish_type = ?, price = ? WHERE id = ?\n";

        try (Connection connection = DbConnection.getDbConnection()) {
            if (dish.getId() > 0) {
                try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
                    ps.setString(1, dish.getName());
                    ps.setString(2, dish.getDishtype().name());
                    ps.setInt(3, dish.getId());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
                    ps.setString(1, dish.getName());
                    ps.setString(2, dish.getDishtype().name());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        dish.setId(rs.getInt("id"));
                    }
                }
            }
            PreparedStatement ps = null;
            if (dish.getPrice() == null) {
                ps.setNull(3, Types.NUMERIC);
            } else {
                ps.setDouble(3, dish.getPrice());
            }

        }

        return dish;
    }

    public List<Dish> findDishByIngredientName(String ingredientName) throws Exception {
        List<Dish> dishes = new ArrayList<>();
        String query = "SELECT DISTINCT d.id, d.name, d.dish_type " +
                "FROM Dish d " +
                "JOIN Ingredient i ON d.id = i.id_dish " +
                "WHERE i.name ILIKE ?";

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();
            Double price = rs.getBigDecimal("price") != null
                    ? rs.getBigDecimal("price").doubleValue()
                    : null;

            while (rs.next()) {
                Dish dish = new Dish(
                                        new ArrayList<>(),
                                        DishtypeEnum.valueOf(rs.getString("dish_type")),
                                        rs.getString("name"),
                        price,
                        rs.getInt("id")
                                );

                dish.setIngredients(findIngredientByDishId(dish.getId(), 10, 0));
                dishes.add(dish);
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



}
