INSERT INTO Dish (id, name, dish_type) VALUES
                                    (1,'Salade fraiche', 'START' ),
                                    (2,'Poulet Grille', 'MAIN'),
                                    (3,'Riz aux legumes', 'MAIN'),
                                    (4,'Gateau aux chocolat', 'DESSERT'),
                                    (5,'Salade de fruit', 'DESSERT');

INSERT INTO Ingredient (id, name, price, Category, id_dish ) VALUES
                    (1,'Laitue',800.00, 'VEGETABLE', 1),
                    (2,'Tomate',600.00, 'VEGETABLE', 1),
                    (3,'Poulet',4500.00, 'ANIMAL', 2),
                    (4,'Chocolat',3000.00, 'OTHER', 4),
                    (5,'Beurre',2500.00, 'DAIRY', 4);