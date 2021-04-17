package basecamp.taxonomien.springwebserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Steuert die API, die es ermöglicht Daten aus der DB zu lesen
 */
@RestController
public class ApiController {

    private SQLConnector connector;
    private Connection conn;

    /**
     * Liefert JSON Objekte, abhänig von der übergebenen Parametern zurück
     * @param parent Parent, dessen Kinder zurückgegeben werden sollen
     * @param root Root, von dem aus ein Baum Objekt gebaut werden soll
     * @param limit Optional bei parent, um maximale Anzahl der Kinder zu begrenzen
     * @param child Child, dessen Parent zurückgegeben werden soll
     */
    @GetMapping("/api")
    public String api(@RequestParam("parent") Optional<String> parent, @RequestParam("root") Optional<String> root,
                      @RequestParam("limit") Optional<String> limit, @RequestParam("child") Optional<String> child,
                      Model model) throws JSONException, SQLException {
        // Baue Verbindung zur Datenbank auf
        connector = new SQLConnector();
        conn = connector.getConn();

        // Wenn root gegeben ist -> Gib JSON Baum
        if (root.isPresent()) {
            if (root.get().equals("random")) {
                return getInitTreeData(getRandom());
            }
            return getInitTreeData(root.get());
        }
        // Wenn parent gegeben ist -> gib Kinder (default: 6)
        else if (parent.isPresent()) {
            JSONArray children = new JSONArray();
            // Wenn limit gegeben ist -> Gib maximal limit Kinder
            if (limit.isPresent()) {
                // Check ob String ein valider Integer ist
                if (limit.get().matches("(0|[1-9]\\d*)")) {
                    for (String s: getChildren(parent.get(), Integer.parseInt(limit.get()))) {
                        children.put(s);
                    }
                }
                else { // sonst default zu 6
                    for (String s: getChildren(parent.get(), 6)) {
                        children.put(s);
                    }
                }

            }
            else {
                for (String s: getChildren(parent.get(), 6)) {
                    children.put(s);
                }
            }
            return children.toString();
        }
        else if (child.isPresent()) {
            String childString = getParent(child.get());
            if (childString != null) {
                return childString; // gibt nur String
            }
            else {
                return "null";
            }
        }
        else {
          return "Invalid request, check API documentation for more info";
        }
    }

    /**
     * Gibt die Kinder von Parent als String Array
     * @param parent Key des Parent Knoten
     * @return String Array mit Keys der Kinderknoten
     */
    private String[] getChildren(String parent, int limit) {
        try {
            String[] result = getChildrenData(parent, limit);
            if (result == null) {
                return new String[] {"null"};
            }
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new String[] {"null"};
    }

    /**
     * Holt die Kinder von Parent aus der DB
     * @param parent parent Knoten als String
     * @param Limit int, Anzahl der Kinder, die maximal zurückgegeben werden sollen (default = 6)
     * @return String Array mit Kindern von Parent, null wenn keine Ergebnis existiert
     * @throws SQLException
     */
    public String[] getChildrenData(String parent, int Limit) throws SQLException {
        ArrayList<String> tempResult = new ArrayList<String>();
        Statement stmt = conn.createStatement();
        // Begrenz auf sechs Kinder, kann verändert werden
        String sql = "SELECT child FROM `taxonomien` WHERE parent = '" + parent + "' ORDER BY weight DESC LIMIT " + Limit;
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()){
            tempResult.add(rs.getString("child"));
        }

        // wenn kein Ergebnis
        if (tempResult.isEmpty()) {
            return null;
        }

        String[] result = tempResult.toArray(new String[0]);

        try {
            rs.close();
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    /**
     * Holt maximal 6 Kinder von Parent aus der DB. getChildrenData(String parent) kann aufgerufen werden, um
     * andere Anzahl von Kindern zu erhalten
     * @param parent parent Knoten als String
     * @return String Array mit Kindern von Parent, null wenn keine Ergebnis existiert
     * @throws SQLException
     */
    public String[] getChildrenData(String parent) throws SQLException {
        return getChildrenData(parent, 6);
    }

    /**
     * Gibt den Höchstgewichteten Parentknoten von einem Kind zurück
     * @param child Knoten, dessen Parent gefunden werden soll
     * @return Parent von child als String
     * @throws SQLException
     */
    public String getParent(String child) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "SELECT DISTINCT parent, weight FROM `taxonomien` WHERE child = '" + child + "' ORDER BY weight DESC LIMIT 1";
        ResultSet rs = stmt.executeQuery(sql);
        String result = "null";
        while(rs.next()){
            result = rs.getString("parent");
        }
        if (result.equals("null")) {
            return null;
        }
        try {
            rs.close();
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return result;
    }

    /**
     * Gibt das JSON Array für die Initialisierung des Baumes
     * @param root Knoten für Baum
     * @return JSON Array mit Daten für Baum mit parent als Knoten
     */
    public String getInitTreeData(String root) throws SQLException {
        JSONArray tree = new JSONArray();
        JSONObject rootNode = new JSONObject();
        rootNode.put("name", root);
        rootNode.put("parent", "null");
        // Auskommentierter Code könnte genutzt werden, um den Baum von einer Ebene über root zu bauen
        // Kann nicht garantieren, dass root wirklich im Baum enthalten ist
       /* String parent = getParent(root);
        // build root
        if (parent != null) {
            rootNode.put("name", parent);
            rootNode.put("parent", "null");
        }
        else {
            rootNode.put("name", root);
            rootNode.put("parent", "null");
        } */
        // Starte setChildTree, um rekusriv den Baum aufzubauen
        rootNode = setChildTree(rootNode, 1);
        tree.put(rootNode);
        return tree.toString();
    }

    /**
     * Hängt an den Parent Knoten ein Children Array ein, das bis zu 5 Ebenen tief geht
     * @param parent JSON Objekt mit name und parent Attribut
     * @param level  Derzeitige Anzahl an Ebenen
     * @return parent mit Attribut children, dass Kinder von Parent enthält
     * @throws SQLException
     */
    public JSONObject setChildTree(JSONObject parent, int level) throws SQLException {
        // Die Zahl gibt den Wert der tiefsten Ebene an.
        // level > 5 --> Es werdenbis zu 5 Ebenen geladen
        if (level > 5) {
            return parent;
        }
        String[] children = getChildrenData(parent.getString("name"));
        JSONArray childrenArray = new JSONArray();
        if (children == null) {
            parent.put("children", childrenArray);
            return parent;
        }
        for (String sChild: children) {
            JSONObject child = new JSONObject();
            child.put("name", sChild);
            child.put("parent", parent.get("name"));
            setChildTree(child,++level);
            childrenArray.put(child);
        }
        parent.put("children", childrenArray);
        return parent;
    }

    /**
     * Gibt einen Parent Knoten aus der DB zurück
     * @return Zufälliger Parent Wert aus DB
     */
    private String getRandom() {
        // weight kann an dieser Stelle den Daten angepasst werden
        // Je höher weight ist, desto besser sollte das Ergebnis sein
        String sql = "SELECT DISTINCT parent FROM taxonomien WHERE weight >= 1 ORDER BY RAND() LIMIT 1";
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

        return result[0];
    }
}
