import scala.collection.JavaConverters._

import org.scala_tools.time.Imports._

import java.io.Console
import java.io.PrintWriter
import java.io.File

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.htmlunit.HtmlUnitDriver

import com.codahale.jerkson.Json._

object Hipscraper {
  def main(args: Array[String]) {
    val email = System.getenv("HIPSCRAPER_EMAIL")
    val room = System.getenv("HIPSCRAPER_ROOM")
    val password = System.getenv("HIPSCRAPER_PASSWORD")

    var driver = new HtmlUnitDriver()

    driver.get("http://pl.wiktionary.org/wiki/Indeks:Polski_-_Najpopularniejsze_s%C5%82owa_1-1000")
    val commonWords = driver.findElements(By.cssSelector("#mw-content-text a")).asScala.map(_.getText).toSet

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
      val historyURL = "https://hipchat.com/history/room/%s/%02d/%02d/%02d".format(room, date.getYear, date.getMonthOfYear, date.getDayOfMonth)
      driver.get(historyURL)

      allWords ++= driver.findElements(By.xpath("//div[(contains(@class, 'chatBlock')) and (not(contains(@class, 'systemMessage')))]//p[contains(@class, 'msgText')]")).asScala.map(_.getText.split("""\s+""").map(_.toLowerCase)).flatten

      date = date.plusDays(1)
    }

    System.err.println("Normalizing words...")
    var normalizedWords = allWords.map((wrd) => wrd.toLowerCase.replaceAll("""[^\p{L}\p{N}]""", ""))

    System.err.println("Filtering words...")
    var filteredWords = normalizedWords.filter((wrd) => wrd.length > 2 && !wrd.startsWith("http") && !commonWords.contains(wrd))

    System.err.println("Map-reducing words...")
    var mapReducedWords = filteredWords.groupBy(identity).mapValues(_.size)

    System.err.println("Sorting words...")
    var sortedWords = mapReducedWords.toList.filter(_._2 >= 10).sortBy { -_._2 }

    val generatedJSON = generate(Map("name" -> "words", "children" -> sortedWords.map((tuple)=>Map("name" -> tuple._1, "size" -> tuple._2))))
    System.out.println(generatedJSON)
  }
}
