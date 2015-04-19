package org.lemurproject.galago.contrib.retrieval.traversal;

import org.lemurproject.galago.core.index.stats.FieldStatistics;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.retrieval.traversal.Traversal;
import org.lemurproject.galago.core.util.TextPartAssigner;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nikita Zhiltsov
 */
public class MLMTraversal extends Traversal {
    public static final String UNIGRAM_FIELD_PREFIX = "uni-";
    protected final List<String> fields;
    protected final Parameters fieldWeights;
    protected final Retrieval retrieval;
    protected final FieldStats fieldStats;
    protected final Parameters globals;

    public MLMTraversal(Retrieval retrieval) {
        this.retrieval = retrieval;
        this.globals = retrieval.getGlobalParameters();
        if (globals.isList("fields", String.class)) {
            this.fields = (List<String>) globals.getAsList("fields");
        } else {
            throw new IllegalArgumentException("MLMTraversal requires having 'fields' parameter initialized");
        }
        fieldStats = new FieldStats();
        this.fieldWeights = globals.isMap("fieldWeights") ? globals.getMap("fieldWeights") : null;
    }

    @Override
    public void beforeNode(Node original, Parameters queryParameters) throws Exception {
    }

    @Override
    public Node afterNode(Node original, Parameters qp) throws Exception {
        if (original.getOperator().equals("mlm")) {
            NodeParameters np = original.getNodeParameters();
            List<Node> children = original.getInternalNodes();
            NodeParameters unigramWeights = new NodeParameters();
            List<Node> unigramNodes = new ArrayList<Node>();
            addUnigramNodes(original, qp, np, children, unigramWeights, unigramNodes);
            Node root = new Node("combine", new NodeParameters(), unigramNodes, original.getPosition());
            root.getNodeParameters().set("norm", false);
            return root;
        } else {
            return original;
        }
    }

    protected double computeWeight(String term, NodeParameters np, Parameters qp) throws Exception {
        return 1.0;
    }

    protected void addUnigramNodes(Node original,
                                   Parameters qp,
                                   NodeParameters np,
                                   List<Node> children,
                                   NodeParameters sdmWeights,
                                   List<Node> sdmNodes) throws Exception {
        for (Node child : children) {
            String term = child.getDefaultParameter();
            double weight = children.size() == 1 ? 1.0 : computeWeight(term, np, qp);
            if (weight != 0) {
                sdmWeights.set(Integer.toString(sdmNodes.size()), weight);
                Node unigramNode = getUnigramNode(original, qp, term);
                sdmNodes.add(unigramNode);
            }
        }
    }

    protected Node getUnigramNode(Node original, Parameters queryParameters, String term) throws Exception {
        String scorerType = queryParameters.get("scorer", globals.get("scorer", "dirichlet"));

        ArrayList<Node> termFields = new ArrayList<Node>();
        NodeParameters nodeweights = new NodeParameters();
        int i = 0;
        double normalizer = 0.0;
        for (String field : fields) {
            Node termFieldCounts, termExtents;

            // if we have access to the correct field-part:
            if (this.retrieval.getAvailableParts().containsKey("field." + field)) {
                NodeParameters par1 = new NodeParameters();
                par1.set("default", term);
                par1.set("part", "field." + field);
                termFieldCounts = new Node("counts", par1, new ArrayList());
            } else {
                // otherwise use an #inside op
                NodeParameters par1 = new NodeParameters();
                par1.set("default", term);
                termExtents = new Node("extents", par1, new ArrayList());
                termExtents = TextPartAssigner.assignPart(termExtents, globals, this.retrieval.getAvailableParts());

                termFieldCounts = new Node("inside");
                termFieldCounts.addChild(StructuredQuery.parse("#extents:part=extents:" + field + "()"));
                termFieldCounts.addChild(termExtents);
            }

            double fieldWeight = 0.0;
            if (fieldWeights != null && fieldWeights.containsKey(UNIGRAM_FIELD_PREFIX + field)) {
                fieldWeight = fieldWeights.getDouble(UNIGRAM_FIELD_PREFIX + field);
            } else {
                fieldWeight = queryParameters.get(UNIGRAM_FIELD_PREFIX + field, 0.0);
            }
            nodeweights.set(Integer.toString(i), fieldWeight);
            normalizer += fieldWeight;

            Node termScore = new Node(scorerType);
            termScore.getNodeParameters().set("lengths", field);
            termScore.addChild(fieldStats.fieldLenNodes.get(field).clone());
            termScore.addChild(termFieldCounts);
            termFields.add(termScore);
            i++;
        }
        // normalize field weights
        if (normalizer != 0) {
            for (i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                nodeweights.set(key, nodeweights.getDouble(key) / normalizer);
            }
        }

        return new Node("wsum", nodeweights, termFields);
    }

    protected class FieldStats {
        private final Map<String, FieldStatistics> fieldStats = new HashMap();
        private final Map<String, Node> fieldLenNodes = new HashMap();

        FieldStats() {
            if (fields == null) throw new IllegalStateException("Fields must be initialized");
            if (retrieval == null) throw new IllegalStateException("Retrieval must be initialized");
            try {
                for (String field : fields) {
                    Node fieldLen = StructuredQuery.parse("#lengths:" + field + ":part=lengths()");
                    FieldStatistics fieldStat = retrieval.getCollectionStatistics(fieldLen);
                    fieldStats.put(field, fieldStat);
                    fieldLenNodes.put(field, fieldLen);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected Map<String, Node> getFieldLenNodes() {
            return fieldLenNodes;
        }

        protected Map<String, FieldStatistics> getFieldStats() {
            return fieldStats;
        }
    }
}
