package com.github.astonbitecode.zoocache.impl.scala

import scala.collection.mutable.HashMap
import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration._
import org.apache.zookeeper.{ ZooKeeper, KeeperException }
import org.apache.zookeeper.KeeperException.Code
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.pattern.gracefulStop
import com.github.astonbitecode.zoocache.messages._
import com.github.astonbitecode.zoocache.CacheUpdaterActor
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache

case class ScakkaZooCacheImpl(zoo: ZooKeeper, actorSystem: ActorSystem) extends ScakkaZooCache {
  // Import from companion
  import ScakkaZooCache.ZkNodeElement
  // The cache
  private[astonbitecode] val cache = HashMap.empty[String, ZkNodeElement]
  // Create only one handler.
  // WARNING: The handler is the only entity that mutates the cache.
  private val updater = actorSystem.actorOf(CacheUpdaterActor.props(cache, zoo))

  @throws(classOf[KeeperException])
  override def getChildren(path: String): List[String] = {
    cache.get(path).fold(throw KeeperException.create(Code.NONODE))(_.children.toList)
  }

  @throws(classOf[KeeperException])
  override def getData(path: String): Array[Byte] = {
    cache.get(path).fold(throw KeeperException.create(Code.NONODE))(_.data)
  }

  override def addPathToCache(path: String): Future[Unit] = {
    val p = Promise[Unit]

    cache.get(path) match {
      case Some(_) => updater ! ScakkaApiWatchUnderPath(path, Some(p))
      case None => {
        updater ! ScakkaApiWatchUnderPath(path, None)
        p.success()
      }
    }

    p.future
  }

  override def removePathFromCache(path: String): Future[Unit] = {
    val p = Promise[Unit]

    cache.get(path) match {
      case Some(_) => updater ! ScakkaApiRemovePath(path, p)
      case None => p.success()
    }

    p.future
  }

  override def stop(): Future[Unit] = {
    implicit val ec = actorSystem.dispatcher
    gracefulStop(updater, 10.seconds, ScakkaApiShutdown).map {_ => }
  }
}