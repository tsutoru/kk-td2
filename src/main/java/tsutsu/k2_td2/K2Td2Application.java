package tsutsu.k2_td2;

import tsutsu.k2_td2.DbConnection.DbConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import tsutsu.k2_td2.DataRetriever.DataRetriever;
import tsutsu.k2_td2.model.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class K2Td2Application {
    public static void main(String[] args) throws Exception {
        DbConnection dbConnection = new DbConnection();
     DataRetriever dataRetriever = new DataRetriever();
//        Dish BOMBOCLAT = new Dish(null, DishtypeEnum.MAIN, "BOMBOCLAT", 7890.00, 4);
//        try{
//            System.out.println(dataRetriever.saveDish(BOMBOCLAT));
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        Ingredient i = new Ingredient(9, "Oeuf", 1200.0, CategoryEnum.OTHER);
//        try{
//            System.out.println(dataRetriever.createIngredient(i));
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        Ingredient i2 = new Ingredient(10, "Oeuf", 9999.0, CategoryEnum.OTHER);
//        try {
//            System.out.println(dataRetriever.createIngredient(i2));
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
        Instant t = LocalDateTime.of(2024, 1, 6, 12, 0)
                .toInstant(ZoneOffset.UTC);

        for (int id = 1; id <= 5; id++) {
            Ingredient ing = dataRetriever.findIngredientById(id);
            System.out.println(
                    ing.getName() + " -> " + ing.getStockValueAt(t).getQuantity() + " KG"
            );
        }


        Instant t1 = LocalDateTime.of(2024, 1, 6, 12, 0).toInstant(ZoneOffset.UTC);
        Instant t2 = LocalDateTime.of(2024, 1, 6, 10, 30).toInstant(ZoneOffset.UTC);
        Instant t3 = LocalDateTime.of(2024, 1, 6, 13, 30).toInstant(ZoneOffset.UTC);

        System.out.println("=== Available at 12:00 ===");
        List<RestaurantTable> a1 = dataRetriever.findAvailableTablesAt(t1);
        a1.forEach(System.out::println);

        System.out.println("=== Available at 10:30 ===");
        List<RestaurantTable> a2 = dataRetriever.findAvailableTablesAt(t2);
        a2.forEach(System.out::println);

        System.out.println("=== Available at 13:30 ===");
        List<RestaurantTable> a3 = dataRetriever.findAvailableTablesAt(t3);
        a3.forEach(System.out::println);

    }
}








