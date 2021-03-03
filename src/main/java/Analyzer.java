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

    public String checkSentence(String sentence) {
        Pattern suchAsPattern = Pattern.compile("\\w*(_B-NP)( \\w*_I-NP)* such_JJ_B-PP as_IN_I-PP \\w*(_B-NP)( \\w*_I-NP)");
        Matcher suchAsMatcher = suchAsPattern.matcher(sentence);
        if (suchAsMatcher.find()) {
            return suchAsMatcher.group(0);
        }
        else {
            return "";
        }

//        handleLine(sentence);
//        ArrayList<String> result = hits;
//        hits.clear();
//        return result;
    }

    /**
     * Überprüft einen gegeben satz auf Patterns
     * @param sentence Satz, der verarbeitet werden soll
     */
    private void handleLine(String sentence) {
        for (Pattern p : patterns) {
            checkForPattern(p, sentence);
        }
    }

    /**
     * Checkt den gegeben String s, ob er das Pattern p enthält und fügt den (Teil-)String zu hits hinzu
     * @param p Patterns auf das geprüft werden soll
     * @param s String der geprüft werden soll
     */
    private void checkForPattern(Pattern p, String s) {
        m = p.matcher(s);
        if (m.find()) {
            hits.add(m.group(0));
        }
    }

    /**
     * Erstellt die Regex Patterns, nach denen im Text geguckt werden soll
     */
    private void initPatterns() {
        List<String> patternStrings = new ArrayList<String>();
//        patternStrings.add("\\w+\\s(such as)\\s([A-Z]\\w*\\s?)+"); // Personen, Firmen etc (Eigennamen)
        patternStrings.add("\\w*_B-NP[A-Za-z0-9_ -]*such_JJ_B-PP as_IN_I-PP [A-Za-z0-9_ -]*_I-NP"); // such as

        for (String pattern : patternStrings) {
            patterns.add(Pattern.compile(pattern));
        }
    }
}
