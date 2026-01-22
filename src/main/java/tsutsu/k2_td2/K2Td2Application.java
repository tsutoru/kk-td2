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
import java.util.ArrayList;
import java.util.List;

public class K2Td2Application {
    public static void main(String[] args) {
        DbConnection dbConnection = new DbConnection();
        DataRetriever dataRetriever = new DataRetriever();
        Dish Lethorica = new Dish(null, DishtypeEnum.MAIN, "Lethorica", 7000.00, 9);
        try{
            System.out.println(dataRetriever.saveDish(Lethorica));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}








