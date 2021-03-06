import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class TokenMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private SentenceAnalyzer sentenceAnalyzer;
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private ChunkerME chunker;
    private LemmatizerME lemmatizer;

    /**
     * Konstruktor, initialisiert die POS-Modelle.
     */
    public TokenMapper() {
        sentenceAnalyzer = new SentenceAnalyzer();
        try {
            createModels();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
//        StringTokenizer itr = new StringTokenizer(value.toString(), "\n\r");

        //Zerlege Input-Line in Sätze
        String[] sentences = sentenceDetector.sentDetect(value.toString());

        for (String sentence : sentences) {
            //Erstelle Token und zugehörige Tags
            String[] tokens = tokenizer.tokenize(sentence);
            String[] tags = posTagger.tag(tokens);
            String[] chunks = chunker.chunk(tokens, tags);
//            String[] lemmas = lemmatizer.lemmatize(tokens, tags);

            String taggedSentence = "";
            for (int i=0;i< chunks.length;i++) {
                //Ersetze Pluralnomen durch ihre Singular Form TODO:NNPS hinzufügen?
//                if (tags[i].equals("NNS")) {
//                    tokens[i] = lemmas[i];
//                }
                //Schreibe alle Wörter außer Eigennamen klein
                if (!(tags[i].equals("NNP")|tags[i].equals("NNPS"))){
                    tokens[i] = tokens[i].toLowerCase(Locale.ROOT);
                }
                //Baue den zu untersuchenden Satz mit Wort- und Chunk-Tag
                taggedSentence += tokens[i] + "_" + tags[i] + "_" + chunks[i] + " ";
            }

            //Prüfe auf Taxonomien
            String[] results = sentenceAnalyzer.checkSentence(taggedSentence);

            //Übergebe die gefundenen Relationen
            for (String result : results){

//                weighedResult(result);
                //TODO: Gewichte einbauen im Format "0.XX"
//                if (!result.equals("")) {
//                    float weight = Float.parseFloat(result.substring(result.length() - 4, result.length()));
//                    result = result.substring(0, result.length() - 4);
//                    context.write(new Text(result), new FloatWritable(weight));
//                }


                context.write(new Text(result), one);

//                context.write(new Text(result), new IntWritable(weight));
            }
        }
    }

    private String weighedResult(String result){
        int weight = 0;
                if (result.length() > 2) {
                    String tag = result.substring(result.length() - 2).replace("#","");
                    switch (tag) {
                        case "1":
                        case "1a":
                            weight = 4; break;
                        case "2":
                        case "2a":
                            weight = 3; break;
                        case "3":
                        case "3a":
                            weight = 3; break;
                        default: break;
                    }
                    // Schneidet den Tag vom Ergebnis ab
                    return result.substring(0,result.indexOf("#", result.indexOf("#") + 1));
                }
                else {
                    return "";
                }
    }




    private void createModels() throws IOException {
        // get line model
        try (InputStream sModelIn = new FileInputStream("en-sent.bin")) {
            SentenceModel sModel = new SentenceModel(sModelIn);
            sentenceDetector = new SentenceDetectorME(sModel);
        }
        // get tokenizer model
        try (InputStream tModelIn = new FileInputStream("en-token.bin")) {
            TokenizerModel tModel = new TokenizerModel(tModelIn);
            tokenizer = new TokenizerME(tModel);
        }
        // get pos tagger model
        try (InputStream posModelIn = new FileInputStream("en-pos-maxent.bin")) {
            POSModel posModel = new POSModel(posModelIn);
            posTagger = new POSTaggerME(posModel);
        }
        // get chunker model
        try (InputStream cModelIn = new FileInputStream("en-chunker.bin")){
            ChunkerModel Cmodel = new ChunkerModel(cModelIn);
            chunker = new ChunkerME(Cmodel);
        }
        try (InputStream lModelIn = new FileInputStream("en-lemmatizer.bin")) {
            LemmatizerModel lModel = new LemmatizerModel(lModelIn);
            lemmatizer = new LemmatizerME(lModel);
        }
    }
}