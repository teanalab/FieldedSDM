package edu.wayne.predfeat

import com.twitter.scalding._
import ru.ksu.niimm.cll.anduin.util.NodeParser._

class PredicateSubjectCountJob(args : Args) extends Job(args) {
  private val subjectPredicates =
    Util.triplesPipe(args("triples"))
      .unique(('subject, 'predicate))

  private val names = Util.namesPipe(args("names"))

  private val withNames = subjectPredicates.joinWithSmaller('predicate -> 'url, names)

  private val unigramsPredicates = Util.unigramsPipe(withNames)

  private val bigramsPredicates = Util.bigramsPipe(withNames)

  private val unigramsCount = unigramsPredicates
    .unique(('unigram, 'subject))
    .groupBy('unigram) {_.size}

  unigramsCount.write(Tsv(args("unigram")))

  private val bigramsCount = bigramsPredicates
    .unique(('bigram, 'subject))
    .groupBy('bigram) {_.size}

  bigramsCount.write(Tsv(args("bigram")))

}
