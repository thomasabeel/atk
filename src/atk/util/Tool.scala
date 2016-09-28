/*
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-nc-nd/3.0/
 * or send a letter to Creative Commons, 444 Castro Street,
 * Suite 900, Mountain View, California, 94041, USA.
 *
 * A copy of the license is included in LICENSE.txt
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * Copyright 2005-2016 Thomas Abeel
 */
package atk.util

import java.io.PrintWriter
import java.util.Date
import java.lang.management.ManagementFactory
import scala.io.Source
import java.io.File
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.text.NumberFormat
import java.util.Locale
import java.time.LocalDateTime
import atk.io.NixWriter

object Tool extends Tool {

}

/**
 * Utility methods to create tools
 *
 * @author Thomas Abeel
 */
trait Tool extends Lines {

  val version = "There is no version information available for this tool"

  val description = "There is no description available for this tool"

  val nfP = NumberFormat.getPercentInstance(Locale.US)
  nfP.setMaximumFractionDigits(2)

  val nf = NumberFormat.getInstance(Locale.US)
  nf.setMaximumFractionDigits(2)

  lazy val naturalOrdering = Ordering.comparatorToOrdering(NaturalOrderComparator.NUMERICAL_ORDER_IGNORE_CASE)
  private var logger: PrintWriter = null;

  private val timestampFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS")

  timestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"))

  def timestamp(): String = {
    timestampFormat.format(new Date(System.currentTimeMillis()))

  }

  private val startTime = System.currentTimeMillis();
  private var progressCounter = 1

  def progress(reportFreq: Int) = {
    if (progressCounter % reportFreq == 0) {
      val interval = System.currentTimeMillis() - startTime
      log("Processing: " + progressCounter + "\t" + new TimeInterval(interval) + "\t" + nf.format(progressCounter * 1000L / (interval + .1)) + " units/s\t" + LocalDateTime.now())
    }
    progressCounter += 1
  }

  def init(location: String = null, config: AnyRef = null): Unit = {
    if (location == null){
      logger = new PrintWriter(System.out)
      logger.print(generatorInfo(config) + "\n")
    }
    else
      logger = new NixWriter(location,config)
    
  }

  def log(str: Any) = {
    if (logger == null) {
      init()

    }
    logger.println(str)
    logger.flush()
  }

  def finish(out: PrintWriter = logger) = {
    print("## This analysis finished " + new Date() + "\n")
    if (out != null) {
      out.print("## This analysis finished " + new Date() + "\n")
      out.print("## Run time: " + new TimeInterval(System.currentTimeMillis() - startTime) + "\n")
      out.close()
    }
  }
  private def classFileName() = { Thread.currentThread().getStackTrace().takeRight(1)(0).getFileName() };

  private def classInfo() = { Thread.currentThread().getStackTrace().takeRight(1)(0).getClassName() };

  private def executeEnvironment() = { this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() }

  private def getDeclaredFields(cc: AnyRef) = {
    val m = (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }
    m.toList.sortBy(_._1)
  }

  /* Added for legacy purposes */
  def generatorInfo(): String = { generatorInfo(null) };

  def generatorInfo(config: AnyRef = null): String = {
    "# Generated with    " + classInfo() + "\n" +
      "# Source code in    " + classFileName() + "\n" +
      "# Binary in         " + executeEnvironment + "\n" +
      "# Date              " + new Date() + "\n" +
      "# Working directory " + new File(".").getAbsolutePath() + "\n#\n" +
      "# Please contact Thomas Abeel for problems or questions:\n#\tt.abeel@tudelft.nl\n#\n" +
      "# Machine configuration summary: \n#\t " + "Current date and time: " + new Date() + "\n#\t " + "Number of processors: " + Integer.toString(Runtime.getRuntime().availableProcessors()) + "\n#\t " + "Free memory :" + Runtime.getRuntime().freeMemory() + "\n#\t " + "Max memory: " + Runtime.getRuntime().maxMemory() + "\n#\t " + "Total JVM: " + Runtime.getRuntime().totalMemory() + "\n#\t " + "OS: " + ManagementFactory.getOperatingSystemMXBean().getName() + " " + ManagementFactory.getOperatingSystemMXBean().getVersion() + "\n#\t " + "Architecture: " + ManagementFactory.getOperatingSystemMXBean().getArch() + "\n#\t " + "JVM version: " + System.getProperty("java.version") + "\n#\n" +
      "# Description: \n" +
      "#     " + description.split("\n").mkString("\n#     ") +
      "\n#\n" +
      "# Detailed version information: \n" +
      "#     " + version.split("\n").mkString("\n#     ") +
      (if (config != null) {
        "\n# Tool configuration: \n#     " + getDeclaredFields(config).mkString("\n#     ")
      } else "")

  }

}