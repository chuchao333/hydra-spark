package hydra.spark.server

import akka.actor.Props
import com.github.vonnagy.service.container.ContainerBuilder
import com.github.vonnagy.service.container.service.ContainerService
import com.typesafe.config.ConfigFactory
import hydra.spark.server.actors.{BinaryFileManager, DataFileManager}
import hydra.spark.server.endpoints.{DataFilesEndpoint, DslEndpoint}
import hydra.spark.server.io.FileDAO

/**
  * Created by alexsilva on 4/28/17.
  */
object HydraSparkService extends App {

  import configs.syntax._

  val config = ConfigFactory.load()

  val endpoints = Seq(classOf[DslEndpoint], classOf[DataFilesEndpoint])

  val rootDataFileDir = config.get[String]("hydra.spark.server.data.rootdir")
    .valueOrElse("/tmp/hydra-spark/data")

  val rootBinaryFileDir = config.get[String]("hydra.spark.server.binaries.rootdir")
    .valueOrElse("/tmp/hydra-spark/binaries")

  def buildContainer(): ContainerService = {
    val builder = ContainerBuilder()
      .withConfig(config)
      .withRoutes(endpoints: _*)
      .withActors(
        "data_file_manager" -> Props(classOf[DataFileManager], new FileDAO(rootDataFileDir)),
        "binary_file_manager" -> Props(classOf[BinaryFileManager], new FileDAO(rootBinaryFileDir)))
      .withName("hydra-spark")

    builder.build
  }

  buildContainer().start()

}