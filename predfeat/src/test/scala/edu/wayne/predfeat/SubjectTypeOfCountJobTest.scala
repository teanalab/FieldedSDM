package ru.ksu.niimm.cll.anduin.adjacency

import org.junit.runner.RunWith
import org.specs.runner.{JUnit4, JUnitSuiteRunner}
import org.specs.Specification
import com.twitter.scalding._
import ru.ksu.niimm.cll.anduin.util.FixedPathLzoTextLine

@RunWith(classOf[JUnitSuiteRunner])
class SubjectTypeOfCountJobTest extends JUnit4(SubjectTypeOfCountJobTestSpec)

object SubjectTypeOfCountJobTestSpec extends Specification with TupleConversions {
  "Subject type-of count job" should {
    JobTest("edu.wayne.predfeat.SubjectTypeOfCountJob").
      arg("triples", "triplesFile").
      arg("names", "namesFile").
      arg("output", "output").
      source(TextLine("triplesFile"), List(
        ("0", "<http://dbpedia.org/resource/Cecily_of_York> <http://dbpedia.org/ontology/spouse> <http://dbpedia.org/resource/Ralph_Scrope,_9th_Baron_Scrope_of_Masham> ."),
        ("1", "<http://dbpedia.org/resource/Bayfield_River> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Stream> ."),
        ("2", "<http://dbpedia.org/resource/River_Meon> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Stream> ."),
        ("3", "<http://dbpedia.org/resource/Nashville_Metros> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/SportsTeam> .")
      )).
      source(TypedTsv[(String, String)]("namesFile"), List(
        ("<http://dbpedia.org/ontology/SportsTeam>", "\"sports team\"@en \"sports team\"@en"),
        ("<http://dbpedia.org/ontology/Stream>", "\"stream\"@en")
      )).
      sink[(String,Int)](Tsv("output/unigram")) {
      outputBuffer =>
        "output the correct unigram counts" in {
          outputBuffer.size must_== 3
          outputBuffer mustContain ("sports", 1)
          outputBuffer mustContain ("team", 1)
          outputBuffer mustContain ("stream", 2)
        }}.
      sink[(String,Int)](Tsv("output/bigram")) {
        outputBuffer =>
        "output the correct bigram counts" in {
          outputBuffer.size must_== 1
          outputBuffer mustContain ("sports team", 1)
        }
    }.run.
      finish
  }
}
