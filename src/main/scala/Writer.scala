import collection.mutable.ListBuffer
import java.io.FileWriter

object Writer {
  val outputFile = "/home/gordon/Development/logging/files/output.txt"

  def writeToFile(lineNumber: Int, feedbackList: List[(String, String)] ,
                  feedbackListWithoutStacktrace: List[String], exceptions: ListBuffer[String]) {
    val fileWriter = new FileWriter(outputFile);

    //prints only unique elements of the sorted list
    feedbackList.distinct.sortWith((pair1, pair2) => pair1._1 < pair2._1).foreach(pair => {
      println(pair._1) // Foutcode line
      println(pair._2) // stacktrace
      println("-----------------------------------------------------------------------types---------------------------------------------------------------------------------------------------------")

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


    exceptions.groupBy(x => x).mapValues(_.length).toSeq.sortWith(_._2 > _._2).foreach(println) //counts the occurences of exceptions found in the stacktrace of feedbacks
    exceptions.groupBy(x => x).mapValues(_.length).toSeq.sortWith(_._2 > _._2).foreach(line => fileWriter.write(line + "\n"))

    println("Feedbacks are written to the file: output.txt (inside logfiles dir)")

    fileWriter.close()
  }
}
