import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dient zur Analyse von POS-getaggten Sätzen auf bestimmte RegEx-Pattern, um Taxonomien zu finden.
 * Dabei wird auf das vorhanden sein der Schlüsselwörter "such as", "is {a {group|member}|part|one} of",
 * ", especially", "{and|or} other", (e.g.,"(e.g.,", "like" und "is a" geprüft und die gefundenen möglichen
 * Taxonomien anschließend als String-Array zurückgegeben.
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
     * Konstruktor, initialisiert die verschiedenen Pattern.
     */
    public SentenceAnalyzer() {
        initPatterns();
    }

    /**
     * Prüft einen gegebenen POS-getaggten Satz auf Schlüsselwörter wie z.B. "such as" oder "is one of" und
     * gibt die gefundenen Matches als Array zurück.
     *
     * @param taggedSentence Der zu prüfende, mit Part-of-Speech getaggte Satz
     * @return Ein String Array mit den gefundenen Taxonomie-Relationen im Format VATER\tKIND\tGEWICHT
     */
    public String[] checkSentence(String taggedSentence) {
        //initialisiere Ergebinsliste und Pattern-Matcher
        results = new ArrayList<String>();
        patternMatcher = suchAsPattern.matcher(taggedSentence);

        //Prüfe auf die verschieden Pattern
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
     * VATER\tKIND\tGEWICHT
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

            //baue die Taxonomie-Relation nach dem Schema VATER[, especially]KIND
            createTaxonomyRelations(subjects,objects,"0.59");
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "is/are one of" und fügt gefundene Matches nach Konvertierung ins
     * VATER\tKIND\tGEWICHT
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

            //baue die Taxonomie-Relation nach dem Schema KIND[is ... of]VATER
            createTaxonomyRelations(objects,subjects,"0.48");
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "such as" und fügt gefundene Matches nach Konvertierung ins
     * VATER\tKIND\tGEWICHT
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

            //baue die Taxonomie-Relation nach dem Schema VATER[such as]KIND
            createTaxonomyRelations(subjects,objects,"0.68");
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "and/or other" und fügt gefundene Matches nach Konvertierung ins
     * VATER\tKIND\tGEWICHT
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

            //baue die Taxonomie-Relation nach dem Schema KIND[, and other]VATER
            createTaxonomyRelations(objects,subjects,"0.65");
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "e.g." und fügt gefundene Matches nach Konvertierung ins
     * VATER\tKIND\tGEWICHT
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

            //baue die Taxonomie-Relation nach dem Schema VATER[(e.g.,]KIND
            createTaxonomyRelations(subjects,examples,"0.53");
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "like" und fügt gefundene Matches nach Konvertierung ins
     * VATER\tKIND\tGEWICHT
     * Format, der Result-Liste hinzu.
     */
    private void checkLikePattern() {
        patternMatcher.usePattern(likePattern);

        while (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" like_IN_B-PP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baue die Taxonomie-Relation nach dem Schema KIND[like]VATER
            createTaxonomyRelations(subjects,objects,"0.67");
        }
    }

    /**
     * Prüft den gegeben Satz auf das Key-Wort "is {a|an}" und fügt gefundene Matches nach Konvertierung ins
     * VATER\tKIND\tGEWICHT
     * Format, der Result-Liste hinzu.
     */
    private void checkIsAPattern() {
        patternMatcher.usePattern(isAPattern);

        if (patternMatcher.find()) {
            //zerlege den gefunden String in Subjekt,Objekt
            String[] subjectObject = patternMatcher.group().split(" is_VBZ_B-VP an?_DT_B-NP ");

            //entferne "and"/"or"/"," und splitte dabei
            String[] subjects = handleAndOrComma(subjectObject[0]);
            String[] objects = handleAndOrComma(subjectObject[1]);

            //baue die Taxonomie-Relation nach dem Schema KIND[is a]VATER
            createTaxonomyRelations(objects,subjects,"0.60");
        }
    }

    /**
     * Baut die VATER KIND-Relation als String und entfernt dabei unerwünschte Wörter und die POS-Tags.
     * Das übergebene Gewicht, sollte dabei die Güte des verwndeten Patterns repräsentieren, mit dem die
     * entsprechnden Satzteile indetifiziert wurden.
     *
     * @param parents Der Satzteil, der den Oberbegriff enthält
     * @param children Der Satzteil, der den Unterbegriff enthält
     * @param weight Das festgelegte Gewicht für die Relation
     */
    private void createTaxonomyRelations(String[] parents,String[] children, String weight) {
        for (String parent : parents) {
            parent = removeExpendablyWords(parent);
            boolean taxonomyAdded = false;

            for (String child : children) {
                child = removeExpendablyWords(child);

                //Treffer ist nur nützlich, wenn mindestens ein Nomen in beiden Phrasen enthält
                if (containsNoun(parent) && containsNoun(child)) {
                    results.add(removeTags(parent) + "\t" + removeTags(child) + weight);
                    taxonomyAdded = true;

                    //prüfe auf Adjektive in den Ausdrücken und baue ggf weitere Relationen nach dem Schema
                    //OHNE ADJEKTIVE#MIT ADJEKTIVE
                    if (containsAdjective(child)) {
                        results.add(removeTags(removeAdjectives(child)) + "\t" + removeTags(child) + "0.50");
                    }
                }
            }
            if (taxonomyAdded && containsAdjective(parent)) {
                results.add(removeTags(removeAdjectives(parent)) + "\t" + removeTags(parent) + "0.50");
            }
        }
    }

    /**
     * Entfernt überflüssige, unerwünschte Wörter wie z.B. Artikel, Adverben, Pronomen, etc..
     *
     * @param sentence Der zu behandelnde Satzteil
     * @return Der Satzteil ohne die unerwünschten Wörter als String
     */
    private String removeExpendablyWords(String sentence){
        //remove "
        sentence = sentence.replaceAll("\"","");
        //remove Determiner and all previous Words (a, an, the, that, those, some, ...)
        sentence = sentence.replaceAll("[\\p{L}0-9_ -]*_DT_(I|B)-NP ?","");
        //remove Adverbs and all previous Words (more, nearby, typically, highly, ...)
        sentence = sentence.replaceAll("[\\p{L}0-9_ -]*_RB(R|S)?_(I|B)-NP ?","");
        //remove Comparative adjectives and all previous Words (higher, larger, best, more, earlier, ...)
        sentence = sentence.replaceAll("[\\p{L}0-9_ -]*_JJ(R|S)_(I|B)-NP ?","");
        //remove Personal pronoun (he, she, it, his, ...)
        sentence = sentence.replaceAll("[\\p{L}0-9_ -]*_PRP\\$?_(I|B)-NP ?", "");
        //remove Prepositions (by, with ...)
        sentence = sentence.replaceAll("[\\p{L}0-9_ -]*_IN_(I|B)-NP ?", "");
        //remove Wh-determiner (which, that, ...)
        sentence = sentence.replaceAll("[\\p{L}0-9_ -]*_WDT_(I|B)-NP ?", "");
        //remove existential there
        sentence =sentence.replaceAll("there_EX_(I|B)-NP ?","");
        //remove verbs
        sentence = sentence.replaceAll("[\\p{L}0-9_ -]*_VB[A-Z]?_(I|B)-NP ?","");
        //remove unwanted adjectives
        sentence = sentence.replaceAll("[\\p{L}0-9_ -]*(many|several|other|various|important|local|common|well-known|similar|certain" +
                "|nearby|numerous|different|diverse|early|major|very|few|regional|such|famous|popular|main|notable)_JJ_(I|B)-NP ?","");
        //remove unwanted nouns
        sentence = sentence.replaceAll("member_NNS?_(I|B)-NP ?","");
        return sentence;
    }

    /**
     * Zerlegt einen Satzteil in mehrere Teile anhand Trenwörter "and", "or" und ",".
     *
     * @param sentence Der zu zerlegende Satzteil
     * @return Die entstehenden Satz-Fragmente als String-Array
     */
    private String[] handleAndOrComma(String sentence){
        return sentence.split(" and_CC_I-NP ?| ?and_CC_O | or_CC_I-NP ?| ?or_CC_O | ,_,_I-NP | ,_,_O ");
    }

    /**
     * Zerlegt einen Satzteil in mehrere Teile anhand Trenwörter "and", "or" und ",",
     * und zusätzlich noch nach dem Trennwort "of".
     *
     * @param sentence der zu zerlegende Satzteil
     * @return die entstehenden Satz-Fragmente als String-Array
     */
    private String[] handleAndOrCommaOf(String sentence) {
        return sentence.split(" and_CC_I-NP ?| ?and_CC_O | or_CC_I-NP ?| or_CC_O | ,_,_I-NP | ,_,_O | of_IN_B-PP ");
    }

    /**
     * Entfernt die Part-of-Speech Tags aller Wörter des gegeben Strings
     *
     * @param s der Gegebene String
     * @return der String ohne Wort-Tags
     */
    private String removeTags(String s){
        return s.replaceAll("(_(([A-Z]{1,4}|-(L|R)RB-)|''|``))?_(I|B)-(NP|O)","").trim();
    }
    //.replaceAll("_|`|'|,","")

    /**
     * Prüft ob der gebebene Ausdruck ein Adejektiv enthält.
     *
     * @param s der zu prüfende Ausdruck
     * @return true, wenn s ein Adjektiv enthält
     */
    private boolean containsAdjective(String s) {
        Matcher adjectiveMatcher = adjectivePattern.matcher(s);
        return adjectiveMatcher.find();
    }

    /**
     * Prüft ob der gebebene Ausdruck ein Nomen enthält.
     *
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
        return s.replaceAll("[\\p{L}0-9_ -]*_JJ_(I|B)-NP ","");
    }

    /**
     * Erstellt die Regex Patterns, nach denen im Text geguckt werden soll
     */
    private void initPatterns() {
        suchAsPattern = Pattern.compile("[\\p{L}0-9_&-]*(_B-NP)( [\\p{L}0-9_&-]*_I-NP)*" +
                        " such_JJ_B-PP as_IN_I-PP " +
                        "[\\p{L}0-9_&-]*(_B-NP)( ([\\p{L}0-9_,&-]*_I-NP|and_CC_O [\\p{L}0-9_&-]*(_B-NP)|or_CC_O [\\p{L}0-9_&-]*(_B-NP)|,_,_O [\\p{L}0-9_&-]*(_B-NP)))*");

        isOneOfPattern = Pattern.compile("^(\"_``_(O|B-NP) )?[\\p{L}0-9_\"&-]*(_(B|I)-NP)( [\\p{L}0-9_&-]*_I-NP)*( \"_''_(O|I-NP))?" +
                " (is_VBZ|are_VBP)_B-VP (a_DT_B-NP (group|member)_NN_I-NP|part_NN_B-NP|one_CD_B-NP) of_IN_B-PP " +
                "[\\p{L}0-9_&-]*(_B-NP)( ([\\p{L}0-9_&-]*_I-NP|of_IN_B-PP [\\p{L}0-9_&-]*(_B-NP)|and_CC_O [\\p{L}0-9_&-]*(_B-NP)|or_CC_O [\\p{L}0-9_&-]*(_B-NP)|,_,_O [\\p{L}0-9_&-]*(_B-NP)))*");

        especiallyPattern = Pattern.compile("[\\p{L}0-9_&-]*(_B-NP)( [\\p{L}0-9_&-]*_I-NP)*" +
                " ,_,_O especially_RB_B-ADVP " +
                "[\\p{L}0-9_&-]*(_B-NP)( ([\\p{L}0-9_,&-]*_I-NP|and_CC_O [\\p{L}0-9_&-]*(_B-NP)|or_CC_O [\\p{L}0-9_&-]*(_B-NP)|,_,_O [\\p{L}0-9_&-]*(_B-NP)))*");

        andOrOtherPattern = Pattern.compile("[\\p{L}0-9_&-]*(_B-NP)( ([\\p{L}0-9_,&-]*_I-NP|(,_,_O) [\\p{L}0-9_&-]*(_B-NP)))*" +
                " (,_,_O )?(and_CC_O|or_CC_O) other_JJ_B-NP" +
                "( ([\\p{L}0-9_&-]*_I-NP|and_CC_O [\\p{L}0-9_&-]*(_B-NP)|or_CC_O [\\p{L}0-9_&-]*(_B-NP)))+");

        forExamplePattern = Pattern.compile("[\\p{L}0-9_&-]*(_B-NP)( [\\p{L}0-9_&-]*_I-NP)*" +
                " \\(_-LRB-_((I|B)-NP|O) e_NN_(B|I)-NP .?g._NNP_I-NP ,_,_(I-NP|O)" +
                "( ([\\p{L}0-9_,&-]*_(I|B)-NP|and_CC_O [\\p{L}0-9_&-]*(_B-NP)|or_CC_O [\\p{L}0-9_&-]*(_B-NP)|,_,_O [\\p{L}0-9_&-]*(_B-NP)))+");

        likePattern = Pattern.compile("[\\p{L}0-9_&-]*(_B-NP)( [\\p{L}0-9_&-]*_I-NP)*" +
                " like_IN_B-PP " +
                "[\\p{L}0-9_&-]*(_B-NP)( ([\\p{L}0-9_&,-]*_I-NP|(,_,_O )?and_CC_O [\\p{L}0-9_&-]*(_B-NP)|or_CC_O [\\p{L}0-9_&-]*(B-NP)|,_,_O [\\p{L}0-9_&-]*(_B-NP)))*");

        isAPattern = Pattern.compile("^(\"_``_(O|B-NP) )?[\\p{L}0-9_\"&-]*(_(B|I)-NP)( [\\p{L}0-9_&-]*_I-NP)*( \"_''_(O|I-NP))?" +
                " is_VBZ_B-VP an?_DT_B-NP " +
                "[\\p{L}0-9_&-]*(_I-NP)( [\\p{L}0-9_&-]*_I-NP)*");

        adjectivePattern = Pattern.compile("_JJ_(I|B)-NP [\\p{L}0-9_-]*(_NN)");

        nounPattern = Pattern.compile("_NN");
    }
}
