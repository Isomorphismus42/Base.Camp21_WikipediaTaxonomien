import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dient zur Analyse von Sätzen auf bestimmte RegEx
 */
public class SentenceAnalyzer {
    private List<String> results;
    private Pattern suchAsPattern;

    public SentenceAnalyzer() {
        initPatterns();
    }

    /**
     * Prüft einen gegebenen getaggten Satz auf Schlüsselwörter wie "such as" oder "is a" und
     * bildet darus entsprechende Taxonimie-Relationen.
     * @param taggedSentence Der zu prüfende getaggte Satz
     * @return ein String Array mit den gefundenen Taxonomie-Relationen im Format VATER#KIND#SchlüsselwortID
     */
    public String[] checkSentence(String taggedSentence) {
        results = new ArrayList<String>();

        Matcher suchAsMatcher = suchAsPattern.matcher(taggedSentence);

        if (suchAsMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = suchAsMatcher.group().split(" such_JJ_B-PP as_IN_I-PP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter
            for (String subject : subjects){
                for(String object : objects){
                    results.add(removeExpendablyWords(subject)
                            + "#" + removeExpendablyWords(object)); //TODO: hänge später #Id zum identifizieren an
                }
            }
        }

        //TODO: weitere Patternprüfungen ergänzen

        //Gebe alle gefundenen Relationen zurück und entferne dabei die Tags
        if (results.isEmpty()){
            return new String[] {""};
        }
        else {
            removeTags();
            return results.toArray(new String[0]);
        }
    }

    /**
     *Entfernt überflüssige, unerwünschte Wörter.
     *
     * @param sentence der zu behandelnde Satzteil
     * @return der Satzteil ohne die unerwünschten Wörter als String
     */
    private String removeExpendablyWords(String sentence){
        //remove Determiner (a, an, the, that, those, some)
        sentence = sentence.replaceAll("[\\w]*_DT_(I|B)-NP ?","");
        //remove Adverbs and all previous Words (more, nearby, typically, highly, ...)
        sentence = sentence.replaceAll("[\\w-_ ]*_RB(R|S)?_(I|B)-NP ?","");
        //remove many,several
        sentence = sentence.replaceAll("((M|m)any|(S|s)everal)_JJ_(I|B)-NP ?","");
        return sentence;
    }

    /**
     * Zerlegt einen Satzteil in mehrere Teile anhand Trenwörter "and", "or" und ",".
     *
     * @param s der zu zerlegende Satzteil
     * @return die entstehenden Satz-Fragmente als String-Array
     */
    private String[] handleAndOrComma(String s){
        return s.split(" and_CC_I-NP ?| and_CC_O | or_CC_I-NP ?| or_CC_O | ,_,_I-NP | ,_,_O ");
    }

    private void removeTags(){
        for (int i=0; i<results.size();i++) {
            results.set(i,results.get(i).replaceAll("(_([A-Z]{2,4}|-LRB-))?_(I|B)-NP","").trim());
        }
    }

    /**
     * Erstellt die Regex Patterns, nach denen im Text geguckt werden soll
     */
    private void initPatterns() {
        suchAsPattern = Pattern.compile("[\\w-]*(_B-NP)( [\\w-]*_I-NP)*" +      //Wort_B-NP (ggf weitere Wörter_I-NP)
                        " such_JJ_B-PP as_IN_I-PP " +                   //such as
                        "[\\w-]*(_B-NP)( ([\\w-,]*_I-NP|and_CC_O [\\w-]*(_B-NP)|or_CC_O [\\w-]*(_B-NP)|,_,_O [\\w-]*(_B-NP)))*");
                        //Wort_B-NP (ggf weitere Wörter_I-NP auch mit "und", "oder" oder "," verkettet)

        //TODO: weitere Pattern ergänzen
    }
}
