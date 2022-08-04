package org.dataspread.sheetanalyzer.dependency;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

import org.dataspread.sheetanalyzer.dependency.util.*;
import org.dataspread.sheetanalyzer.util.Pair;
import org.dataspread.sheetanalyzer.util.Ref;
import org.dataspread.sheetanalyzer.util.RefImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.dataspread.sheetanalyzer.dependency.util.PatternTools.getRectangleFromRef;

public class DependencyGraphNoComp implements DependencyGraph {
    protected HashMap<Ref, Set<Ref>> precToDepSet = new HashMap<>();
    protected HashMap<Ref, Set<Ref>> depToPrecSet = new HashMap<>();
    private RTree<Ref, Rectangle> _rectToRef = RTree.create();

    public Set<Ref> getDependents(Ref precedent) {
        LinkedHashSet<Ref> result = new LinkedHashSet<>();
        if (RefUtils.isValidRef(precedent)) {
            getDependentsInternal(precedent, result);
        }
        return result;
    }

    public HashMap<Ref, Set<Ref>> getGraph() {
        return precToDepSet;
    }

    private void getDependentsInternal(Ref prec, LinkedHashSet<Ref> result) {
        RTree<Ref, Rectangle> resultSet = RTree.create();
        Queue<Ref> updateQueue = new LinkedList<>();
        updateQueue.add(prec);
        while (!updateQueue.isEmpty()) {
            Ref updateRef = updateQueue.remove();
            for (Ref precRef: getNeighbors(updateRef)) {
                for (Ref depRef: getDirectDependents(precRef)) {
                    if (!isContained(resultSet, depRef)) {
                        resultSet = resultSet.add(depRef, getRectangleFromRef(depRef));
                        result.add(depRef);
                        updateQueue.add(depRef);
                    }
                }
            }
        }
    }

    public Set<Ref> postProcessDependents(Set<Ref> result) {
        Set<Ref> newColumnResult = new HashSet<>();
        Set<Ref> newRowResult = new HashSet<>();

        ArrayList<Ref> resultArray = new ArrayList<>(result);
        Collections.sort(resultArray, new Comparator<Ref>() {
            @Override
            public int compare(Ref o1, Ref o2) {
                if (o1.getColumn() != o2.getColumn()) {
                    return o1.getColumn() - o2.getColumn();
                } else {
                    return o1.getRow() - o2.getRow();
                }
            }
        });

        int idx = 0;
        while (idx < resultArray.size()) {
            Ref mergedRef = resultArray.get(idx);
            int nextIdx = idx + 1;
            while (nextIdx < resultArray.size()) {
                Ref refNext = resultArray.get(nextIdx);
                if (!isVerticalMergable(mergedRef, refNext)) {
                    break;
                } else {
                    mergedRef = mergeRef(mergedRef, refNext);
                }
                nextIdx += 1;
            }
            newColumnResult.add(mergedRef);
            idx = nextIdx;
        }

        resultArray = new ArrayList<>(newColumnResult);
        Collections.sort(resultArray, new Comparator<Ref>() {
            @Override
            public int compare(Ref o1, Ref o2) {
                if (o1.getRow() != o2.getRow()) {
                    return o1.getRow() - o2.getRow();
                } else {
                    return o1.getColumn() - o2.getColumn();
                }
            }
        });

        idx = 0;
        while (idx < resultArray.size()) {
            Ref mergedRef = resultArray.get(idx);
            int nextIdx = idx + 1;
            while (nextIdx < resultArray.size()) {
                Ref refNext = resultArray.get(nextIdx);
                if (!isHorizontalMergable(mergedRef, refNext)) {
                    break;
                } else {
                    mergedRef = mergeRef(mergedRef, refNext);
                }
                nextIdx += 1;
            }
            newRowResult.add(mergedRef);
            idx = nextIdx;
        }

        return newRowResult;
    }

    private Ref mergeRef(Ref ref, Ref refNext) {
        int newLastRow = Math.max(ref.getLastRow(), refNext.getLastRow());
        int newLastColumn = Math.max(ref.getLastColumn(), refNext.getLastColumn());

        Ref newRef = new RefImpl(ref.getBookName(), ref.getSheetName(),
                ref.getRow(), ref.getColumn(), newLastRow, newLastColumn);

        if (ref.getPrecedents() != null) {
            for (Ref r: ref.getPrecedents()) {
                newRef.addPrecedent(r);
            }
        }
        if (refNext.getPrecedents() != null) {
            for (Ref r: refNext.getPrecedents()) {
                ref.addPrecedent(r);
            }
        }
        return newRef;
    }

    private boolean isVerticalMergable(Ref ref, Ref refNext) {
        if (!ref.getSheetName().equals(refNext.getSheetName())) {
            return false;
        }

        // Same column
        if (ref.getColumn() == refNext.getColumn() && ref.getLastColumn() == refNext.getLastColumn()) {
            if (refNext.getRow() <= ref.getLastRow() + 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isHorizontalMergable(Ref ref, Ref refNext) {
        if (!ref.getSheetName().equals(refNext.getSheetName())) {
            return false;
        }

        // Same row
        if (ref.getRow() == refNext.getRow() && ref.getLastRow() == refNext.getLastRow()) {
            if (refNext.getColumn() <= ref.getLastColumn() + 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isContained(RTree<Ref, Rectangle> resultSet, Ref input) {
        boolean isContained = false;
        Iterator<Entry<Ref, Rectangle>> matchIter =
                resultSet.search(RefUtils.refToRect(input)).toBlocking().getIterator();
        while (matchIter.hasNext()) {
            if (isSubsume(matchIter.next().value(), input)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    private boolean isSubsume(Ref large, Ref small) {
        Ref overlap = large.getOverlap(small);
        if (overlap == null)
            return false;
        return overlap.equals(small);
    }

    private Set<Ref> getNeighbors(Ref ref) {
        Iterator<Entry<Ref, Rectangle>> rTreeIter = this._rectToRef.search(getRectangleFromRef(ref))
                .toBlocking().getIterator();
        Set<Ref> neighbors = new HashSet<>();
        while (rTreeIter.hasNext()) {
            neighbors.add(rTreeIter.next().value());
        }
        return neighbors;
    }

    private Set<Ref> getDirectDependents(Ref prec) {
        if (this.precToDepSet.containsKey(prec)) {
            return this.precToDepSet.get(prec);
        } else {
            return new HashSet<>();
        }
    }

    public Set<Ref> getPrecedents(Ref dependent) {
        LinkedHashSet<Ref> result = new LinkedHashSet<>();
        if (RefUtils.isValidRef(dependent)) {
            getPrecedentInternal(dependent, result);
        }
        return result;
    }

    private void getPrecedentInternal(Ref dep, LinkedHashSet<Ref> result) {
        RTree<Ref, Rectangle> resultSet = RTree.create();
        Queue<Ref> updateQueue = new LinkedList<>();
        updateQueue.add(dep);
        while (!updateQueue.isEmpty()) {
            Ref updateRef = updateQueue.remove();
            for (Ref depRef: getNeighbors(updateRef)) {
                for (Ref precRef: getDirectPrecedent(depRef)) {
                    if (!isContained(resultSet, precRef)) {
                        resultSet = resultSet.add(precRef, getRectangleFromRef(precRef));
                        result.add(precRef);
                        updateQueue.add(precRef);
                    }
                }
            }
        }
    }

    private Set<Ref> getDirectPrecedent(Ref dep) {
        if (this.depToPrecSet.containsKey(dep)) {
            return this.depToPrecSet.get(dep);
        } else {
            return new HashSet<>();
        }
    }

    private void insertMemEntry(Ref prec, Ref dep) {
        Set<Ref> depSet = precToDepSet.getOrDefault(prec, new HashSet<>());
        depSet.add(dep);
        precToDepSet.put(prec, depSet);

        Set<Ref> precSet = depToPrecSet.getOrDefault(dep, new HashSet<>());
        precSet.add(prec);
        depToPrecSet.put(dep, precSet);

        _rectToRef = _rectToRef.add(prec, RefUtils.refToRect(prec));
        _rectToRef = _rectToRef.add(dep, RefUtils.refToRect(dep));
    }

    private void deleteMemEntry(Ref prec, Ref dep) {
        Set<Ref> depSet = precToDepSet.get(prec);
        if (depSet != null) {
            depSet.remove(dep);
            if (depSet.isEmpty()) {
                precToDepSet.remove(prec);
            }
        }

        Set<Ref> precSet = depToPrecSet.get(dep);
        if (precSet != null) {
            precSet.remove(prec);
            if (precSet.isEmpty()) {
                depToPrecSet.remove(dep);
            }
        }

        _rectToRef = _rectToRef.delete(prec, RefUtils.refToRect(prec));
        _rectToRef = _rectToRef.delete(dep, RefUtils.refToRect(dep));
    }

    public void add(Ref precedent, Ref dependent) {
        insertMemEntry(precedent, dependent);
    }

    public void addBatch(List<Pair<Ref, Ref>> edgeBatch) {
        edgeBatch.forEach(oneEdge -> {
            Ref prec = oneEdge.first;
            Ref dep = oneEdge.second;
            add(prec, dep);
        });
    }

    public void clearDependents(Ref delDep) {
        assert (delDep.getRow() == delDep.getLastRow()) && (delDep.getColumn() == delDep.getLastColumn());
        for (Ref depRef: getNeighbors(delDep)) {
            for (Ref precRef: getDirectPrecedent(depRef)) {
                deleteMemEntry(precRef, depRef);
            }
        }
    }

    public long getNumEdges() {
        AtomicLong numEdges = new AtomicLong(0);
        depToPrecSet.forEach((dep, precSet) -> {
            numEdges.addAndGet(precSet.size());
        });
        return numEdges.get();
    }

    public long getNumVertices() {
        HashSet<Ref> refSet = new HashSet<>();
        depToPrecSet.forEach((dep, precSet) -> {
            refSet.add(dep);
            refSet.addAll(precSet);
        });
        return refSet.size();
    }

}
