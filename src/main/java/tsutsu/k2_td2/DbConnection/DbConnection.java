package tsutsu.k2_td2.DbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    public static Connection getDbConnection() throws Exception {

        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new RuntimeException("Les variables d'environnement DB_URL, DB_USER ou DB_PASSWORD ne sont pas définies !");
        }

        return DriverManager.getConnection(url, user, password);
    }
}
