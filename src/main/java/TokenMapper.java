import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class TokenMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text sentence = new Text();
    private Text result = new Text();
    private Analyzer analyzer = new Analyzer();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
       // int z = 0;
        StringTokenizer itr = new StringTokenizer(value.toString(), "\n\r");

        while (itr.hasMoreTokens()) {
            sentence.set(itr.nextToken());

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
            context.write(sentence, one);
        }
    }
}