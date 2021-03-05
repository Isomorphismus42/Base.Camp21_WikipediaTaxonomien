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

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class TokenMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private SentenceAnalyzer sentenceAnalyzer;
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private ChunkerME chunker;
    private LemmatizerME lemmatizer;

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

        String[] sentences = sentenceDetector.sentDetect(value.toString());

        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            String[] tags = posTagger.tag(tokens);
            String[] chunks = chunker.chunk(tokens, tags);
//            String[] lemmas = lemmatizer.lemmatize(tokens, tags);

            String taggedSentence = "";
            for (int i=0;i< chunks.length;i++) {
                /*if (tags[i].equals("NN") || tags[i].equals("NNS")) {
                    tokens[i] = lemmas[i];
                }*/
                taggedSentence += tokens[i] + "_" + tags[i] + "_" + chunks[i] + " ";
            }
            String[] results = sentenceAnalyzer.checkSentence(taggedSentence);
            int weight = 0;

            for (String result : results){
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
                    result= result.substring(0,result.indexOf("#", result.indexOf("#") + 1));
                }

                context.write(new Text(result), new IntWritable(weight));
            }
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