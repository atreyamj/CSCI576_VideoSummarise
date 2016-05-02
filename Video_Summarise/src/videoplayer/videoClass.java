import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;

public class videoClass implements Runnable {
	private AtomicBoolean keepRunning;
	String filename;
	AVPlayer player;

	public videoClass(String aFile, AVPlayer aPlayer) {
		keepRunning = new AtomicBoolean(true);
		filename = aFile;
		player = aPlayer;
	}

	public void stop() {
		keepRunning.set(false);
	}

	@Override
	public void run() {
		try {
			File file = new File(filename);
			InputStream is = new FileInputStream(file);
			int width = 480;
			int height = 270;
			// long len = file.length();
			long len = width * height * 3;
			byte[] bytes = new byte[(int) len];
			int offset = 0;
			int numRead = 388800;
			int flag = 0;
			
			while (numRead != -1) {
				if(player.status==1){
					//System.out.println("running");
					offset = 0;
					long startTime = System.nanoTime(); 
					
					numRead = is.read(bytes, offset, bytes.length - offset);
					BufferedImage aNewImage = newImg(bytes);
					player.lbIm1.setIcon(new ImageIcon(aNewImage));
					
					long endTime = System.nanoTime();
					long difference = endTime-startTime;
					long waitseconds = 66666666 - difference;
					//System.out.println(difference);
					try {
						TimeUnit.NANOSECONDS.sleep(waitseconds);
					} catch (InterruptedException e) {
						// 
						e.printStackTrace();
					}
					flag=1;
				} else if (player.status==0){
					Thread.sleep(1);
				} else{
//					System.out.println("resetting");
					if (flag == 1){
						//System.out.println("flagged");
						is.close();
						is = new FileInputStream(file);
						flag=0;
					}
					Thread.sleep(1);
				}
			}
			is.close();

		} catch (Exception e) {

		}
	}

	private BufferedImage newImg(byte[] bytes) {
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