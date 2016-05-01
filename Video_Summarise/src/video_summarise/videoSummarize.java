package video_summarise;
/*
 * Summarizing audio and video
 * 
 * This file will read audio and video files,
 * break them into number of shots,
 * 
 * depending on heuristic they would be assigned some weight
 * 
 * And based on the percentage value provided by the user,
 * only shots with higher weight are taken as a part of summary
 * 
 */

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/*
 * record would store data related to one shot
 */
class record {
	int shot_no;
	int rank;
	int start_frame, end_frame;
	double avg_entropy, avg_audio;
	double avg_motion;
	int key_frames;
	boolean write;

	public record() {

	}
};

public class videoSummarize {

	public static String video_fileName;
	public static String audio_fileName;

	public static String trailer_vfileName;
	public static String trailer_afileName;

	public static final int height = 270, width = 480;
	public static final int numLevels = 256; // Number of levels in histogram
	public static final int group = 256 / numLevels; // Number of levels in
														// histogram
	public static final int motionThresh = 10; // Threshold for pixel motion
	public static final int keyFrameThresh = 40; // Threshold for key frame
													// determination
	public static double list_index[][];

	public static void main(String[] args) {

		BufferedImage img1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int t_prev = 0, t_now;
		int shot_no = 0, i;
		JFrame frame = new JFrame();
		ArrayList<record> shot_data = null;
		ArrayList<record> Keyshot_data = null;
		double percent_value;
		long trailer_frames;

		if (args.length < 5) {
			System.out.println(" Please pass input-output video and audio filename, percentage");
			return;
		}
		video_fileName = args[0];
		audio_fileName = args[1];
		percent_value = Double.parseDouble(args[2]);
		trailer_vfileName = args[3];
		trailer_afileName = args[4];

		try {
			File file = new File(video_fileName);
			long len = width * height * 3;
			int offset = 0;
			InputStream video_is = new FileInputStream(file);
			byte[] bytes = new byte[(int) len];

			int[] grayImg = new int[width * height];
			int[] prevGrayImg = new int[width * height];
			double[] entropy = new double[(int) (file.length() / len)];
			int[] histDiff = new int[(int) (file.length() / len)];
			int[] histRatio = new int[(int) (file.length() / len)];
			int[] hist = new int[numLevels];
			int[] prevHist = new int[numLevels];
			double[] stat = new double[3];
			double[] prevStat = new double[3];
			double[] motion = new double[(int) (file.length() / len)];
			double[] audio_level = new double[(int) (file.length() / len)];
			double[] entropy_diff = new double[(int) (file.length() / len)];
			double[] euclDist = new double[(int) (file.length() / len)];
			int keyFrames = 0;
			double motion_sum = 0;
			double entropy_sum = 0;
			double audio_sum = 0;

			histDiff[0] = 100;
			shot_data = new ArrayList<record>();
			Keyshot_data = new ArrayList<record>();
			audio_level cur_audio_level = null;
			InputStream audio_is = null;
			trailer_frames = (long) (percent_value * (file.length() / len));
			cur_audio_level = new audio_level(audio_fileName);

			record current_record = new record();
			record keyRecord = new record();

			int k;
			/*
			 * Loop for covering every frame 1st pass for calculating mean and
			 * std-deviation
			 */
			for (i = 0; i < file.length() / len; i++) {
				if (i % 1000 == 0)
					System.out.println(" First Pass: processing frame " + i + " of " + (int) (file.length() / len));

				video_is.read(bytes, 0, bytes.length);
				offset = offset + bytes.length;

				/*
				 * Initial Frame
				 */
				if (i == 0) {
					histDiff[0] = 1;
					histRatio[0] = 200;
					audio_level[0] = cur_audio_level.get_audiolevel(audio_is, i) / 100000;

					getGrayImg(bytes, grayImg);
					getHist(grayImg, hist);

					entropy[0] = getEntropy(hist);

					getStats(hist, stat);

					entropy_diff[0] = 0;
					euclDist[0] = 0;

					for (k = 0; k < numLevels; k++)
						prevHist[k] = hist[k];

					/*
					 * For next iteration, previous image = current image
					 */
					for (int ind = 0; ind < width * height; ind++)
						prevGrayImg[ind] = grayImg[ind];

					prevStat[0] = stat[0];
					prevStat[1] = stat[1];
					prevStat[2] = stat[2];

				} else {

					getGrayImg(bytes, grayImg);
					getHist(grayImg, hist);
					entropy[i] = getEntropy(hist);

					getStats(hist, stat);

					entropy_diff[i] = Math.abs(entropy[i] - entropy[i - 1]);
					int diff = 0;
					for (k = 0; k < numLevels; k++)
						diff = diff + Math.abs((hist[k] - prevHist[k]));

					histDiff[i] = diff;

					if (histDiff[i] == 0)
						histDiff[i] = 1;

					if (histDiff[i] > histDiff[i - 1])
						histRatio[i] = histDiff[i] / histDiff[i - 1];
					else
						histRatio[i] = histDiff[i - 1] / histDiff[i];

					motion[i] = getMotion(grayImg, prevGrayImg);

					audio_level[i] = cur_audio_level.get_audiolevel(audio_is, i) / 100000;

					double euclDist_val = Math.pow((prevStat[0] - stat[0]), 2) + Math.pow((prevStat[1] - stat[1]), 2)
							+ Math.pow((prevStat[2] - stat[2]), 2);
					euclDist[i] = Math.sqrt(euclDist_val);

					for (k = 0; k < numLevels; k++)
						prevHist[k] = hist[k];

					/*
					 * For next iteration, previous image = current image
					 */
					for (k = 0; k < width * height; k++)
						prevGrayImg[k] = grayImg[k];

					/*
					 * stat[0] = mean stat[1] = sigma stat[2] = third level
					 */
					prevStat[0] = stat[0];
					prevStat[1] = stat[1];
					prevStat[2] = stat[2];
				}

			}

			/*
			 * Computing mean for Entropy, Hist Ratio, Euclidian distance of
			 * color
			 */
			double mean_entropy_diff = 0, mean_histRatio = 0, mean_euclDist = 0;
			for (i = 0; i < file.length() / len; i++) {
				mean_entropy_diff = mean_entropy_diff + (entropy_diff[i] / (file.length() / len));
				mean_histRatio = mean_histRatio + (histRatio[i] / (file.length() / len));
				mean_euclDist = mean_euclDist + (euclDist[i] / (file.length() / len));
			}

			/*
			 * Loop for covering every frame Pass 2 divide into shots
			 */

			double entropy_diff_threshold = mean_entropy_diff;
			double histRatio_threshold = mean_histRatio;
			double euclDist_threshold = mean_euclDist;
			int prevKeyFrame = 0;
			for (i = 0; i < file.length() / len; i++) {
				int no_frames;
				double a_temp;

				/*
				 * Calculate number of key frames If euclidian distance is >
				 * mean_eucl_dist, call the frame as key_frame
				 */
				if (euclDist[i] > euclDist_threshold) {
					keyRecord = new record();

					keyFrames++;
					int count = 30;
					int fl=-1;
					if (prevKeyFrame != 0) {

						while (((i - count)<(prevKeyFrame))) {
							count--;
						}
						keyRecord.start_frame = i;
						fl=1;
					}
					else
					{
						keyRecord.start_frame = i;
					}
					if(fl==1 && i!=(file.length() / len ) -2)
					{
						keyRecord.end_frame = i+1;
						
					}
					else
					{
						keyRecord.end_frame = i+1;
					}
					fl=-1;
					prevKeyFrame = keyRecord.end_frame;

					keyRecord.write = true;
					Keyshot_data.add(keyRecord);
				}

				entropy_sum = entropy_sum + entropy_diff[i];
				motion_sum = motion_sum + motion[i];
				audio_sum = audio_sum + audio_level[i];

				/*
				 * Decide whether we have a new shot
				 */
				System.out.println("*****" + histRatio_threshold);
				if (entropy_diff[i] > entropy_diff_threshold && histRatio[i] > histRatio_threshold) {
					// keyFrames=0;
					current_record = new record();
					keyRecord = new record();

					if (i <= t_prev + 20 && shot_no != 0) {
						current_record = shot_data.get(shot_no - 1);
						no_frames = current_record.end_frame - current_record.start_frame;

						current_record.key_frames = current_record.key_frames + keyFrames;

						a_temp = current_record.avg_audio * no_frames;
						a_temp = (a_temp + 0.0) / (i - current_record.start_frame);

						a_temp = current_record.avg_motion * no_frames;
						a_temp = (a_temp + motion_sum) / (i - current_record.start_frame);

						current_record.end_frame = i - 1;
						keyFrames = 0;
						motion_sum = 0;
						entropy_sum = 0;
						audio_sum = 0;

						t_now = i;
						t_prev = t_now;
						shot_data.remove(shot_no - 1);
						shot_data.add(shot_no - 1, current_record);
						continue;
					}
					// This is when sht
					current_record.shot_no = shot_no;
					System.out.println(shot_no + "-- trev");
					current_record.start_frame = t_prev;
					current_record.end_frame = keyFrames;
					current_record.key_frames = keyFrames++;
					current_record.avg_audio = audio_sum / (current_record.end_frame - current_record.start_frame + 1);
					current_record.avg_entropy = entropy_sum
							/ (current_record.end_frame - current_record.start_frame + 1);
					current_record.avg_motion = motion_sum
							/ (current_record.end_frame - current_record.start_frame + 1);
					current_record.rank = 0;
					current_record.write = false;

					shot_data.add(shot_no, current_record);

					/* Reset values */
					keyFrames = 0;
					motion_sum = 0;
					entropy_sum = 0;
					audio_sum = 0;

					shot_no++;
					t_now = i;
					t_prev = t_now;

				}
			}

			/* Add the last record */
			current_record = new record();
			current_record.shot_no = shot_no;
			System.out.println(shot_no + "-- trev1");
			current_record.start_frame = t_prev;
			current_record.end_frame = i - 1;
			current_record.key_frames = keyFrames++;
			current_record.avg_audio = 0;
			current_record.avg_entropy = entropy_sum / (current_record.end_frame - current_record.start_frame + 1);
			current_record.avg_motion = motion_sum / (current_record.end_frame - current_record.start_frame + 1);

			shot_data.add(shot_no, current_record);

			double max_avg_audio = 0;
			double max_avg_entropy = 0;
			double max_avg_motion = 0;
			int max_key_frames = 0;

			for (i = 0; i < shot_data.size(); i++) {
				current_record = new record();
				current_record = shot_data.get(i);

				if (current_record.avg_audio > max_avg_audio)
					max_avg_audio = current_record.avg_audio;

				if (current_record.avg_entropy > max_avg_entropy)
					max_avg_entropy = current_record.avg_entropy;

				if (current_record.avg_motion > max_avg_motion)
					max_avg_motion = current_record.avg_motion;

				if (current_record.key_frames > max_key_frames)
					max_key_frames = current_record.key_frames;
			}

			/*
			 * Sort the values of average audio, entropy, motion and key_frames
			 */
			sort(shot_data, max_avg_audio, max_avg_entropy, max_avg_motion, max_key_frames);

			/*
			 * Set write bit of shots to be taken as a part of summary
			 */
			for (i = 0; i < shot_data.size(); i++) {
				current_record = new record();
				current_record = shot_data.get((int) list_index[i][0]);
				System.out.println(current_record.end_frame - current_record.start_frame + 1);
				System.out.println(trailer_frames);
				if ((current_record.end_frame - current_record.start_frame + 1) < trailer_frames) {
					current_record.write = true;
					trailer_frames = trailer_frames - (current_record.end_frame - current_record.start_frame + 1);
				}
				shot_data.remove((int) list_index[i][0]);
				shot_data.add((int) list_index[i][0], current_record);

				if (trailer_frames < 0)
					break;
			}

			OutputStream OutputStream1;
			/* Create a new AudioFile with name trailer_vfileName */
			OutputStream1 = new FileOutputStream(new File(trailer_vfileName), false);

			/* Create a new AudioFile with name atrailer.wav */
			OutputStream1 = new FileOutputStream(new File(trailer_afileName), false);

			FileInputStream inputStream1;
			try {
				inputStream1 = new FileInputStream(audio_fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println(" Here 0");
				return;
			}

			Write_audio wa = new Write_audio(inputStream1, audio_fileName, trailer_afileName);
			wa.write_header();

			for (i = 0; i < Keyshot_data.size(); i++) {
				current_record = new record();
				current_record = Keyshot_data.get(i);
				if (current_record.write == true) {
					//System.out.println(" Writing shot_no " + current_record.shot_no + " of " + Keyshot_data.size());
					WriteShot w = new WriteShot(Integer.toString(current_record.start_frame),
							Integer.toString(current_record.end_frame), video_fileName, trailer_vfileName);
					WriteShot.write_shot();
					wa.write_audio_shot(current_record.start_frame, current_record.end_frame);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(" Done ");
	}

	/*
	 * Function to convert each frame to gray value and store it in an array
	 */
	public static void getGrayImg(byte bytes[], int[] grayImg) {
		int r, g, b;
		int ind;
		for (ind = 0; ind < width * height; ind++) {
			r = bytes[ind] & 0xff;
			g = bytes[ind + height * width] & 0xff;
			b = bytes[ind + height * width * 2] & 0xff;
			grayImg[ind] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
		}
	}

	/*
	 * Function for calculating histogram value of each frame
	 */
	public static void getHist(int grayImg[], int hist[]) {
		int k, ind, gray;
		for (k = 0; k < numLevels; k++)
			hist[k] = 0;

		/* read the image bytes from gray array, compute the histogram */
		for (ind = 0; ind < (width * height); ind++) {
			gray = grayImg[ind];
			hist[gray / group]++;
		}
	}

	/*
	 * Function for calculating mean, sigma and s
	 */
	public static void getStats(int hist[], double stat[]) {
		int k;
		double mean = 0, sigma = 0, s = 0;

		for (k = 0; k < numLevels; k++)
			mean = mean + hist[k];

		mean = mean / (width * height);

		for (k = 0; k < numLevels; k++) {
			sigma += (hist[k] - mean) * (hist[k] - mean);
			s += (hist[k] - mean) * (hist[k] - mean) * (hist[k] - mean);
		}
		sigma = sigma / (width * height);
		sigma = Math.sqrt(sigma);
		s = s / (width * height);
		s = Math.cbrt(s);
		stat[0] = mean;
		stat[1] = sigma;
		stat[2] = s;
	}

	/*
	 * Function for calculating entropy value of each frame
	 */
	public static double getEntropy(int hist[]) {
		int k;
		double entropy = 0, p = 0;

		for (k = 0; k < numLevels; k++)
			if (hist[k] != 0) {
				p = (double) (hist[k]) / (width * height);
				entropy = entropy - p * Math.log10(p) / (Math.log10(2));
			}

		return entropy;
	}

	public static void get_img(byte bytes[], BufferedImage img) {
		int x, y, ind = 0;
		int pix;
		for (y = 0; y < height; y++) {
			for (x = 0; x < width; x++) {
				byte r = bytes[ind];
				byte g = bytes[ind + height * width];
				byte b = bytes[ind + height * width * 2];
				pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x, y, pix);
				ind++;
			}
		}
	}

	/*
	 * Function for calculating global motion value of each frame
	 */
	public static double getMotion(int[] grayImg, int[] prevGrayImg) {
		int ind;
		double frameMotion = 0;
		for (ind = 0; ind < width * height; ind++)
			if (Math.abs(grayImg[ind] - prevGrayImg[ind]) > motionThresh)
				frameMotion++;

		frameMotion = frameMotion / (width * height);

		return frameMotion;
	}

	/*
	 * Sort the records of shots
	 */
	public static void sort(ArrayList<record> shot_data, double max_avg_audio, double max_avg_entropy,
			double max_avg_motion, int max_key_frames) {
		int i, j;
		double weight;
		list_index = new double[shot_data.size()][2];
		record current_record;

		for (i = 0; i < shot_data.size(); i++) {
			list_index[i][0] = i;
			current_record = new record();
			current_record = shot_data.get(i);

			/*
			 * Assign weight to each shot including audio_value, entropy, motion
			 * and key_frames
			 */
			weight = current_record.avg_audio / max_avg_audio + current_record.avg_entropy / max_avg_entropy
					+ current_record.avg_motion / max_avg_motion
					+ (double) (current_record.key_frames) / max_key_frames;
			/*
			 * list_index[i][0] stores shot_no list_index[i][1] stores the
			 * weight of shot
			 */
			list_index[i][1] = weight;
		}

		double temp_ind, temp_weight;
		/*
		 * Sort the list using descending order of weight list_index[i][1]
		 * stores the weight of shot
		 */
		for (i = 0; i < shot_data.size(); i++) {
			for (j = 0; j < shot_data.size() - 1; j++) {
				if (list_index[j][1] < list_index[j + 1][1]) {
					temp_ind = list_index[j][0];
					temp_weight = list_index[j][1];

					list_index[j][0] = list_index[j + 1][0];
					list_index[j][1] = list_index[j + 1][1];

					list_index[j + 1][0] = temp_ind;
					list_index[j + 1][1] = temp_weight;
				}
			}
		}

		/*
		 * As per the position of shot in the sorted array, assign the rank to
		 * each shot
		 */
		for (i = 0; i < shot_data.size(); i++) {
			current_record = new record();
			current_record = shot_data.get((int) list_index[i][0]);
			current_record.rank = i;
			shot_data.set((int) list_index[i][0], current_record);
		}
	}
}

/*
 * Class for calculating audio_level for each frame
 */
class audio_level {

	/* Each frame has (918.75 * 2 samples * 8 bits) number of audio samples */
	private final int EXTERNAL_BUFFER_SIZE = 919 * 2 * 8;
	public static String audio_fileName;

	public audio_level(String f_n) {
		audio_fileName = f_n;
	}

	public double get_audiolevel(InputStream waveStream_passed, int frame_no) {
		int samplesPerFrame = 919 * 2;
		AudioInputStream audioInputStream = null;
		/*
		 * try { InputStream audio_is = new FileInputStream(audio_fileName);
		 * audioInputStream = AudioSystem.getAudioInputStream(audio_is); } catch
		 * (UnsupportedAudioFileException e1) { e1.printStackTrace(); } catch
		 * (IOException e1) { e1.printStackTrace(); }
		 */

		int i;
		byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

		try {
			audioInputStream.skip(frame_no * samplesPerFrame);
			audioInputStream.read(audioBuffer, 0, samplesPerFrame);
		} catch (Exception e) {

		}
		double audio_level = 0;
		/* Calculate audio_level for each frame */
		for (i = 0; i < audioBuffer.length; i = i + 2)
			audio_level = audio_level + Math.abs((audioBuffer[i]) + (audioBuffer[i + 1] << 8));

		return audio_level;
	}
}