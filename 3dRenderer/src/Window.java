import java.awt.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.*;

public class Window{

	
	public static class Drawer extends JPanel{
		public ArrayList<Integer> Controls;
		private static final long serialVersionUID = -5930096611050347846L;
		private ArrayList<Structures.Tri> World;
		Dimension screen;
		JFrame win;
		boolean fill;
		boolean drawWireframe;
		boolean paused = false;
		boolean AAlines;
		Color bgcolor;
		ArrayList<Structures.Vertex> lightSources;
		
		public Drawer(ArrayList<Structures.Vertex> lightSources,boolean fill,boolean drawWireframe, boolean AAlines, Color bgcolor) {
			this.Controls = new ArrayList<Integer>();
			this.win = new JFrame("3D Engine");
			this.win.setSize(800,800);
			Dimension screen = this.win.getSize();
			//Dimension Gscreen = Toolkit.getDefaultToolkit().getScreenSize();
			this.win.setLocationRelativeTo(null);
			this.screen = screen;
			this.win.setVisible(true);
			this.win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.win.add(this);
			this.Redraw(this.World);
			this.fill = fill;
			this.lightSources = lightSources;
			this.drawWireframe = drawWireframe;
			this.bgcolor = bgcolor;
			this.paused = false;
			this.AAlines = AAlines;

			this.win.addKeyListener(new KeyListener(){
				public void keyTyped(KeyEvent arg0){
				}
				public void keyPressed(KeyEvent arg1){
					if (!Controls.contains(arg1.getKeyCode()))
					Controls.add(arg1.getKeyCode());
				}
				public void keyReleased(KeyEvent arg2){
					Controls.remove(Controls.indexOf(arg2.getKeyCode()));
				}
			});
		}
		
		
		public void paint(Graphics g, ArrayList<Structures.Tri> Mesh) {
			//Transparent Mouse
			BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					cursorImg, new Point(0, 0), "blank cursor");
			this.win.getContentPane().setCursor(blankCursor);
			
			super.paintComponent(g);
			this.setBackground(bgcolor);
			Graphics2D g2 = (Graphics2D) g;
			if (this.AAlines) g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setStroke(new BasicStroke(1.0f));

			for(Structures.Tri triangle:Mesh){

				if (this.fill) g2.setColor(Color.BLACK);	
				else g2.setColor(Color.WHITE);

				int[] XCoords = {(int)triangle.a.x,(int)triangle.b.x,(int)triangle.c.x};
				int[] YCoords = {(int)triangle.a.y,(int)triangle.b.y,(int)triangle.c.y};
				Polygon tri = new Polygon(XCoords,YCoords,3);
				if (this.drawWireframe) {
					g2.draw(new Line2D.Float(triangle.a.x,triangle.a.y,triangle.b.x,triangle.b.y));
					g2.draw(new Line2D.Float(triangle.b.x,triangle.b.y,triangle.c.x,triangle.c.y));
					g2.draw(new Line2D.Float(triangle.c.x,triangle.c.y,triangle.a.x,triangle.a.y));
				}
				if(this.fill) { 
					Structures.Vertex triNormal = Structures.Vector_Normalize(triangle.Normal);
					float biggestIllumination = 0.0f;
					for(Structures.Vertex light:lightSources) {
						Structures.Vertex RelativeLight = Structures.Vector_Sub(light,triangle.WorldPos);
						Structures.Vertex Zlight = Structures.Vector_Normalize(RelativeLight);
						float Percentage = Structures.Vector_Dot(triNormal,Zlight);
						if(Percentage > biggestIllumination) {
							biggestIllumination = Percentage;
						}	
					}
					int newR = (int) (triangle.color.getRed() * biggestIllumination);
					int newG = (int) (triangle.color.getGreen() * biggestIllumination);
					int newB = (int) (triangle.color.getBlue() * biggestIllumination);
					g2.setColor(new Color(newR,newG,newB));
					g2.fillPolygon(tri);
				}
			}
				
		}
		
		public void Redraw(ArrayList<Structures.Tri> WorldMesh) {
			this.World = WorldMesh; //Not updating ?!?!?!!?
			//System.out.println(this.World + " " +WorldMesh);
			this.repaint();
			this.screen = this.win.getSize();
		}
	}
	
	
	
	public static Structures.Tri Scale(Structures.Tri tris,Drawer dim,boolean isStretched){
			
		float aspectRatio = (float)dim.getWidth() / (float)dim.getHeight(); 

		tris.a = Structures.Vector_Add(tris.a, new Structures.Vertex(1.0f,1.0f,0.0f));
		tris.b = Structures.Vector_Add(tris.b, new Structures.Vertex(1.0f,1.0f,0.0f));
		tris.c = Structures.Vector_Add(tris.c, new Structures.Vertex(1.0f,1.0f,0.0f));
		    
		tris.a.x *= 0.5f * (float)dim.getHeight();
		tris.a.y *= 0.5f * (float)dim.getHeight();
		tris.b.x *= 0.5f * (float)dim.getHeight();
		tris.b.y *= 0.5f * (float)dim.getHeight();
		tris.c.x *= 0.5f * (float)dim.getHeight();
		tris.c.y *= 0.5f * (float)dim.getHeight();
		  	
		if (isStretched) {
			tris.a.x *= aspectRatio;
		  	tris.b.x *= aspectRatio;
		  	tris.c.x *= aspectRatio;
			}
		return new Structures.Tri(tris.a,tris.b,tris.c);
		}
	
	public static ArrayList<Structures.Tri> OBJFile(String file,float Zoff) {
		try {
		Scanner a = new Scanner(new File(file));
		ArrayList<Structures.Vertex> Vertexs = new ArrayList<Structures.Vertex>();
		ArrayList<Structures.Tri> mesh = new ArrayList<Structures.Tri>();
		ArrayList<Structures.Vertex> Normals = new ArrayList<Structures.Vertex>();
		while (a.hasNextLine()) {
			String line = a.nextLine();
			if (line.isEmpty()) {
				
			}
			else {
			if (line.indexOf("  ") != -1) {
				line = line.replaceAll("  ", " ");
			}
			
			if (line.charAt(0) == 'v') {
				String[] sub = line.split(" ");
				if (sub[0].equals("v")) {
					Structures.Vertex vert = new Structures.Vertex(0,0,0);
					vert.x = Float.parseFloat(sub[1]);
					vert.y = Float.parseFloat(sub[2]);
					vert.z = Float.parseFloat(sub[3]);
					Vertexs.add(vert);	
					}
				else if (sub[0].equals("vn")){
					Structures.Vertex vert = new Structures.Vertex(0,0,0);
					vert.x = Float.parseFloat(sub[1]);
					vert.y = Float.parseFloat(sub[2]);
					vert.z = Float.parseFloat(sub[3]);
					Normals.add(vert);	
				}
				}	
			if (line.charAt(0) == 'f') {
				String[] sub = line.split(" ");
				if(! sub[1].contains("/")) {
					for(int i =2; i < sub.length-1;i++) {
						mesh.add(new Structures.Tri(Vertexs.get(Integer.parseInt(sub[1])-1),Vertexs.get(Integer.parseInt(sub[i])-1),Vertexs.get(Integer.parseInt(sub[i+1])-1)));
						}
					}
				else if(sub[1].contains("//")) {
					String[] insub1 = sub[1].split("//"); String[] insub2 = sub[2].split("//"); String[] insub3 = sub[3].split("//"); 
					int vert1 = Integer.parseInt(insub1[0]);
					int vert2 = Integer.parseInt(insub2[0]);
					int vert3 = Integer.parseInt(insub3[0]);
					mesh.add(new Structures.Tri(Vertexs.get(vert1-1),Vertexs.get(vert2-1),Vertexs.get(vert3-1)));
					}
				}
			}
		}
		/*Collections.sort(mesh, new Comparator<tri>(){
			@Override
			public int compare(tri tri1, Structures.Tri tri2) {
				// tr1 should be in first : returns -1 | else : returns 1
				return (int)(((tri2.a.z + tri2.b.z + tri2.c.z) - (tri1.a.z + tri1.b.z + tri1.c.z))/Math.abs((tri2.a.z + tri2.b.z + tri2.c.z) - (tri1.a.z + tri1.b.z + tri1.c.z)));
				}
			});
		if (Structures.Vector_Dot(mesh.get(0).a,new Structures.Vertex(0,0,0))<0.0f) {
			mesh = MeshInvertNormal(mesh);
		}*/
		return mesh;
		}
		catch(FileNotFoundException e) {
			System.out.println("File : " + file + " not found");
			return new ArrayList<Structures.Tri>();
		}
	}
}

