package org.dataspread.sheetanalyzer.dependency;

import com.redislabs.redisgraph.Record;
import com.redislabs.redisgraph.ResultSet;
import com.redislabs.redisgraph.impl.api.RedisGraph;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.dataspread.sheetanalyzer.dependency.util.RedisGraphConstants;
import org.dataspread.sheetanalyzer.dependency.util.RefUtils;
import org.dataspread.sheetanalyzer.util.Pair;
import org.dataspread.sheetanalyzer.util.RedisGraphFailedException;
import org.dataspread.sheetanalyzer.util.Ref;
import org.dataspread.sheetanalyzer.util.RefImpl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.util.Pool;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisDataException;


public class DependencyGraphRedis implements DependencyGraph {

    private final RedisGraph redisGraph;
    private String graphName;
    private final String emptyGraphName = "";

    public DependencyGraphRedis() {
        Pool<Jedis> jedis = new JedisPool(new GenericObjectPoolConfig(),
                "localhost", 6379, 60000);
        redisGraph = new RedisGraph(jedis);
        graphName = emptyGraphName;
    }

    @Override
    public Set<Ref> getDependents(Ref precedent) {
        try {
            HashSet<Ref> resSet = new HashSet<>();
            String query = String.format("MATCH (prec:%s)-[:%s*]->(dep:%s)" +
                    " WHERE prec.%s = %s RETURN DISTINCT dep",
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
        } catch (JedisDataException | JedisConnectionException e) {
           throw new RedisGraphFailedException("Query timeout");
        }
    }

    @Override
    public Set<Ref> getPrecedents(Ref dependent) {
        return null;
    }

    @Override
    public void add(Ref prec, Ref dep) {
        String queryTemplate = "CREATE (:%s {%s: %s})-[:%s]->(:%s {%s: %s})";
        for (int row = prec.getRow(); row <= prec.getLastRow(); row++) {
            for (int col = prec.getColumn(); col <= prec.getLastColumn(); col++) {
                Ref precCell = new RefImpl(row, col);
                String query = String.format(queryTemplate,
                        RedisGraphConstants.nodeName,
                        RedisGraphConstants.cellAttr,
                        "'" + precCell + "'",
                        RedisGraphConstants.relName,
                        RedisGraphConstants.nodeName,
                        RedisGraphConstants.cellAttr,
                        "'" + dep + "'");
                redisGraph.query(graphName, query);
            }
        }
    }

    @Override
    public void clearDependents(Ref dep) {
        String query = String.format("MATCH (dep:%s {%s: %s}) DETACH DELETE dep",
                RedisGraphConstants.nodeName,
                RedisGraphConstants.cellAttr,
                "'" + dep + "'");
        redisGraph.query(graphName, query);
    }

    @Override
    public void addBatch(List<Pair<Ref, Ref>> edgeBatch) {

    }

    @Override
    public long getNumEdges() {
        String query = String.format("MATCH (n)-[:%s]->() RETURN count(n) as count",
                RedisGraphConstants.relName);
        ResultSet resultSet = redisGraph.query(graphName, query);
        return Long.parseLong(resultSet.next().getValue("count").toString());
    }

    @Override
    public long getNumVertices() {
        String query  = String.format("MATCH (n:%s) RETURN count(n) as count",
                RedisGraphConstants.nodeName);
        ResultSet resultSet = redisGraph.query(graphName, query);
        return Long.parseLong(resultSet.next().getValue("count").toString());
    }

    public void bulkLoad(String graphName,
                         File nodeFile, File relFile) {
        this.graphName = graphName;

        try {
            redisGraph.deleteGraph(this.graphName);
        } catch (JedisDataException ignored) {}

        String command = RedisGraphConstants.bulkLoadCmd + " " + graphName
                + " -n " + nodeFile.getAbsolutePath()
                + " -r " + relFile.getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.directory(new File(System.getProperty("user.home")));
        try {
            Process process = pb.start();
            int exitVal = process.waitFor();
            System.out.println("Bulk load result of " + graphName + " : " + exitVal);
            if (exitVal != 0) {
                throw new RedisGraphFailedException("Data loading failed");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void clearGraph() {
        if (!this.graphName.equalsIgnoreCase(this.emptyGraphName)) {
            try {
                redisGraph.deleteGraph(graphName);
            } catch (JedisDataException e) {
                // e.printStackTrace();
                System.out.println(graphName + " failed being cleared" );
            }
        }
    }

}
