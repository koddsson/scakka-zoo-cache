package com.github.astonbitecode.zoocache

import scala.concurrent.Promise

package messages {

  private[astonbitecode] case class ScakkaApiWatchUnderPath(path: String, promiseOpt: Option[Promise[Unit]]) extends MessageNotifyable {
    override def success(accessedPath: String): Unit = {
      if (promiseOpt.nonEmpty && path == accessedPath) {
        promiseOpt.get.success()
      }
    }

    override def failure(accessedPath: String, error: Throwable): Unit = {
      if (promiseOpt.nonEmpty && path == accessedPath) {
        promiseOpt.get.failure(error)
      }
    }
  }

  private[astonbitecode] case class ScakkaApiRemovePath(path: String, promise: Promise[Unit]) extends MessageNotifyable {
    override def success(accessedPath: String): Unit = {
      if (path == accessedPath) {
        promise.success()
      }
    }

    override def failure(accessedPath: String, error: Throwable): Unit = {
      if (path == accessedPath) {
        promise.failure(error)
      }
    }
  }

  private[astonbitecode] case object ScakkaApiShutdown

  private[astonbitecode] case class Add(path: String, value: Array[Byte], updateChildren: Boolean, notifyOpt: Option[MessageNotifyable])

  private[astonbitecode] case class Update(path: String, recursive: Boolean, notifyOpt: Option[MessageNotifyable])

  private[astonbitecode] case class SetWatcher(path: String, notifyOpt: Option[MessageNotifyable])

  private[astonbitecode] case class Remove(path: String, notifyOpt: Option[MessageNotifyable])

  private[astonbitecode] case class Unwatch(path: String, notifyOpt: Option[MessageNotifyable])
}