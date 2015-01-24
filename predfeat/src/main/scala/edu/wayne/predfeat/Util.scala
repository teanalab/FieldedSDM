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
    val tokenStream: TokenStream = analyzer.tokenStream("", new StringReader(text))
    tokenStream.reset
    val tokens = read(tokenStream)
    tokenStream.end
    tokenStream.close
    tokens
  }

  def tokenizedNames(text: String) : Iterable[Iterable[String]] = {
    (for (m <- """\"([^\"]*)\"@en""".r findAllMatchIn text) yield tokens(m group 1)).toIterable
  }

  def urlToName(text: String) : Iterable[Iterable[String]] = {
    List(tokens("[A-Z\\d]".r.replaceAllIn(stripURI(text),
      {m => " " + m.group(0).toLowerCase()})))
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

  def unigramsPipe(pipe: RichPipe, namesExtractor: String => Iterable[Iterable[String]] = tokenizedNames) = {
    pipe.flatMap('name -> 'unigram)
      { predicateName : String => namesExtractor(predicateName).flatten }
  }

  def bigramsPipe(pipe: RichPipe, namesExtractor: String => Iterable[Iterable[String]] = tokenizedNames) = {
    pipe.flatMap('name -> 'bigram)
      { name : String => namesExtractor(name).flatMap(_.sliding(2).filter(_.size == 2)).map(_.mkString(" ")) }
  }
}
