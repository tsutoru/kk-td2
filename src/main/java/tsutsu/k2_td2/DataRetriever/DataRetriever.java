package tsutsu.k2_td2.DataRetriever;

import tsutsu.k2_td2.DbConnection.DbConnection;
import tsutsu.k2_td2.model.*;


import java.math.BigDecimal;
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

        String query = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                BigDecimal sp = rs.getBigDecimal("selling_price");
                Double price = (sp == null) ? null : sp.doubleValue();

                Dish dish = new Dish(
                        new ArrayList<>(),
                        DishtypeEnum.valueOf(rs.getString("dish_type")),
                        rs.getString("name"),
                        price,
                        rs.getInt("id")
                );

                dish.setIngredients(findIngredientByDishId(dish.getId(), 10, 0));
                return dish;
            }
        }
    }


    public List<Ingredient> findIngredientByDishId(Integer dishId, int limit, int offset) throws Exception {
        List<Ingredient> ingredients = new ArrayList<>();

        String query = """
        SELECT i.id, i.name, i.price, i.category
        FROM dish_ingredient di
        JOIN ingredient i ON i.id = di.id_ingredient
        WHERE di.id_dish = ?
        ORDER BY i.id
        LIMIT ? OFFSET ?;
    """;

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, dishId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.math.BigDecimal p = rs.getBigDecimal("price");
                    Double price = (p == null) ? null : p.doubleValue();

                    Ingredient ingredient = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            price,
                            CategoryEnum.valueOf(rs.getString("category"))
                    );
                    ingredients.add(ingredient);
                }
            }
        }
        return ingredients;
    }



    public Ingredient createIngredient(Ingredient ingredient) throws Exception {

        String query = """
        INSERT INTO ingredient (name, price, category)
        VALUES (?, ?, ?)
        ON CONFLICT (name) DO UPDATE
          SET price = EXCLUDED.price,
              category = EXCLUDED.category
        RETURNING id;
    """;

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, ingredient.getName());

            if (ingredient.getPrice() == null)
                ps.setNull(2, Types.NUMERIC);
            else
                ps.setBigDecimal(2, java.math.BigDecimal.valueOf(ingredient.getPrice()));

            // 🔥 ENUM Postgres → Types.OTHER
            ps.setObject(3, ingredient.getCategory().name(), Types.OTHER);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ingredient.setId(rs.getInt("id"));
                else throw new Exception("createIngredient: aucun id retourné");
            }

            return ingredient;
        }
    }




    public Dish saveDish(Dish dish) throws Exception {

        String insertQuery =
                "INSERT INTO dish (name, dish_type, selling_price) VALUES (?, ?, ?) RETURNING id";
        String updateQuery =
                "UPDATE dish SET name = ?, dish_type = ?, selling_price = ? WHERE id = ?";

        Integer id = dish.getId();

        boolean shouldUpdate = false;
        if (id != null && id > 0) {
            // Vérifie si l'id existe vraiment en base
            Dish existing = findDishById(id);
            shouldUpdate = (existing != null);
        }

        try (Connection connection = DbConnection.getDbConnection()) {

            if (shouldUpdate) {
                try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
                    ps.setString(1, dish.getName());
                    ps.setObject(2, dish.getDishtype().name(), java.sql.Types.OTHER); // enum PG safe

                    if (dish.getPrice() == null) ps.setNull(3, java.sql.Types.NUMERIC);
                    else ps.setBigDecimal(3, java.math.BigDecimal.valueOf(dish.getPrice()));

                    ps.setInt(4, dish.getId());

                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        throw new Exception("UPDATE a modifié 0 ligne alors que l'id existe supposément: " + dish.getId());
                    }
                }
                return dish;

            } else {
                // INSERT
                try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
                    ps.setString(1, dish.getName());
                    ps.setObject(2, dish.getDishtype().name(), java.sql.Types.OTHER);

                    if (dish.getPrice() == null) ps.setNull(3, java.sql.Types.NUMERIC);
                    else ps.setBigDecimal(3, java.math.BigDecimal.valueOf(dish.getPrice()));

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) dish.setId(rs.getInt("id"));
                        else throw new Exception("INSERT n'a pas retourné d'id");
                    }
                }
                return dish;
            }
        }
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
        String sql = "UPDATE dish SET selling_price = ? WHERE id = ?";

        try (Connection conn = DbConnection.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (price == null) ps.setNull(1, Types.NUMERIC);
            else ps.setBigDecimal(1, java.math.BigDecimal.valueOf(price));

            ps.setInt(2, dishId);

            int updated = ps.executeUpdate();
            if (updated == 0) throw new Exception("Dish id=" + dishId + " introuvable");
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
                            CategoryEnum.valueOf(rs.getString("i_category"))
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
    public Ingredient findIngredientById(Integer id) throws Exception {

        String query = "SELECT id, name, price, category FROM ingredient WHERE id = ?";

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price") == null ? null : rs.getBigDecimal("price").doubleValue(),
                        CategoryEnum.valueOf(rs.getString("category")),
                        new ArrayList<>()
                );

                ingredient.setStockMovementList(findStockMovementsByIngredientId(id));
                return ingredient;
            }
        }
    }
    private List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId) throws Exception {

        List<StockMovement> movements = new ArrayList<>();

        String query = """
        SELECT id, quantity, type, unit, creation_datetime
        FROM stock_movement
        WHERE id_ingredient = ?
        ORDER BY creation_datetime
    """;

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, ingredientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movements.add(new StockMovement(
                            rs.getInt("id"),
                            ingredientId,
                            new StockValue(rs.getBigDecimal("quantity").doubleValue(),
                                    UnitType.valueOf(rs.getString("unit"))),
                            MovementType.valueOf(rs.getString("type")),
                            rs.getTimestamp("creation_datetime").toInstant()
                    ));
                }
            }
        }

        return movements;
    }
    public Ingredient saveIngredient(Ingredient ingredient) throws Exception {

        String insertIngredient = """
        INSERT INTO ingredient (name, price, category)
        VALUES (?, ?, ?)
        RETURNING id
    """;

        String updateIngredient = """
        UPDATE ingredient
        SET name = ?, price = ?, category = ?
        WHERE id = ?
    """;

        try (Connection connection = DbConnection.getDbConnection()) {

            if (ingredient.getId() == null) {
                try (PreparedStatement ps = connection.prepareStatement(insertIngredient)) {
                    ps.setString(1, ingredient.getName());
                    ps.setBigDecimal(2, BigDecimal.valueOf(ingredient.getPrice()));
                    ps.setObject(3, ingredient.getCategory().name(), Types.OTHER);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) ingredient.setId(rs.getInt("id"));
                    }
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(updateIngredient)) {
                    ps.setString(1, ingredient.getName());
                    ps.setBigDecimal(2, BigDecimal.valueOf(ingredient.getPrice()));
                    ps.setObject(3, ingredient.getCategory().name(), Types.OTHER);
                    ps.setInt(4, ingredient.getId());
                    ps.executeUpdate();
                }
            }

            // 🔥 Gestion des mouvements
            if (ingredient.getStockMovementList() != null) {
                for (StockMovement m : ingredient.getStockMovementList()) {
                    saveStockMovement(connection, ingredient.getId(), m);
                }
            }

            return findIngredientById(ingredient.getId());
        }
    }
    private void saveStockMovement(Connection connection, Integer ingredientId, StockMovement m) throws Exception {

        String sql = """
        INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
        VALUES (?, ?, ?, ?, ?, ?)
        ON CONFLICT (id) DO NOTHING
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, m.getId());
            ps.setInt(2, ingredientId);
            ps.setBigDecimal(3, BigDecimal.valueOf(m.getValue().getQuantity()));
            ps.setObject(4, m.getType().name(), Types.OTHER);
            ps.setObject(5, m.getValue().getUnit().name(), Types.OTHER);
            ps.setTimestamp(6, Timestamp.from(m.getCreationDatetime()));
            ps.executeUpdate();
        }
    }






}

