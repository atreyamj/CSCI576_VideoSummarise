/* 
 * This class will write video_shot when a start and end frame number is given
 * 
 * Input and output file names are provided
 * Raw video file has no header
 * Each shot has a start_frame and end_frame
 * Everytime, the file is appended with the shot provided
 * 
 */
package video_summarise;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class WriteShot {

	static int i = 0;
	static int frame_start1, frame_end1;
	static String input_fname, output_fname;

	/*
	 * Initialize the variables with the passed
	 * input_video_file_name, input_audio_file_name 
	 * frame_start, frame_end
	 * 
	 * The data is appended to the existing file
	 */
	public WriteShot(String start, String end, String ip_f_n, String op_f_n)
	{
		frame_start1 = Integer.parseInt(start);
		frame_end1 = Integer.parseInt(end);
		input_fname = ip_f_n;
		output_fname = op_f_n;
	}

	public static void main(String args[]) {
		
	}

	/*
	 * write_shot would write video_shot when start and end frame are provided
	 * The data is appended to the existing file
	 */
	public static void write_shot() {

		int frame_start = frame_start1;
		int frame_end = frame_end1;

	try {
	    File input_file = new File(input_fname);

	    InputStream input_is = new FileInputStream(input_file);

	    File output_file = new File(output_fname);
	    OutputStream output_is = new FileOutputStream(output_file, true);

	    long len = 480 * 270 * 3;
	    byte[] bytes = new byte[(int)len];
	    long offset = (long) 480 * 270 * 3 * frame_start;

	    /* 
	     * Read the input video file
	     * And skip the part till we find start of current_shot
	     */
        input_is.skip(offset);

        for (int j = 0; j < (frame_end - frame_start); j++) {
    			input_is.read(bytes, 0, bytes.length);
    			output_is.write(bytes);
    		}
    		input_is.close();
    		output_is.close();
		} catch (IOException e) {
			System.out.println(" File Exception " + e.toString());
		}
	}
}
