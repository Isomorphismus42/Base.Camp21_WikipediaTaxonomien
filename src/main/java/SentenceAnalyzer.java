import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dient zur Analyse von Sätzen auf bestimmte RegEx
 */
public class SentenceAnalyzer {
    private List<String> results;
    private Pattern suchAsPattern;
    private Pattern isOneOfPattern;
    private Pattern especiallyPattern;
    private Matcher patternMatcher;

    public SentenceAnalyzer() {
        initPatterns();
    }

    /**
     * Prüft einen gegebenen getaggten Satz auf Schlüsselwörter wie z.B. "such as" oder "is one of" und
     * gibt die gefundenen Matches als Array zurück.
     * @param taggedSentence Der zu prüfende getaggte Satz
     * @return ein String Array mit den gefundenen Taxonomie-Relationen im Format VATER#KIND#SchlüsselwortID
     */
    public String[] checkSentence(String taggedSentence) {
        results = new ArrayList<String>();
        patternMatcher = suchAsPattern.matcher(taggedSentence);

        checkSuchAsPattern();

        checkIsOneOfPattern();

        checkEspeciallyPattern();

        //Gebe alle gefundenen Relationen zurück
        if (results.isEmpty()){
            return new String[] {""};
        }
        else {
            return results.toArray(new String[0]);
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "especially" und fügt gefundene Matches nach Konvertierung ins
     * VATER:KIND:2
     * Format, der Result-Liste hinzu.
     */
    private void checkEspeciallyPattern() {
        patternMatcher.usePattern(especiallyPattern);
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "is/are one of" und fügt gefundene Matches nach Konvertierung ins
     * VATER:KIND:2
     * Format, der Result-Liste hinzu.
     */
    private void checkIsOneOfPattern() {
        patternMatcher.usePattern(isOneOfPattern);

        if (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" (is_VBZ|are_VBP)_B-VP one_CD_B-NP of_IN_B-PP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrCommaOf(subjectObject[1]);;

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects){
                for(String object : objects){
                    subject = removeExpendablyWords(subject);
                    subject = removeTags(subject);
                    object = removeExpendablyWords(object);
                    object = removeTags(object);

                    if (!subject.isEmpty() && !object.isEmpty()) {
                        results.add(object.toLowerCase(Locale.ROOT) + "#" + subject.toLowerCase(Locale.ROOT) + "#2");
                    }
                }
            }
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "such as" und fügt gefundene Matches nach Konvertierung ins
     * VATER:KIND:2
     * Format, der Result-Liste hinzu.
     */
    private void checkSuchAsPattern() {
        patternMatcher.usePattern(suchAsPattern);

        if (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" such_JJ_B-PP as_IN_I-PP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects){
                for(String object : objects){
                    subject = removeExpendablyWords(subject);
                    subject = removeTags(subject);
                    object = removeExpendablyWords(object);
                    object = removeTags(object);

                    if (!subject.isEmpty() && !object.isEmpty()) {
                        results.add(subject.toLowerCase(Locale.ROOT) + "#" + object.toLowerCase(Locale.ROOT) + "#1");
                    }
                }
            }
        }
    }



    /**
     *Entfernt überflüssige, unerwünschte Wörter.
     *
     * @param sentence der zu behandelnde Satzteil
     * @return der Satzteil ohne die unerwünschten Wörter als String
     */
    private String removeExpendablyWords(String sentence){
        //remove Determiner and all previous Words (a, an, the, that, those, some, ...)
        sentence = sentence.replaceAll("[\\w -]*_DT_(I|B)-NP ?","");
        //remove Adverbs and all previous Words (more, nearby, typically, highly, ...)
        sentence = sentence.replaceAll("[\\w -]*_RB(R|S)?_(I|B)-NP ?","");
        //remove Comparative adjectives and all previous Words (higher, larger, best, more, earlier, ...)
        sentence = sentence.replaceAll("[\\w -]*_JJ(R|S)_(I|B)-NP ?","");
        //remove Personal pronoun (he, she, it, his, ...)
        sentence = sentence.replaceAll("[\\w -]*_PRP\\$?_(I|B)-NP ?", "");
        //remove verbs
        sentence = sentence.replaceAll("[\\w -]*_VBN_(I|B)-NP ?","");
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

    /**
     * Zerlegt einen Satzteil in mehrere Teile anhand Trenwörter "and", "or" und ",",
     * und zusätzlich noch nach dem Trennwort "of".
     *
     * @param s der zu zerlegende Satzteil
     * @return die entstehenden Satz-Fragmente als String-Array
     */
    private String[] handleAndOrCommaOf(String s) {
        return s.split(" and_CC_I-NP ?| and_CC_O | or_CC_I-NP ?| or_CC_O | ,_,_I-NP | ,_,_O | of_IN_B-PP ");
    }

    /**
     * Entfernt die Part-of-Speech Tags aller Wörter des gegeben Strings
     * @param s der Gegebene String
     * @return der String ohne Wort-Tags
     */
    private String removeTags(String s){
//        for (int i=0; i<results.size();i++) {
//            results.set(i,results.get(i).replaceAll("(_([A-Z]{2,4}|-LRB-))?_(I|B)-NP","").trim());
//        }
        return s.replaceAll("(_([A-Z]{2,4}|-LRB-))?_(I|B)-NP","").trim();
    }

    /**
     * Erstellt die Regex Patterns, nach denen im Text geguckt werden soll
     */
    private void initPatterns() {
        suchAsPattern = Pattern.compile("[\\w-]*(_B-NP)( [\\w-]*_I-NP)*" +      //Wort_B-NP (ggf weitere Wörter_I-NP)
                        " such_JJ_B-PP as_IN_I-PP " +                           //such as
                        "[\\w-]*(_B-NP)( ([\\w-,]*_I-NP|and_CC_O [\\w-]*(_B-NP)|or_CC_O [\\w-]*(_B-NP)|,_,_O [\\w-]*(_B-NP)))*");
                        //Wort_B-NP (ggf weitere Wörter_I-NP auch mit "und", "oder" oder "," verkettet)

        isOneOfPattern = Pattern.compile("[\\w-]*(_B-NP)( [\\w-]*_I-NP)*" +
                " (is_VBZ|are_VBP)_B-VP one_CD_B-NP of_IN_B-PP " +
                "[\\w-]*(_B-NP)( ([\\w-]*_I-NP|of_IN_B-PP [\\w-]*(_B-NP)|and_CC_O [\\w-]*(_B-NP)|or_CC_O [\\w-]*(_B-NP)|,_,_O [\\w-]*(_B-NP)))*");
        //Wort_B-NP (ggf weitere Wörter_I-NP auch mit "und", "oder" , "," oder "of" verkettet)

        especiallyPattern = Pattern.compile("[\\w-]*(_B-NP)( [\\w-]*_I-NP)*" +
                " ,_,_O especially_RB_B-ADVP " +
                "[\\w-]*(_B-NP)( ([\\w,-]*_I-NP|and_CC_O [\\w-]*(_B-NP)|or_CC_O [\\w-]*(_B-NP)|,_,_O [\\w-]*(_B-NP)))*");

        //TODO: weitere Pattern ergänzen
    }
}
