package basecamp.taxonomien.springwebserver;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Die Klasse dient dazu, eine Verbindung zu der MySQl Datenbank aufzubauen
 */
public class SQLConnector {

    private String db_url;
    private String user;
    private String pass;

    private Connection conn = null;

    public SQLConnector() {
        init();
    }

    /**
     * Initialisiert alle benötigten Variablen und baut die Verbindung auf
     */
    private void init() {
        // Lade Zugangsdaten
        getConnectionData();

        try {
            // Öffne Verbindung
            conn = DriverManager.getConnection(db_url, user,pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Lädt die Zugangsdaten für die DB
     */
    private void getConnectionData() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("credentials.json");
            BufferedReader bR = new BufferedReader(  new InputStreamReader(is));
            String line = "";

            StringBuilder responseStrBuilder = new StringBuilder();
            while((line =  bR.readLine()) != null){

                responseStrBuilder.append(line);
            }
            is.close();
            String creds = responseStrBuilder.toString();
            JSONObject json = new JSONObject(creds);

            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            db_url = "jdbc:mysql://" + json.getString("db_url");
            user = json.getString("user");
            pass = json.getString("pass");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt die connection an die Datenbank
     * @return Connection Objekt für die Datenbankverbindung
     */
    public Connection getConn() {
        return conn;
    }

    /**
     * Schließt die Connection an die Datenbank
     */
    public void closeConn() throws SQLException {
        conn.close();
    }

    /**
     * Liest eine Datei und returned sie als String
     * @param path Path zur Datei
     * @return Dateiinhalt als String
     * @throws IOException
     */
    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

}
