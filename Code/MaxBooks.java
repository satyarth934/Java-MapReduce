/*
Find out how many book were published based on ranking in the year 2002.
*/

import java.io.IOException;
import java.util.regex.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MaxBooks {

	public static class Map extends
			Mapper<LongWritable, Text, Text, IntWritable> {
		
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();

			Pattern pat = Pattern.compile("\"[0-9]{4}\"");
			Matcher mat = pat.matcher(line);
			if(mat.find()){
				String yr = mat.group();
				yr = yr.replaceAll("\"", "");
				value.set(yr);
				context.write(value, new IntWritable(1));
			}
		}
	}
	
	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable>{
		
		int max = 0;
		Text yr = new Text("");
		
		public void reduce(Text key, Iterable<IntWritable> value, Context context){
			
			int sum = 0;
			for(IntWritable x : value){
				sum += x.get();
			}
			
			if(sum>max){
				max = sum;
				yr.set(key);
			}
		}
		
		@Override
		protected void cleanup(Context context)throws IOException, InterruptedException{
			context.write(yr, new IntWritable(max));
		}
	}

	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Maximum number of books published in a year");
		
		job.setJarByClass(MaxBooks.class);
		
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		Path outputPath = new Path(args[1]);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		outputPath.getFileSystem(conf).delete(outputPath, true);
		
		System.exit(job.waitForCompletion(true)?0:1);
	}

}
