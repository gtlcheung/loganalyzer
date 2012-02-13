import java.io.FileWriter
import scala.io.Source
import scala.collection.mutable.ListBuffer
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._


object Analyzer {
  def main(args: Array[String]) {
    val praktijkidRegex = """PR(\d+)""".r
    val foutcodeRegex = """FOUTCODE: (\d+)(-)(\d+)(-)(\d+)_(\d+)(.)+(_)(\d+)""".r
    val filesDir = "/home/gordon/Development/logging/files"

    //  val lines = Source.fromFile(args(0)).getLines.buffered // returns a bufferedIterator so you can look ahead without advancing the iterator using the head()
    //  val lines = Source.fromFile(args(0)).getLines.toArray                   //read file and put lines in an array

    // Reading files from dir
    def readFiles : Array[String] =  {
      val files = new java.io.File(filesDir).listFiles.filter(_.getName.endsWith(".log"))
      var tempList = new ListBuffer[String]
      files.foreach(file => tempList = tempList ++ Source.fromFile(file).getLines.toList)
      tempList.toArray                                               // Assign all the lines of the logfiles to an array
    }

    val lines = readFiles

    def getPrakijkId(prId :Option[String]): String = prId match {
      case Some(x) => x.tail.tail                                 //if PR123456 is matched, function returns 123456
      case None => ""
    }

    def getFoutcode(foutcode :Option[String]): String = foutcode match {
      case Some(x) => foutcode.get
      case None => ""
    }

    def getStacktrace(startLineNr : Int): String = {
      val stacktraceLength = 70;
      lines.slice(startLineNr + 1, startLineNr + stacktraceLength).mkString("", "\n", "")
    }

    def getException(startLineNr : Int): String = {
      val lineNumberOfStacktraceHeader =  lines.indexOf("Stack trace:", startLineNr + 1)
      lines(lineNumberOfStacktraceHeader + 1);                                  // Line containing the exception
    }

    //ListBuffer is mutable
    val errors = new ListBuffer[Pair[String, String]]                //List containing the foutcodeLine with the stacktrace
    val errorsWithoutStacktrace = new ListBuffer[String]             //List for showing only the foutcode line without the stacktrace
    val exceptions = new ListBuffer[String]                          //List used for counting the number of time a certain exception occurs

    RegisterJodaTimeConversionHelpers()
    val mongoConn = MongoConnection()
    val mongoCollection = mongoConn("log_analyzer")("feedbacks")

    var lineNumber = 0

//    if (args.length > 0) {
    def lineContainsPraktijkId(line: String): Boolean = {
      getPrakijkId(praktijkidRegex findFirstIn line).isEmpty
    }


    for (line <- lines) {
        // to prevent index out of bounds exception
        if (lineNumber < lines.length - 1) {

          //If the line of the feedback contains a praktijkID or the nextLine contains a praktijkId
          if ((!lineContainsPraktijkId(line) || ( lines(lineNumber + 1).contains("PraktijkId:")
          || ( lines(lineNumber + 1).contains("Stack trace:") && ( !lines(lineNumber + 2).contains("GWTClientSideException:") || !lines(lineNumber + 3).contains("null") ) ) ))
          && !getFoutcode(foutcodeRegex findFirstIn line).isEmpty) {
            val (foutcodeLine, stacktrace) = (lines(lineNumber), getStacktrace(lineNumber))  //tuple containing the foutcode and stacktrace

            if (lines(lineNumber + 1).contains("PraktijkId:")) {
              val extendedFoutcodeLine = lines(lineNumber + 1) + " " + foutcodeLine          //praktijkId is prepended to the foutcodeLine
//              mongoCollection += MongoDBObject("praktijkId" -> lines(lineNumber + 1), "foutcode" -> foutcodeLine)
              mongoCollection.insert(MongoDBObject("praktijkId" -> lines(lineNumber + 1), "foutcode" -> foutcodeLine))

              exceptions += getException(lineNumber + 1)

              errors += extendedFoutcodeLine -> stacktrace                                    // another way of creating a tuple
              errorsWithoutStacktrace += extendedFoutcodeLine
            } else {
              errors += foutcodeLine -> stacktrace
              errorsWithoutStacktrace += foutcodeLine
            }
          }
        }
        lineNumber += 1
      }
      
      val result = mongoCollection.find()

      val feedbackList = errors.toList                                    // contains stacktrace
      val feedbackListWithoutStacktrace = errorsWithoutStacktrace.toList

      val fileWriter = new FileWriter(filesDir + "/output.txt");

      //prints only unique elements of the sorted list
      feedbackList.distinct.sortWith((pair1, pair2) => pair1._1 < pair2._1 ).foreach(pair => {
        println(pair._1)       // Foutcode line
        println(pair._2)       // stacktrace
        println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------")
        
        fileWriter.write(pair._1 + "\n")
        fileWriter.write(pair._2 + "\n")
        fileWriter.write("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n")
        }
      )

      feedbackListWithoutStacktrace.distinct.sortWith(_ < _).foreach(println)
      feedbackListWithoutStacktrace.distinct.sortWith(_ < _).foreach(line => fileWriter.write(line + "\n"))


      println("Number of unique feedbacks: " + feedbackList.distinct.length)
      println("number of lines: " + lineNumber)
      println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------")
      println("Number of exceptions (feedback has praktijkId): " + exceptions.length)

      fileWriter.write("Number of unique feedbacks: " + feedbackList.distinct.length + "\n")
      fileWriter.write("number of lines: " + lineNumber + "\n")
      fileWriter.write("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n")
      fileWriter.write("Number of exceptions (feedback has praktijkId): " + exceptions.length + "\n")

      
      exceptions.groupBy(x => x).mapValues(_.length).toSeq.sortWith(_._2 > _._2).foreach(println)                  //counts the occurences of exceptions found in the stacktrace of feedbacks
      exceptions.groupBy(x => x).mapValues(_.length).toSeq.sortWith(_._2 > _._2).foreach(line => fileWriter.write(line + "\n"))
      
      println("Feedbacks are written to the file: output.txt (inside logfiles dir)")

      fileWriter.close()

//    } else Console.err.println("please provide a directory containing the logfile(s)!")

  }
}


//val longestLine = lines.reduceLeft(
//  (a, b) => if (a.length > b.length) a else b
//)
//
//println(longestLine)



//      // sort a mutable map by it keys
//      val t = Map[String, String]()
//       t += ("test" -> "ok")
//       t +=  ("nederland" -> "amsterdam")
//      t.toList sortBy {_._1}
//      println(t.toList sortBy {_._1})
