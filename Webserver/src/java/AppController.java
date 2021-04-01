package basecamp.taxonomien.springwebserver;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

/**
 * Steuert die Browser Seite, die die Daten visualisiert
 */
@Controller
public class AppController {

    private String[] suggestionstrings;
    private ArrayList<String> suggestions;
    String[] currentSuggestions = new String[4];

    @GetMapping("/app")
    public String app(Model model) {
        suggestionstrings = getRandomSuggestions();
        initSuggestions();
        model.addAttribute("suggestions",currentSuggestions);
        return "app";
    }

    /**
     * Entfernt einen Wert aus suggestions und gibt diesen Zurück
     * @return Zufälliger Wert aus suggestions
     */
    private String getSuggestion() {
        String s = suggestions.get(new Random().nextInt(suggestions.size()));
        suggestions.remove(s);
        return s;
    }

    /**
     * Initialisert das currentSuggestions Array mit Werten aus suggestionstrings
     */
    private void initSuggestions() {
        suggestions = new ArrayList<String>();
        for (String s: suggestionstrings) {
            suggestions.add(s);
        }

        for (int i = 0; i < currentSuggestions.length; i++) {
            currentSuggestions[i] = getSuggestion();
        }
    }

    /**
     * Holt vier zufällige Parent-Begriffe aus der Datenbank und speichert sie in ein Array
     * @return Array mit 4 validen Parent Begriffen
     */
    private String[] getRandomSuggestions() {
        // weight kann an dieser Stelle den Daten angepasst werden
        // Je höher weight ist, desto besser sollte das Ergebnis sein
        // LIMIT kann geändert werden, um die Anzahl der zufälligen Ergbenisse zu verändern
        String sql = "SELECT DISTINCT parent FROM taxonomien WHERE weight >= 5 ORDER BY RAND() LIMIT 4";
        ArrayList<String> tempResult = new ArrayList<String>();

        // Build connection and execute query
        SQLConnector connect = new SQLConnector();
        Connection conn = connect.getConn();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
                tempResult.add(rs.getString("parent"));
            }

            try {
                connect.closeConn();
                rs.close();
                stmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        String[] result = tempResult.toArray(new String[0]);

        return result;
    }
}
