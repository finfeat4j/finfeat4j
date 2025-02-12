/*
 *    HoeffdingTree.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package moa.classifiers.efdt;

import com.github.javacliparser.FlagOption;
import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.MultiChoiceOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.AbstractMOAObject;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.core.AttributeSplitSuggestion;
import moa.classifiers.core.attributeclassobservers.AttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.DiscreteAttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.NullAttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.NumericAttributeClassObserver;
import moa.classifiers.core.conditionaltests.InstanceConditionalTest;
import moa.classifiers.core.conditionaltests.NominalAttributeBinaryTest;
import moa.classifiers.core.conditionaltests.NominalAttributeMultiwayTest;
import moa.classifiers.core.splitcriteria.SplitCriterion;
import moa.core.*;
import moa.options.ClassOption;

import java.util.*;

/**
 *
 * Bug-fixed version of Richard Kirkby's  HoeffdingTree.Java written by Chaitanya Manapragada.
 *
 * Bug1 : average delta G's are computed where specified in the Domingos/Hulten paper algorithm listing. This was a bug in the HoeffdingTree.java implementation- fixed here.
 *
 * Bug2: splitting was occurring when the top attribute had no IG, and splitting was occurring on the same attribute. This was also fixed.
 *
 * Bug 3: Nodetime fixes a bug in which attempt tp split was not happening at grace period intervals but at weight changed = grace period,
 * which doesn't translate as Naive Bayes doesn't do a +1 count when learning
 * 
 * If learnFromInstance is overridden, always increment nodetime if super's learnFromInstance using VFDT learnFromInstance is not called!
 * 
// * VFDT fixes this for learning nodes only; split nodes don't learn at all. Split nodes come in a large number of varieties,
 * so implement nodeTime++ individually for split nodes as rquired. Anything that derives from Learning node, as long as it calls
 * super's learnFromInstance is called that leads back to LearningNode
 * 
 *
 * 
 * The correct reference is:
 *  * Domingos, P., & Hulten, G. (2000, August). Mining high-speed data streams. In Proceedings of the sixth ACM SIGKDD international conference on Knowledge discovery and data mining (pp. 71-80). ACM.
 *
 *
 */


/**
 * Hoeffding Tree or VFDT.
 *
 * A Hoeffding tree is an incremental, anytime decision tree induction algorithm
 * that is capable of learning from massive data streams, assuming that the
 * distribution generating examples does not change over time. Hoeffding trees
 * exploit the fact that a small sample can often be enough to choose an optimal
 * splitting attribute. This idea is supported mathematically by the Hoeffding
 * bound, which quantiﬁes the number of observations (in our case, examples)
 * needed to estimate some statistics within a prescribed precision (in our
 * case, the goodness of an attribute).</p> <p>A theoretically appealing feature
 * of Hoeffding Trees not shared by other incremental decision tree learners is
 * that it has sound guarantees of performance. Using the Hoeffding bound one
 * can show that its output is asymptotically nearly identical to that of a
 * non-incremental learner using inﬁnitely many examples. See for details:</p>
 *
 * <p>G. Hulten, L. Spencer, and P. Domingos. Mining time-changing data streams.
 * In KDD’01, pages 97–106, San Francisco, CA, 2001. ACM Press.</p>
 *
 * <p>Parameters:</p> <ul> <li> -m : Maximum memory consumed by the tree</li>
 * <li> -n : Numeric estimator to use : <ul> <li>Gaussian approximation
 * evaluating 10 splitpoints</li> <li>Gaussian approximation evaluating 100
 * splitpoints</li> <li>Greenwald-Khanna quantile summary with 10 tuples</li>
 * <li>Greenwald-Khanna quantile summary with 100 tuples</li>
 * <li>Greenwald-Khanna quantile summary with 1000 tuples</li> <li>VFML method
 * with 10 bins</li> <li>VFML method with 100 bins</li> <li>VFML method with
 * 1000 bins</li> <li>Exhaustive binary tree</li> </ul> </li> <li> -e : How many
 * instances between memory consumption checks</li> <li> -g : The number of
 * instances a leaf should observe between split attempts</li> <li> -s : Split
 * criterion to use. Example : InfoGainSplitCriterion</li> <li> -c : The
 * allowable error in split decision, values closer to 0 will take longer to
 * decide</li> <li> -t : Threshold below which a split will be forced to break
 * ties</li> <li> -b : Only allow binary splits</li> <li> -z : Stop growing as
 * soon as memory limit is hit</li> <li> -r : Disable poor attributes</li> <li>
 * -p : Disable pre-pruning</li>
 *  <li> -l : Leaf prediction to use: MajorityClass (MC), Naive Bayes (NB) or NaiveBayes
 * adaptive (NBAdaptive).</li>
 *  <li> -q : The number of instances a leaf should observe before
 * permitting Naive Bayes</li>
 * </ul>
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 7 $
 */
public class VFDT extends AbstractClassifier {

    private static final long serialVersionUID = 1L;

    protected int numInstances = 0;

    protected int splitCount=0;

    @Override
    public String getPurposeString() {
        return "Hoeffding Tree or VFDT.";
    }

    public IntOption maxByteSizeOption = new IntOption("maxByteSize", 'm',
            "Maximum memory consumed by the tree.", 33554432, 0,
            Integer.MAX_VALUE);

    /*
     * public MultiChoiceOption numericEstimatorOption = new MultiChoiceOption(
     * "numericEstimator", 'n', "Numeric estimator to use.", new String[]{
     * "GAUSS10", "GAUSS100", "GK10", "GK100", "GK1000", "VFML10", "VFML100",
     * "VFML1000", "BINTREE"}, new String[]{ "Gaussian approximation evaluating
     * 10 splitpoints", "Gaussian approximation evaluating 100 splitpoints",
     * "Greenwald-Khanna quantile summary with 10 tuples", "Greenwald-Khanna
     * quantile summary with 100 tuples", "Greenwald-Khanna quantile summary
     * with 1000 tuples", "VFML method with 10 bins", "VFML method with 100
     * bins", "VFML method with 1000 bins", "Exhaustive binary tree"}, 0);
     */
    public ClassOption numericEstimatorOption = new ClassOption("numericEstimator",
            'n', "Numeric estimator to use.", NumericAttributeClassObserver.class,
            "GaussianNumericAttributeClassObserver");

    public ClassOption nominalEstimatorOption = new ClassOption("nominalEstimator",
            'd', "Nominal estimator to use.", DiscreteAttributeClassObserver.class,
            "NominalAttributeClassObserver");

    public IntOption memoryEstimatePeriodOption = new IntOption(
            "memoryEstimatePeriod", 'e',
            "How many instances between memory consumption checks.", 1000000,
            0, Integer.MAX_VALUE);

    public IntOption gracePeriodOption = new IntOption(
            "gracePeriod",
            'g',
            "The number of instances a leaf should observe between split attempts.",
            200, 0, Integer.MAX_VALUE);

    public ClassOption splitCriterionOption = new ClassOption("splitCriterion",
            's', "Split criterion to use.", SplitCriterion.class,
            "InfoGainSplitCriterion");

    public FloatOption splitConfidenceOption = new FloatOption(
            "splitConfidence",
            'c',
            "The allowable error in split decision, values closer to 0 will take longer to decide.",
            0.0000001, 0.0, 1.0);

    public FloatOption tieThresholdOption = new FloatOption("tieThreshold",
            't', "Threshold below which a split will be forced to break ties.",
            0.05, 0.0, 1.0);

    public FlagOption binarySplitsOption = new FlagOption("binarySplits", 'b',
        "Only allow binary splits.");

    public FlagOption stopMemManagementOption = new FlagOption(
            "stopMemManagement", 'z',
            "Stop growing as soon as memory limit is hit.");

    public FlagOption removePoorAttsOption = new FlagOption("removePoorAtts",
            'r', "Disable poor attributes.");

    public FlagOption noPrePruneOption = new FlagOption("noPrePrune", 'p',
            "Disable pre-pruning.");

    public FlagOption nominalAttributeReuseBug = new FlagOption("nominalAttributeReuseBug", 'C',
            "Simulate original code bug");

    public FlagOption noAveragingInfogain = new FlagOption("noAveragingInfogain", 'D',
            "Dont Average Infogain");    

    public FlagOption noNodeTime = new FlagOption("noNodeTime", 'E',
            "Use getWeightSeen instead of nodeTime");    

    public FlagOption clearNodeInsteadOfResplitFeature = new FlagOption("clearNodeInsteadOfResplitFeature", 'J',
            "Simply clear node instead of \"resplitting\". Use with nominalAttributeReuseBug. ");    

    public FlagOption eagerSplitting = new FlagOption("eagerSplitting", 'K',
            "Split as soon as an attribute is better than no split");    

    
    public static class FoundNode {

        public Node node;

        public SplitNode parent;

        public int parentBranch;

        public FoundNode(Node node, SplitNode parent, int parentBranch) {
            this.node = node;
            this.parent = parent;
            this.parentBranch = parentBranch;
        }
    }

    public static class Node extends AbstractMOAObject {

    	private HashMap<Integer, Double> infogainSum;

    	private int numSplitAttempts = 0;

        private static final long serialVersionUID = 1L;

        protected DoubleVector observedClassDistribution;

        protected DoubleVector classDistributionAtTimeOfCreation;

        protected int nodeTime;

        protected List<Integer> usedNominalAttributes = new ArrayList<Integer>();

        public Node(double[] classObservations) {
            this.observedClassDistribution = new DoubleVector(classObservations);
            this.classDistributionAtTimeOfCreation = new DoubleVector(classObservations);
            this.infogainSum = new HashMap<Integer, Double>();
            this.infogainSum.put(-1, 0.0); // Initialize for null split

        }

        public int getNumSplitAttempts(){
        	return numSplitAttempts;
        }

        public void addToSplitAttempts(int i){
        	numSplitAttempts += i;
        }

        public HashMap<Integer, Double> getInfogainSum() {
        	return infogainSum;
        }

        public void setInfogainSum(HashMap<Integer, Double> igs) {
        	infogainSum = igs;
        }

        public int calcByteSize() {
            return (int) (SizeOf.sizeOf(this) + SizeOf.fullSizeOf(this.observedClassDistribution));
        }

        public int calcByteSizeIncludingSubtree() {
            return calcByteSize();
        }

        public boolean isLeaf() {
            return true;
        }

        public FoundNode filterInstanceToLeaf(Instance inst, SplitNode parent,
                int parentBranch) {
            return new FoundNode(this, parent, parentBranch);
        }

        public double[] getObservedClassDistribution() {
            return this.observedClassDistribution.getArrayCopy();
        }

        public double[] getClassVotes(Instance inst, VFDT ht) {
            return this.observedClassDistribution.getArrayCopy();
        }

        public double[] getClassDistributionAtTimeOfCreation() {
        	return this.classDistributionAtTimeOfCreation.getArrayCopy();
        }

        public boolean observedClassDistributionIsPure() {
            return this.observedClassDistribution.numNonZeroEntries() < 2;
        }

        public void describeSubtree(VFDT ht, StringBuilder out,
                int indent) {
            StringUtils.appendIndented(out, indent, "Leaf ");
            out.append(ht.getClassNameString());
            out.append(" = ");
            out.append(ht.getClassLabelString(this.observedClassDistribution.maxIndex()));
            out.append(" weights: ");
            this.observedClassDistribution.getSingleLineDescription(out,
                    ht.treeRoot.observedClassDistribution.numValues());
            StringUtils.appendNewline(out);
        }

        public int subtreeDepth() {
            return 0;
        }

        public double calculatePromise() {
            double totalSeen = this.observedClassDistribution.sumOfValues();
            return totalSeen > 0.0 ? (totalSeen - this.observedClassDistribution.getValue(this.observedClassDistribution.maxIndex()))
                    : 0.0;
        }

        @Override
        public void getDescription(StringBuilder sb, int indent) {
            describeSubtree(null, sb, indent);
        }
    }

    public static class SplitNode extends Node {

        private static final long serialVersionUID = 1L;

        protected InstanceConditionalTest splitTest;

        protected AutoExpandVector<Node> children; // = new AutoExpandVector<Node>();

        @Override
        public int calcByteSize() {
            return super.calcByteSize()
                    + (int) (SizeOf.sizeOf(this.children) + SizeOf.fullSizeOf(this.splitTest));
        }

        @Override
        public int calcByteSizeIncludingSubtree() {
            int byteSize = calcByteSize();
            for (Node child : this.children) {
                if (child != null) {
                    byteSize += child.calcByteSizeIncludingSubtree();
                }
            }
            return byteSize;
        }

        public SplitNode(InstanceConditionalTest splitTest,
                double[] classObservations, int size) {
            super(classObservations);
            this.splitTest = splitTest;
            this.children = new AutoExpandVector<Node>(size);
        }

        public SplitNode(InstanceConditionalTest splitTest,
                double[] classObservations) {
            super(classObservations);
            this.splitTest = splitTest;
            this.children = new AutoExpandVector<Node>();
        }


        public int numChildren() {
            return this.children.size();
        }

        public void setChild(int index, Node child) {
            if ((this.splitTest.maxBranches() >= 0)
                    && (index >= this.splitTest.maxBranches())) {
                throw new IndexOutOfBoundsException();
            }
            this.children.set(index, child);
        }

        public Node getChild(int index) {
            return this.children.get(index);
        }

        public int instanceChildIndex(Instance inst) {
            return this.splitTest.branchForInstance(inst);
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public FoundNode filterInstanceToLeaf(Instance inst, SplitNode parent,
                int parentBranch) {

        	//System.err.println("OVERRIDING ");

            int childIndex = instanceChildIndex(inst);
            if (childIndex >= 0) {
                Node child = getChild(childIndex);
                if (child != null) {
                    return child.filterInstanceToLeaf(inst, this, childIndex);
                }
                return new FoundNode(null, this, childIndex);
            }
            return new FoundNode(this, parent, parentBranch);
        }

        @Override
        public void describeSubtree(VFDT ht, StringBuilder out,
                int indent) {
            for (int branch = 0; branch < numChildren(); branch++) {
                Node child = getChild(branch);
                if (child != null) {
                    StringUtils.appendIndented(out, indent, "if ");
                    out.append(this.splitTest.describeConditionForBranch(branch,
                            ht.getModelContext()));
                    out.append(": ");
                    StringUtils.appendNewline(out);
                    child.describeSubtree(ht, out, indent + 2);
                }
            }
        }

        @Override
        public int subtreeDepth() {
            int maxChildDepth = 0;
            for (Node child : this.children) {
                if (child != null) {
                    int depth = child.subtreeDepth();
                    if (depth > maxChildDepth) {
                        maxChildDepth = depth;
                    }
                }
            }
            return maxChildDepth + 1;
        }
    }

    public static abstract class LearningNode extends Node {

        private static final long serialVersionUID = 1L;

        public LearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        public abstract void learnFromInstance(Instance inst, VFDT ht);
    }

    public static class InactiveLearningNode extends LearningNode {

        private static final long serialVersionUID = 1L;

        public InactiveLearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        @Override
        public void learnFromInstance(Instance inst, VFDT ht) {
        	this.nodeTime++;
            this.observedClassDistribution.addToValue((int) inst.classValue(),
                    inst.weight());
        }
    }

    public static class ActiveLearningNode extends LearningNode {

        private static final long serialVersionUID = 1L;

        protected double weightSeenAtLastSplitEvaluation;

        protected AutoExpandVector<AttributeClassObserver> attributeObservers = new AutoExpandVector<AttributeClassObserver>();

        protected boolean isInitialized;

        public ActiveLearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
            this.weightSeenAtLastSplitEvaluation = getWeightSeen();
            this.isInitialized = false;
        }

        @Override
        public int calcByteSize() {
            return super.calcByteSize()
                    + (int) (SizeOf.fullSizeOf(this.attributeObservers));
        }

        @Override
        public void learnFromInstance(Instance inst, VFDT ht) {
        	this.nodeTime++;

            if (this.isInitialized == false) {
                this.attributeObservers = new AutoExpandVector<AttributeClassObserver>(inst.numAttributes());
                this.isInitialized = true;
            }
            this.observedClassDistribution.addToValue((int) inst.classValue(), inst.weight());
            for (int i = 0; i < inst.numAttributes() - 1; i++) {
                int instAttIndex = modelAttIndexToInstanceAttIndex(i, inst);
                AttributeClassObserver obs = this.attributeObservers.get(i);
                if (obs == null) {
                    obs = inst.attribute(instAttIndex).isNominal() ? ht.newNominalClassObserver() : ht.newNumericClassObserver();
                    this.attributeObservers.set(i, obs);
                }
                obs.observeAttributeClass(inst.value(instAttIndex), (int) inst.classValue(), inst.weight());
            }
        }

        public double getWeightSeen() {
            return this.observedClassDistribution.sumOfValues();
        }

        public double getWeightSeenAtLastSplitEvaluation() {
            return this.weightSeenAtLastSplitEvaluation;
        }

        public void setWeightSeenAtLastSplitEvaluation(double weight) {
            this.weightSeenAtLastSplitEvaluation = weight;
        }

        public AttributeSplitSuggestion[] getBestSplitSuggestions(
                SplitCriterion criterion, VFDT ht) {
            List<AttributeSplitSuggestion> bestSuggestions = new LinkedList<AttributeSplitSuggestion>();
            double[] preSplitDist = this.observedClassDistribution.getArrayCopy();
            if (!ht.noPrePruneOption.isSet()) {
                // add null split as an option
                bestSuggestions.add(new AttributeSplitSuggestion(null,
                        new double[0][], criterion.getMeritOfSplit(
                        preSplitDist, new double[][]{preSplitDist})));
            }
            for (int i = 0; i < this.attributeObservers.size(); i++) {
                AttributeClassObserver obs = this.attributeObservers.get(i);
                if (obs != null) {
                    AttributeSplitSuggestion bestSuggestion = obs.getBestEvaluatedSplitSuggestion(criterion,
                            preSplitDist, i, ht.binarySplitsOption.isSet());
                    if (bestSuggestion != null) {
                        bestSuggestions.add(bestSuggestion);
                    }
                }
            }
            return bestSuggestions.toArray(new AttributeSplitSuggestion[bestSuggestions.size()]);
        }

        public void disableAttribute(int attIndex) {
            this.attributeObservers.set(attIndex,
                    new NullAttributeClassObserver());
        }

		public void reInitialise() {
            this.observedClassDistribution = new DoubleVector(new double[0]);
            this.classDistributionAtTimeOfCreation = new DoubleVector(new double[0]);
            this.setInfogainSum(new HashMap<Integer, Double>());
            this.getInfogainSum().put(-1, 0.0); // Initialize for null split
            this.weightSeenAtLastSplitEvaluation = 0.0;
            this.isInitialized = false; //set this to false because you want attributeObservers reset in learnFromInstance	
		}
    }

    protected Node treeRoot = null;

    protected int decisionNodeCount;

    protected int activeLeafNodeCount;

    protected int inactiveLeafNodeCount;

    protected double inactiveLeafByteSizeEstimate;

    protected double activeLeafByteSizeEstimate;

    protected double byteSizeEstimateOverheadFraction;

    protected boolean growthAllowed;

    public int calcByteSize() {
        int size = (int) SizeOf.sizeOf(this);
        if (this.treeRoot != null) {
            size += this.treeRoot.calcByteSizeIncludingSubtree();
        }
        return size;
    }

    @Override
    public long measureByteSize() {
        return calcByteSize();
    }

    @Override
    public void resetLearningImpl() {
        this.treeRoot = null;
        this.decisionNodeCount = 0;
        this.activeLeafNodeCount = 0;
        this.inactiveLeafNodeCount = 0;
        this.inactiveLeafByteSizeEstimate = 0.0;
        this.activeLeafByteSizeEstimate = 0.0;
        this.byteSizeEstimateOverheadFraction = 1.0;
        this.growthAllowed = true;
        if (this.leafpredictionOption.getChosenIndex()>0) {
            this.removePoorAttsOption = null;
        }
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
    	//System.err.println(i++);
        if (this.treeRoot == null) {
            this.treeRoot = newLearningNode();
            this.activeLeafNodeCount = 1;
        }
        FoundNode foundNode = this.treeRoot.filterInstanceToLeaf(inst, null, -1);
        Node leafNode = foundNode.node;

        if (leafNode == null) {
            leafNode = newLearningNode();
            foundNode.parent.setChild(foundNode.parentBranch, leafNode);
            this.activeLeafNodeCount++;
        }

        if (leafNode instanceof LearningNode) {
            LearningNode learningNode = (LearningNode) leafNode;
            learningNode.learnFromInstance(inst, this);
            if (this.growthAllowed
                    && (learningNode instanceof ActiveLearningNode)) {
                ActiveLearningNode activeLearningNode = (ActiveLearningNode) learningNode;
                double weightSeen = activeLearningNode.getWeightSeen();
                //noNodeTime.isSet() && 
                if (noNodeTime.isSet() && weightSeen
                        - activeLearningNode.getWeightSeenAtLastSplitEvaluation() >= this.gracePeriodOption.getValue()) {
                    attemptToSplit(activeLearningNode, foundNode.parent,
                            foundNode.parentBranch);
                    activeLearningNode.setWeightSeenAtLastSplitEvaluation(weightSeen);
                }
   
                else if (!noNodeTime.isSet() && activeLearningNode.nodeTime % this.gracePeriodOption.getValue() == 0) {
                    attemptToSplit(activeLearningNode, foundNode.parent,
                            foundNode.parentBranch);
                    activeLearningNode.setWeightSeenAtLastSplitEvaluation(weightSeen);
                }
                
            }
        }

        if (this.trainingWeightSeenByModel
                % this.memoryEstimatePeriodOption.getValue() == 0) {
            estimateModelByteSizes();
        }

    	//System.out.println(this.measureTreeDepth());

        numInstances++;

    }
    
    @Override
    public double[] getVotesForInstance(Instance inst) {
        if (this.treeRoot != null) {
            FoundNode foundNode = this.treeRoot.filterInstanceToLeaf(inst,
                    null, -1);
            Node leafNode = foundNode.node;
            if (leafNode == null) {
                leafNode = foundNode.parent;
            }
            return leafNode.getClassVotes(inst, this);
          } else {
            int numClasses = inst.dataset().numClasses();
            return new double[numClasses];
          }
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
		FoundNode[] learningNodes = findLearningNodes();

        return new Measurement[]{

                    new Measurement("tree size (nodes)", this.decisionNodeCount
                    + this.activeLeafNodeCount + this.inactiveLeafNodeCount),
                    new Measurement("tree size (leaves)", learningNodes.length),
                    new Measurement("active learning leaves",
                    this.activeLeafNodeCount),
                    new Measurement("tree depth", measureTreeDepth()),
                    new Measurement("active leaf byte size estimate",
                    this.activeLeafByteSizeEstimate),
                    new Measurement("inactive leaf byte size estimate",
                    this.inactiveLeafByteSizeEstimate),
                    new Measurement("byte size estimate overhead",
                    this.byteSizeEstimateOverheadFraction),
                    new Measurement("splits",
                            this.splitCount)};
    }

    public int measureTreeDepth() {
        if (this.treeRoot != null) {
            return this.treeRoot.subtreeDepth();
        }
        return 0;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        this.treeRoot.describeSubtree(this, out, indent);
    }

    @Override
    public boolean isRandomizable() {
        return false;
    }

    public static double computeHoeffdingBound(double range, double confidence,
            double n) {
        return Math.sqrt(((range * range) * Math.log(1.0 / confidence))
                / (2.0 * n));
    }

    //Procedure added for Hoeffding Adaptive Trees (ADWIN)
    protected SplitNode newSplitNode(InstanceConditionalTest splitTest,
            double[] classObservations, int size) {
        return new SplitNode(splitTest, classObservations, size);
    }

    protected SplitNode newSplitNode(InstanceConditionalTest splitTest,
            double[] classObservations) {
        return new SplitNode(splitTest, classObservations);
    }


    protected AttributeClassObserver newNominalClassObserver() {
        AttributeClassObserver nominalClassObserver = (AttributeClassObserver) getPreparedClassOption(this.nominalEstimatorOption);
        return (AttributeClassObserver) nominalClassObserver.copy();
    }

    protected AttributeClassObserver newNumericClassObserver() {
        AttributeClassObserver numericClassObserver = (AttributeClassObserver) getPreparedClassOption(this.numericEstimatorOption);
        return (AttributeClassObserver) numericClassObserver.copy();
    }

    protected boolean decideToSplitAndPrune(ActiveLearningNode node, SplitNode parent,
            int parentIndex, AttributeSplitSuggestion[] bestSplitSuggestions,
            SplitCriterion splitCriterion) {

        Arrays.sort(bestSplitSuggestions);
        boolean shouldSplit = false;

        for (int i = 0; i < bestSplitSuggestions.length; i++){

        	if (bestSplitSuggestions[i].splitTest != null){
        		if (!node.getInfogainSum().containsKey((bestSplitSuggestions[i].splitTest.getAttsTestDependsOn()[0])))
        		{
        			node.getInfogainSum().put((bestSplitSuggestions[i].splitTest.getAttsTestDependsOn()[0]), 0.0);
        		}
               	double currentSum = node.getInfogainSum().get((bestSplitSuggestions[i].splitTest.getAttsTestDependsOn()[0]));
               	node.getInfogainSum().put((bestSplitSuggestions[i].splitTest.getAttsTestDependsOn()[0]), currentSum + bestSplitSuggestions[i].merit);
        	}

        	else { // handle the null attribute
        		double currentSum = node.getInfogainSum().get(-1); // null split
        		node.getInfogainSum().put(-1, currentSum + Math.max(0.0, bestSplitSuggestions[i].merit));
				assert node.getInfogainSum().get(-1) >= 0.0 : "Negative infogain shouldn't be possible here.";
        	}

        }

        if (bestSplitSuggestions.length < 2) {
            shouldSplit = bestSplitSuggestions.length > 0;
        }

        else {


        	double hoeffdingBound = computeHoeffdingBound(splitCriterion.getRangeOfMerit(node.getObservedClassDistribution()),
                    this.splitConfidenceOption.getValue(), node.getWeightSeen());

            AttributeSplitSuggestion bestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 1];
            AttributeSplitSuggestion secondBestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 2];


            double bestSuggestionAverageMerit = 0.0;
            double secondBestSuggestionAverageMerit = 0.0;
            
            if(noAveragingInfogain.isSet()) {
            	bestSuggestionAverageMerit = bestSuggestion.merit;
            	secondBestSuggestionAverageMerit = secondBestSuggestion.merit;
            } else {
            	
                if(bestSuggestion.splitTest == null){ // if you have a null split
                	bestSuggestionAverageMerit = node.getInfogainSum().get(-1) / node.getNumSplitAttempts();
                } else{
                	bestSuggestionAverageMerit = node.getInfogainSum().get((bestSuggestion.splitTest.getAttsTestDependsOn()[0])) / node.getNumSplitAttempts();
                }

                if(secondBestSuggestion.splitTest == null){ // if you have a null split
                	secondBestSuggestionAverageMerit = node.getInfogainSum().get(-1) / node.getNumSplitAttempts();
                } else{
                	secondBestSuggestionAverageMerit = node.getInfogainSum().get((secondBestSuggestion.splitTest.getAttsTestDependsOn()[0])) / node.getNumSplitAttempts();
                }
            }
            
            if(eagerSplitting.isSet()) {
            	secondBestSuggestionAverageMerit = 0.0;
            }         
            
            // VFDT bug option; don't split on no merit - nominal attributes not reused
            if(bestSuggestion.merit < 1e-10 && !nominalAttributeReuseBug.isSet()){ // we don't use average here
            	shouldSplit = false;
            } else if ((bestSuggestionAverageMerit - secondBestSuggestionAverageMerit > hoeffdingBound)
                    || (hoeffdingBound < this.tieThresholdOption.getValue()))
                	{
                shouldSplit = true;
            }
               

            if(shouldSplit && !nominalAttributeReuseBug.isSet()){
            	for(Integer i : node.usedNominalAttributes){
            		if(bestSuggestion.splitTest.getAttsTestDependsOn()[0] == i){
            			shouldSplit = false;

            			break;
            		}
            	}
            }
            
            // clearing child node instead of resplitting when no / negative merit causes resplit
            // if clear node option is set and a resplit is locked in
            if(bestSuggestion.merit < 1e-10 
            		&& shouldSplit 
            		&& nominalAttributeReuseBug.isSet() // set this in conjunction always!
            		&& clearNodeInsteadOfResplitFeature.isSet()
            		) {
            	shouldSplit = false;
            	
                //LearningNode newChild = newLearningNode();
                //parent.setChild(parentIndex, newChild);
            	node.reInitialise();

            	// clear node statistics
            	// so next time it attempts to learn it will clear attributeObservers
            	// it keeps the class distribution though and gets rid of it on the next resplit
            	// so clear class distribution also as there will be no more re-re-splitting
            	// we also need to clear infogain--- may as well make a new child node and clear everything
                // else if: merit is not necessarily negative, but an attribute is being repeated...
            } else if (shouldSplit 
            		&& nominalAttributeReuseBug.isSet() // set this in conjunction always!
            		&& clearNodeInsteadOfResplitFeature.isSet()) {
            	for(Integer i : node.usedNominalAttributes){
            		if(bestSuggestion.splitTest.getAttsTestDependsOn()[0] == i){
            			
            			shouldSplit = false;	
            			
                        //Node newChild = newLearningNode();
                        //parent.setChild(parentIndex, newChild);
            			
                        node.reInitialise();
            			
            			break;
            		}
            	}
            }

            
            
            
            // }
            if ((this.removePoorAttsOption != null)
                    && this.removePoorAttsOption.isSet()) {
                Set<Integer> poorAtts = new HashSet<Integer>();
                // scan 1 - add any poor to set
                for (int i = 0; i < bestSplitSuggestions.length; i++) {
                    if (bestSplitSuggestions[i].splitTest != null) {
                        int[] splitAtts = bestSplitSuggestions[i].splitTest.getAttsTestDependsOn();
                        if (splitAtts.length == 1) {
                            if (bestSuggestion.merit
                                    - bestSplitSuggestions[i].merit > hoeffdingBound) {
                                poorAtts.add(splitAtts[0]);
                            }
                        }
                    }
                }
                // scan 2 - remove good ones from set
                for (int i = 0; i < bestSplitSuggestions.length; i++) {
                    if (bestSplitSuggestions[i].splitTest != null) {
                        int[] splitAtts = bestSplitSuggestions[i].splitTest.getAttsTestDependsOn();
                        if (splitAtts.length == 1) {
                            if (bestSuggestion.merit
                                    - bestSplitSuggestions[i].merit < hoeffdingBound) {
                                poorAtts.remove(splitAtts[0]);
                            }
                        }
                    }
                }
                for (int poorAtt : poorAtts) {
                    node.disableAttribute(poorAtt);
                }
            }
        }
        return shouldSplit;
    }
    
    
    
   protected void attemptToSplit(ActiveLearningNode node, SplitNode parent,
            int parentIndex) {
    	
        if (!node.observedClassDistributionIsPure()) {
        	node.addToSplitAttempts(1); // even if we don't actually attempt to split, we've computed infogains
            SplitCriterion splitCriterion = (SplitCriterion) getPreparedClassOption(this.splitCriterionOption);
            AttributeSplitSuggestion[] bestSplitSuggestions = node.getBestSplitSuggestions(splitCriterion, this);
	
            if (decideToSplitAndPrune(node, parent, parentIndex, bestSplitSuggestions, splitCriterion)) {
            	splitCount++;

                AttributeSplitSuggestion splitDecision = bestSplitSuggestions[bestSplitSuggestions.length - 1];
                if (splitDecision.splitTest == null) {
                    // preprune - null wins
                    deactivateLearningNode(node, parent, parentIndex);
                } else {
                    SplitNode newSplit = newSplitNode(splitDecision.splitTest,
                            node.getObservedClassDistribution(), splitDecision.numSplits());
                    for (int i = 0; i < splitDecision.numSplits(); i++) {

                        double[] j = splitDecision.resultingClassDistributionFromSplit(i);

                        Node newChild = newLearningNode(splitDecision.resultingClassDistributionFromSplit(i));

                        if(splitDecision.splitTest.getClass() == NominalAttributeBinaryTest.class
                        		||splitDecision.splitTest.getClass() == NominalAttributeMultiwayTest.class){
                        	newChild.usedNominalAttributes = new ArrayList<Integer>(node.usedNominalAttributes); //deep copy
                        	newChild.usedNominalAttributes.add(splitDecision.splitTest.getAttsTestDependsOn()[0]);
                        	// no  nominal attribute should be split on more than once in the path
                        }
                        newSplit.setChild(i, newChild);
                    }
                    
                    this.activeLeafNodeCount--;
                    this.decisionNodeCount++;
                    this.activeLeafNodeCount += splitDecision.numSplits();
                    if (parent == null) {
                        this.treeRoot = newSplit;
                    } else {
                        parent.setChild(parentIndex, newSplit);
                    }
                }

                // manage memory
                enforceTrackerLimit();
            }
        }
    }

     

    
    public void enforceTrackerLimit() {
        if ((this.inactiveLeafNodeCount > 0)
                || ((this.activeLeafNodeCount * this.activeLeafByteSizeEstimate + this.inactiveLeafNodeCount
                * this.inactiveLeafByteSizeEstimate)
                * this.byteSizeEstimateOverheadFraction > this.maxByteSizeOption.getValue())) {
            if (this.stopMemManagementOption.isSet()) {
                this.growthAllowed = false;
                return;
            }
            FoundNode[] learningNodes = findLearningNodes();
            Arrays.sort(learningNodes, new Comparator<FoundNode>() {

                @Override
                public int compare(FoundNode fn1, FoundNode fn2) {
                    return Double.compare(fn1.node.calculatePromise(), fn2.node.calculatePromise());
                }
            });
            int maxActive = 0;
            while (maxActive < learningNodes.length) {
                maxActive++;
                if ((maxActive * this.activeLeafByteSizeEstimate + (learningNodes.length - maxActive)
                        * this.inactiveLeafByteSizeEstimate)
                        * this.byteSizeEstimateOverheadFraction > this.maxByteSizeOption.getValue()) {
                    maxActive--;
                    break;
                }
            }
            int cutoff = learningNodes.length - maxActive;
            for (int i = 0; i < cutoff; i++) {
                if (learningNodes[i].node instanceof ActiveLearningNode) {
                    deactivateLearningNode(
                            (ActiveLearningNode) learningNodes[i].node,
                            learningNodes[i].parent,
                            learningNodes[i].parentBranch);
                }
            }
            for (int i = cutoff; i < learningNodes.length; i++) {
                if (learningNodes[i].node instanceof InactiveLearningNode) {
                    activateLearningNode(
                            (InactiveLearningNode) learningNodes[i].node,
                            learningNodes[i].parent,
                            learningNodes[i].parentBranch);
                }
            }
        }
    }

    public void estimateModelByteSizes() {
        FoundNode[] learningNodes = findLearningNodes();
        long totalActiveSize = 0;
        long totalInactiveSize = 0;
        for (FoundNode foundNode : learningNodes) {
            if (foundNode.node instanceof ActiveLearningNode) {
                totalActiveSize += SizeOf.fullSizeOf(foundNode.node);
            } else {
                totalInactiveSize += SizeOf.fullSizeOf(foundNode.node);
            }
        }
        if (totalActiveSize > 0) {
            this.activeLeafByteSizeEstimate = (double) totalActiveSize
                    / this.activeLeafNodeCount;
        }
        if (totalInactiveSize > 0) {
            this.inactiveLeafByteSizeEstimate = (double) totalInactiveSize
                    / this.inactiveLeafNodeCount;
        }
        long actualModelSize = this.measureByteSize();
        double estimatedModelSize = (this.activeLeafNodeCount
                * this.activeLeafByteSizeEstimate + this.inactiveLeafNodeCount
                * this.inactiveLeafByteSizeEstimate);
        this.byteSizeEstimateOverheadFraction = actualModelSize
                / estimatedModelSize;
        if (actualModelSize > this.maxByteSizeOption.getValue()) {
            enforceTrackerLimit();
        }
    }

    public void deactivateAllLeaves() {
        FoundNode[] learningNodes = findLearningNodes();
        for (int i = 0; i < learningNodes.length; i++) {
            if (learningNodes[i].node instanceof ActiveLearningNode) {
                deactivateLearningNode(
                        (ActiveLearningNode) learningNodes[i].node,
                        learningNodes[i].parent, learningNodes[i].parentBranch);
            }
        }
    }

    protected void deactivateLearningNode(ActiveLearningNode toDeactivate,
            SplitNode parent, int parentBranch) {
        Node newLeaf = new InactiveLearningNode(toDeactivate.getObservedClassDistribution());
        if (parent == null) {
            this.treeRoot = newLeaf;
        } else {
            parent.setChild(parentBranch, newLeaf);
        }
        this.activeLeafNodeCount--;
        this.inactiveLeafNodeCount++;
    }

    protected void activateLearningNode(InactiveLearningNode toActivate,
            SplitNode parent, int parentBranch) {
        Node newLeaf = newLearningNode(toActivate.getObservedClassDistribution());
        if (parent == null) {
            this.treeRoot = newLeaf;
        } else {
            parent.setChild(parentBranch, newLeaf);
        }
        this.activeLeafNodeCount++;
        this.inactiveLeafNodeCount--;
    }

    protected FoundNode[] findLearningNodes() {
        List<FoundNode> foundList = new LinkedList<FoundNode>();
        findLearningNodes(this.treeRoot, null, -1, foundList);
        return foundList.toArray(new FoundNode[foundList.size()]);
    }

    protected void findLearningNodes(Node node, SplitNode parent,
            int parentBranch, List<FoundNode> found) {
        if (node != null) {
            if (node instanceof LearningNode) {
                found.add(new FoundNode(node, parent, parentBranch));
            }
            if (node instanceof SplitNode) {
                SplitNode splitNode = (SplitNode) node;
                for (int i = 0; i < splitNode.numChildren(); i++) {
                    findLearningNodes(splitNode.getChild(i), splitNode, i,
                            found);
                }
            }
        }
    }

    public MultiChoiceOption leafpredictionOption = new MultiChoiceOption(
            "leafprediction", 'l', "Leaf prediction to use.", new String[]{
                "MC", "NB", "NBAdaptive"}, new String[]{
                "Majority class",
                "Naive Bayes",
                "Naive Bayes Adaptive"}, 2);

    public IntOption nbThresholdOption = new IntOption(
            "nbThreshold",
            'q',
            "The number of instances a leaf should observe before permitting Naive Bayes.",
            0, 0, Integer.MAX_VALUE);

    public static class LearningNodeNB extends ActiveLearningNode {

        private static final long serialVersionUID = 1L;

        public LearningNodeNB(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        @Override
        public double[] getClassVotes(Instance inst, VFDT ht) {
            if (getWeightSeen() >= ht.nbThresholdOption.getValue()) {
                return NaiveBayes.doNaiveBayesPrediction(inst,
                        this.observedClassDistribution,
                        this.attributeObservers);
            }
            return super.getClassVotes(inst, ht);
        }

        @Override
        public void disableAttribute(int attIndex) {
            // should not disable poor atts - they are used in NB calc
        }
    }

    public static class LearningNodeNBAdaptive extends LearningNodeNB {

        private static final long serialVersionUID = 1L;

        protected double mcCorrectWeight = 0.0;

        protected double nbCorrectWeight = 0.0;

        public LearningNodeNBAdaptive(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        @Override
        public void learnFromInstance(Instance inst, VFDT ht) {
            int trueClass = (int) inst.classValue();
            if (this.observedClassDistribution.maxIndex() == trueClass) {
                this.mcCorrectWeight += inst.weight();
            }
            if (Utils.maxIndex(NaiveBayes.doNaiveBayesPrediction(inst,
                    this.observedClassDistribution, this.attributeObservers)) == trueClass) {
                this.nbCorrectWeight += inst.weight();
            }
            super.learnFromInstance(inst, ht);
        }

        @Override
        public double[] getClassVotes(Instance inst, VFDT ht) {
            if (this.mcCorrectWeight > this.nbCorrectWeight) {
                return this.observedClassDistribution.getArrayCopy();
            }
            return NaiveBayes.doNaiveBayesPrediction(inst,
                    this.observedClassDistribution, this.attributeObservers);
        }
    }

    protected LearningNode newLearningNode() {
        return newLearningNode(new double[0]);
    }

    protected LearningNode newLearningNode(double[] initialClassObservations) {
        LearningNode ret;
        int predictionOption = this.leafpredictionOption.getChosenIndex();
        if (predictionOption == 0) { //MC
            ret = new ActiveLearningNode(initialClassObservations);
        } else if (predictionOption == 1) { //NB
            ret = new LearningNodeNB(initialClassObservations);
        } else { //NBAdaptive
            ret = new LearningNodeNBAdaptive(initialClassObservations);
        }
        return ret;
    }
}
