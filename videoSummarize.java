import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.imageio.ImageIO;



public class videoSummarize{
	public static int width = 480;
	public static int height = 270;
	public static class rgb_hist{
		public int[] r_list;
		public int[] g_list;
		public int[] b_list;
		
		rgb_hist(int[] r, int[] g, int[] b){
			r_list = r;
			g_list = g;
			b_list = b;
		}
	}
	public final static int FPS = 15;
	public final static int seg_length=10;
	public final static int keyframe_length = seg_length*15;
	public final static int CLUSTER_NUMBER = 9;
	public final static int TOTAL_FRAME_LENGTH=4500;
	public static void main(String[] args){
		File file = new File(args[0]);
		InputStream is = null;
		try {
			ArrayList<Point> histogramList = new ArrayList<Point>();
			is = new FileInputStream(file);		
			
			long len = width*height*3;
			byte[] bytes = new byte[(int)len];

			
			int total_frames = (int) (file.length()/len);
			int sample_freq = FPS*1; // per X frames
			int keyFrame_max = total_frames/sample_freq;
			byte[] byteHolder = new byte[(int) (len*(sample_freq-1))];			
			
			int frameIndex = 0;
			for (int i= 0; i < keyFrame_max; i++){
				is.read(bytes, 0, bytes.length);
				if(i%2==0)
				{
				BufferedImage imgg=createRGBImages(bytes);
				 File op = new File("C:\\Users\\Monisha\\workspace\\EgoCentricVideos\\src\\altframes\\"+frameIndex+".rgb");
				 
				  ImageIO.write(imgg, "png", op);
				}

				Point frameNum = histogram(bytes);
				frameNum.frameNumber=frameIndex;
				histogramList.add(frameNum);
				
				is.read(byteHolder, 0, (int) (len*(sample_freq-1)));
				frameIndex+=15;
			}
			System.out.println(frameIndex);
			int cluster_marks = keyFrame_max/CLUSTER_NUMBER;
			ArrayList<Point> clusters_init = new ArrayList<Point>();
			for (int i =0 ; i <9; i++){
				Random rand_val = new Random();
				int nextVal = (int) (i*cluster_marks + rand_val.nextFloat()*cluster_marks);
				clusters_init.add(histogramList.get(nextVal));
			}
			
			 String fn="C:\\Users\\Monisha\\workspace\\EgoCentricVideos\\src\\altframes\\12651.rgb";
		     File fi = new File(fn);
		      
		      new SimilarityFinder(fi);
			
		      if (SimilarityFinder.distances[0]<=1000)
		      {
		    	  if (SimilarityFinder.distances[1]<=1000)
		    	  {
		    		  
		    	  }
		    	  String matched_frameString=SimilarityFinder.others[0].getName();
		    	  int matched_frameno=Integer.parseInt(matched_frameString.substring(0, matched_frameString.length()-4));
		    	  System.out.println("matched"+matched_frameno);
		    	  ArrayList<Integer> queryframes = new ArrayList<Integer>();
		    	  for (int i =30; i >0 ; i--){
						
						
		    		  queryframes.add(matched_frameno-i);
						System.out.println(matched_frameno-i);
					}
		    	  queryframes.add(matched_frameno);
		    	  for (int i =0; i < 30; i++){
						
					
		    		  queryframes.add(matched_frameno+i);
						System.out.println(matched_frameno+i);
					}
		    	  File input_file = new File(args[0]);

				    InputStream input_is = new FileInputStream(input_file);

				    File output_file = new File(args[0].substring(0, args[0].length()-4) + "_query.rgb");
				    OutputStream output_is = new FileOutputStream(output_file, true);
				    byte[] newByteHolder = new byte[Collections.max(queryframes)*480*270*3];
				    byte[] header = new byte[44];
				    
				    File audio_file = new File(args[0].substring(0, args[0].length()-4) + "_query.wav");
				    OutputStream audio_is = new FileOutputStream(audio_file, true);
				    
				    
				    
				    File input_audio = new File(args[1]);
				    InputStream audio_input_is = new FileInputStream(input_audio);
				    audio_input_is.read(header, 0, 44);
				    audio_is.write(header);
				    byte[] audio_bytes = new byte[seg_length*48000];
				    byte[] audio_filler=new byte[Collections.max(queryframes)/FPS*48000+48000];
				    
				    int current_frame = 0;
				    for (int i =0; i < queryframes.size(); i++){
				    	
				    	if (queryframes.get(i) >0){
				    		
				    		input_is.read(newByteHolder, 0, queryframes.get(i)*width*height*3);
				    		current_frame+=queryframes.get(i);
				    		double seconds = queryframes.get(i)/FPS;
				    		audio_input_is.read(audio_filler,0,(int) (seconds * 48000));
				    	}
				    	int num_read_frames=keyframe_length;
				    	int audio_length=seg_length;
				    	if (queryframes.get(i)<0){
				    		num_read_frames=num_read_frames+queryframes.get(i);
				    		double d_audio_length = num_read_frames/keyframe_length*seg_length;
				    		audio_length=(int) d_audio_length;
				    		current_frame+=num_read_frames;
				    	}
				    	if (current_frame > TOTAL_FRAME_LENGTH){
				    		num_read_frames=current_frame-4500;
					    	for (int j = 0; j < num_read_frames; j++){
					    		int remainder = input_is.read(bytes, 0 , width*height*3);
					    		if (remainder < 0){
					    			break;
					    		}
					    		output_is.write(bytes);
					    	}
					    	
					    	int remainder_audio = audio_input_is.read(audio_bytes, 0, audio_length*48000);
					    	
					    	if (remainder_audio < 0){
					    		break;
					    	}
					    	audio_is.write(audio_bytes);
					    	break;
				    	}
				    	for (int j = 0; j < num_read_frames; j++){
				    		int remainder = input_is.read(bytes, 0 , width*height*3);
				    		if (remainder < 0){
				    			break;
				    		}
				    		output_is.write(bytes);
				    	}
				    	
				    	int remainder_audio = audio_input_is.read(audio_bytes, 0, audio_length*48000);
				    	
				    	if (remainder_audio < 0){
				    		break;
				    	}
				    	audio_is.write(audio_bytes);
				    	
				    	
				    }
				    input_is.close();
				    output_is.close();
				    audio_input_is.close();
				    audio_is.close();
		    	  
		    	  
		      }
			KMeans cluster_outputs = new KMeans(histogramList, clusters_init);
			cluster_outputs.calculate();
			
			int clusterIter = 0;
			int numClusters = 0;
			while (true){
				for (Cluster p : cluster_outputs.clusters){
					if (p.points.size() > 1){
						numClusters++;
					}
				}
				if (numClusters > 8){
					System.out.println("Sufficient Clusters");
					break;
				} else{
					numClusters =0;
					System.out.println("Redoing, insufficient clusters");
					clusters_init.clear();
					for (int i =0 ; i <9; i++){
						Random rand_val = new Random();
						int nextVal = (int) (i*cluster_marks + rand_val.nextFloat()*cluster_marks);
						clusters_init.add(histogramList.get(nextVal));
					}
					
					cluster_outputs = new KMeans(histogramList,clusters_init);
					cluster_outputs.calculate();
				}
			}
			
			for (Cluster p : cluster_outputs.clusters){
				System.out.println("Cluster " + clusterIter +" Frames: ");
				for (int i = 0; i < p.points.size(); i++){
					System.out.print(p.points.get(i).frameNumber + ", ");
					p.points.get(i).centroidCompare=p.getCentroid();
				}
				clusterIter++;
				
				Collections.sort(p.points);
			}
			System.out.println();
			System.out.println("Target frames : ");
			System.out.println();
			ArrayList<Integer> targetFrames = new ArrayList<Integer>();
			for (Cluster p : cluster_outputs.clusters){
				System.out.print(p.points.get(1).frameNumber + ", ");
				targetFrames.add(p.points.get(1).frameNumber);
			}
			Collections.sort(targetFrames);
			ArrayList<Integer> frames_skip = new ArrayList<Integer>();
			System.out.println();
			
			System.out.println("Ordered frames :");
			for (int i =0 ; i < targetFrames.size(); i ++){
				System.out.print(targetFrames.get(i)+ ", ");
			}
			System.out.println();
			for (int i =0; i < targetFrames.size()-1; i++){
				int content_length = targetFrames.get(i)+keyframe_length;
				int diff = targetFrames.get(i+1)-content_length;
//				if (diff < 0 ){
//					diff =0;
//				}
				frames_skip.add(diff);
				System.out.println(diff);
			}
			frames_skip.add(0,targetFrames.get(0));
			
		    File input_file = new File(args[0]);

		    InputStream input_is = new FileInputStream(input_file);

		    File output_file = new File(args[0].substring(0, args[0].length()-4) + "_sum.rgb");
		    OutputStream output_is = new FileOutputStream(output_file, true);
		    byte[] newByteHolder = new byte[Collections.max(frames_skip)*480*270*3];
		    byte[] header = new byte[44];
		    
		    File audio_file = new File(args[0].substring(0, args[0].length()-4) + "_sum.wav");
		    OutputStream audio_is = new FileOutputStream(audio_file, true);
		    
		    
		    
		    File input_audio = new File(args[1]);
		    InputStream audio_input_is = new FileInputStream(input_audio);
		    audio_input_is.read(header, 0, 44);
		    audio_is.write(header);
		    byte[] audio_bytes = new byte[seg_length*48000];
		    byte[] audio_filler=new byte[Collections.max(frames_skip)/FPS*48000+48000];
		    
		    int current_frame = 0;
		    for (int i =0; i < frames_skip.size(); i++){
		    	
		    	if (frames_skip.get(i) >0){
		    		
		    		input_is.read(newByteHolder, 0, frames_skip.get(i)*width*height*3);
		    		current_frame+=frames_skip.get(i);
		    		double seconds = frames_skip.get(i)/FPS;
		    		audio_input_is.read(audio_filler,0,(int) (seconds * 48000));
		    	}
		    	int num_read_frames=keyframe_length;
		    	int audio_length=seg_length;
		    	if (frames_skip.get(i)<0){
		    		num_read_frames=num_read_frames+frames_skip.get(i);
		    		double d_audio_length = num_read_frames/keyframe_length*seg_length;
		    		audio_length=(int) d_audio_length;
		    		current_frame+=num_read_frames;
		    	}
		    	if (current_frame > TOTAL_FRAME_LENGTH){
		    		num_read_frames=current_frame-4500;
			    	for (int j = 0; j < num_read_frames; j++){
			    		int remainder = input_is.read(bytes, 0 , width*height*3);
			    		if (remainder < 0){
			    			break;
			    		}
			    		output_is.write(bytes);
			    	}
			    	
			    	int remainder_audio = audio_input_is.read(audio_bytes, 0, audio_length*48000);
			    	
			    	if (remainder_audio < 0){
			    		break;
			    	}
			    	audio_is.write(audio_bytes);
			    	break;
		    	}
		    	for (int j = 0; j < num_read_frames; j++){
		    		int remainder = input_is.read(bytes, 0 , width*height*3);
		    		if (remainder < 0){
		    			break;
		    		}
		    		output_is.write(bytes);
		    	}
		    	
		    	int remainder_audio = audio_input_is.read(audio_bytes, 0, audio_length*48000);
		    	
		    	if (remainder_audio < 0){
		    		break;
		    	}
		    	audio_is.write(audio_bytes);
		    	
		    	
		    }
		    input_is.close();
		    output_is.close();
		    audio_input_is.close();
		    audio_is.close();
		    
		    
		    /*48000 bytes/second for wav files*/
		    
		    
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static Point histogram(byte[] inputFrame){
		int[] r_HistVals = new int[256];
		int[] g_HistVals = new int[256];
		int[] b_HistVals = new int[256];
		for (int i = 0; i < width*height; i ++){
			
			int rVal = inputFrame[i] & 0xFF;
			int gVal = inputFrame[i + height*width] & 0xFF;
			int bVal = inputFrame[i + height*width*2] & 0xFF;
			r_HistVals[rVal]++;
			g_HistVals[gVal]++;
			b_HistVals[bVal]++;
			
		}
		Point x =new Point(r_HistVals, g_HistVals, b_HistVals); 
		return x;
	}
	
	public static BufferedImage createRGBImages(byte[] bytes)
	{
		int width = 480;
		int height = 270;
		BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int ind = 0;
		for (int y = 0; y < height; y++) {

			for (int x = 0; x < width; x++) {

				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind + height * width];
				byte b = bytes[ind + height * width * 2];

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				// int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				newImg.setRGB(x, y, pix);
				ind++;
			}
		}
		return newImg;
	}

}