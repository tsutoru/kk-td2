package tsutsu.k2_td2;

import tsutsu.k2_td2.DbConnection.DbConnection;
import java.sql.Connection;

public class K2Td2Application {

    public static void main(String[] args) {
        try (Connection conn = DbConnection.getDbConnection()) {

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion PostgreSQL OK");
            }

        } catch (Exception e) {
            System.out.println("❌ Connexion échouée");
            e.printStackTrace();
        }
    }
}


