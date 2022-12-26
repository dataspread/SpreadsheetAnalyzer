package org.dataspread.sheetanalyzer.dependency.util;

import org.dataspread.sheetanalyzer.util.Ref;

import java.util.*;

public class CalcRefManager {

    private static class Container {
        int rowID = 0;
        int colID = 0;

        public Container(int rowID, int colID) {
            this.rowID = rowID;
            this.colID = colID;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Container))
                return false;
            Container other = (Container) obj;

            return this.rowID == other.rowID && this.colID == other.colID;
        }

        @Override
        public int hashCode() {
            return 31 * this.rowID + this.colID;
        }
    }

    HashMap<Container, List<Ref>> cMap = new HashMap<>();
    int rowLargeRange = 256;
    int lastLargeRowID = 127;
    int upperPart = 32768;
    int rowSmallRange = 128;
    int colRange = 256;

    public void insert(Ref ref) {
        Set<Container> cSet = fromRefToContainers(ref);
        for (Container c: cSet) {
            List<Ref> rList = cMap.getOrDefault(c, new LinkedList<>());
            rList.add(ref);
            cMap.put(c, rList);
        }
    }

    public void delete(Ref ref) {
        Set<Container> cSet = fromRefToContainers(ref);
        for (Container c: cSet) {
            List<Ref> rList = cMap.get(c);
            if (rList != null)
                rList.remove(ref);
        }
    }

    public Set<Ref> findOverlap(Ref ref) {
        Set<Container> cSet = fromRefToContainers(ref);
        HashSet<Ref> oSet = new HashSet<>();

        for (Container c: cSet) {
           List<Ref>  rList = cMap.get(c);
           if (rList != null)
               rList.forEach(otherRef -> {
                   if (otherRef.getOverlap(ref) != null)
                       oSet.add(otherRef);
               });
        }
        return oSet;
    }

    private Set<Container> fromRefToContainers(Ref ref) {
        int rowID = fromRowToRowID(ref.getRow());
        int lastRowID = fromRowToRowID(ref.getLastRow());
        int colID = fromColToColID(ref.getColumn());
        int lastColID = fromColToColID(ref.getLastColumn());
        HashSet<Container> cSet = new HashSet<>();

        for (int i = rowID; i <= lastRowID; i++) {
            for (int j = colID; j <= lastColID; j++) {
                Container c = new Container(i, j);
                cSet.add(c);
            }
        }

        return cSet;
    }

    private int fromRowToRowID(int row) {
        if (row < upperPart) {
            return row/rowLargeRange;
        } else {
            return lastLargeRowID + (row - upperPart)/rowSmallRange;
        }
    }

    private int fromColToColID(int col) {
        return col/colRange;
    }
}
