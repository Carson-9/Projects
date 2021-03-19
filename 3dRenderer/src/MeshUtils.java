import java.awt.Color;
import java.util.ArrayList;


public class MeshUtils extends Structures{

	public static ArrayList<tri> meshFusion(ArrayList<ArrayList<tri>> meshList){
		ArrayList<tri> finalMesh = new ArrayList<tri>();
		for (ArrayList<tri> mesh : meshList) {
			for (tri tri : mesh) {
				finalMesh.add(tri);
				}
		}
		return finalMesh;
	}
	
	public static ArrayList<tri> meshOffset(ArrayList<tri> mesh,float xOffset,float yOffset,float zOffset){
		ArrayList<tri> newMesh = new ArrayList<tri>();
		Vertex offset = new Vertex(xOffset,yOffset,zOffset);
		for (tri tri : mesh) {
			tri.a = Vector_Add(tri.a,offset);
			tri.b = Vector_Add(tri.b,offset);
			tri.c = Vector_Add(tri.c,offset);
			newMesh.add(tri);
		}
		return newMesh;
	}
	
	public static ArrayList<tri> meshColoring (ArrayList<tri> mesh,int red,int green,int blue){
		Color color = new Color(red,green,blue);
		ArrayList<tri> fMesh = new ArrayList<tri>();
		for(tri tri : mesh) {
			tri.setColor(color);
			fMesh.add(tri);
		}
		return fMesh;
	}
	
	/*public static ArrayList<tri> generateTerrain(int width, int height){
		float leftCoords = width/2;
		float depth = -height/2;
		ArrayList<tri> Mesh = new ArrayList<tri>();
		for(depth,  depth < height/2, depth+1) {
			for(int x = -width/2, x < width/2, x+1) {
				Mesh.add(new tri(new Vertex()))
			}
		}
	}*/

	public static ArrayList<tri> Subdivide(ArrayList<tri> mesh){
		ArrayList<tri> newMesh = new ArrayList<tri>();
		for(tri tri : mesh){
			Vertex midab = Vector_Midpoint(tri.a,tri.b);
			Vertex midac = Vector_Midpoint(tri.a,tri.c);
			Vertex midbc = Vector_Midpoint(tri.b,tri.c);
			newMesh.add(new tri(tri.a,midab,midac));
			newMesh.add(new tri(midab,tri.b,midbc));
			newMesh.add(new tri(midac,midbc,tri.c));
			newMesh.add(new tri(midab,midbc,midac));
		}
		return newMesh;
	}
	public static ArrayList<tri> MeshInvertNormal(ArrayList<tri> mesh){
		for (tri tri : mesh) {
			tri.Normal = Vector_Mult(tri.Normal,-1f);
		}
		return mesh;
	}
}
