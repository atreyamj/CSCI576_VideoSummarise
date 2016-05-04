import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

	private final int EXTERNAL_BUFFER_SIZE = 524;
	
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
		audioInputStream.mark(Integer.MAX_VALUE);
		try {
			while (readBytes != -1) {
				if (player.status ==1){
					readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
					if (readBytes >= 0) {
						dataLine.write(audioBuffer, 0, readBytes);
					}
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