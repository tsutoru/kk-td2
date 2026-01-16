DO $$ BEGIN
    CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

ALTER TABLE dish
    ADD COLUMN IF NOT EXISTS selling_price NUMERIC NULL;

ALTER TABLE ingredient
    DROP COLUMN IF EXISTS id_dish;

CREATE TABLE IF NOT EXISTS dish_ingredient (
                                               id SERIAL PRIMARY KEY,
                                               id_dish INT NOT NULL REFERENCES dish(id) ON DELETE CASCADE,
                                               id_ingredient INT NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
                                               quantity_required NUMERIC NOT NULL,
                                               unit unit_type NOT NULL,

    -- empêche d'ajouter 2 fois le même ingrédient pour le même plat
                                               CONSTRAINT uq_dish_ing UNIQUE (id_dish, id_ingredient)
);

INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
VALUES
    (1, 1, 0.20, 'KG'),
    (1, 2, 0.15, 'KG'),
    (2, 3, 1.00, 'KG'),
    (4, 4, 0.30, 'KG'),
    (4, 5, 0.20, 'KG');

UPDATE dish SET selling_price = 3500.00 WHERE id = 1;
UPDATE dish SET selling_price = 12000.00 WHERE id = 2;
UPDATE dish SET selling_price = NULL WHERE id = 3;
UPDATE dish SET selling_price = 8000.00 WHERE id = 4;
UPDATE dish SET selling_price = NULL WHERE id = 5;
