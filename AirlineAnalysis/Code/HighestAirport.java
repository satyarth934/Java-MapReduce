import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class HighestAirport {

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String[] arr = line.split("\t");

			// arr[3] = Country
			// arr[8] = Altitude

			String country = arr[3].trim();
			String altitude = arr[8].trim();

			context.write(new Text(altitude), new Text(country));

		}

	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {

		List<Text> val = new ArrayList<Text>();
		float max = 0;
		
		public void reduce(Text key, Iterable<Text> value, Context context){
			System.err.println("Reduce In");
			float alt = Float.parseFloat(key.toString());
			if(alt>max){
				max = alt;
				for(Text x : value){
					if(!val.contains(x))
						val.add(x);
				}
			}
		}
		
		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException{
			System.err.println("Cleanup In");
			for(Text x : this.val){
//				context.write(x, new FloatWritable(max));
				context.write(x, new Text(max+""));
			}
		}

	}

	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		
		job.setJarByClass(HighestAirport.class);
		
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		Path outputPath = new Path(args[1]);
		
		outputPath.getFileSystem(conf).delete(outputPath, true);
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
		
	}

}
