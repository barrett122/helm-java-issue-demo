package example

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random
import java.util.concurrent.Executors

import com.marcnuri.helm._
import one.profiler.AsyncProfiler

object Application extends App {
  require(args.length == 1, "Exactly one argument should be provided: <true/false> to enable/disable profiling")
  val shouldProfile = args(0).toBoolean
  val releases = 100
  val threadCount = 5

  val executor = Executors.newFixedThreadPool(threadCount)
  implicit val ec = ExecutionContext.fromExecutor(executor)

  try {
    println(s"Starting")
    Helm.version().call() // JNI loading is lazy, this forces native libraries to be loaded before profiling starts

    val profiler = AsyncProfiler.getInstance()
    if (shouldProfile) profiler.execute(s"start,jfr,event=wall,event=alloc,file=profile_%t.jfr")

    val futures = (1 to releases).map { i =>
      val name = s"release$i-${Random.alphanumeric.take(5).mkString.toLowerCase}"
      Future {
        println(s"Installing: $name")
        val installCommand = Helm.install("example-chart.tgz")
        installCommand.set("configmapName", s"$name-configmap")
        installCommand.withName(name).call()
      }
    }

    Await.ready(Future.sequence(futures), 5.minutes)
    println("complete")
    val failures = futures.flatMap(_.value).filter(_.isFailure)
    println(s"Failures: $failures")
    if (shouldProfile) profiler.execute("stop")
  }finally {
    executor.shutdownNow()
  }
}