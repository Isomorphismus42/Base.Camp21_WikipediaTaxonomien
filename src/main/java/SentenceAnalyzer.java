import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dient zur Analyse von Sätzen auf bestimmte RegEx
 */
public class SentenceAnalyzer {
    private List<String> results;
    private Pattern adjectivePattern;
    private Pattern suchAsPattern;
    private Pattern isOneOfPattern;
    private Pattern especiallyPattern;
    private Pattern andOrOtherPattern;
    private Pattern forExamplePattern;
    private Pattern likePattern;
    private Matcher patternMatcher;

    /**
     * Konstruktor, initialisiert die Pattern.
     */
    public SentenceAnalyzer() {
        initPatterns();
    }

    /**
     * Prüft einen gegebenen getaggten Satz auf Schlüsselwörter wie z.B. "such as" oder "is one of" und
     * gibt die gefundenen Matches als Array zurück.
     * @param taggedSentence Der zu prüfende getaggte Satz
     * @return ein String Array mit den gefundenen Taxonomie-Relationen im Format VATER#KIND#GEWICHTs
     */
    public String[] checkSentence(String taggedSentence) {
        results = new ArrayList<String>();
        patternMatcher = suchAsPattern.matcher(taggedSentence);

        checkSuchAsPattern(); //#1

        checkIsOneOfPattern(); //#2

        checkEspeciallyPattern(); //#3

        checkAndOrOtherPattern(); //#4

        checkForExamplePattern(); //#5

        checkLikePattern(); //#6


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
     * VATER#KIND#GEWICHT
     * Format, der Result-Liste hinzu.
     */
    private void checkEspeciallyPattern() {
        patternMatcher.usePattern(especiallyPattern);

        while (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" ,_,_O especially_RB_B-ADVP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects) {
                subject = removeExpendablyWords(subject);
                String rmTagS = removeTags(subject);
                String rmTagO = "";
                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    rmTagO = removeTags(object);
                    if (!rmTagS.isEmpty() && !rmTagO.isEmpty()) {
                        results.add(rmTagS + "#" + rmTagO + "0.89");

                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE#Id+a
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + rmTagO + "0.70");
                        }
                    }
                }
                if (!rmTagS.isEmpty() && !rmTagO.isEmpty() && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + rmTagS + "0.70");
                }
            }
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "is/are one of" und fügt gefundene Matches nach Konvertierung ins
     * VATER#KIND#GEWICHT
     * Format, der Result-Liste hinzu.
     */
    private void checkIsOneOfPattern() {
        patternMatcher.usePattern(isOneOfPattern);

        while (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" (is_VBZ|are_VBP)_B-VP (a_DT_B-NP group_NN_I-NP|part_NN_B-NP|one_CD_B-NP) of_IN_B-PP ");

            //entferne "and"/"or"/",","of" und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrCommaOf(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects) {
                subject = removeExpendablyWords(subject);
                String rmTagS = removeTags(subject);
                String rmTagO = "";
                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    rmTagO = removeTags(object);
                    if (!rmTagS.isEmpty() && !rmTagO.isEmpty()) {
                        results.add(rmTagO + "#" + rmTagS + "0.65");

                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE#Id+a
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + rmTagO + "0.70");
                        }
                    }
                }
                if (!rmTagS.isEmpty() && !rmTagO.isEmpty() && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + rmTagS + "0.70");
                }
            }
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "such as" und fügt gefundene Matches nach Konvertierung ins
     * VATER#KIND#GEWICHT
     * Format, der Result-Liste hinzu.
     */
    private void checkSuchAsPattern() {
        patternMatcher.usePattern(suchAsPattern);

        while (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" such_JJ_B-PP as_IN_I-PP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects) {
                subject = removeExpendablyWords(subject);
                String rmTagS = removeTags(subject);
                String rmTagO = "";
                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    rmTagO = removeTags(object);
                    if (!rmTagS.isEmpty() && !rmTagO.isEmpty()) {
                        results.add(rmTagS + "#" + rmTagO + "0.88");

                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE#Id+a
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + rmTagO + "0.70");
                        }
                    }
                }
                if (!rmTagS.isEmpty() && !rmTagO.isEmpty() && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + rmTagS + "0.70");
                }
            }
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "and/or other" und fügt gefundene Matches nach Konvertierung ins
     * VATER#KIND#GEWICHT
     * Format, der Result-Liste hinzu.
     */
    private void checkAndOrOtherPattern() {
        patternMatcher.usePattern(andOrOtherPattern);

        while (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" (,_,_O )?(and_CC_O|or_CC_O) other_JJ_B-NP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects) {
                subject = removeExpendablyWords(subject);
                String rmTagS = removeTags(subject);
                String rmTagO = "";
                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    rmTagO = removeTags(object);
                    if (!rmTagS.isEmpty() && !rmTagO.isEmpty()) {
                        results.add(rmTagO + "#" + rmTagS + "0.80");

                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE#Id+a
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + rmTagO + "0.70");
                        }
                    }
                }
                if (!rmTagS.isEmpty() && !rmTagO.isEmpty() && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + rmTagS + "0.70");
                }
            }
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "e.g." und fügt gefundene Matches nach Konvertierung ins
     * VATER#KIND#GEWICHT
     * Format, der Result-Liste hinzu.
     */
    private void checkForExamplePattern() {
        patternMatcher.usePattern(forExamplePattern);

        while (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" \\(_-LRB-_((I|B)-NP|O) e_NN_(B|I)-NP .?g._NNP_I-NP ,_,_(I-NP|O) ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] examples = handleAndOrComma(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects) {
                subject = removeExpendablyWords(subject);
                String rmTagS = removeTags(subject);
                String rmTagO = "";
                for (String object : examples) {
                    object = removeExpendablyWords(object);
                    rmTagO = removeTags(object);
                    if (!rmTagS.isEmpty() && !rmTagO.isEmpty()) {
                        results.add(rmTagS + "#" + rmTagO + "0.79");

                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE#Id+a
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + rmTagO + "0.70");
                        }
                    }
                }
                if (!rmTagS.isEmpty() && !rmTagO.isEmpty() && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + rmTagS + "0.70");
                }
            }
        }
    }

    private void checkLikePattern() {
        patternMatcher.usePattern(likePattern);

        while (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" like_IN_B-PP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects) {
                subject = removeExpendablyWords(subject);
                String rmTagS = removeTags(subject);
                String rmTagO = "";
                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    rmTagO = removeTags(object);
                    if (!rmTagS.isEmpty() && !rmTagO.isEmpty()) {
                        results.add(rmTagS + "#" + rmTagO + "0.82");

                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE#Id+a
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + rmTagO + "0.70");
                        }
                    }
                }
                if (!rmTagS.isEmpty() && !rmTagO.isEmpty() && containsAdjective(subject)) {
                        results.add(removeTags(removeAdjectives(subject)) + "#" + rmTagS + "0.70");
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
        //remove Prepositions (by, with ...)
        sentence = sentence.replaceAll("[\\w -]*_IN_(I|B)-NP ?", "");
        //remove Wh-determiner (which, that, ...)
        sentence = sentence.replaceAll("[\\w -]*_WDT_(I|B)-NP ?", "");
        //remove verbs
        sentence = sentence.replaceAll("[\\w -]*_VB[A-Z]?_(I|B)-NP ?","");
        //remove many,several,other, various, important, local, common, well-known, similar, certain
        sentence = sentence.replaceAll("[\\w -]*(many|several|other|various|important|local|common|well-known|similar|certain" +
                "|nearby|numerous|different|diverse|early|major|very|few|regional|such)_JJ_(I|B)-NP ?","");
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
        return s.split(" and_CC_I-NP ?| ?and_CC_O | or_CC_I-NP ?| or_CC_O | ,_,_I-NP | ,_,_O | of_IN_B-PP ");
    }

    /**
     * Entfernt die Part-of-Speech Tags aller Wörter des gegeben Strings
     * @param s der Gegebene String
     * @return der String ohne Wort-Tags
     */
    private String removeTags(String s){
        return s.replaceAll("(_([A-Z]{2,4}|-LRB-))?_(I|B)-NP","").trim();
    }

    /**
     * Prüft ob der gebebene Ausdruck ein Adejektiv mit nachfolgendem Nomen enthält.
     * @param s der zu prüfende Ausdruck
     * @return true, wenn s ein Adjektiv mit nachfolgendem Nomen enthält
     */
    private boolean containsAdjective(String s) {
        Matcher adjectiveMatcher = adjectivePattern.matcher(s);
        return adjectiveMatcher.find();
    }

    /**
     * Entfernt alle auftretenden Adjektive in dem gegeben Ausdruck
     *
     * @param s der gegebene Ausdruck
     * @return der Ausdruck ohne Adjektive
     */
    private String removeAdjectives(String s){
        return s.replaceAll("[\\w -]*_JJ_(I|B)-NP ","");
    }


    /**
     * Erstellt die Regex Patterns, nach denen im Text geguckt werden soll
     */
    //TODO: \\w durch \p{L} ersetzen?
    private void initPatterns() {
        suchAsPattern = Pattern.compile("[\\w&-]*(_B-NP)( [\\w&-]*_I-NP)*" +      //Wort_B-NP (ggf weitere Wörter_I-NP)
                        " such_JJ_B-PP as_IN_I-PP " +                           //such as
                        "[\\w&-]*(_B-NP)( ([\\w,&-]*_I-NP|and_CC_O [\\w&-]*(_B-NP)|or_CC_O [\\w&-]*(_B-NP)|,_,_O [\\w&-]*(_B-NP)))*");
                        //Wort_B-NP (ggf weitere Wörter_I-NP auch mit "und", "oder" oder "," verkettet)

        isOneOfPattern = Pattern.compile("[\\w&-]*(_B-NP)( [\\w&-]*_I-NP)*" +
                " (is_VBZ|are_VBP)_B-VP (a_DT_B-NP group_NN_I-NP|part_NN_B-NP|one_CD_B-NP) of_IN_B-PP " +
                "[\\w&-]*(_B-NP)( ([\\w&-]*_I-NP|of_IN_B-PP [\\w&-]*(_B-NP)|and_CC_O [\\w&-]*(_B-NP)|or_CC_O [\\w&-]*(_B-NP)|,_,_O [\\w&-]*(_B-NP)))*");
        //Wort_B-NP (ggf weitere Wörter_I-NP auch mit "und", "oder" , "," oder "of" verkettet)

        especiallyPattern = Pattern.compile("[\\w&-]*(_B-NP)( [\\w&-]*_I-NP)*" +
                " ,_,_O especially_RB_B-ADVP " +
                "[\\w&-]*(_B-NP)( ([\\w,&-]*_I-NP|and_CC_O [\\w&-]*(_B-NP)|or_CC_O [\\w&-]*(_B-NP)|,_,_O [\\w&-]*(_B-NP)))*");

        andOrOtherPattern = Pattern.compile("[\\w&-]*(_B-NP)( ([\\w,&-]*_I-NP|(,_,_O) [\\w&-]*(_B-NP)))*" +
                " (,_,_O )?(and_CC_O|or_CC_O) other_JJ_B-NP" +
                "( ([\\w&-]*_I-NP|and_CC_O [\\w&-]*(_B-NP)|or_CC_O [\\w&-]*(_B-NP)))+");
        forExamplePattern = Pattern.compile("[\\w&-]*(_B-NP)( [\\w&-]*_I-NP)*" +
                " \\(_-LRB-_((I|B)-NP|O) e_NN_(B|I)-NP .?g._NNP_I-NP ,_,_(I-NP|O)" +
                "( ([\\w,&-]*_(I|B)-NP|and_CC_O [\\w&-]*(_B-NP)|or_CC_O [\\w&-]*(_B-NP)|,_,_O [\\w&-]*(_B-NP)))+");

        likePattern = Pattern.compile("[\\w&-]*(_B-NP)( [\\w&-]*_I-NP)*" +
                " like_IN_B-PP " +
                "[\\w&-]*(_B-NP)( ([\\w&,-]*_I-NP|(,_,_O )?and_CC_O [\\w&-]*(_B-NP)|or_CC_O [\\w&-]*(B-NP)|,_,_O [\\w&-]*(_B-NP)))*");

        adjectivePattern = Pattern.compile("_JJ_(I|B)-NP [\\w-]*(_NN)");
    }
}
