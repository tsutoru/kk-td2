package tsutsu.k2_td2;

import tsutsu.k2_td2.DbConnection.DbConnection;

import java.math.BigDecimal;
import java.sql.*;

import tsutsu.k2_td2.DataRetriever.DataRetriever;
import tsutsu.k2_td2.model.*;

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


        LocalDateTime ldt = LocalDateTime.of(2024, 1, 6, 12, 0);
        Timestamp ts = Timestamp.valueOf(ldt);
        LocalDateTime ldt2 = LocalDateTime.of(2024, 1, 6, 10, 30);
        Timestamp ts2 = Timestamp.valueOf(ldt);
        LocalDateTime ldt3 = LocalDateTime.of(2024, 1, 6, 13, 30);
        Timestamp ts3 = Timestamp.valueOf(ldt);


        System.out.println("=== Available at 12:00 ===");
        List<RestaurantTable> a1 = dataRetriever.findAvailableTablesAt((ldt));
        a1.forEach(System.out::println);

        System.out.println("=== Available at 10:30 ===");
        List<RestaurantTable> a2 = dataRetriever.findAvailableTablesAt(ldt2);
        a2.forEach(System.out::println);

        System.out.println("=== Available at 13:30 ===");
        List<RestaurantTable> a3 = dataRetriever.findAvailableTablesAt(ldt3);
        a3.forEach(System.out::println);

    }
}








