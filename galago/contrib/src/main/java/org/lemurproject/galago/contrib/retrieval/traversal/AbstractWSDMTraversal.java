package org.lemurproject.galago.contrib.retrieval.traversal;

import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.util.TextPartAssigner;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nikita Zhiltsov
 */
public abstract class AbstractWSDMTraversal extends FieldedSequentialDependenceTraversal {

    public AbstractWSDMTraversal(Retrieval retrieval) {
        super(retrieval);
    }

    protected double computeWeightForDocumentFrequency(String term, Parameters queryParams) throws Exception {
        Node t = new Node("counts", term);
        t = TextPartAssigner.assignPart(t, queryParams, retrieval.getAvailableParts());
        NodeStatistics featureStats = this.retrieval.getNodeStatistics(t);
//        long totalDocumentNumber = this.fieldStats.getFieldStats().get("names").documentCount;
        return featureStats.nodeDocumentCount != 0 ? featureStats.nodeDocumentCount : 1 + 1e-10;
    }

    protected double computeWeightForDocumentFrequency(List<Node> bigram, Parameters queryParams, boolean isOrdered) throws Exception {
        long df = 0;
        if (isOrdered) {
            Node od1 = getOrderedNode(bigram, queryParams);
            NodeStatistics orderedFeatureStats = this.retrieval.getNodeStatistics(od1);
            df = orderedFeatureStats.nodeDocumentCount;
        } else {
            Node ud8 = getUnorderedNode(bigram, queryParams);
            NodeStatistics unorderedFeatureStats = this.retrieval.getNodeStatistics(ud8);
            df = unorderedFeatureStats.nodeDocumentCount;
        }
//        long totalDocumentNumber = this.fieldStats.getFieldStats().get("names").documentCount;
        return df != 0 ? df : 1 + 1e-10;
    }

    protected double computeWeightForDocumentFrequency(String term, Parameters queryParams, String field) throws Exception {
        NodeParameters par1 = new NodeParameters();
        par1.set("default", term);
        par1.set("part", "field." + field);
        Node termFieldCounts = new Node("counts", par1, new ArrayList());
        NodeStatistics featureStats = this.retrieval.getNodeStatistics(termFieldCounts);
        if (featureStats.nodeDocumentCount != 0) {
            return Math.log(featureStats.nodeDocumentCount);
        } else {
            return 1e-10;
        }
    }

    protected double computeWeightForDocumentFrequency(List<Node> bigram, Parameters queryParams, String field,
                                                       boolean isOrdered) throws Exception {
        long df = 0;
        if (isOrdered) {
            Node od1 = getOrderedNode(bigram, queryParams, field);
            NodeStatistics orderedFeatureStats = this.retrieval.getNodeStatistics(od1);
            df = orderedFeatureStats.nodeDocumentCount;
        } else {
            Node ud8 = getUnorderedNode(bigram, queryParams, field);
            NodeStatistics unorderedFeatureStats = this.retrieval.getNodeStatistics(ud8);
            df = unorderedFeatureStats.nodeDocumentCount;
        }
        return (df != 0) ? Math.log(df) : 1e-10;
    }

    protected double computeWeightForCollectionFrequency(List<Node> bigram, Parameters queryParams, String field,
                                                         boolean isOrdered) throws Exception {
        long totalCount = 0;
        if (isOrdered) {
            Node od1 = getOrderedNode(bigram, queryParams, field);
            NodeStatistics orderedFeatureStats = this.retrieval.getNodeStatistics(od1);
            totalCount = orderedFeatureStats.nodeFrequency;
        } else {
            Node ud8 = getUnorderedNode(bigram, queryParams, field);
            NodeStatistics unorderedFeatureStats = this.retrieval.getNodeStatistics(ud8);
            totalCount = unorderedFeatureStats.nodeFrequency;
        }
        if (totalCount != 0) {
            return totalCount;
        } else {
            return 1 + 1e-10;
        }
    }

    protected double computeWeightForCollectionFrequency(String term, Parameters queryParams, String field) throws Exception {
        NodeParameters par1 = new NodeParameters();
        par1.set("default", term);
        par1.set("part", "field." + field);
        Node termFieldCounts = new Node("counts", par1, new ArrayList());
        NodeStatistics featureStats = this.retrieval.getNodeStatistics(termFieldCounts);
        if (featureStats.nodeFrequency != 0) {
            return featureStats.nodeFrequency;
        } else {
            return 1 + 1e-10;
        }
    }

    protected double computeWeightForCollectionFrequency(List<Node> bigram, Parameters queryParams, boolean isOrdered) throws Exception {
        long totalCount = 0;
        if (isOrdered) {
            Node od1 = getOrderedNode(bigram, queryParams);
            NodeStatistics orderedFeatureStats = this.retrieval.getNodeStatistics(od1);
            totalCount = orderedFeatureStats.nodeFrequency;
        } else {
            Node ud8 = getUnorderedNode(bigram, queryParams);
            NodeStatistics unorderedFeatureStats = this.retrieval.getNodeStatistics(ud8);
            totalCount = unorderedFeatureStats.nodeFrequency;
        }
        if (totalCount != 0) {
            return totalCount;
        } else {
            return 1 + 1e-10;
        }
    }

    protected double computeWeightForCollectionFrequency(String term, Parameters queryParams) throws Exception {
        Node t = new Node("counts", term);
        t = TextPartAssigner.assignPart(t, queryParams, retrieval.getAvailableParts());
        NodeStatistics featureStats = this.retrieval.getNodeStatistics(t);
        if (featureStats.nodeFrequency != 0) {
            return featureStats.nodeFrequency;
        } else {
            return 1 + 1e-10;
        }
    }

    private Node getOrderedNode(List<Node> bigram, Parameters queryParams) throws IOException {
        return getBigramNode(bigram, queryParams, true, 1);
    }

    private Node getUnorderedNode(List<Node> bigram, Parameters queryParams) throws IOException {
        return getBigramNode(bigram, queryParams, false, 8);
    }

    protected String asString(List<Node> bigram) {
        String term1 = bigram.get(0).getDefaultParameter();
        String term2 = bigram.get(1).getDefaultParameter();
        return term1 + " " + term2;
    }

    private Node getBigramNode(List<Node> bigram, Parameters queryParams, boolean isOrdered, int window) throws IOException {
        String term1 = bigram.get(0).getDefaultParameter();
        String term2 = bigram.get(1).getDefaultParameter();
        Node t1 = new Node("extents", term1);
        t1 = TextPartAssigner.assignPart(t1, queryParams, retrieval.getAvailableParts());
        Node t2 = new Node("extents", term2);
        t2 = TextPartAssigner.assignPart(t2, queryParams, retrieval.getAvailableParts());

        Node bigramNode = new Node(isOrdered ? "ordered" : "unordered");
        bigramNode.getNodeParameters().set("default", window);
        bigramNode.addChild(t1);
        bigramNode.addChild(t2);
        return bigramNode;
    }

    private Node getOrderedNode(List<Node> bigram, Parameters queryParams, String field) throws IOException {
        return getBigramNode(bigram, queryParams, field, true, 1);
    }

    private Node getUnorderedNode(List<Node> bigram, Parameters queryParams, String field) throws IOException {
        return getBigramNode(bigram, queryParams, field, false, 8);
    }

    private Node getBigramNode(List<Node> bigram, Parameters queryParams, String field, boolean isOrdered, int window) throws IOException {
        String term1 = bigram.get(0).getDefaultParameter();
        String term2 = bigram.get(1).getDefaultParameter();
        // term 1
        NodeParameters par1 = new NodeParameters();
        par1.set("default", term1);
        par1.set("part", "field." + field);
        Node t1 = new Node("extents", par1);
        t1 = TextPartAssigner.assignPart(t1, queryParams, retrieval.getAvailableParts());
        // term 2
        NodeParameters par2 = new NodeParameters();
        par2.set("default", term2);
        par2.set("part", "field." + field);
        Node t2 = new Node("extents", par2);
        t2 = TextPartAssigner.assignPart(t2, queryParams, retrieval.getAvailableParts());

        Node bigramNode = new Node(isOrdered ? "ordered" : "unordered");
        bigramNode.getNodeParameters().set("default", window);
        bigramNode.addChild(t1);
        bigramNode.addChild(t2);
        return bigramNode;
    }
}

enum WSDMFeatures {
    /**
     * plays a role of bias for unigrams
     */
    CONST,
    /**
     * plays a role of bias for ordered bigrams
     */
    OD_CONST,
    /**
     * plays a role of bias for unordered bigrams
     */
    UNW_CONST,
    /**
     * document frequency
     */
    DF,
    /**
     * collection frequency
     */
    CF,
    /**
     * document frequency in 'name' field
     */
    NAME_DF,
    /**
     * collection frequency name in 'name' field
     */
    NAME_CF,
    /**
     * document frequency in 'similar entity names' field
     */
    SIMNAME_DF,
    /**
     * collection frequency in 'similar entity names' field
     */
    SIMNAME_CF,
    /**
     * document frequency in 'categories' field
     */
    CAT_DF,
    /**
     * collection frequency in 'categories' field
     */
    CAT_CF,

    ORDERED_CF,
    UNWINDOW_CF,

    ORDERED_DF,
    UNWINDOW_DF,

    ORDERED_NAME_CF,
    UNWINDOW_NAME_CF,

    ORDERED_NAME_DF,
    UNWINDOW_NAME_DF,

    ORDERED_SIMNAME_CF,
    UNWINDOW_SIMNAME_CF,

    ORDERED_SIMNAME_DF,
    UNWINDOW_SIMNAME_DF,

    ORDERED_CAT_CF,
    UNWINDOW_CAT_CF,

    ORDERED_CAT_DF,
    UNWINDOW_CAT_DF,

    PREDICATE_DF,
    PREDICATE_BIGRAM_DF;

}
