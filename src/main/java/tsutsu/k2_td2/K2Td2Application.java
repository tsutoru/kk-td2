package tsutsu.k2_td2;

import tsutsu.k2_td2.DbConnection.DbConnection;
import java.sql.Connection;
import tsutsu.k2_td2.DataRetriever.DataRetriever;
import tsutsu.k2_td2.model.Dish;
import tsutsu.k2_td2.model.DishtypeEnum;
import tsutsu.k2_td2.model.Ingredient;
import tsutsu.k2_td2.model.CategoryEnum;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class K2Td2Application {


    public static void main(String[] args) throws SQLException {
        DataRetriever dr = new DataRetriever();
        try {
            Dish newDish = new Dish(
                    new ArrayList<>(),
                    DishtypeEnum.MAIN,
                    "Nouveau plat de canard",
                    1200.5,
                    7,
                    new Ingredient(5, "salade de fruit", 1500.0, CategoryEnum.VEGETABLE,1)
            );

            Dish saveDish = dr.saveDish(newDish);
            System.out.println("Plat sauvegardé : " + saveDish);

////                System.out.println("\n===== Test findIngredientByDishId =====");
////                List<Ingredient> ingredients = dr.findIngredientByDishId(1, 10, 0); // dish id 1, max 10 ingrédients
////                ingredients.forEach(System.out::println);
////                System.out.println("\n===== Test createIngredient =====");
////                Ingredient newIngredient = new Ingredient(
////                        0,
////                        "Concombre",
////                        500.0,
////                        CategoryEnum.VEGETABLE,
////                        dish1
////                );
////
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////        }
//
//        } catch (Exception e) {
//            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

  public  void testSaveDish(DataRetriever dr) throws SQLException {



       try {
            Dish newDish = new Dish(
                    new ArrayList<>(),
                    DishtypeEnum.MAIN,
                    "Nouveau plat de canard",
                    1200.5,
                    7
            );

            Dish saveDish = dr.saveDish(newDish);
            System.out.println("Plat sauvegardé : " + saveDish);

////                System.out.println("\n===== Test findIngredientByDishId =====");
////                List<Ingredient> ingredients = dr.findIngredientByDishId(1, 10, 0); // dish id 1, max 10 ingrédients
////                ingredients.forEach(System.out::println);
////                System.out.println("\n===== Test createIngredient =====");
////                Ingredient newIngredient = new Ingredient(
////                        0,
////                        "Concombre",
////                        500.0,
////                        CategoryEnum.VEGETABLE,
////                        dish1
////                );
////
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////        }
//
//        } catch (Exception e) {
//            e.printStackTrace();
        } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

       public void TestFindDishById (DataRetriever dr) throws SQLException{
          try {


              Dish dish = dr.findDishById(2);
              System.out.println(dish);

              double margin = dish.getGrossMargin();
              System.out.println("Marge brute : " + margin);

          } catch (RuntimeException e) {
              System.err.println("Erreur : " + e.getMessage());
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
}








