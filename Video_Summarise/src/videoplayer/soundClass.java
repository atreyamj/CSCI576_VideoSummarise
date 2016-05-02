import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
public class soundClass implements Runnable{
	private AtomicBoolean keepRunning;
	String filename;
	AVPlayer player;
	InputStream waveStream;
	InputStream fileIO;
	
	private final int EXTERNAL_BUFFER_SIZE = 8192;
	
	public soundClass(String aFile, AVPlayer aPlayer) {
        keepRunning = new AtomicBoolean(true);
        filename = aFile;
        player = aPlayer;
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
        this.waveStream = new BufferedInputStream(inputStream);
    }
	
	@Override
	public void run() {


		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
		} catch (UnsupportedAudioFileException e1) {
			
		} catch (IOException e1) {
			
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
			
		}

		// Starts the music :P
		dataLine.start();
		int readBytes = 0;
		byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
//		audioInputStream.mark(Integer.MAX_VALUE);
//		File input_audio = new File(filename);
//		InputStream audio_input_is = null;
//		byte[] allAudio = null;
//	    try {
//			audio_input_is = new FileInputStream(input_audio);
//		
//		 allAudio = new byte[(int) input_audio.length()];
//		 audio_input_is.read(allAudio, 0, (int) input_audio.length());
//	    } catch (Exception e2) {
//			e2.printStackTrace();
//		}
		try {
			int i = 44;
			while (readBytes != -1) {
				if (player.status ==1){
					long startTime = System.nanoTime();
					readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
					//audioBuffer = Arrays.copyOfRange(allAudio, i, i+audioBuffer.length);
					if (readBytes >= 0) {
						dataLine.write(audioBuffer, 0, readBytes);
						//i+=audioBuffer.length;
					}
					long endTime = System.nanoTime();
					long totalTime = endTime-startTime;
					//System.out.println(totalTime);
				} else if (player.status == 0){
					Thread.sleep(1);
				} else {
					audioInputStream.reset();
					Thread.sleep(1);
				}
			}
		} catch (IOException e1) {
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// plays what's left and and closes the audioChannel
			dataLine.drain();
			dataLine.close();
		}

	}
}