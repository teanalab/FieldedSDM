package edu.wayne.predfeat

import java.io.StringReader
import scala.annotation.tailrec
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import ru.ksu.niimm.cll.belegaer.lucene.DbpediaLiteralAnalyzer

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
}
