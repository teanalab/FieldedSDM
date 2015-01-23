package edu.wayne.predfeat

import java.io.StringReader
import scala.annotation.tailrec
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import com.twitter.scalding._
import Dsl._
import cascading.flow.FlowDef
import ru.ksu.niimm.cll.belegaer.lucene.DbpediaLiteralAnalyzer
import ru.ksu.niimm.cll.anduin.util.NodeParser._

object Util {
  def read(tokenStream: TokenStream): List[String] = read(List.empty, tokenStream)

  @tailrec
  def read(accum: List[String], tokenStream: TokenStream): List[String] = if (!tokenStream.incrementToken) accum
  else read(accum :+ tokenStream.getAttribute(classOf[CharTermAttribute]).toString, tokenStream)

  def tokens(text: String): List[String] = {
    val analyzer = new DbpediaLiteralAnalyzer(1)
    val tokenStream: TokenStream = analyzer.tokenStream("", new StringReader(removeEn(text)))
    tokenStream.reset
    val tokens = read(tokenStream)
    tokenStream.end
    tokenStream.close
    tokens
  }

  def removeEn(text: String): String = {
    text.replaceAll("\"([^\"]*)\"@en", "$1")
  }

  def triplesPipe(arg: String)(implicit mode: Mode, fd: FlowDef) = {
    TextLine(arg).read.filter('line) {
      line: String =>
      val cleanLine = line.trim
      cleanLine.startsWith("<")
    }
      .mapTo('line ->('subject, 'predicate, 'object))(extractNodesFromN3)
  }

  def namesPipe(arg: String)(implicit mode: Mode, fd: FlowDef) = {
    TypedTsv[(String, String)](arg).read.rename((0, 1) -> ('url, 'name))
  }

  def unigramsPipe(pipe: RichPipe) = {
    pipe.flatMap('name -> 'unigram)
      { predicateName : String => Util.tokens(predicateName) }
  }

  def bigramsPipe(pipe: RichPipe) = {
    pipe.flatMap('name -> 'bigram)
      { name : String => Util.tokens(name).sliding(2).filter(_.size == 2).map(_.mkString(" ")) }
  }
}
