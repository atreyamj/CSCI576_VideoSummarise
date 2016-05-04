
import java.util.ArrayList;
import java.util.List;

public class Cluster{
	
	public ArrayList<Point> points;
	public Point centroid;
	public int id;
	
	//Creates a new Cluster
	public Cluster(int id) {
		this.id = id;
		this.points = new ArrayList<Point>();
		this.centroid = null;
	}

	public ArrayList<Point> getPoints() {
		return points;
	}
	
	public void addPoint(Point point) {
		points.add(point);
	}

	public void setPoints(ArrayList<Point> points) {
		this.points = points;
	}

	public Point getCentroid() {
		return centroid;
	}

	public void setCentroid(Point centroid) {
		this.centroid = centroid;
	}

	public int getId() {
		return id;
	}
	
	public void clear() {
		points.clear();
	}
	
	public void plotCluster() {


	}

}