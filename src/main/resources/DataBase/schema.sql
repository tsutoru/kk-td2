CREATE TYPE dish_type AS ENUM('START', 'MAIN', 'DESSERT');

CREATE TABLE Dish(
                                  id SERIAL PRIMARY KEY,
                                  name VARCHAR(255) NOT NULL,
                                  Dish_type dish_type
);

CREATE TYPE catogory AS ENUM('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

CREATE TABLE Ingredient (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         price NUMERIC(12,2) NOT NULL,
                         Category category,
                         id_dish INTEGER REFERENCES Dish(id)
);


