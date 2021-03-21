import java.util.ArrayList;

public class Algs{
    
    public static ArrayList<Structures.Tri> mergeSortPainter(ArrayList<Structures.Tri> list1, ArrayList<Structures.Tri> list2, Structures.Vertex camPos){
        int ind1, ind2;
        ind1 = ind2 = 0;
        ArrayList<Structures.Tri> end = new ArrayList<Structures.Tri>();
        while(ind1 < list1.size() && ind2 < list2.size()){
            if (Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(list2.get(ind2).WorldPos,camPos)) > Structures.Vector_MagnitudeSquared(Structures.Vector_Sub(list1.get(ind1).WorldPos,camPos))) {end.add(list2.get(ind2));ind2 ++;}
            else {end.add(list1.get(ind1));ind1 ++;}
        }
        if (ind1 == list1.size()){for(int temp = ind2; temp < list2.size(); temp++){end.add(list2.get(temp));}}
        else {for(int temp = ind1; temp < list1.size(); temp++){end.add(list1.get(temp));}}
        return end;
    }

    public static ArrayList<Structures.Tri> PainterDivisionSort(ArrayList<Structures.Tri> original, Structures.Vertex camPos){
        if (original.size() < 2) return original;
        ArrayList<Structures.Tri> lowerList = new ArrayList<Structures.Tri>(original.subList(0, original.size()>>1));
        ArrayList<Structures.Tri> upperList = new ArrayList<Structures.Tri>(original.subList(original.size()>>1, original.size()));
        return mergeSortPainter(PainterDivisionSort(lowerList, camPos), PainterDivisionSort(upperList, camPos),camPos);
    }
}