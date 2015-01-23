package ru.ksu.niimm.cll.anduin.adjacency

import org.junit.runner.RunWith
import org.specs.runner.{JUnit4, JUnitSuiteRunner}
import org.specs.Specification
import com.twitter.scalding._
import ru.ksu.niimm.cll.anduin.util.FixedPathLzoTextLine

@RunWith(classOf[JUnitSuiteRunner])
class PredicateSubjectCountJobTest extends JUnit4(PredicateSubjectCountJobTestSpec)

object PredicateSubjectCountJobTestSpec extends Specification with TupleConversions {
  "Predicate subject count job" should {
    JobTest("edu.wayne.predfeat.PredicateSubjectCountJob").
      arg("triples", "triplesFile").
      arg("names", "namesFile").
      arg("unigram", "unigramFile").
      arg("bigram", "bigramFile").
      source(TextLine("triplesFile"), List(
        ("0", "<http://dbpedia.org/resource/Cecily_of_York> <http://dbpedia.org/ontology/spouse> <http://dbpedia.org/resource/Ralph_Scrope,_9th_Baron_Scrope_of_Masham> ."),
        ("1", "<http://dbpedia.org/resource/Cecily_of_York> <http://dbpedia.org/ontology/spouse> <http://dbpedia.org/resource/John_Welles,_1st_Viscount_Welles> ."),
        ("2", "<http://dbpedia.org/resource/Isabella_of_Aragon,_Queen_of_Portugal> <http://dbpedia.org/ontology/spouse> <http://dbpedia.org/resource/Afonso,_Prince_of_Portugal> ."),
        ("3", "<http://ca.dbpedia.org/resource/Timeu> <http://dbpedia.org/ontology/wikiPageDisambiguates> <http://ca.dbpedia.org/resource/Timeu_(di\u00E0leg)> ."),
        ("4", "<http://dbpedia.org/resource/one> <http://dbpedia.org/ontology/predone> <http://dbpedia.org/resource/two> ."),
        ("5", "<http://dbpedia.org/resource/one> <http://dbpedia.org/ontology/predtwo> <http://dbpedia.org/resource/two> .")
      )).
      source(TypedTsv[(String, String)]("namesFile"), List(
        ("<http://dbpedia.org/ontology/spouse>", "\"spouse\"@en"),
        ("<http://dbpedia.org/ontology/wikiPageDisambiguates>", "\"Wikipage disambiguates\"@en"),
        ("<http://dbpedia.org/ontology/predone>", "\"wikipedia\"@en"),
        ("<http://dbpedia.org/ontology/predtwo>", "\"wikipedia page\"@en")
      )).
      sink[(String,Int)](Tsv("unigramFile")) {
      outputBuffer =>
        "output the correct unigram counts" in {
          outputBuffer.size must_== 5
          outputBuffer mustContain ("spouse", 2)
          outputBuffer mustContain ("wikipage", 1)
          outputBuffer mustContain ("disambiguate", 1)
          outputBuffer mustContain ("wikipedia", 1)
          outputBuffer mustContain ("page", 1)
        }}.
      sink[(String,Int)](Tsv("bigramFile")) {
        outputBuffer =>
        "output the correct bigram counts" in {
          outputBuffer.size must_== 2
          outputBuffer mustContain ("wikipage disambiguate", 1)
          outputBuffer mustContain ("wikipedia page", 1)
        }
    }.run.
      finish
  }
}
