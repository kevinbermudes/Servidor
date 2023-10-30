CREATE TABLE FUNKOS (
                        ID INT AUTO_INCREMENT PRIMARY KEY,
                        cod UUID DEFAULT RANDOM_UUID() NOT NULL,
                        MyId BIGINT NOT NULL,
                        nombre VARCHAR(255),
                        modelo VARCHAR(255) NOT NULL CHECK (modelo IN ('MARVEL', 'DISNEY', 'ANIME', 'OTROS')),
                        precio DECIMAL(10, 2) NOT NULL,
                        fecha_lanzamiento DATE NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

