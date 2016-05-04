
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Point implements Comparable<Point>{

    public int[] r_hist;
    public int[] g_hist;
    public int[] b_hist;
    public int frameNumber;
    public int cluster_number = 0;
    public Point centroidCompare;
    
    public Point(int[] rVals, int[] gVals, int[] bVals)
    {
        r_hist=rVals;
        g_hist=gVals;
        b_hist=bVals;
    }
    
    public void setR(int[] x) {
        this.r_hist = x;
    }
    
    public int[] getR()  {
        return this.r_hist;
    }
    
    public void setG(int[] x) {
        this.g_hist = x;
    }
    
    public int[] getG()  {
        return this.g_hist;
    }
    public void setB(int[] x) {
        this.b_hist = x;
    }
    
    public int[] getB()  {
        return this.b_hist;
    }
    
    public void setCluster(int n) {
        this.cluster_number = n;
    }
    
    public int getCluster() {
        return this.cluster_number;
    }
    
    //Calculates the distance between two points.
    protected static double distance(Point p, Point centroid) {
    	double sum = 0;
    	for (int i =0; i < p.b_hist.length; i++){
    		if (i==256){
    			int x =6;
    			
    		}
    		double rs = Math.pow(p.r_hist[i]-centroid.r_hist[i],2);
    		double gs = Math.pow(p.g_hist[i]-centroid.g_hist[i],2);
    		double bs = Math.pow(p.b_hist[i]-centroid.b_hist[i],2);
    		sum=sum+rs+gs+bs;
    	}
    	sum=Math.sqrt(sum);
        return sum;
    }
    
    //Creates random point
    protected static Point createRandomPoint(int min, int max) {
    	Random r = new Random();
    	int[] r_rand = new int[480*270];
    	int[] g_rand = new int[480*270];
    	int[] b_rand = new int[480*270];
    	for (int i =0; i < r_rand.length; i++){
    		r_rand[i]=r.nextInt(255);
    		g_rand[i]=r.nextInt(255);
    		b_rand[i]=r.nextInt(255);
    	}
    	return new Point(r_rand,g_rand,b_rand);
    }
    
    protected static ArrayList<Point> createRandomPoints(int min, int max, int number) {
    	ArrayList<Point> points = new ArrayList<Point>(number);
    	for(int i = 0; i < number; i++) {
    		points.add(createRandomPoint(min,max));
    	}
    	return points;
    }
    
    public String toString() {
    	return null;
    }

	@Override
	public int compareTo(Point o) {
		// TODO Auto-generated method stub
		int distToCentroid = (int) distance(this, centroidCompare);
		int oToCentroid = (int) distance(o,centroidCompare);
		return distToCentroid-oToCentroid;
		
	}
}