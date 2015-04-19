package org.lemurproject.galago.contrib.retrieval.traversal;

import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Nikita Zhiltsov
 */
public class FieldedWSDMTraversal extends AbstractWSDMTraversal {
    private static final Logger logger = Logger.getLogger("EWSDM");

    private final Parameters wsdmWeights;

    private final Map<String, Double> predicateUnigramDF = new HashMap<String, Double>();

    private final Map<String, Double> predicateBigramDF = new HashMap<String, Double>();;

    public FieldedWSDMTraversal(Retrieval retrieval) {
        super(retrieval);
        this.wsdmWeights = globals.isMap("wsdm") ? globals.getMap("wsdm") : null;
//        predicateUnigramDF = readFeatureMap(new BufferedReader(
//                new InputStreamReader(this.getClass().getResourceAsStream("/predicate.name.unigram.feature.txt"))));
//        predicateBigramDF = readFeatureMap(new BufferedReader(
//                new InputStreamReader(this.getClass().getResourceAsStream("/predicate.name.bigram.feature.txt"))));
    }

    private Map<String, Double> readFeatureMap(BufferedReader reader) {
        Map<String, Double> featureMap = new HashMap<String, Double>();
        String line;
        try {
            while (null != (line = reader.readLine())) {
                String[] values = line.split("\t");
                if (values.length != 2)
                    throw new IllegalArgumentException("Found a malformed line in the feature map input: " + line);
                String featureKey = values[0];
                double featureValue = Math.log(java.lang.Double.parseDouble(values[1]));
                featureMap.put(featureKey, featureValue);
            }
            reader.close();
        } catch (IOException ex) {
            logger.severe("Failed to read the feature map due to: " + ex.getMessage());
        }
        return featureMap;
    }

    private double weight(WSDMFeatures feature, Parameters queryParams) {
        double wsdmWeight = 0;
        if (wsdmWeights != null && wsdmWeights.containsKey(feature.toString())) {
            try {
                wsdmWeight = wsdmWeights.getDouble(feature.toString());
            } catch (IllegalArgumentException e) {
                wsdmWeight = Double.parseDouble(wsdmWeights.getString(feature.toString()));
            }
        } else {
            wsdmWeight = queryParams.get(feature.toString(), 0.0);
        }
        return wsdmWeight;
    }

    private double getPredicateDF(String term) {
        Double score = this.predicateUnigramDF.get(term);
        return score != null ? score : 1e-10;
    }

    private double getPredicateDF(List<Node> bigram) {
        Double score = this.predicateBigramDF.get(asString(bigram));
        return score != null ? score : 1e-10;
    }

    @Override
    protected double computeWeight(String term, NodeParameters np, Parameters queryParams) throws Exception {
        return
                weight(WSDMFeatures.CONST, queryParams);
//                        weight(WSDMFeatures.DF, queryParams) * Math.log(computeWeightForDocumentFrequency(term, queryParams)) +
//                        weight(WSDMFeatures.CF, queryParams) * Math.log(computeWeightForCollectionFrequency(term, queryParams));
//                weight(WSDMFeatures.NAME_DF, queryParams) * computeWeightForDocumentFrequency(term, queryParams, "names") +
//                        weight(WSDMFeatures.NAME_CF, queryParams) * Math.log(computeWeightForCollectionFrequency(term, queryParams, "names") + computeWeightForCollectionFrequency(term, queryParams, "similarentitynames"));
//                weight(WSDMFeatures.SIMNAME_DF, queryParams) * computeWeightForDocumentFrequency(term, queryParams, "similarentitynames") +
//                weight(WSDMFeatures.SIMNAME_CF, queryParams) * computeWeightForCollectionFrequency(term, queryParams, "similarentitynames") +
//                        weight(WSDMFeatures.CAT_DF, queryParams) * computeWeightForDocumentFrequency(term, queryParams, "categories") +
//                        weight(WSDMFeatures.CAT_CF, queryParams) * Math.log(computeWeightForCollectionFrequency(term, queryParams, "categories"));
//                        weight(WSDMFeatures.PREDICATE_DF, queryParams) * getPredicateDF(term);
    }

    @Override
    protected double computeWeight(List<Node> bigram, NodeParameters np, Parameters queryParams,
                                   boolean isOrdered) throws Exception {
        return
                weight(isOrdered ? WSDMFeatures.OD_CONST : WSDMFeatures.UNW_CONST, queryParams);
//                        weight(isOrdered ? WSDMFeatures.ORDERED_CF : WSDMFeatures.UNWINDOW_CF, queryParams) * Math.log(computeWeightForCollectionFrequency(bigram, queryParams, isOrdered));
//                        weight(isOrdered ? WSDMFeatures.ORDERED_DF : WSDMFeatures.UNWINDOW_DF, queryParams) * Math.log(computeWeightForDocumentFrequency(bigram, queryParams, isOrdered)) +
//                        weight(isOrdered ? WSDMFeatures.ORDERED_NAME_CF : WSDMFeatures.UNWINDOW_NAME_CF, queryParams) * Math.log(computeWeightForCollectionFrequency(bigram, queryParams, "names", isOrdered) + computeWeightForCollectionFrequency(bigram, queryParams, "similarentitynames", isOrdered));
//        weight(isOrdered ? WSDMFeatures.ORDERED_NAME_DF : WSDMFeatures.UNWINDOW_NAME_DF, queryParams) * computeWeightForDocumentFrequency(bigram, queryParams, "names", isOrdered) +
//                        weight(isOrdered ? WSDMFeatures.ORDERED_SIMNAME_CF : WSDMFeatures.UNWINDOW_SIMNAME_CF, queryParams) * computeWeightForCollectionFrequency(bigram, queryParams, "similarentitynames", isOrdered) +
//                        weight(isOrdered ? WSDMFeatures.ORDERED_SIMNAME_DF : WSDMFeatures.UNWINDOW_SIMNAME_DF, queryParams) * computeWeightForDocumentFrequency(bigram, queryParams, "similarentitynames", isOrdered) +
//        weight(isOrdered ? WSDMFeatures.ORDERED_CAT_CF : WSDMFeatures.UNWINDOW_CAT_CF, queryParams) * Math.log(computeWeightForCollectionFrequency(bigram, queryParams, "categories", isOrdered));
//        weight(isOrdered ? WSDMFeatures.ORDERED_CAT_DF : WSDMFeatures.UNWINDOW_CAT_DF, queryParams) * computeWeightForDocumentFrequency(bigram, queryParams, "categories", isOrdered) +
//                weight(WSDMFeatures.PREDICATE_BIGRAM_DF, queryParams) * getPredicateDF(bigram);
    }

    @Override
    public Node afterNode(Node original, Parameters qp) throws Exception {
        if (original.getOperator().equals("ewsdm")) {
            return buildSDMNode(original, qp);
        } else {
            return original;
        }
    }

}
