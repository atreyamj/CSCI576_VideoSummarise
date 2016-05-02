/* 
 * KMeans.java ; Cluster.java ; Point.java
 *
 * Solution implemented by DataOnFocus
 * www.dataonfocus.com
 * 2015
 *
*/


import java.util.ArrayList;
import java.util.List;

public class KMeans {

	//Number of Clusters. This metric should be related to the number of points
    private int NUM_CLUSTERS = 9;    
    //Number of Points
    private int NUM_POINTS = 15;
    //Min and Max X and Y
    private static final int MIN_COORDINATE = 0;
    private static final int MAX_COORDINATE = 255;
    
    private ArrayList<Point> points;
    public ArrayList<Cluster> clusters;
    public ArrayList<Point> init_centroid_list;
    public KMeans() {
    	this.points = new ArrayList();
    	this.clusters = new ArrayList();    	
    }
    public KMeans(ArrayList<Point> histograms){
    	this.points = histograms;
    	this.clusters = new ArrayList<Cluster>();
    	
    	init();
    }
    public KMeans(ArrayList<Point> histograms, ArrayList<Point> centroid_list){
    	this.points = histograms;
    	this.init_centroid_list=centroid_list;
    	this.clusters = new ArrayList<Cluster>();
    	init2();
    }
    
    //Initializes the process
    public void init() {
    	//Create Clusters
    	//Set Random Centroids
    	for (int i = 0; i < NUM_CLUSTERS; i++) {
    		Cluster cluster = new Cluster(i);
    		Point centroid = Point.createRandomPoint(MIN_COORDINATE,MAX_COORDINATE);
    		cluster.setCentroid(centroid);
    		clusters.add(cluster);
    	}

    }
    public void init2(){
    	for (int i = 0; i < NUM_CLUSTERS; i++) {
    		Cluster cluster = new Cluster(i);
    		cluster.setCentroid(init_centroid_list.get(i));
    		clusters.add(cluster);
    	}
    }

	//The process to calculate the K Means, with iterating method.
    public ArrayList<Cluster> calculate() {
        boolean finish = false;
        int iteration = 0;
        
        // Add in new data, one at a time, recalculating centroids with each new one. 
        while(!finish) {
        	//Clear cluster state
        	clearClusters();
        	
        	ArrayList<Point> lastCentroids = getCentroids();
        	
        	//Assign points to the closer cluster
        	assignCluster();
            
            //Calculate new centroids.
        	calculateCentroids();
        	
        	iteration++;
        	
        	ArrayList<Point> currentCentroids = getCentroids();
        	
        	//Calculates total distance between new and old Centroids
        	double distance = 0;
        	for(int i = 0; i < lastCentroids.size(); i++) {
        		distance += Point.distance(lastCentroids.get(i),currentCentroids.get(i));
        	}

        	        	
        	if(distance == 0 || iteration > 15) {
        		finish = true;
        	}
        	System.out.println("Iteration number : " + iteration);
        }
        return clusters;
    }
    
    private void clearClusters() {
    	for(Cluster cluster : clusters) {
    		cluster.clear();
    	}
    }
    
    private ArrayList<Point> getCentroids() {
    	ArrayList<Point> centroids = new ArrayList<Point>(NUM_CLUSTERS);
    	for(Cluster cluster : clusters) {
    		Point aux = cluster.getCentroid();
    		Point point = new Point(aux.getR(),aux.getG(),aux.getB());
    		centroids.add(point);
    	}
    	return centroids;
    }
    
    private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min = max; 
        int cluster = 0;                 
        double distance = 0.0; 
        
        for(Point point : points) {
        	min = max;
            for(int i = 0; i < NUM_CLUSTERS; i++) {
            	Cluster c = clusters.get(i);
                distance = Point.distance(point, c.getCentroid());
                if(distance < min){
                    min = distance;
                    cluster = i;
                }
            }
            point.setCluster(cluster);
            clusters.get(cluster).addPoint(point);
        }
    }
    public void clearAll(){
    	this.points =null;
    	this.clusters=null;
    	this.init_centroid_list=null;
    }
    private void calculateCentroids() {
        for(Cluster cluster : clusters) {
            double[] sumR = new double[480*270];
            double[] sumG = new double[480*270];
            double[] sumB = new double[480*270];
            int[] cent_R = new int[480*270];
            int[] cent_G = new int[480*270];
            int[] cent_B = new int[480*270];
            ArrayList<Point> list = cluster.getPoints();
            int n_points = list.size();
            
            for(Point point : list) {
            	int[] p_r = point.getR();
            	int[] p_g = point.getG();
            	int[] p_b = point.getB();
            	
                for (int i =0; i <255; i++){
                	sumR[i]+=p_r[i];
                	sumG[i]+=p_g[i];
                	sumB[i]+=p_b[i];
                }
            }
            
            Point centroid = cluster.getCentroid();
            if(n_points > 0) {
                for (int i =0; i <255; i++){
                	sumR[i]/=n_points;
                	sumG[i]/=n_points;
                	sumB[i]/=n_points;
                	cent_R[i]=(int) sumR[i];
                	cent_G[i]=(int) sumG[i];
                	cent_B[i]=(int) sumB[i];
                }            	
                centroid.setR(cent_R);
                centroid.setG(cent_G);
                centroid.setB(cent_B);
            }
        }
    }
}