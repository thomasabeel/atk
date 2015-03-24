package atk.io

import atk.util.MD5Tools
import java.io.File
import java.net.URL
import scala.io.Source
import java.io.PrintWriter
import java.util.Date
/**
 * Simple object to cache URL reads
 */
object URLCache {

  /**
   *  Older than a month
   */
  def old(time: Long) = {
	  (System.currentTimeMillis() - time) > (1000L * 60 * 60 * 24 * 30)
  }
  var lastQuery = System.currentTimeMillis()
  def query(url: String): List[String] = {

    val hash = MD5Tools.md5(url)

    val cached = new File(".url-cache/" + hash + ".blob")
    cached.getParentFile().mkdirs()
    println(cached.exists()+"\t"+cached.length()+"\t"+old(cached.lastModified()))

    if (!cached.exists() || cached.length() == 0 || old(cached.lastModified())) {

      cached.delete()
      while (!cached.exists()) {
        /* Wait at least 15 seconds between queries */
        while (System.currentTimeMillis() - lastQuery < 15 * 1000) {
          Thread.sleep(1000)
        }
        lastQuery = System.currentTimeMillis()

        val u = new URL(url);
        val conn = u.openConnection();
        val lines = Source.fromInputStream(conn.getInputStream()).getLines.toList
        
        if (!lines.mkString(" ").contains("Timed out")) {
          val pw = new PrintWriter(cached)
          pw.println(lines.mkString("\n"))
          pw.close
        }

      }
    }
    Source.fromFile(cached).getLines.toList

  }

}