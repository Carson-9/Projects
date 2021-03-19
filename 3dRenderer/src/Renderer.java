import java.time.Duration;
import java.time.Instant;
import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;



public class Renderer{
	
	public static final float PI = 3.1415926535f;
	protected static float elapsedTime;
	protected static Structures.Vertex Cam;

	public static void main(String[] args) throws InterruptedException {
		
		// INIT Camera, DeltaTime, Lights, Screen, Controls, Mesh to draw.
		
		Duration deltaTime = Duration.ZERO;
		Structures.Vertex origin = new Structures.Vertex(0,0,0);
		
		//float elapsedTime = 0.01f;

		ArrayList<Structures.Vertex> lightSources = new ArrayList<Structures.Vertex>();
		lightSources.add(new Structures.Vertex(0,0,-1f));
		ArrayList<Object> World = new ArrayList<Object>();
		
		ArrayList<Structures.tri> Mesh = Window.OBJFile(System.getProperty("user.dir") + "/objects/cube.txt",0);
		Mesh = MeshUtils.meshColoring(Mesh,255,255,255);
		Object cube = new Object(Mesh,new Structures.Vertex(-3f, 0f, 5f),new float[]{0.0f,0.0f,0.0f});
		World.add(cube);
		
		ArrayList<Structures.tri> Mesh2 = Window.OBJFile(System.getProperty("user.dir") + "/objects/lamp.txt",0);
		Mesh2 = MeshUtils.meshColoring(Mesh2,255,255,255);
		Object lamp = new Object(Mesh2,new Structures.Vertex(3f, 0f, 5f),new float[]{0.0f,0.0f,0.0f});
		World.add(lamp);

		//teapot.PerFrameRotation = new float[]{0.001f,0.001f,0.001f};
		
		// Params : ArrayList of light sources , is filled, draw Wireframe, AntiAliased (smooth) lines,BgColor
		Window.Drawer Window = new Window.Drawer(lightSources,true,false,false,new Color(63, 146, 171)); 
		Cam cam = new Cam(origin,new float[]{0,0,0},Window,World,true);

		cam.pos = origin;
		while(true) {
			Instant beginTime = Instant.now();
			cam.render();
			for(Object obj:World){
				obj.update();
				obj.ParseControls(Window.Controls);
			}
			cam.CamParseControls(Window.Controls);
			cam.rotation[2] = 0;
			TimeUnit.NANOSECONDS.sleep(deltaTime.getNano()/5);
			deltaTime = Duration.between(beginTime, Instant.now());
			elapsedTime = (float)(deltaTime.getNano());
			
			}

			//Unstable FPS : 500-41
	}
	
	
	
}