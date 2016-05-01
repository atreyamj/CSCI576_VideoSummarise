/*
 * AV player
 * playing wav Audio file and raw Video in sync
 * 
 * The input video and audio filenames are provided as argument
 * audio class in a child of video class 
 */

package videoPlayback;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.swing.*;
import java.io.File.*;
import java.lang.Math.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

public class videoPlayback {

	public static int height, width;
	public static String videofileName;
	public static String audioFileName;
	public static long current_audio_frame;
	public static long audio_start_time;

public static void main(String[] args) {

	videofileName = "abc.rgb";
	audioFileName = "abc.wav";
	/*if (args.length < 2) {
		System.out.println(" Please pass video and audio filename ");
		return;
	}*/
	current_audio_frame = 0;
	videofileName = "abc.rgb";
	audioFileName = "abc.wav";

	/*
	 * Thread for video playback
	 */
	final Runnable r1 = new Runnable() {
		  public void run() {
			  int i;

			  width = Integer.parseInt("480");
			  height = Integer.parseInt("270");
	
			  BufferedImage img1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			  JFrame frame = new JFrame();

			  try {
				  File file = new File(videofileName);
				  
				  /* 
				   * Size of each frame is 240 * 320 * 3 (1 byte each for R, G, B)
				   */
				  long len = 270 * 480 * 3;
				  byte[] bytes = new byte[(int)len];
				  int offset = 0;
				  InputStream is = new FileInputStream(file);
				  long start, stop;
				  /*
				   * The video framerate is 24 fps
				   * means around 1000 / 24 = 41.67
				   */
				  long delay_val = 67;
				  bytes = new byte[(int)len];

				  /*
				   * Display First frame
				   */
				  is.read(bytes, 0, bytes.length);
				  offset = offset + bytes.length;

				  get_img(bytes, img1);
				  start = System.currentTimeMillis();
				  JLabel label = new JLabel(new ImageIcon(img1));

				  frame.getContentPane().add(label, BorderLayout.WEST);
				  frame.pack();
				  frame.setVisible(true);
				  stop = System.currentTimeMillis();
					FileInputStream inputStream;
					try {
						inputStream = new FileInputStream(audioFileName);
					} catch (FileNotFoundException e) {
					    e.printStackTrace();
					    return;
					}

				/*
				 * Wait for 41 milliseconds before displaying next frame 
				 */
				  while ((stop - start) < delay_val) {
					  stop = System.currentTimeMillis();
				  }

				  int current_video_frame = 1;
				  long current_expected_frame;
				  for (i = 1; i < file.length() / bytes.length; i++) {
		              start = System.currentTimeMillis();

                      current_expected_frame = (long) ((start - audio_start_time) / 66.67);

                      /*
                       * current_video_frame is not equal to current_expected_frame
                       * if video is lagging, drop (skip) some frames to compensate the lag
                       * 
                       *  if video is leading over audio, wait to compensate the lag
                       */
                      if (current_video_frame != current_expected_frame) {
                    	  
                    	  /* video is lagging so skip some frames */
                    	  if (current_video_frame < current_expected_frame)
                    		  is.skip((current_expected_frame - current_video_frame) * bytes.length);
                    	  
                    	  /* video is leading so wait till the lag is compensated by audio */
                    	  else if (current_video_frame > current_expected_frame) {
                    		  long time;
                    		  time = System.currentTimeMillis();
                    		  while (current_expected_frame < current_video_frame) {
                    			  time = System.currentTimeMillis();
                    			  current_expected_frame = (long) ((time - audio_start_time) / 41.67);
                    		  }
                    	  }
                          current_video_frame = (int) current_expected_frame;
                      }
                      
                      /*
                       * Read current frame
                       */
                      is.read(bytes, 0, bytes.length);
					  offset = offset + bytes.length;

					  get_img(bytes, img1);
					  label.setIcon(new ImageIcon(img1));
					  current_video_frame++;

					  stop = System.currentTimeMillis();
					  /*
					   * As the video fps = 24 and actual delay value is 41.67 and we are considering 41,
					   * we have to wait 2 milliseconds extra for every 3rd frame
					   */
					  if (i % 3 == 0) {
						  while ((stop - start) < (delay_val + 2)) {
							  stop = System.currentTimeMillis();
						  }
					  }	else {
						  while ((stop - start) < delay_val) {
								  stop = System.currentTimeMillis();
							  }					  		
					  }
				  }
			  } catch (FileNotFoundException e) {
				  e.printStackTrace();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
		  }
	};
	
	/*
	 * Runnable interface for the audio thread
	 */
	Runnable r2 = new Runnable() {
	  public void run() {
					FileInputStream inputStream;
					try {
						inputStream = new FileInputStream(audioFileName);
					} catch (FileNotFoundException e) {
					    e.printStackTrace();
					    return;
					}
				 
					PlaySound playSound = new PlaySound(audioFileName);

					/* 
					 * Start audio playback
					 */
					try {
						playSound.play();
					} catch (PlayWaveException e) {
					    e.printStackTrace();
						return;
					}
			}
	}; 

	Thread thr1 = new Thread(r1);
	Thread thr2 = new Thread(r2);

	/* 
	 * Start Audio and Video thread
	 */
	thr2.start();
	thr1.start();
	}

	public static void get_img(byte bytes[], BufferedImage img)
	{
		int x, y, ind = 0;
		int pix;
		for(y = 0; y < height; y++){
			for(x = 0; x < width; x++){
				byte r = bytes[ind];
				byte g = bytes[ind + height * width];
				byte b = bytes[ind + height * width * 2];
				pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x, y, pix);
				ind++;
				}
			}
	}
}

/*
 * class for playing audio
 * extending video 
 */
class PlaySound extends videoPlayback {

  private String waveStream;
  public long current_audio_frame;
  /*
   * Buffer_size = 918.75 (samples per frame * 2 channel audio) * 8 
   */
  
  private final int EXTERNAL_BUFFER_SIZE = (int)918.75 * 2 * 8;

  public PlaySound(String waveStream) {
	this.waveStream = waveStream;
  }

  public void play() throws PlayWaveException {

	AudioInputStream audioInputStream = null;
	try {
		audioInputStream = AudioSystem.getAudioInputStream(new File(this.waveStream).toURI().toURL());
	} catch (UnsupportedAudioFileException e1) {
	    throw new PlayWaveException(e1);
	} catch (IOException e1) {
	    throw new PlayWaveException(e1);
	}
	// Obtain the information about the AudioInputStream
	AudioFormat audioFormat = audioInputStream.getFormat();
	Info info = new Info(SourceDataLine.class, audioFormat);
	// opens the audio channel
	SourceDataLine dataLine = null;
	try {
	    dataLine = (SourceDataLine) AudioSystem.getLine(info);
	    dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
	} catch (LineUnavailableException e1) {
	    throw new PlayWaveException(e1);
	}

	/*
	 *  Starts the music :P
	 */
	dataLine.start();

	this.current_audio_frame = 0;
	int readBytes = 0;
	byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
	try {
		/*
		 *  Set the audio_start_time for its use in video thread
		 */
		super.audio_start_time = System.currentTimeMillis();
	    while (readBytes != -1) {
		readBytes = audioInputStream.read(audioBuffer, 0,
			audioBuffer.length);

			if (readBytes >= 0){
		    	dataLine.write(audioBuffer, 0, readBytes);
			}
	    }    
	} catch (IOException e1) {
	    throw new PlayWaveException(e1);
	} finally {
	    /*
	     *  plays what's left and and closes the audioChannel
	     */
	    dataLine.drain();
	    dataLine.close();
	}
  }
}
