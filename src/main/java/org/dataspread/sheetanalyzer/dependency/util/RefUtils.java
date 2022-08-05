package org.dataspread.sheetanalyzer.dependency.util;

import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import org.dataspread.sheetanalyzer.util.Ref;
import org.dataspread.sheetanalyzer.util.RefImpl;


public class RefUtils {
    public static boolean isValidRef(Ref ref) {
        return (ref.getRow() >= 0 &&
                ref.getColumn() >=0 &&
                ref.getRow() <= ref.getLastRow() &&
                ref.getColumn() <= ref.getLastColumn());
    }

    public static Rectangle refToRect(Ref ref)
    {
        return RectangleFloat.create(ref.getRow(),ref.getColumn(),
                (float) 0.5 + ref.getLastRow(), (float) 0.5 + ref.getLastColumn());
    }

    public static Ref coordToRef(Ref ref, int firstRow, int firstCol,
                           int lastRow, int lastCol) {
        return new RefImpl(ref.getBookName(), ref.getSheetName(),
                firstRow, firstCol, lastRow, lastCol);
    }

    public static Offset refToOffset(Ref prec, Ref dep, boolean isStart) {
        if (isStart) {
            return new Offset(dep.getRow() - prec.getRow(),
                    dep.getColumn() - prec.getColumn());
        } else {
            return new Offset(dep.getLastRow() - prec.getLastRow(),
                    dep.getLastColumn() - prec.getLastColumn());
        }
    }

    public static Ref fromStringToCell(String cellStr) {
        String[] rowAndColumn = cellStr.split(":");
        String rowStr = rowAndColumn[1];
        int rowIdx = Integer.parseInt(rowStr) - 1;

        int colIdx = 0;
        char[] colChars = rowAndColumn[0].toLowerCase().toCharArray();
        for (int i = 0; i < colChars.length; i++) {
            colIdx += (colChars[i] - 'a') * Math.pow(colChars.length - i - 1, 26);
        }
        colIdx -= 1;
        return new RefImpl(colIdx, rowIdx);
    }
}
