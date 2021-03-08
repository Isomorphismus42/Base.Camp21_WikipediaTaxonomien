import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dient zur Analyse von Sätzen auf bestimmte RegEx
 */
public class SentenceAnalyzer {
    private List<String> results;
    private Pattern nounPattern;
    private Pattern adjectivePattern;
    private Pattern suchAsPattern;
    private Pattern isOneOfPattern;
    private Pattern especiallyPattern;
    private Pattern andOrOtherPattern;
    private Pattern forExamplePattern;
    private Pattern likePattern;
    private Pattern isAPattern;
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

        checkSuchAsPattern();     //#1  [Noun Phrase] such as [Noun Phrases]
        checkIsOneOfPattern();    //#2  ^[Noun Phrase] is {a {group|member}|part|one} of [Noun Phrases]
        checkEspeciallyPattern(); //#3  [Noun Phrase] , especially [Noun Phrases]
        checkAndOrOtherPattern(); //#4  [Noun Phrase] {and|or} other [Noun Phrases]
        checkForExamplePattern(); //#5  [Noun Phrase] (e.g., [Noun Phrases]
        checkLikePattern();       //#6  [Noun Phrase] like [Noun Phrases]
        checkIsAPattern();        //#7  ^[Noun Phrase] is a [Noun Phrase]

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
                boolean useful = containsNoun(subject);
                subject = removeTags(subject);

                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    useful = useful && containsNoun(object);       //Treffer ist nur nützlich, wenn mindestens ein Nomen in beiden Phrasen enthält
                    object = removeTags(object);
                    if (useful) {
                        results.add(subject + "#" + object + "0.89");
                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + object + "0.70");
                        }
                    }
                }
                if (useful && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + subject + "0.70");
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
            String[] subjectObject = patternMatcher.group().split(" (is_VBZ|are_VBP)_B-VP (a_DT_B-NP (group|member)_NN_I-NP|part_NN_B-NP|one_CD_B-NP) of_IN_B-PP ");

            //entferne "and"/"or"/",","of" und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrCommaOf(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
            for (String subject : subjects) {
                subject = removeExpendablyWords(subject);
                boolean useful = containsNoun(subject);
                subject = removeTags(subject);

                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    useful = useful && containsNoun(object);       //Treffer ist nur nützlich, wenn mindestens ein Nomen in beiden Phrasen enthält
                    object = removeTags(object);
                    if (useful) {
                        results.add(object + "#" + subject + "0.65");
                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + object + "0.70");
                        }
                    }
                }
                if (useful && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + subject + "0.70");
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
                boolean useful = containsNoun(subject);
                subject = removeTags(subject);

                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    useful = useful && containsNoun(object);       //Treffer ist nur nützlich, wenn mindestens ein Nomen in beiden Phrasen enthält
                    object = removeTags(object);
                    if (useful) {
                        results.add(subject + "#" + object + "0.88");
                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + object + "0.70");
                        }
                    }
                }
                if (useful && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + subject + "0.70");
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
                boolean useful = containsNoun(subject);
                subject = removeTags(subject);

                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    useful = useful && containsNoun(object);       //Treffer ist nur nützlich, wenn mindestens ein Nomen in beiden Phrasen enthält
                    object = removeTags(object);
                    if (useful) {
                        results.add(object + "#" + subject + "0.80");
                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + object + "0.70");
                        }
                    }
                }
                if (useful && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + subject + "0.70");
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
                boolean useful = containsNoun(subject);
                subject = removeTags(subject);

                for (String object : examples) {
                    object = removeExpendablyWords(object);
                    useful = useful && containsNoun(object);       //Treffer ist nur nützlich, wenn mindestens ein Nomen in beiden Phrasen enthält
                    object = removeTags(object);
                    if (useful) {
                        results.add(subject + "#" + object + "0.79");
                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + object + "0.70");
                        }
                    }
                }
                if (useful && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + subject + "0.70");
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
                boolean useful = containsNoun(subject);
                subject = removeTags(subject);

                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    useful = useful && containsNoun(object);       //Treffer ist nur nützlich, wenn mindestens ein Nomen in beiden Phrasen enthält
                    object = removeTags(object);
                    if (useful) {
                        results.add(subject + "#" + object + "0.82");
                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + object + "0.70");
                        }
                    }
                }
                if (useful && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + subject + "0.70");
                }
            }
        }
    }

    private void checkIsAPattern() {
        patternMatcher.usePattern(isAPattern);

        if (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" is_VBZ_B-VP a_DT_B-NP ");

            String subject = subjectObject[0];
            //entferne "and"/"or"/"," und splitte dabei
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baut die VATER#KIND-Relation als String und entfernt dabei unerwünschte Wörter und die Tags
                subject = removeExpendablyWords(subject);
                boolean useful = containsNoun(subject);
                subject = removeTags(subject);

                for (String object : objects) {
                    object = removeExpendablyWords(object);
                    useful = useful && containsNoun(object);       //Treffer ist nur nützlich, wenn mindestens ein Nomen in beiden Phrasen enthält
                    object = removeTags(object);
                    if (useful) {
                        results.add(subject + "#" + object + "0.75");
                        //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                        //OHNE ADJEKTIVE#MIT ADJEKTIVE
                        if (containsAdjective(object)) {
                            results.add(removeTags(removeAdjectives(object)) + "#" + object + "0.70");
                        }
                    }
                }
                if (useful && containsAdjective(subject)) {
                    results.add(removeTags(removeAdjectives(subject)) + "#" + subject + "0.70");
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
        //remove "
        sentence = sentence.replaceAll("\"","");
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
        //remove existential there
        sentence =sentence.replaceAll("there_EX_(I|B)-NP ?","");
        //remove verbs
        sentence = sentence.replaceAll("[\\w -]*_VB[A-Z]?_(I|B)-NP ?","");
        //remove unwanted adjectives
        sentence = sentence.replaceAll("[\\w -]*(many|several|other|various|important|local|common|well-known|similar|certain" +
                "|nearby|numerous|different|diverse|early|major|very|few|regional|such|famous|popular|main)_JJ_(I|B)-NP ?","");
        //remove unwanted nouns
        sentence = sentence.replaceAll("member_NNS?_(I|B)-NP ?","");
        return sentence;
    }

    /**
     * Zerlegt einen Satzteil in mehrere Teile anhand Trenwörter "and", "or" und ",".
     *
     * @param s der zu zerlegende Satzteil
     * @return die entstehenden Satz-Fragmente als String-Array
     */
    private String[] handleAndOrComma(String s){
        return s.split(" and_CC_I-NP ?| ?and_CC_O | or_CC_I-NP ?| ?or_CC_O | ,_,_I-NP | ,_,_O ");
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
        return s.replaceAll("(_(([A-Z]{2,4}|-(L|R)RB-)|''|``))?_(I|B)-(NP|O)","").replaceAll("_|,","").trim();
    }

    /**
     * Prüft ob der gebebene Ausdruck ein Adejektiv enthält.
     * @param s der zu prüfende Ausdruck
     * @return true, wenn s ein Adjektiv enthält
     */
    private boolean containsAdjective(String s) {
        Matcher adjectiveMatcher = adjectivePattern.matcher(s);
        return adjectiveMatcher.find();
    }

    /**
     * Prüft ob der gebebene Ausdruck ein Nomen enthält.
     * @param s der zu prüfende Ausdruck
     * @return true, wenn s ein Nomen enthält
     */
    private boolean containsNoun(String s) {
        Matcher nounMatcher = nounPattern.matcher(s);
        return nounMatcher.find();
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
    //TODO: \\w durch \p{Nl} ersetzen?
    private void initPatterns() {
        suchAsPattern = Pattern.compile("[\\w&-]*(_B-NP)( [\\w&-]*_I-NP)*" +
                        " such_JJ_B-PP as_IN_I-PP " +
                        "[\\w&-]*(_B-NP)( ([\\w,&-]*_I-NP|and_CC_O [\\w&-]*(_B-NP)|or_CC_O [\\w&-]*(_B-NP)|,_,_O [\\w&-]*(_B-NP)))*");

        isOneOfPattern = Pattern.compile("^(\"_``_(O|B-NP) )?[\\w\"&-]*(_(B|I)-NP)( [\\w&-]*_I-NP)*( \"_''_(O|I-NP))?" +
                " (is_VBZ|are_VBP)_B-VP (a_DT_B-NP (group|member)_NN_I-NP|part_NN_B-NP|one_CD_B-NP) of_IN_B-PP " +
                "[\\w&-]*(_B-NP)( ([\\w&-]*_I-NP|of_IN_B-PP [\\w&-]*(_B-NP)|and_CC_O [\\w&-]*(_B-NP)|or_CC_O [\\w&-]*(_B-NP)|,_,_O [\\w&-]*(_B-NP)))*");

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

        isAPattern = Pattern.compile("^(\"_``_(O|B-NP) )?[\\w\"&-]*(_(B|I)-NP)( [\\w&-]*_I-NP)*( \"_''_(O|I-NP))?" +
                " is_VBZ_B-VP a_DT_B-NP " +
                "[\\w&-]*(_I-NP)( [\\w&-]*_I-NP)*");

        adjectivePattern = Pattern.compile("_JJ_");

        nounPattern = Pattern.compile("_NN");
    }
}
