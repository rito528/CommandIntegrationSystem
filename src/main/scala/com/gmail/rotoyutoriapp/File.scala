package com.gmail.rotoyutoriapp

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter, PrintWriter}
import java.nio.file.{Files, Paths}
import scala.io.Source

object File {

  def dirCheck(dirPath: String): Unit = {
    val file = Paths.get(dirPath)
    if (Files.notExists(file)) file.toFile.mkdirs()
  }

  def isExistsFile(filePath: String): Boolean = Paths.get(filePath).toFile.exists()

  def createFile(filePath: String): Unit = Files.createFile(Paths.get(filePath))

  def deleteFile(filePath: String): Unit = Files.delete(Paths.get(filePath))

  def readCommandFile(cmdName: String): Array[String] = {
    var data = Array.empty[String]
    val source = Source.fromFile("plugins/CommandIntegrationSystem/CommandLists/" + cmdName,"UTF-8")
    source.getLines().foreach(line => data :+= line)
    source.close()
    data
  }

  def writeCommandFile(cmdName:String,data:Array[String],addCmd: String,line: Int): Unit = {
    val pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plugins/CommandIntegrationSystem/CommandLists/" + cmdName), "UTF-8")))
    for (i <- data.indices) {
      if (i == line - 1) pw.println(addCmd)
      pw.println(data(i))
    }
    //lineが-1だった場合は最終行に追加
    if (line == -1) pw.println(addCmd)
    pw.close()
  }

  def deleteCommandFileLine(cmdName:String,data:Array[String],deleteLine: Int): Unit = {
    val pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plugins/CommandIntegrationSystem/CommandLists/" + cmdName), "UTF-8")))
    for (i <- data.indices) if (i != deleteLine - 1) pw.println(data(i))
    pw.close()
  }

}
