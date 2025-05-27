CREATE TABLE IF NOT EXISTS expenses (
                          id SERIAL PRIMARY KEY,
                          date DATE NOT NULL,
                          amount DOUBLE PRECISION NOT NULL,
                          source VARCHAR(255) NOT NULL,
                          description TEXT,
                          username VARCHAR(255) NOT NULL
);