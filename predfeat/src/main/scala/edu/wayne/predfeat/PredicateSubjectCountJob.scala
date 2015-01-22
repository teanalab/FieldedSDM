package edu.wayne.predfeat

import com.twitter.scalding._
import ru.ksu.niimm.cll.anduin.util.NodeParser._

class PredicateSubjectCountJob(args : Args) extends Job(args) {
  private val subjectPredicates =
    TextLine(args("triples")).read.filter('line) {
      line: String =>
      val cleanLine = line.trim
      cleanLine.startsWith("<")
    }
      .mapTo('line ->('subject, 'predicate, 'object))(extractNodesFromN3)
      .unique(('subject, 'predicate))

  private val names = TypedTsv[(String, String)](args("names")).read.rename((0, 1) -> ('predicateUrl, 'predicateName))

  private val subjectsWithNames = subjectPredicates.joinWithSmaller('predicate -> 'predicateUrl, names)

  private val unigramsPredicates = subjectsWithNames.flatMap('predicateName -> 'unigram)
  { predicateName : String => Util.tokens(predicateName) }

  private val bigramsPredicates = subjectsWithNames.flatMap('predicateName -> 'bigram)
  { predicateName : String => Util.tokens(predicateName).sliding(2).filter(_.size == 2).map(_.mkString(" ")) }

  private val unigramsCount = unigramsPredicates
    .unique(('unigram, 'subject))
    .groupBy('unigram) {_.size}

  unigramsCount.write(Tsv(args("unigram")))

  private val bigramsCount = bigramsPredicates
    .unique(('bigram, 'subject))
    .groupBy('bigram) {_.size}

  bigramsCount.write(Tsv(args("bigram")))

}
