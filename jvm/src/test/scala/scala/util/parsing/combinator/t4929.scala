import scala.util.parsing.json._
import java.util.concurrent._
import collection.JavaConverters._

import org.junit.Test

class t4929 {

  val LIMIT = 2000
  val THREAD_COUNT = 20
  val count = new java.util.concurrent.atomic.AtomicInteger(0)

  val begin = new CountDownLatch(THREAD_COUNT)
  val finish = new CountDownLatch(THREAD_COUNT)

  val errors = new ConcurrentLinkedQueue[Throwable]

  @Test
  def test: Unit = {
    (1 to THREAD_COUNT) foreach { i =>
      val thread = new Thread {
        override def run(): Unit = {
          begin.await(1, TimeUnit.SECONDS)
          try {
            while (count.getAndIncrement() < LIMIT && errors.isEmpty) {
              JSON.parseFull("""{"foo": [1,2,3,4]}""")
            }
          } catch {
            case t: Throwable => errors.add(t)
          }

          finish.await(10, TimeUnit.SECONDS)
        }
      }

      thread.setDaemon(true)
      thread.start()
    }
    errors.asScala foreach { throw(_) }
  }
}
