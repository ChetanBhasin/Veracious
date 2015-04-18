package spark

import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class BasicSparkTest extends Specification {

  "Application" should {

    "be able to connect to spark and do basic comparison" in new WithApplication {

      val conf = new SparkConf().setAppName("Test-Application-Veracious").setMaster("local")
      val sc = new SparkContext(conf)

      val textFile = sc.textFile("test/resources/testText.txt")
      textFile.first must equalTo("This is a text file.")
    }
  }

}
