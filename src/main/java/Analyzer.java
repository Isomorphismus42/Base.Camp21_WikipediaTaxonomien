import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dient zur Analyse von Sätzen auf bestimmte RegEx
 */
public class Analyzer {
    private  List<Pattern> patterns = new ArrayList<Pattern>();
    private  Matcher m;
    private  ArrayList<String> hits = new ArrayList<String>();

    public Analyzer() {
        initPatterns();
    }

    public ArrayList<String> checkSentence(String sentence) {
        handleLine(sentence);
        ArrayList<String> result = hits;
        hits.clear();
        return result;
    }

    /**
     * Überprüft einen gegeben satz auf Patterns
     * @param sentence Satz, der verarbeitet werden soll
     */
    private void handleLine(String sentence) {
            patterns.forEach((p) -> checkForPattern(p,sentence));
    }

    /**
     * Checkt den gegeben String s, ob er das Pattern p enthält und fügt den (Teil-)String zu hits hinzu
     * @param p Patterns auf das geprüft werden soll
     * @param s String der geprüft werden soll
     */
    private void checkForPattern(Pattern p, String s) {
        m = p.matcher(s);
        if (m.find()) hits.add(m.group(0));
    }

    /**
     * Erstellt die Regex Patterns, nach denen im Text geguckt werden soll
     */
    private void initPatterns() {
        List<String> patternsString = new ArrayList<String>();
        patternsString.add("\\w+\\s(such as)\\s([A-Z]\\w*\\s?)+"); // Personen, Firmen etc (Eigennamen)
        patternsString.add("(\\w+\\s(such as)\\s[a-z]+(\\sand\\s\\w+)?)|(\\w+\\s(such as)\\s[a-z]+)"); // andere Begriffe
        patternsString.add("\\w+(,\\s[\\w\\s]*)*(\\sor other)\\s\\w+");

        patternsString.forEach((p) -> patterns.add(Pattern.compile(p)));
    }
}
