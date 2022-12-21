package org.dataspread.sheetanalyzer.dependency;

import com.redislabs.redisgraph.Record;
import com.redislabs.redisgraph.ResultSet;
import com.redislabs.redisgraph.impl.api.RedisGraph;

import org.dataspread.sheetanalyzer.dependency.util.RedisGraphConstants;
import org.dataspread.sheetanalyzer.dependency.util.RefUtils;
import org.dataspread.sheetanalyzer.util.Pair;
import org.dataspread.sheetanalyzer.util.Ref;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DependencyGraphRedis implements DependencyGraph {

    private RedisGraph redisGraph = new RedisGraph();
    private String graphName;

    @Override
    public Set<Ref> getDependents(Ref precedent) {
        HashSet<Ref> resSet = new HashSet<>();
        String query = String.format("MATCH (prec:%s)-[:%s*]->(dep:%s)" +
                "WHERE prec.%s = %s RETURN dep",
                RedisGraphConstants.nodeName,
                RedisGraphConstants.relName,
                RedisGraphConstants.nodeName,
                RedisGraphConstants.cellAttr,
                "'" + precedent.toString() + "'");
        ResultSet resultSet = redisGraph.query(graphName, query);
        while(resultSet.hasNext()) {
            Record record = resultSet.next();
            String depCell = record.getValue("dep").toString();
            resSet.add(RefUtils.fromStringToCell(depCell));
        }
        return resSet;
    }

    @Override
    public Set<Ref> getPrecedents(Ref dependent) {
        return null;
    }

    @Override
    public void add(Ref precedent, Ref dependent) {

    }

    @Override
    public void clearDependents(Ref dependent) {

    }

    @Override
    public void addBatch(List<Pair<Ref, Ref>> edgeBatch) {

    }

    @Override
    public long getNumEdges() {
        return 0;
    }

    @Override
    public long getNumVertices() {
        return 0;
    }

    public void bulkLoad(String sheetName,
                         File nodeFile, File relFile) {
        graphName = sheetName;
        String command = RedisGraphConstants.bulkLoadCmd + " " + graphName
                + " -n " + nodeFile.getAbsolutePath()
                + " -r " + relFile.getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.directory(new File(System.getProperty("user.home")));
        try {
            Process process = pb.start();
            int exitVal = process.waitFor();
            System.out.println("Bulk load result : " + exitVal);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
