package edu.wayne.predfeat

import com.twitter.scalding._

class SubjectTypeOfCountJob(args : Args) extends Job(args) {
  private val rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"

  private val typeOfs =
    Util.triplesPipe(args("triples"))
      .filter('predicate) { predicate : String => predicate == rdfType }
      .unique(('object, 'subject))

  private val names = Util.namesPipe(args("names"))

  private val withNames = typeOfs.joinWithSmaller('object -> 'url, names)

  private val unigramsObjects = Util.unigramsPipe(withNames)

  private val bigramsObjects = Util.bigramsPipe(withNames)

  private val unigramsCount = unigramsObjects
    .unique(('unigram, 'subject))
    .groupBy('unigram) {_.size}

  unigramsCount.write(Tsv(args("output") + "/unigram"))

  private val bigramsCount = bigramsObjects
    .unique(('bigram, 'subject))
    .groupBy('bigram) {_.size}

  bigramsCount.write(Tsv(args("output") + "/bigram"))
}
