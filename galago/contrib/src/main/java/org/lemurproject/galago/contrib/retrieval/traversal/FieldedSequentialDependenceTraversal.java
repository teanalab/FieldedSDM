package org.lemurproject.galago.contrib.retrieval.traversal;

import org.apache.commons.lang.math.NumberUtils;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * For queries like "#fieldedsdm:uw.attributes.width=8:uw.width=4(president barack obama)"
 *
 * @author Nikita Zhiltsov
 */
public class FieldedSequentialDependenceTraversal extends MLMTraversal {

    public static final String ORDERED_FIELD_PREFIX = "od-";
    public static final String UNWINDOW_FIELD_PREFIX = "uww-";

    private final int windowLimitDefault;
    private final double unigramDefault;
    private final double orderedDefault;
    private final double unorderedDefault;

    private final String odOp;
    private final int odWidth;
    private final String uwOp;
    private final int uwWidth;

    public FieldedSequentialDependenceTraversal(Retrieval retrieval) {
        super(retrieval);

        unigramDefault = globals.get("uniw", 0.8);
        orderedDefault = globals.get("odw", 0.1);
        unorderedDefault = globals.get("uww", 0.1);
        windowLimitDefault = (int) globals.get("windowLimit", 2);

        odOp = globals.get("sdm.od.op", "ordered");
        odWidth = (int) globals.get("sdm.od.width", 1);

        uwOp = globals.get("sdm.uw.op", "unordered");
        uwWidth = (int) globals.get("sdm.uw.width", 8);

    }

    @Override
    public Node afterNode(Node original, Parameters qp) throws Exception {
        if (original.getOperator().equals("fieldedsdm")) {
            return buildSDMNode(original, qp);
        } else {
            return original;
        }
    }

    protected Node buildSDMNode(Node original, Parameters qp) throws Exception {
        NodeParameters np = original.getNodeParameters();
        List<Node> children = original.getInternalNodes();

        NodeParameters sdmWeights = new NodeParameters();
        List<Node> sdmNodes = new ArrayList<Node>();

        addUnigramNodes(original, qp, np, children, sdmWeights, sdmNodes);

        addBigramNodes(original, qp, np, children, sdmWeights, sdmNodes);

        return new Node("combine", sdmWeights, sdmNodes, original.getPosition());
    }

    protected void addBigramNodes(Node original, Parameters qp, NodeParameters np, List<Node> children, NodeParameters sdmWeights, List<Node> sdmNodes) throws Exception {
        int windowLimit = (int) qp.get("windowLimit", windowLimitDefault);
        windowLimit = (int) np.get("windowLimit", windowLimit);
        for (int n = 2; n <= windowLimit; n++) {
            for (int i = 0; i < (children.size() - n + 1); i++) {
                List<Node> termSequence = children.subList(i, i + n);
                double odWeight = computeWeight(termSequence, np, qp, true);
                BigramNodes bigramNodes = getBigramNodes(original, qp, termSequence);
                if (odWeight != 0) {
                    sdmWeights.set(Integer.toString(sdmNodes.size()), odWeight);
                    sdmNodes.add(bigramNodes.ordered);
                }
                double uwWeight = computeWeight(termSequence, np, qp, false);
                if (uwWeight != 0) {
                    sdmWeights.set(Integer.toString(sdmNodes.size()), uwWeight);
                    sdmNodes.add(bigramNodes.unordered);
                }
            }
        }
    }

    protected BigramNodes getBigramNodes(Node original, Parameters qp, List<Node> seq) throws Exception {
        NodeParameters np = original.getNodeParameters();

        NodeParameters orderedFieldWeights = new NodeParameters();
        double odNormalizer = 0.0;
        NodeParameters unwindowFieldWeights = new NodeParameters();
        double uwwNormalizer = 0.0;
        for (int i = 0; i < fields.size(); i++) {
            double odFieldWeight = 0.0;
            double uwdFieldWeight = 0.0;
            if (this.fieldWeights != null && this.fieldWeights.containsKey(ORDERED_FIELD_PREFIX + fields.get(i))) {
                odFieldWeight = this.fieldWeights.getDouble(ORDERED_FIELD_PREFIX + fields.get(i));
            } else {
                odFieldWeight = qp.get(ORDERED_FIELD_PREFIX + fields.get(i), 0.0);
            }
            if (this.fieldWeights != null && this.fieldWeights.containsKey(UNWINDOW_FIELD_PREFIX + fields.get(i))) {
                uwdFieldWeight = this.fieldWeights.getDouble(UNWINDOW_FIELD_PREFIX + fields.get(i));
            } else {
                uwdFieldWeight = qp.get(UNWINDOW_FIELD_PREFIX + fields.get(i), 0.0);
            }
            orderedFieldWeights.set(Integer.toString(i), odFieldWeight);
            odNormalizer += odFieldWeight;
            unwindowFieldWeights.set(Integer.toString(i), uwdFieldWeight);
            uwwNormalizer += uwdFieldWeight;
        }
        // normalize field weights
        if (odNormalizer != 0) {
            for (int i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                orderedFieldWeights.set(key, orderedFieldWeights.getDouble(key) / odNormalizer);
            }
        }
        if (uwwNormalizer != 0) {
            for (int i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                unwindowFieldWeights.set(key, unwindowFieldWeights.getDouble(key) / uwwNormalizer);
            }
        }

        String scorerType = qp.get("scorer", globals.get("scorer", "dirichlet"));
        List<Node> orderedBigramFields = new ArrayList<Node>();
        List<Node> unorderedBigramFields = new ArrayList<Node>();
        for (String field : fields) {
            Node orderedOperationNode = new Node(odOp, new NodeParameters(np.get("od.width", odWidth)));
            long unorderedWindow = np.get(("uw." + field + ".width"), np.get("uw.width", uwWidth));
            Node unorderedOperationNode = new Node(uwOp, new NodeParameters(unorderedWindow));
            for (Node t : seq) {
                String inFieldTerm = t.getNodeParameters().getAsSimpleString("default");
                if (NumberUtils.isNumber(inFieldTerm)) inFieldTerm = "@/" + inFieldTerm + "/";
                orderedOperationNode.addChild(StructuredQuery.parse("#extents:" + inFieldTerm + ":part=field." + field + "()"));
                unorderedOperationNode.addChild(StructuredQuery.parse("#extents:" + inFieldTerm + ":part=field." + field + "()"));
            }
            Node orderedBigramScore = new Node(scorerType);
            orderedBigramScore.getNodeParameters().set("lengths", field);
            orderedBigramScore.addChild(fieldStats.getFieldLenNodes().get(field).clone());
            orderedBigramScore.addChild(orderedOperationNode);
            orderedBigramFields.add(orderedBigramScore);

            Node unorderedBigramScore = new Node(scorerType);
            unorderedBigramScore.getNodeParameters().set("lengths", field);
            unorderedBigramScore.addChild(fieldStats.getFieldLenNodes().get(field).clone());
            unorderedBigramScore.addChild(unorderedOperationNode);
            unorderedBigramFields.add(unorderedBigramScore);
        }

        Node orderedNode = new Node("wsum", orderedFieldWeights, orderedBigramFields);
        Node unorderedNode = new Node("wsum", unwindowFieldWeights, unorderedBigramFields);
        return new BigramNodes(orderedNode, unorderedNode);
    }

    protected double computeWeight(String term, NodeParameters np, Parameters qp) throws Exception {
        double unigramW = qp.get("uniw", unigramDefault);
        unigramW = np.get("uniw", unigramW);
        return unigramW;
    }

    protected double computeWeight(List<Node> bigram, NodeParameters np, Parameters qp, boolean isOrdered) throws Exception {
        if (isOrdered) {
            double orderedW = qp.get("odw", orderedDefault);
            orderedW = np.get("odw", orderedW);
            return orderedW;
        } else {
            double unorderedW = qp.get("uww", unorderedDefault);
            unorderedW = np.get("uww", unorderedW);
            return unorderedW;
        }
    }

    protected class BigramNodes {
        protected final Node ordered;
        protected final Node unordered;

        public BigramNodes(Node ordered, Node unordered) {
            this.ordered = ordered;
            this.unordered = unordered;
        }
    }

}
