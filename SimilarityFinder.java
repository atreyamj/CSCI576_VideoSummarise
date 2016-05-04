

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public  class SimilarityFinder extends JFrame {
	
  private Color[][] signature;

  private static final int baseSize = 200;

  private static final String basePath = "C:\\Users\\sanjana\\workspace\\VideoSummarise\\src\\altframes";

  
  
public static  double[] distances;
public static File[] others;
  
 /*
  * The constructor, which creates the GUI and start the image processing task.
  */
  public SimilarityFinder(File reference) throws IOException {
    super("");
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    System.out.println("ref"+reference);
    BufferedImage ig=setRGBValues(reference);
  // BufferedImage ig= ImageIO.read(reference);
    RenderedImage ref = rescale(ig);
    cp.add(new DisplayJAI(ref), BorderLayout.WEST);
    signature = calcSignature(ref);
    System.out.println("Sig"+signature);
    others = getOtherImageFiles(reference);
    JPanel otherPanel = new JPanel(new GridLayout(others.length, 2));
    cp.add(new JScrollPane(otherPanel), BorderLayout.CENTER);
    RenderedImage[] rothers = new RenderedImage[others.length];
    distances= new double[others.length];
    for (int o = 0; o < others.length; o++) {
      //  System.out.println("others"+others[o]);
        // BufferedImage ig1=setRGBValuesothers(others[o]);
        BufferedImage ig1= ImageIO.read(others[o]);
      rothers[o] = rescale(ig1);
      distances[o] = calcDistance(rothers[o]);
    }
    for (int p1 = 0; p1 < others.length - 1; p1++)
      for (int p2 = p1 + 1; p2 < others.length; p2++) {
        if (distances[p1] > distances[p2]) {
          double tempDist = distances[p1];
          distances[p1] = distances[p2];
          distances[p2] = tempDist;
          RenderedImage tempR = rothers[p1];
          rothers[p1] = rothers[p2];
          rothers[p2] = tempR;
          File tempF = others[p1];
          others[p1] = others[p2];
          others[p2] = tempF;
          }
        }
    for (int o = 0; o < others.length; o++)
      {
      otherPanel.add(new DisplayJAI(rothers[o]));
      JLabel ldist = new JLabel("<html>" + others[o].getName() + "<br>"
          + String.format("% 13.3f", distances[o]) + "</html>");
      ldist.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 36));
      System.out.printf("<td class=\"simpletable legend\"> "+
          "<img src=\"MiscResources/ImageSimilarity/icons/miniicon_%s\" "+
          "alt=\"Similarity result\"><br>% 13.3f</td>\n", others[o].getName(),distances[o]);
      System.out.println("distances"+distances[o]);
      otherPanel.add(ldist);
      }
    // More GUI details.
    pack();
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
 
 /*
  * This method rescales an image to 300,300 pixels 
  * using the JAI scale operator.
  */
  static BufferedImage rescale(BufferedImage i) {
	BufferedImage scaledImage = new BufferedImage(baseSize, baseSize, BufferedImage.TYPE_INT_RGB);
	Graphics2D graphics2D = scaledImage.createGraphics();
	graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	graphics2D.drawImage(i, 0, 0, baseSize, baseSize, null);
	graphics2D.dispose();
	return scaledImage;

    }
  
 /*
  * This method calculates and returns signature vectors for the input image.
  */
  static Color[][] calcSignature(RenderedImage i)
    {
    // Get memory for the signature.
    Color[][] sig = new Color[5][5];
    // For each of the 25 signature values average the pixels around it.
    // Note that the coordinate of the central pixel is in proportions.
    float[] prop = new float[]
      {1f / 10f, 3f / 10f, 5f / 10f, 7f / 10f, 9f / 10f};
    for (int x = 0; x < 5; x++)
      for (int y = 0; y < 5; y++)
        sig[x][y] = averageAround(i, prop[x], prop[y]);
    return sig;
    }
 
 /*
  * This method averages the pixel values around a central point and return the
  * average as an instance of Color. The point coordinates are proportional to
  * the image.
  */
  static Color averageAround(RenderedImage i, double px, double py)
    {
        
        
    // Get an iterator for the image.
    //RandomIter iterator = RandomIterFactory.create(i, null);
         
    // Get memory for a pixel and for the accumulator.
    double[] pixel = new double[3];
    double[] accum = new double[3];
    // The size of the sampling area.
    int sampleSize = 20;
    int numPixels = 0;
    // Sample the pixels.
    for (double x = px * baseSize - sampleSize; x < px * baseSize + sampleSize; x++)
      {
      for (double y = py * baseSize - sampleSize; y < py * baseSize + sampleSize; y++)
        {
            i.getData().getPixel((int) x, (int) y, pixel);
        //iterator.getPixel((int) x, (int) y, pixel);
        accum[0] += pixel[0];
        accum[1] += pixel[1];
        accum[2] += pixel[2];
        numPixels++;
        }
      }
    // Average the accumulated values.
    accum[0] /= numPixels;
    accum[1] /= numPixels;
    accum[2] /= numPixels;
    return new Color((int) accum[0], (int) accum[1], (int) accum[2]);
    }
 
 /*
  * This method calculates the distance between the signatures of an image and
  * the reference one. The signatures for the image passed as the parameter are
  * calculated inside the method.
  */
  double calcDistance(RenderedImage other)
    {
    // Calculate the signature for that image.
    Color[][] sigOther = calcSignature(other);
    // There are several ways to calculate distances between two vectors,
    // we will calculate the sum of the distances between the RGB values of
    // pixels in the same positions.
    double dist = 0;
    for (int x = 0; x < 5; x++)
      for (int y = 0; y < 5; y++)
        {
        int r1 = signature[x][y].getRed();
       
        int g1 = signature[x][y].getGreen();
        int b1 = signature[x][y].getBlue();
        int r2 = sigOther[x][y].getRed();
        int g2 = sigOther[x][y].getGreen();
        int b2 = sigOther[x][y].getBlue();
        double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2)
            * (g1 - g2) + (b1 - b2) * (b1 - b2));
        dist += tempDist;
        }
    return dist;
    }
public static   BufferedImage setRGBValues(File file )
    {
        BufferedImage img;
      int width = 1280;
		int height =720;

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
			//File file = new File("C:\\Users\\sanjana\\Documents\\NetBeansProjects\\ImageReader\\src\\imagereader\\16192.rgb");
			InputStream is = new FileInputStream(file);

			long len = file.length();
			byte[] bytes = new byte[(int)len];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}


			int ind = 0;
			for(int y = 0; y < height; y++){

				for(int x = 0; x < width; x++){

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
                return img;
    }
public static   BufferedImage setRGBValuesothers(File file )
    {
       BufferedImage img =null;
       BufferedImage img1;

		try {
			//File file = new File(Filename);
			InputStream is = new FileInputStream(file);
                        
			long len = file.length();
			byte[] bytes = new byte[(int)len];
            img1= ImageIO.read(file);
                            int    height=512;
                            int width=512;       
                            
 img = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}


			int ind = 0;
			for(int y = 0; y < height; y++){

				for(int x = 0; x < width; x++){

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}

                  
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
return img;
    }
 /*
  * This method get all image files in the same directory as the reference.
  * Just for kicks include also the reference image.
  */
  File[] getOtherImageFiles(File reference)
    {
    File dir = new File(reference.getParent());
    // List all the image files in that directory.
    File[] others = dir.listFiles();
    return others;
    }
 
  public static void main(String[] args) throws IOException {
  
   
   String fn="C:\\Users\\sanjana\\workspace\\VideoSummarise\\src\\altframes\\16192.rgb";
     File file = new File(fn);
      
      new SimilarityFinder(file);
  }
  
};

class DisplayJAI extends JPanel {

    /** image to display */
    protected RenderedImage source = null;

    /** image origin relative to panel origin */
    protected int originX = 0;
    protected int originY = 0;


    /** default constructor */
    public DisplayJAI() {
        super();
        setLayout(null);
    }

    /** constructor with given image */
    public DisplayJAI(RenderedImage image) {
        super();
        setLayout(null);
        source = image;
        setPreferredSize(new Dimension(source.getWidth(),
                                       source.getHeight()));
    }

    /** move image within it's container */
    public void setOrigin(int x, int y) {
        originX = x;
        originY = y;
        repaint();
    }

    /** get the image origin */
    public Point getOrigin() {
        return new Point(originX, originY);
    }

    /** use to display a new image */
    public void setImage(RenderedImage im) {
        source = im;
        repaint();
    }

    /** @returns the Image */
    public RenderedImage getImage() {
        return source;
    }

    /** paint routine */
    public synchronized void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D)g;

        // empty component (no image)
        if ( source == null ) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        // account for borders
        Insets insets = getInsets();
        int tx = insets.left + originX;
        int ty = insets.top  + originY;

        // clear damaged component area
        Rectangle clipBounds = g2d.getClipBounds();
        g2d.setColor(getBackground());
        g2d.fillRect(clipBounds.x,
                     clipBounds.y,
                     clipBounds.width,
                     clipBounds.height);

        /**
            Translation moves the entire image within the container
        */
        g2d.drawRenderedImage(source,
                              AffineTransform.getTranslateInstance(tx, ty));
    }
}
