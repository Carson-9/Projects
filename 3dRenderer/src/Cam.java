import java.util.ArrayList;
/*import java.util.Collections;
import java.util.Comparator;*/
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;

public class Cam extends Object {

    Window.Drawer WindowD;
    ArrayList<Object> World = new ArrayList<Object>();
    ArrayList<Structures.Tri> WorldMesh = new ArrayList<Structures.Tri>();
    ArrayList<Structures.Tri> tempMesh = new ArrayList<Structures.Tri>();

    Structures.Matrix ProjectionMatrix = new Structures.Matrix();

    Structures.Vertex Up = new Structures.Vertex(0,1,0);
    Structures.Vertex lookDir = new Structures.Vertex(0,0,1);
    Structures.Vertex target = new Structures.Vertex(0,0,0); //Target must be absolute - cam.pos

    public static final float PI = 3.141592653589f;
    public float PitchLimit = 1.56f; // ~ PI / 2
    boolean stretched;

    ArrayList<Structures.Vertex> clipPlanes = new ArrayList<Structures.Vertex>();
    ArrayList<Structures.Vertex> clipPoints = new ArrayList<Structures.Vertex>();

    public Cam(Structures.Vertex pos, float[] rotation,Window.Drawer Win,ArrayList<Object> World, boolean isStretched){
        super(new ArrayList<Structures.Tri>(),pos,rotation);
        WindowD = Win;
        this.World= World;
        ProjectionMatrix.makeProjection(this.WindowD.screen);
        this.stretched = isStretched;

        this.clipPlanes.add(new Structures.Vertex(0,0,1)); // Znear
        this.clipPlanes.add(new Structures.Vertex(1,0,0)); // Left
        this.clipPlanes.add(new Structures.Vertex(0,-1,0)); // Top
        this.clipPlanes.add(new Structures.Vertex(-1,0,0)); // Right
        this.clipPlanes.add(new Structures.Vertex(0,1,0)); // Bottom

        this.clipPoints.add(new Structures.Vertex(0,0,0.1f)); // Znear
        this.clipPoints.add(new Structures.Vertex(0, 0, 0)); // TopLeft
        this.clipPoints.add(new Structures.Vertex(0, 0, 0)); // TopLeft 2 | Need to add 2 for the clipping planes loop & need to figure out if 0,0 or -scr/2
        this.clipPoints.add(new Structures.Vertex(this.WindowD.getWidth() - 1, this.WindowD.screen.height - 1, 0)); // BottomRight
        this.clipPoints.add(new Structures.Vertex(this.WindowD.screen.width - 1, this.WindowD.screen.height - 1, 0)); // BottomRight 2

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


    public Structures.Vertex PlaneFindPointIntersect(Structures.Vertex plane_normal, Structures.Vertex plane_point, Structures.Vertex lineStart, Structures.Vertex lineEnd){
        // Finds the intersection of a line and a defined plane
        plane_normal = Structures.Vector_Normalize(plane_normal);
        float plane_dist = Structures.Vector_Dot(plane_normal, plane_point);
        float adist = Structures.Vector_Dot(lineStart, plane_normal);
        float bdist = Structures.Vector_Dot(lineEnd, plane_normal);
        float mult = (plane_dist - adist) / (bdist - adist);

        Structures.Vertex startEnd = Structures.Vector_Sub(lineEnd, lineStart);
        Structures.Vertex startToIntersect = Structures.Vector_Mult(startEnd, mult);
        return Structures.Vector_Add(lineStart, startToIntersect);

    }

    public float DistToPlane(Structures.Vertex plane_normal, Structures.Vertex plane_point, Structures.Vertex extPoint){
        plane_normal = Structures.Vector_Normalize(plane_normal);
        Structures.Vertex planeToPoint = Structures.Vector_Sub(extPoint, plane_point);
        return Structures.Vector_Dot(planeToPoint, plane_normal);
    }


    public void ClipOnPlane(Structures.Vertex plane_normal, Structures.Vertex plane_point, Structures.Tri tri){
        ArrayList<Structures.Vertex> insidePoints = new ArrayList<Structures.Vertex>();
        ArrayList<Structures.Vertex> outsidePoints = new ArrayList<Structures.Vertex>();
        // CLIPPING : 
        // Depending on how much points are "inside" our planes (screen edges and znear), create 0 / 1 / 2 new tris or Discard the triangle
        plane_normal = Structures.Vector_Normalize(plane_normal);

        float d0 = DistToPlane(plane_normal, plane_point, tri.a);
        float d1 = DistToPlane(plane_normal, plane_point, tri.b);
        float d2 = DistToPlane(plane_normal, plane_point, tri.c);
        //Depending on their distance from the plane (>= 0 : same side as normal), is the point in or out
        if (d0 >= 0) insidePoints.add(tri.a);
        else outsidePoints.add(tri.a);
        if (d1 >= 0) insidePoints.add(tri.b);
        else outsidePoints.add(tri.b);
        if (d2 >= 0) insidePoints.add(tri.c);
        else outsidePoints.add(tri.c);

        int inside = insidePoints.size();
        int outside = outsidePoints.size();

        if (inside == 0) return;
        if (inside == 3) {this.tempMesh.add(tri); return;}
        if (inside == 1 && outside == 2){

            Structures.Tri clippedTri = new Structures.Tri(insidePoints.get(0), 
            PlaneFindPointIntersect(plane_normal, plane_point, insidePoints.get(0), outsidePoints.get(0)), 
            PlaneFindPointIntersect(plane_normal, plane_point, insidePoints.get(0), outsidePoints.get(1)));
            clippedTri.color = tri.color;
            clippedTri.Normal = tri.Normal;
            clippedTri.WorldPos = tri.WorldPos;
            this.tempMesh.add(clippedTri);
            return;

        }
        if (inside == 2 && outside == 1){
            //Creates a quadrilateral
            Structures.Tri clippedTri1 = new Structures.Tri(insidePoints.get(0), insidePoints.get(1), 
            PlaneFindPointIntersect(plane_normal, plane_point, insidePoints.get(0), outsidePoints.get(0)));
            clippedTri1.color = tri.color;
            clippedTri1.Normal = tri.Normal;
            clippedTri1.WorldPos = tri.WorldPos;

            Structures.Tri clippedTri2 = new Structures.Tri(insidePoints.get(1), clippedTri1.c,
            PlaneFindPointIntersect(plane_normal, plane_point, insidePoints.get(1), outsidePoints.get(0)));
            clippedTri2.color = tri.color;
            clippedTri2.Normal = tri.Normal;
            clippedTri2.WorldPos = tri.WorldPos;
    
            this.tempMesh.add(clippedTri1); this.tempMesh.add(clippedTri2);
            return;

        }

    }


    public ArrayList<Structures.Tri> ToDraw(){
        this.WorldMesh.clear();
        Structures.Matrix localTransform = Structures.CameraMakeTransform(this.pos, this.target, this.Up);
        Structures.Matrix transformInverse = Structures.MatrixOrthogonalInverse(localTransform);
        for(Object obj:World){
            for(Structures.Tri tri:obj.mesh){

                tri = new Structures.Tri(Structures.Vector_Add(obj.pos, tri.a),Structures.Vector_Add(obj.pos, tri.b),Structures.Vector_Add(obj.pos, tri.c));

                tri.UpdateNormal();

                Structures.Vertex triWorldPos = tri.a;
                Structures.Vertex triNormal = tri.Normal;
                Structures.Vertex camToTri = Structures.Vector_Normalize(Structures.Vector_Sub(tri.a,this.pos));

                if (Structures.Vector_Dot(camToTri,triNormal) < 0f) {

                    tri = new Structures.Tri(transformInverse.MatrixMultiply(tri.a),transformInverse.MatrixMultiply(tri.b),transformInverse.MatrixMultiply(tri.c));
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

        // Clipping WorldMesh by modifying every triangle for each plane Znear here
        this.tempMesh.clear();
        for (Structures.Tri tri : this.WorldMesh){ClipOnPlane(this.clipPlanes.get(0), this.clipPoints.get(0), tri);}
        this.WorldMesh.clear();
        for(Structures.Tri tri : this.tempMesh){this.WorldMesh.add(tri);}
        this.tempMesh.clear();


        // Cam space -> Screen Space
        for (int z = 0; z < WorldMesh.size();z++){
            Structures.Vertex Normal = WorldMesh.get(z).Normal; Structures.Vertex WorldPos = WorldMesh.get(z).WorldPos; Color color = WorldMesh.get(z).color;
            Structures.Tri newTri = new Structures.Tri(ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).a),ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).b),ProjectionMatrix.MatrixMultiply(WorldMesh.get(z).c));
            Structures.Tri FinTri = Window.Scale(newTri,this.WindowD,this.stretched); FinTri.Normal = Normal; FinTri.WorldPos = WorldPos; FinTri.color = color;
            WorldMesh.set(z, FinTri);
            
        }
        // Sort by distance from cam
        /*Collections.sort(WorldMesh, new Comparator<Structures.Tri>(){
			@Override
			public int compare(Structures.Tri tri1, Structures.Tri tri2) {
				// tr1 should be in first : returns -1 | else : returns 1
                // Calculates the distance to the camera and compares t 2 and 1 with 2 - 1 > 0 ? 1 : -1
                float dt2 = Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri2.WorldPos,pos));
                float dt1 = Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(tri1.WorldPos,pos));
                return (dt2 > dt1) ? 1 : -1;
				} 
			});*/

        this.WorldMesh = Algs.PainterDivisionSort(this.WorldMesh, this.pos);

        // Clipping against Screen Edges, Need Screen coordinates for the triangles
        for(int index = 0; index < 4; index++){
            this.tempMesh.clear();
            for(Structures.Tri tri : WorldMesh){
                switch(index){
                    case 0: //Left
                        ClipOnPlane(new Structures.Vertex(1,0,0), new Structures.Vertex(0,0,0), tri);
                        break;
                    case 1: //Top
                        ClipOnPlane(new Structures.Vertex(0,1,0), new Structures.Vertex(0,0,0), tri);
                        break;
                    case 2: //Right
                        ClipOnPlane(new Structures.Vertex(-1,0,0), new Structures.Vertex(this.WindowD.getWidth(), 0, 0), tri);
                        break;
                    case 3: //Bottom
                        ClipOnPlane(new Structures.Vertex(0,-1,0), new Structures.Vertex(0,this.WindowD.getHeight(),0), tri);
                        break;
                }
            }
            this.WorldMesh.clear();
            for(Structures.Tri tri : this.tempMesh){this.WorldMesh.add(tri);}
        }

        //Transmit WorldMesh to Window to draw
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
