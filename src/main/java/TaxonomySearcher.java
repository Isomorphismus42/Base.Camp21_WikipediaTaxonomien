import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Main-Klasse zum initialisieren des MapReduce-Jobs.
 */
// Tool implementiert um Parameter übergabe in der Konsole zu ermöglichen.
public class TaxonomySearcher extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        // Let ToolRunner handle generic command-line options
        int res = ToolRunner.run(new Configuration(), new TaxonomySearcher(), args);

        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();

        // Create job
        Job job = Job.getInstance(conf, "taxonomysearcher");
        job.setJarByClass(TaxonomySearcher.class);

        job.setMapperClass(TokenMapper.class);
        // optional combiner class
        job.setCombinerClass(WeightReducer.class);
        job.setReducerClass(WeightReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FloatWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }
}

