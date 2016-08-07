package cassandra

import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.{Cluster, Session}
import config.ConfigurationHelper
import play.api.Logger
import scala.collection.JavaConversions._

/**
  * Maintains connects to cassadra, recovers if host disapperars
  */
trait CassandraConnector extends ConfigurationHelper {
  private val Log = Logger(classOf[CassandraConnector])

  private var currentCluster = buildCluster()
  private var currentSession = buildSession()

  def cluster() = this.synchronized {
    if(currentCluster.isClosed) currentCluster = buildCluster()
    currentCluster
  }

  implicit def session = this.synchronized {
    if(currentSession.isClosed) currentSession = buildSession()
    currentSession
  }

  val metadata = initMetadata

  /**
    * TODO, make this connect to multiple nodes
    * @return
    */
  private def buildCluster(): Cluster = {
    catchNoHostAndRetry( () =>
      Cluster.builder()
        .addContactPoints(CassandraNodes: _*)
        .build()
    )
  }

  private def buildSession(): Session = catchNoHostAndRetry(() => cluster().connect())

  private def close() = {
    currentSession.close()
    cluster.close()
  }

  private def initMetadata = {
    val meta = cluster().getMetadata
    Log.info(s"Connected to cluster: ${meta.getClusterName()}");
    meta.getAllHosts.foreach { host =>
      Log.info(s"Datacenter: %s; Host: %s; Rack: ${host.getDatacenter()}, ${host.getAddress()}, ${host.getRack()}")
    }
    meta
  }

  /**
    * Sometimes hosts disappear. because clouds are volotile
    *
    * @param action
    * @tparam T
    * @return
    */
  private def catchNoHostAndRetry[T](action: () => T, interval: Long = CassandraConnectionRetryIntervalMillis): T = this.synchronized {
    try {
      action()
    } catch {
      case noHost: NoHostAvailableException =>
        Log.error(s"could not connect to hosts: $CassandraNodes, retrying in $interval")
        Thread.sleep(interval)
        catchNoHostAndRetry(action, math.min(interval * 2, CassandraConnectioinRetryIntervalMillisMax))
    }
  }
}
