package com.adamyi.courseplanner.nlp;

// import android.text.StringUtils;
// import android.util.SparseArray;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class ParseTree {

    public static final int NOT_EXIST = -1;

    /*
     * Universal POS tags by Stanford
     */
    public static final String POS_ADJECTIVE = "ADJ";
    public static final String POS_ADVERB = "ADV";
    public static final String POS_VERB = "VERB";

    public static final String POS_NUMERAL = "NUM";
    public static final String POS_NOUN = "NOUN";
    public static final String POS_PROPERNOUN = "PROPN";
    public static final String POS_PRONOUN = "PRON";

    public static final String POS_COORDINATING_CONJUNCTION = "CCONJ";
    public static final String POS_SUBORDINATING_CONJUNCTION = "SCONJ";

    public static final String POS_ADPOSITION = "ADP";
    public static final String POS_AUXILIARY = "AUX";
    public static final String POS_DETERMINER = "DET";
    public static final String POS_INTERJECTION = "INTJ";

    public static final String POS_PARTICLE = "PART";
    public static final String POS_PUNCTUAATION = "PUNCT";
    public static final String POS_SYMBOL = "SYM";

    public static final String POS_UNKNOWN = "X";

    /*
     * Universal Dependencies
     */
    public static final String DEP_NOUN_SUBJECT = "NSUBJ";
    public static final String DEP_OBJECTIVE = "OBJ";
    public static final String DEP_INDIRECT_OBJECTIVE = "IOBJ"; // Tom teaches Sam (direct obj) math (indirect obj)

    public static final String DEP_CLAUSE_SUBJECT = "CSUBJ";
    public static final String DEP_CLAUSAL_COMPLEMENT = "CCOMP"; // this one has its own subject. eg. Adam says that Mars likes to swim.
    public static final String DEP_OPEN_CLAUSAL_COMPLEMENT = "XCOMP"; // this one does not. eg Fanglin looks great.

    public static final String DEP_OBLIQUE_NOMINAL = "OBL";
    public static final String DEP_VOCATIVE = "VOCATIVE";
    public static final String DEP_EXPLETIVE = "EXPL";
    public static final String DEP_DISLOCATED = "DISLOCATED";

    public static final String DEP_AUXILIARY = "AUX";
    public static final String DEP_COPULA = "COP";
    public static final String DEP_MARKER = "MARK";

    public static final String DEP_ADVERB_CLAUSE_MODIFIER = "ADVCL";
    public static final String DEP_ADVERB_MODIFIER = "ADVMOB";
    public static final String DEP_NOMINAL_MODIFIER = "NMOD";
    public static final String DEP_APPOSITIONAL_MODIFIER = "APPOS";
    public static final String DEP_NUMERIC_MODIFIER = "NUMMOD";
    public static final String DEP_CLAUSAL_MODIFIER = "ACL";
    public static final String DEP_ADJECTIVE_MODIFIER = "AMOD";

    public static final String DEP_DISCOURSE = "DISCOURSE";
    public static final String DEP_DETERMINER = "DET";
    public static final String DEP_CLASSIFIER = "CLF";
    public static final String DEP_CASE = "CASE";

    public static final String DEP_CONJUNCTION = "CONJ";
    public static final String DEP_COORDINATING_CONJUNCTION = "CC";
    public static final String DEP_NOUN_NOUN = "NN";

    public static final String DEP_FIXED_MULTIWORD_EXPRESSION = "FIXED";
    public static final String DEP_FLAT_MULTIWORDD_EXPRESSION = "FLAT";
    public static final String DEP_COMPOUND = "COMPOUND";

    public static final String DEP_LIST = "LIST";
    public static final String DEP_PARATAXIS = "PARATAXIS";

    public static final String DEP_ORPHAN = "ORPHAN";
    public static final String DEP_GOES_WITH = "GOESWITH";
    public static final String DEP_REPARANDUM = "REPARANDUM"; //overridden disfluency

    public static final String DEP_PUNCTUATION = "PUNCT";
    public static final String DEP_ROOT = "ROOT";
    public static final String DEP_UNKNOWN = "DEP";

    public enum Flag {NORMAL, DELETE, MERGE}
    public enum Requisite {PRE, CO};


    private SparseArray<Node> mNodeList;
    private int mRootId;

    /*
     * Node of ParseTree
     * Variables:                                                Functions:
     * - mId                                                     + GETTER & SETTER
     * - mType                                                   + GETTER & SETTER
     * - mWord                                                   + GETTER & SETTER
     * - mChildrenIds                                            + GETTER & SETTER
     * - mParentId                                               + GETTER & SETTER
     * - mRelation                                               + GETTER & SETTER
     * - mFlag                                                   + GETTER & SETTER
     * - mTagList                                                + GETTER & SETTER
     * + toString()
     * + print()
     */


    // START Node Class
    static public class Node {

        private int mId;
        private String mType;
        private String mWord;
        private Set<Integer> mChildrenIds;
        private int mParentId;
        private String mRelation;
        private Flag mFlag;
        private Requisite mRequisite;
        private Boolean mParsed;

        public Node() {
            this.mChildrenIds = new HashSet<>();
            this.mRequisite = Requisite.PRE;
            this.mFlag = Flag.DELETE;
        }

        public Node(Node another) {
            this.mId = another.mId;
            this.mType = another.mType;
            this.mWord = another.mWord;
            this.mChildrenIds = new HashSet<>(another.mChildrenIds);
            this.mParentId = another.mParentId;
            this.mRelation = another.mRelation;
            this.mFlag = another.mFlag;
            this.mRequisite = another.mRequisite;
        }

        public void setValue(String type, String word) {
            this.mType = type;
            this.mWord = word;
            this.mFlag = Flag.NORMAL;
            //this.entity = null;
            //this.parentId = null;
            //this.childrenIds = new ArrayList<>();
        }

        public void setId(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }

        public String getType() {
            return mType;
        }

        public void setType(String type) {
            this.mType = type;
        }

        public String getWord() {
            return mWord;
        }

        public void setWord(String word) {
            this.mWord = word;
        }

        public Set<Integer> getChildrenIds() {
            return mChildrenIds;
        }

        public void addChildrenId(int id) {
            mChildrenIds.add(id);
        }

        public void setChildrenIds(Set<Integer> childrenIds) {
            this.mChildrenIds = childrenIds;
        }

        public int getParentId() {
            return mParentId;
        }

        public void setParentId(int parentId) {
            this.mParentId = parentId;
        }

        public String getRelation() {
            return mRelation;
        }

        public void setRelation(String relation) {
            this.mRelation = relation;
        }

        public Flag getFlag() {
            return mFlag;
        }

        public void setFlag(Flag flag) {
            this.mFlag = flag;
        }

        public Requisite getRequisite() {
            return mRequisite;
        }

        public void setRequisite(Requisite requisite) {
            this.mRequisite = requisite;
        }
        public boolean isRoot() {
            return mParentId == NOT_EXIST;
        }

    }
    // END Node Class

    public ParseTree() {
        this.mNodeList = new SparseArray<>();
    }

    public ParseTree(ParseTree another) {
        this.mNodeList = new SparseArray<>();
        if (another.mNodeList != null) {
            for (int i = another.mNodeList.size() - 1; i > -1; --i) {
                mNodeList.put(another.mNodeList.keyAt(i),
                        new Node(another.mNodeList.valueAt(i)));
            }
        }
        this.mRootId = another.mRootId;
    }

    public void setNodeById(int id, Node node) {
        node.setId(id);
        mNodeList.put(id, node);
    }

    public Node getNodeById(int id) {
        return mNodeList.get(id);
    }

    public void setNodeByIndex(int index, Node node) {
        mNodeList.setValueAt(index, node);
    }

    public Node getNodeByIndex(int index) {
        return mNodeList.valueAt(index);
    }

    public void deleteNodeById(int id) {
        mNodeList.delete(id);
    }

    public void deleteNodeByIndex(int index) {
        mNodeList.removeAt(index);
    }

    public int size() {
        return mNodeList.size();
    }

    public void setNodeWordById(int id, String word) {
        mNodeList.get(id).setWord(word);
    }

    public void setNodeParentId(int nodeId, int parentId) {
        mNodeList.get(nodeId).setParentId(parentId);
    }

    public void addChildById(int nodeId, int childId) {
        mNodeList.get(nodeId).addChildrenId(childId);
    }

    public void removeChildrenById(int nodeId, ArrayList<Integer> removeChildrenIdList) {
        mNodeList.get(nodeId).getChildrenIds().removeAll(removeChildrenIdList);
    }

    public void addChildrenById(int nodeId, ArrayList<Integer> addChildrenList) {
        mNodeList.get(nodeId).getChildrenIds().addAll(addChildrenList);
    }

    public void setNodeTypeById(int id, String type) {
        mNodeList.get(id).setType(type);
    }

    public void setFlagById(int nodeId, Flag flag) {
        mNodeList.get(nodeId).setFlag(flag);
    }

    public void setNodeRequisiteById(int nodeId, Requisite requisite) {
        mNodeList.get(nodeId).setRequisite(requisite);
    }

    public void setRelationById(int advmodNodeId, String relation) {
        mNodeList.get(advmodNodeId).setRelation(relation);
    }

    public Node getRoot() {
        return mNodeList.get(mRootId);
    }

    /**
     * Check if two nodes share the same parent
     *
     * @param a a node to be checked
     * @param b a node to be checked
     * @return boolean whether they share the same parent
     */
    public boolean isConcatenation(Node a, Node b) {
        return a.getParentId() == b.getParentId();
    }

    /**
     * Check if one node is subordinate to another.
     *
     * @param a      the Node that is checked to be the father/ancestor of b
     * @param b      a Node that is checked to be subordinate to a
     * @param nested only allow the situation that b is the direct child
     *               of a when set to false
     * @return boolean whether b is subordinate to a
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSubordinate(Node a, Node b, Boolean nested) {
        int parentId = b.getParentId(),
                aId = a.getId();
        if (parentId == aId)
            return true;
        while (nested) {
            b = getNodeById(parentId);
            parentId = b.getParentId();
            if (parentId == NOT_EXIST)
                return false;
            if (parentId == aId)
                return true;
        }
        return false;
    }

    /**
     * Merge the Nodes in the ParseTree
     * Example: Let's meet at Hilton Garden Inn
     * [Hilton Garden Inn] Should be merged
     */
    public void merge() {
        merge(getRoot());
        clearMergedNodes(getRoot());
    }

    /**
     * @param node : Start From Root Node
     *             Post-order Traversal Recursive Function
     */
    public void merge(Node node) {

        if (node.getChildrenIds() != null) {
            for (int childId : node.getChildrenIds()) {
                Node child = mNodeList.get(childId);
                merge(child);
                if (child.getFlag() == Flag.MERGE) {
                    if (child.getId() < node.getId())
                        node.setWord(child.getWord() + " " + node.getWord());
                    else
                        node.setWord(node.getWord() + " " + child.getWord());
                    child.setFlag(Flag.DELETE);
                }
            }
        }
    }

    /**
     * Clear the Merged Nodes in the Tree
     *
     * @param node : start From Root Node
     */
    public void clearMergedNodes(Node node) {
        if (node.getChildrenIds() != null) {
            for (int childId : node.getChildrenIds()) {
                Node child = mNodeList.get(childId);
                clearMergedNodes(child);
                if (child.getFlag() == Flag.DELETE) {
                    node.getChildrenIds().remove(childId);
                }
            }
        }
    }


    public boolean isMerge(ArrayList<Node> mNodeList) {
        if (mNodeList == null) return false;
        for (Node n : mNodeList) {
            if (n.getFlag() == Flag.MERGE)
                return true;
        }
        return false;
    }

    private void setRootId(int newRootId) {
        mRootId = newRootId;
        mNodeList.get(newRootId).setParentId(NOT_EXIST);
    }

    /**
     * Change the root of the tree
     *
     * @author adamyi
     */
    public void changeRoot(int nodeId) {
        if (this.mRootId == nodeId)
            return;
        setRootId(nodeId);
        Node currentNode = mNodeList.get(nodeId);
        Stack<Node> ancestors = new Stack<>();
        ancestors.push(currentNode);
        while (currentNode.mParentId != NOT_EXIST) {
            currentNode = mNodeList.get(currentNode.mParentId);
            ancestors.push(currentNode);
        }
        Node nextNode = ancestors.pop();
        while (!ancestors.isEmpty()) {
            currentNode = nextNode;
            if (ancestors.isEmpty()) {
                currentNode.mParentId = NOT_EXIST;
            } else {
                nextNode = ancestors.pop();
                currentNode.mParentId = nextNode.getId();
                currentNode.mChildrenIds.remove(nextNode.getId());
            }
        }
    }

    public int getRootId() {
        return mRootId;
    }

    /**
     * Override Function toString()
     * Use the Recursive Function toString() Defined in Node
     * To Print the ParseTree
     */
    @Override
    public String toString() {
        return print("", getRoot()).toString();
    }

    private StringBuilder print(String indent, Node node) {
        StringBuilder ret = new StringBuilder(indent);
        if (node.getRelation() != null)
            ret.append(node.getRelation());
        ret.append("(").append(node.getType()).append(" ").append(node.getWord()).append(")");
        if (node.getChildrenIds().size() > 0) {
            ret.append(" {\n");
            for (int childId : node.getChildrenIds()) {
                ret.append(print(indent + "  ", getNodeById(childId)));
            }
            ret.append(indent).append("}");
        }
        ret.append("\n");
        return ret;
    }

    public String toJson() {
        return JSONUtils.simpleObjectToJson(this, ParseTree.class);
    }

    public static ArrayList<ParseTree> split(ParseTree tree) {
        /*
        TODO: change this
        ArrayList<ParseTree> list = new ArrayList<>();
        if (tree.getRoot().getFlag() == FLAG_NORMAL) {
            list.add(new ParseTree(tree.getRoot()));
        } else {
            for (Node node : tree.getRoot().getChildren()) {
                list.add(new ParseTree(node));
            }
        }
        return list;*/
        return null;
    }

    public void setNodeList(SparseArray<Node> nodeList) {
        mNodeList = nodeList;
    }

    public SparseArray<Node> getNodeList() {
        return mNodeList;
    }


}