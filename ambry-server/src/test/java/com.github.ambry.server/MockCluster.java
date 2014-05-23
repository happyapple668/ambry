package com.github.ambry.server;

import com.github.ambry.clustermap.DataNodeId;
import com.github.ambry.clustermap.MockClusterMap;
import com.github.ambry.clustermap.MockDataNodeId;
import com.github.ambry.config.VerifiableProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A mock cluster that is setup with multiple datacenters.
 * The setup configuration is determined by the mock cluster map.
 * For each data node in the mock cluster map, we start up a server.
 * On shutdown we ensure the servers are shutdown.
 */
public class MockCluster {
  private final MockClusterMap clusterMap;
  private List<AmbryServer> serverList = null;

  public MockCluster() throws IOException, InstantiationException {
    clusterMap = new MockClusterMap();
    serverList = new ArrayList<AmbryServer>();
    List<MockDataNodeId> dataNodes = clusterMap.getDataNodes();
    for (MockDataNodeId dataNodeId: dataNodes) {
      startServer(dataNodeId);
    }
  }

  public List<AmbryServer> getServers() {
    return serverList;
  }

  public MockClusterMap getClusterMap() {
    return clusterMap;
  }

  private void startServer(DataNodeId dataNodeId) throws IOException, InstantiationException {
    Properties props = new Properties();
    props.setProperty("host.name", dataNodeId.getHostname());
    props.setProperty("port", Integer.toString(dataNodeId.getPort()));
    props.setProperty("store.data.flush.interval.seconds", "1");
    props.setProperty("replication.token.flush.interval.seconds", "5");
    props.setProperty("replication.wait.time.between.replicas.ms", "0");
    VerifiableProperties propverify = new VerifiableProperties(props);
    AmbryServer server = new AmbryServer(propverify, clusterMap);
    server.startup();
    serverList.add(server);
  }

  public void cleanup() {
    for (AmbryServer server : serverList)
      server.shutdown();
    clusterMap.cleanup();
  }
}