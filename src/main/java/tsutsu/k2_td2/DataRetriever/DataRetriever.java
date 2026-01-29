package tsutsu.k2_td2.DataRetriever;

import org.springframework.web.ErrorResponseException;
import tsutsu.k2_td2.DbConnection.DbConnection;
import tsutsu.k2_td2.model.*;


import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
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
                    ps.setObject(2, dish.getDishType().name(), java.sql.Types.OTHER); // enum PG safe

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
                    ps.setObject(2, dish.getDishType().name(), java.sql.Types.OTHER);

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

    public Order createOrder(Order order) throws Exception {

        String insertOrder = """
        INSERT INTO "order"(reference, creation_datetime)
        VALUES (?, NOW())
        RETURNING id, creation_datetime
    """;

        String insertDishOrder = """
        INSERT INTO dish_order(id_order, id_dish, quantity)
        VALUES (?, ?, ?)
        ON CONFLICT (id_order, id_dish) DO UPDATE SET quantity = EXCLUDED.quantity
        RETURNING id
    """;

        try (Connection connection = DbConnection.getDbConnection()) {
            connection.setAutoCommit(false);

            try {
                String ref = nextOrderReference(connection);

                // Insert order
                try (PreparedStatement ps = connection.prepareStatement(insertOrder)) {
                    ps.setString(1, ref);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        order.setId(rs.getInt("id"));
                        order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                    }
                }

                order.setReference(ref);

                // Insert dish_order
                for (DishOrder d : order.getDishOrders()) {
                    try (PreparedStatement ps = connection.prepareStatement(insertDishOrder)) {
                        ps.setInt(1, order.getId());
                        ps.setInt(2, d.getDish().getId());
                        ps.setInt(3, d.getQuantity());

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            d.setId(rs.getInt("id"));
                        }
                    }
                }

                connection.commit();
                return order;

            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private String nextOrderReference(Connection connection) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT nextval('order_ref_seq') AS n");
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            long n = rs.getLong("n");
            return String.format("ORD%05d", n);
        }

    }
    private double getCurrentStockKg(Connection connection, int ingredientId) throws Exception {
        String q = """
        SELECT COALESCE(SUM(
          CASE WHEN type = 'IN' THEN quantity ELSE -quantity END
        ), 0) AS stock
        FROM stock_movement
        WHERE id_ingredient = ?
    """;

        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setInt(1, ingredientId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("stock").doubleValue();
            }
        }
    }

    private List<int[]> getDishIngredientNeeds(Connection connection, int dishId) throws Exception {
        // retourne une liste de (ingredientId, quantityRequired*1000?) -> on reste en double
        String q = """
        SELECT id_ingredient, quantity_required
        FROM dish_ingredient
        WHERE id_dish = ?
    """;

        List<int[]> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int ingId = rs.getInt("id_ingredient");
                    // on va gérer en double ailleurs, mais on peut stocker ingId ici
                    out.add(new int[]{ingId, 0});
                }
            }
        }
        return out;
    }

    public Order saveOrder(Order orderToSave) throws Exception {
        if (orderToSave.getDishOrders() == null || orderToSave.getDishOrders().isEmpty()) {
            throw new Exception("Order must contain at least one DishOrder");
        }

        String insertOrder = """
        INSERT INTO "order"(reference, creation_datetime)
        VALUES (?, NOW())
        RETURNING id, creation_datetime
    """;

        String insertDishOrder = """
        INSERT INTO dish_order(id_order, id_dish, quantity)
        VALUES (?, ?, ?)
        RETURNING id
    """;

        // 1 requête pour connaître le besoin total par ingrédient de toute la commande
        // need = SUM(quantity_required * dish_quantity)
        String neededPerIngredient = """
        SELECT di.id_ingredient,
               i.name AS ingredient_name,
               SUM(di.quantity_required * do.quantity) AS needed
        FROM dish_order do
        JOIN dish_ingredient di ON di.id_dish = do.id_dish
        JOIN ingredient i ON i.id = di.id_ingredient
        WHERE do.id_order = -1
        GROUP BY di.id_ingredient, i.name
    """;
        // Astuce: on ne peut pas l'utiliser avant d'avoir order_id,
        // donc on fera le calcul avant insertion via les dishOrders list en mémoire.

        try (Connection connection = DbConnection.getDbConnection()) {
            connection.setAutoCommit(false);

            try {
                // ---- (A) Vérification stock AVANT création ----
                // On agrège les besoins en mémoire (ingredientId -> neededKg)
                java.util.Map<Integer, Double> neededMap = new java.util.HashMap<>();

                String qNeedsDish = """
                SELECT id_ingredient, quantity_required
                FROM dish_ingredient
                WHERE id_dish = ?
            """;

                for (DishOrder d : orderToSave.getDishOrders()) {
                    if (d.getDish() == null || d.getDish().getId() == null) {
                        throw new Exception("DishOrder must contain a Dish with id");
                    }
                    int dishId = d.getDish().getId();
                    int dishQty = d.getQuantity();

                    try (PreparedStatement ps = connection.prepareStatement(qNeedsDish)) {
                        ps.setInt(1, dishId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                int ingId = rs.getInt("id_ingredient");
                                double qtyRequired = rs.getBigDecimal("quantity_required").doubleValue();
                                double add = qtyRequired * dishQty;
                                neededMap.put(ingId, neededMap.getOrDefault(ingId, 0.0) + add);
                            }
                        }
                    }
                }

                // Vérifier chaque ingrédient
                String qIngName = "SELECT name FROM ingredient WHERE id = ?";
                for (var entry : neededMap.entrySet()) {
                    int ingId = entry.getKey();
                    double needed = entry.getValue();
                    double stock = getCurrentStockKg(connection, ingId);

                    if (stock + 1e-9 < needed) { // petite tolérance
                        String ingName;
                        try (PreparedStatement ps = connection.prepareStatement(qIngName)) {
                            ps.setInt(1, ingId);
                            try (ResultSet rs = ps.executeQuery()) {
                                rs.next();
                                ingName = rs.getString("name");
                            }
                        }
                        throw new Exception("Not enough stock for ingredient: " + ingName);
                    }
                }


                String ref = nextOrderReference(connection);
                orderToSave.setReference(ref);

                try (PreparedStatement ps = connection.prepareStatement(insertOrder)) {
                    ps.setString(1, ref);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        orderToSave.setId(rs.getInt("id"));
                        orderToSave.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                    }
                }

                // ---- (C) Insert dish_order lines ----
                for (DishOrder d : orderToSave.getDishOrders()) {
                    try (PreparedStatement ps = connection.prepareStatement(insertDishOrder)) {
                        ps.setInt(1, orderToSave.getId());
                        ps.setInt(2, d.getDish().getId());
                        ps.setInt(3, d.getQuantity());
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            d.setId(rs.getInt("id"));
                        }
                    }
                }

                connection.commit();
                return orderToSave;

            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Order findOrderByReference(String reference) throws Exception {
        String qOrder = """
        SELECT id, reference, creation_datetime
        FROM "order"
        WHERE reference = ?
    """;

        String qDishOrders = """
        SELECT do.id AS do_id, do.id_dish, do.quantity,
               d.name, d.dish_type, d.selling_price
        FROM dish_order do
        JOIN dish d ON d.id = do.id_dish
        WHERE do.id_order = ?
        ORDER BY do.id
    """;

        try (Connection connection = DbConnection.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(qOrder)) {

            ps.setString(1, reference);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Order not found: " + reference);
                }

                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setReference(rs.getString("reference"));
                o.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                o.setDishOrders(new ArrayList<>());

                int orderId = o.getId();

                try (PreparedStatement ps2 = connection.prepareStatement(qDishOrders)) {
                    ps2.setInt(1, orderId);

                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            Dish dish = new Dish(
                                    new ArrayList<>(),
                                    DishtypeEnum.valueOf(rs2.getString("dish_type")),
                                    rs2.getString("name"),
                                    rs2.getBigDecimal("selling_price") == null ? null : rs2.getBigDecimal("selling_price").doubleValue(),
                                    rs2.getInt("id_dish")
                            );

                            DishOrder dOrder = new DishOrder(
                                    rs2.getInt("do_id"),
                                    dish,
                                    rs2.getInt("quantity")
                            );

                            o.getDishOrders().add(dOrder);
                        }
                    }
                }

                return o;
            }
        }
    }


    public List<RestaurantTable> findAvailableTablesAt(LocalDateTime t) throws Exception {

        String sql = """
        SELECT rt.id, rt.label, rt.seats
        FROM restaurant_table rt
        WHERE NOT EXISTS (
          SELECT 1 FROM "order" o
          WHERE o.id_table = rt.id
            AND o.start_datetime <= ?
            AND ? < o.end_datetime
        )
        AND NOT EXISTS (
          SELECT 1 FROM table_unavailability u
          WHERE u.id_table = rt.id
            AND u.start_datetime <= ?
            AND ? < u.end_datetime
        )
        ORDER BY rt.id
    """;

        Timestamp ts = Timestamp.valueOf(t);
        List<RestaurantTable> out = new ArrayList<>();

        try (Connection c = DbConnection.getDbConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, ts);
            ps.setTimestamp(2, ts);
            ps.setTimestamp(3, ts);
            ps.setTimestamp(4, ts);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new RestaurantTable(
                            rs.getInt("id"),
                            rs.getString("label"),
                            rs.getInt("seats")
                    ));
                }
            }
        }
        return out;
    }


}

