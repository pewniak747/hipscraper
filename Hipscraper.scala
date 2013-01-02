import scala.collection.JavaConverters._

import org.scala_tools.time.Imports._

import java.io.Console
import java.io.PrintWriter
import java.io.File

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.htmlunit.HtmlUnitDriver

object Hipscraper {
  def main(args: Array[String]) {
    val email = System.getenv("HIPSCRAPER_EMAIL")
    val room = System.getenv("HIPSCRAPER_ROOM")
    val password = System.getenv("HIPSCRAPER_PASSWORD")

    var driver = new HtmlUnitDriver()

    driver.get("https://www.hipchat.com/sign_in")
    var emailField = driver.findElement(By.name("email"))
    emailField.sendKeys(email)
    var passwordField = driver.findElement(By.name("password"))
    passwordField.sendKeys(password)
    passwordField.submit()

    var allWords = List[String]()
    var year = Integer.parseInt(System.getenv("HIPSCRAPER_YEAR"))
    var date = new DateTime(year, 1, 1, 0, 1)
    val endDay = if(year == DateTime.now.getYear) DateTime.now.getDayOfYear else 365
    for(i <- 1 to endDay) {
      System.err.println("Munching words for %s...".format(date))
      val historyURL = "https://hipchat.com/history/room/%s/%d/%d/%d".format(room, date.getYear, date.getMonthOfYear, date.getDayOfMonth)
      driver.get(historyURL)

      allWords ++= driver.findElements(By.xpath("//div[(contains(@class, 'chatBlock')) and (not(contains(@class, 'systemMessage')))]//p[contains(@class, 'msgText')]")).asScala.map(_.getText.split("""\s+""").map(_.toLowerCase)).flatten

      date = date.plusDays(1)
    }

    System.err.println("Normalizing words...")
    var normalizedWords = allWords.map((wrd) => wrd.toLowerCase.replaceAll("""[^\p{L}\p{N}]""", ""))

    System.err.println("Filtering words...")
    var filteredWords = normalizedWords.filter((wrd) => wrd.length > 2 && !wrd.startsWith("http"))

    System.err.println("Map-reducing words...")
    var mapReducedWords = filteredWords.groupBy(identity).mapValues(_.size)

    System.err.println("Sorting words...")
    var sortedWords = mapReducedWords.toList.sortBy { -_._2 }

    System.err.println(sortedWords)

    val writer = new PrintWriter(new File("words.csv"))
    writer.write(sortedWords.map((wrd) => "\"%s\",%s".format(wrd._1, wrd._2)).mkString("\n"))
    writer.close()
  }
}
