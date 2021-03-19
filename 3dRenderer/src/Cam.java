import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.AWTException;
import java.awt.Robot;

public class Cam extends Object {

    Window.Drawer WindowD;
    ArrayList<Object> World = new ArrayList<Object>();
    ArrayList<Structures.tri> WorldMesh = new ArrayList<Structures.tri>();

    Structures.Matrix ProjectionMatrix = new Structures.Matrix();

    Structures.Vertex Up = new Structures.Vertex(0,1,0);
    Structures.Vertex lookDir = new Structures.Vertex(0,0,1);
    Structures.Vertex target = new Structures.Vertex(0,0,0); //Target must be absolute - cam.pos

    public static final float PI = 3.141592653589f;
    public float PitchLimit = 1.56f; // ~ PI / 2
    boolean stretched;

    public Cam(Structures.Vertex pos, float[] rotation,Window.Drawer Win,ArrayList<Object> World, boolean isStretched){
        super(new ArrayList<Structures.tri>(),pos,rotation);
        WindowD = Win;
        this.World= World;
        ProjectionMatrix.makeProjection(this.WindowD.screen);
        this.stretched = isStretched;

        this.WindowD.win.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent evt) {
            }
            public void mouseMoved(MouseEvent evt) {
                if (!WindowD.paused){
                    int absWinCenterX = (int)WindowD.getLocationOnScreen().getX() + WindowD.screen.width/2;
                    int absWinCenterY = (int)WindowD.getLocationOnScreen().getY() + WindowD.screen.height/2;
                    try {
                        Robot rob = new Robot();
                        rotation[1] += (float) (evt.getXOnScreen() - absWinCenterX ) / 1000;
                        rotation[0] += (float) (evt.getYOnScreen() - absWinCenterY ) / 1000;
                        if (rotation[0] > PitchLimit) rotation[0] = PitchLimit;
                        else if (rotation[0] < -PitchLimit) rotation[0] = -PitchLimit;
                        rob.mouseMove(absWinCenterX,absWinCenterY);
                    }
                    catch (AWTException e) {
                        e.printStackTrace();
                    }
                }
            }	
        });

    }

    public ArrayList<Structures.tri> ToDraw(){
        this.WorldMesh.clear();

        Structures.Matrix localTransform = Structures.CameraMakeTransform(this.pos, this.target, this.Up);
        Structures.Matrix transformInverse = Structures.MatrixOrthogonalInverse(localTransform);
        for(Object obj:World){
            for(Structures.tri tri:obj.mesh){

                tri = new Structures.tri(Structures.Vector_Add(obj.pos, tri.a),Structures.Vector_Add(obj.pos, tri.b),Structures.Vector_Add(obj.pos, tri.c));

                tri.UpdateNormal();

                Structures.Vertex triWorldPos = tri.a;
                Structures.Vertex triNormal = tri.Normal;
                Structures.Vertex camToTri = Structures.Vector_Normalize(Structures.Vector_Sub(tri.a,this.pos));

                tri = new Structures.tri(transformInverse.MatrixMultiply(tri.a),transformInverse.MatrixMultiply(tri.b),transformInverse.MatrixMultiply(tri.c));

                if (Structures.Vector_Dot(camToTri,triNormal) < 0f & (tri.a.z >0 & tri.b.z >0 & tri.c.z >0)){
                    tri.Normal = triNormal;
                    tri.WorldPos = triWorldPos;
                    WorldMesh.add(tri);
                }
            }
        }
        return WorldMesh;
    }
    public void render(){

        this.lookDir = new Structures.Vertex((float)Math.sin(this.rotation[1]),(float)Math.sin(this.rotation[0]),(float)Math.cos(this.rotation[1]));
        this.target = Structures.Vector_Normalize(this.lookDir);
        this.WorldMesh = ToDraw();

        for (int z = 0; z < WorldMesh.size();z++){

            Structures.Vertex Normal = WorldMesh.get(z).Normal; Structures.Vertex WorldPos = WorldMesh.get(z).WorldPos;
            Structures.tri newTri = new Structures.tri(ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).a),ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).b),ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).c));
            Structures.tri FinTri = Window.Scale(newTri,this.WindowD,this.stretched); FinTri.Normal = Normal; FinTri.WorldPos = WorldPos;
            WorldMesh.set(z, FinTri);
            
        }

        Collections.sort(WorldMesh, new Comparator<Structures.tri>(){
			@Override
			public int compare(Structures.tri tri1, Structures.tri tri2) {
				// tr1 should be in first : returns -1 | else : returns 1
                // Calculates the distance to the camera and compares bt 2 and 1 with 2 - 1 > 0 ? 1 : -1
                float dt2 = Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri2.a,pos)) + Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri2.b,pos)) + Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri2.c,pos));
                float dt1 = Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri1.a,pos)) + Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri1.b,pos)) + Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri1.c,pos));
				
                return (dt2 > dt1) ? 1 : -1;
				} 
			});

        this.WindowD.paint(this.WindowD.getGraphics(),WorldMesh);
        this.WorldMesh.clear();
        }

        public void CamParseControls(ArrayList<Integer> Controls){
            for (Integer event : Controls){
                switch(event){
                    case 32: // SPACE
                        this.pos.y -= 0.01f;
                        break;
                    case 16: // SHIFT
                        this.pos.y += 0.01f;
                        break;
                    case 81: // Q
                        this.pos.x -= 0.01f * Math.cos(this.rotation[1]);
                        this.pos.z += 0.01f * Math.sin(this.rotation[1]);
                        break;
                    case 68: // D
                        this.pos.x += 0.01f * Math.cos(this.rotation[1]);
                        this.pos.z -= 0.01f * Math.sin(this.rotation[1]);
                        break; 
                    case 90: // Z
                        this.pos.z += 0.01f * Math.cos(this.rotation[1]);
                        this.pos.x += 0.01f * Math.sin(this.rotation[1]);
                        break;
                    case 83: // S
                        this.pos.z -= 0.01f * Math.cos(this.rotation[1]);
                        this.pos.x -= 0.01f * Math.sin(this.rotation[1]);
                        break;  
                    case 27: // esc
                        this.WindowD.paused = true;
                        break;
                }
            }
            if (!Controls.contains(27))this.WindowD.paused = false;
        }
}
