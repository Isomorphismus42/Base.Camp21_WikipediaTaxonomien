import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
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
import java.io.FileNotFoundException;
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

    public TokenMapper() {
        sentenceAnalyzer = new SentenceAnalyzer();
        createModels();
    }

    private void createModels() {
        // get line model
        try (InputStream sModelIn = new FileInputStream("en-sent.bin")) {
            SentenceModel sModel = new SentenceModel(sModelIn);
            sentenceDetector = new SentenceDetectorME(sModel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // get tokenizer model
        try (InputStream tModelIn = new FileInputStream("en-token.bin")) {
            TokenizerModel tModel = new TokenizerModel(tModelIn);
            tokenizer = new TokenizerME(tModel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // get pos tagger model
        try (InputStream posModelIn = new FileInputStream("en-pos-maxent.bin")) {
            POSModel posModel = new POSModel(posModelIn);
            posTagger = new POSTaggerME(posModel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // get chunker model
        try (InputStream modelIn = new FileInputStream("en-chunker.bin")){
            ChunkerModel Cmodel = new ChunkerModel(modelIn);
            chunker = new ChunkerME(Cmodel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
            //TODO:konvertiere Wöter in Singular mit Lemmatizer?

            String taggedSentence = "";
            for (int i=0;i< chunks.length;i++) {
                taggedSentence += tokens[i] + "_" + tags[i] + "_" + chunks[i] + " ";
            }
            String[] results = sentenceAnalyzer.checkSentence(taggedSentence);
            for (String result : results){
                context.write(new Text(result), one);
            }
        }
    }
}