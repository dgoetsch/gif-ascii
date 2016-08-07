package dev.yn.cassandra

import play.api.Configuration

trait CassandraConfigurationHelper {
  val configuration: Configuration

  val CassandraNodesKey = "cassandra.nodes"
  val CassandraConnectionRetryIntervalMillisKey = "cassandra.connectionRetryIntervalMillis"
  val CassandraConnectionRetryIntervalMaxKey = "cassandra.connectionRetryIntervalMax"

  val CassandraNodes: Seq[String] = configuration
    .getStringSeq(CassandraNodesKey)
    .getOrElse(throw new RuntimeException(s"missing configuration value for $CassandraNodesKey"))

  val CassandraConnectionRetryIntervalMillis: Long = configuration
    .getLong(CassandraConnectionRetryIntervalMillisKey)
    .getOrElse(2000L)

  val CassandraConnectionRetryIntervalMillisMax: Long = configuration
    .getLong(CassandraConnectionRetryIntervalMaxKey)
    .getOrElse(CassandraConnectionRetryIntervalMillis * 32)
}
