/* 
 * This class will write audio_shot when a start and end frame number is given
 * 
 * The wav file hreader is 44 bytes long
 * Header should be written only once
 * Each shot has a start_frame and end_frame
 * Everytime, the file is appended with the shot provided
 * 
 */
package video_summarise;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.swing.*;
import java.io.File.*;
import java.lang.Math.*;
import java.lang.*;

import javax.sound.*;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;


public class Write_audio {

 private InputStream waveStream;
 
 private final int EXTERNAL_BUFFER_SIZE = 919 * 2 * 8;
 private static final double MAX_16_BIT = Short.MAX_VALUE;
 private static String input_file, output_file;

 public Write_audio(InputStream waveStream, String ip_f_n, String op_f_n) {
       this.waveStream = waveStream;
       this.input_file = ip_f_n;
       this.output_file = op_f_n;       
}

public void write_header() {

    try {
            InputStream is = new FileInputStream(input_file);
            OutputStream output_is = new FileOutputStream(output_file);

            byte[] buffer_header = new byte[44];

            /* Write header of audio file */
           is.read(buffer_header);
		   output_is.write(buffer_header);
		   is.close();
		   output_is.close();
    } catch (Exception e) {
        System.out.println(" Here 6");
        System.out.println(e);
        System.exit(1);
    }
 }

public void write_audio_shot(int frame_start, int frame_end) {

       try {
               InputStream is = new FileInputStream(input_file);
               OutputStream output_is = new FileOutputStream(output_file, true);
       		
       			byte[] audioBuffer = new byte[919 *2];//this.EXTERNAL_BUFFER_SIZE];

               int i = 0;
    		   long offset = 0;
			   is = new FileInputStream(input_file);

			   /* 
			    * length of wav file header is 44
			    * Skip 44 bytes + start_frame * number_of_bytes_per_audio_frame
			    * and then write that audio shot to summary file
			    */
			   offset = offset + 44;
			   offset = offset + frame_start * audioBuffer.length;
			   is.skip(offset);

			   i = frame_start;
			   while (is.read(audioBuffer) > 0) {
				   output_is.write(audioBuffer);
				   i = i + 1;
				   /* 
				    * If we have reached last frame in the shot, stop
				    */
				   if (i >= frame_end) {
					   break;
				   }
			   }

			   is.close();
			   output_is.close();
       }
       catch (Exception e) {
           System.out.println(e);
           System.exit(1);
       }
}

 public static void main(String args[]) {
               FileInputStream inputStream;
               try {
                   inputStream = new FileInputStream(input_file);
               } catch (FileNotFoundException e) {
                   e.printStackTrace();
                   return;
               }
 	}
 }
