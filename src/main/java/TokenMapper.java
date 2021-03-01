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
import java.util.ArrayList;
import java.util.StringTokenizer;

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class TokenMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text sentence = new Text();
    private Text result = new Text();
    private Analyzer analyzer = new Analyzer();
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        // StringTokenizer itr = new StringTokenizer(value.toString(), "\n\r");
        StringTokenizer itr = new StringTokenizer(value.toString());

        // get sentence model
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
        // TODO Später rausnehmen?
        while (itr.hasMoreTokens()) {
            sentence.set(itr.nextToken());


            String sentences[] = sentenceDetector.sentDetect(sentence.toString());

            for (int i = 0; i < sentences.length; i++) {
                String tokens[] = tokenizer.tokenize(sentences[i]);
                String tags[] = posTagger.tag(tokens);
                for (int j = 0; j < tags.length; j++) {
                     context.write(new Text(tags[j]), one);
                }
            }

            //            ArrayList<String> groups = analyzer.checkSentence(sentence.toString());

//            groups.forEach((s) -> {

//              result = new Text(s);
            /*  try {
                    context.write(result, one);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } */
//            });
        }
    }
}