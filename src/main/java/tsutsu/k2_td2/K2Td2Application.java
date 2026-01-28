package tsutsu.k2_td2;

import tsutsu.k2_td2.DbConnection.DbConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import tsutsu.k2_td2.DataRetriever.DataRetriever;
import tsutsu.k2_td2.model.Dish;
import tsutsu.k2_td2.model.DishtypeEnum;
import tsutsu.k2_td2.model.Ingredient;
import tsutsu.k2_td2.model.CategoryEnum;

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



    }
}








