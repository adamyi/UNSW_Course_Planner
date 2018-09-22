package com.adamyi.courseplanner.nlp;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static com.adamyi.courseplanner.nlp.ParseTree.NOT_EXIST;

public class TreeParser {
    private ParseTree mTree;

    public ParseTree getTree() {
        return mTree;
    }

    public TreeParser(ParseTree tree) {
        mTree = tree;
    }

    /**
     * Reduce the tree to keep only essential data
     * This should not be called directly, but
     * instead called from the other reduce function.
     */
    private void recursiveReduce(int nodeId) {
        if (mTree.getNodeById(nodeId).getChildrenIds() != null) {
            ArrayList<Integer> toDelete = new ArrayList<>();
            ArrayList<Integer> toAdd = new ArrayList<>();
            int subjNodeId = NOT_EXIST;
            int dobjNodeId = NOT_EXIST;
            int iobjNodeId = NOT_EXIST;
            for (int childId : mTree.getNodeById(nodeId).getChildrenIds()) {
                recursiveReduce(childId);
                switch (mTree.getNodeById(childId).getFlag()) {
                    case MERGE:
                        mTree.setNodeWordById(nodeId, mTree.getNodeById(childId).getWord() + " " + mTree.getNodeById(nodeId).getWord());   //???TODO: Or node.getWord() + child.getWord()
                    case DELETE:
                        if (mTree.getNodeById(childId).getChildrenIds() != null) {
                            for (int ccId : mTree.getNodeById(childId).getChildrenIds()) {
                                mTree.setNodeParentId(ccId, nodeId);
                                toAdd.add(ccId);
                            }
                        }
                        toDelete.add(childId);
                        mTree.deleteNodeById(childId);
                        continue;
                }
                /* TODO check the relationship between Subjects (getRelation == "nsubj")
                   TODO and Objects (dobj and pobj)
                 */
                if (mTree.getNodeById(childId).getRelation().equals(ParseTree.DEP_NOUN_SUBJECT))
                    subjNodeId = childId;
                if (mTree.getNodeById(childId).getRelation().equals(ParseTree.DEP_OBJECTIVE))
                    dobjNodeId = childId;
                if (mTree.getNodeById(childId).getRelation().equals(ParseTree.DEP_INDIRECT_OBJECTIVE))
                    iobjNodeId = childId;
            }
            if (subjNodeId != NOT_EXIST) {
                if (dobjNodeId != NOT_EXIST) {
                    mTree.setNodeParentId(subjNodeId, dobjNodeId);
                    mTree.addChildById(dobjNodeId, subjNodeId);
                    toDelete.add(subjNodeId);
                } else if (iobjNodeId != NOT_EXIST) {
                    mTree.setNodeParentId(subjNodeId, iobjNodeId);
                    mTree.addChildById(iobjNodeId, subjNodeId);
                    toDelete.add(subjNodeId);
                }
            }
            mTree.removeChildrenById(nodeId, toDelete);
            mTree.addChildrenById(nodeId, toAdd);
        }
        if (mTree.getNodeById(nodeId).getWord().toLowerCase().equals("when")) {
            mTree.setNodeWordById(nodeId, "time");
            mTree.setNodeTypeById(nodeId, ParseTree.POS_NOUN);
        }
        if (mTree.getNodeById(nodeId).getWord().toLowerCase().equals("where")) {

            mTree.setNodeWordById(nodeId, "location");
            mTree.setNodeTypeById(nodeId, ParseTree.POS_NOUN);
        }
        if (mTree.getNodeById(nodeId).getType().equals(ParseTree.POS_NOUN) || mTree.getNodeById(nodeId).getType().equals(ParseTree.POS_PROPERNOUN)) { // Nouns
            return;
        }
        if (mTree.getNodeById(nodeId).getType().startsWith(ParseTree.POS_PRONOUN)) {
            if (mTree.getNodeById(nodeId).getWord().toLowerCase().equals("you")) {
                if (mTree.getNodeById(nodeId).getRelation().equals(ParseTree.DEP_NOUN_SUBJECT)) {
                    return;
                } else {
                    mTree.getNodeById(nodeId).setFlag(ParseTree.Flag.DELETE);
                    return;
                }
            }
            if (!mTree.getNodeById(nodeId).getWord().toLowerCase().equals("me")) {
                return;
            }
        }
        // TODO: double check why we need to remove this strange node.
        // This node already has a matched tag.
        // Do we really want to delete it?
        //mTree.setNodeFlagById(nodeId, ParseTree.Flag.DELETE);
    }

    private void resolveObjectRelation(int nodeId) {
        if (mTree.getNodeById(nodeId).getChildrenIds() != null) {
            ArrayList<Integer> toDelete = new ArrayList<>();
            int subjNodeId = NOT_EXIST;
            int dobjNodeId = NOT_EXIST;
            int iobjNodeId = NOT_EXIST;
            for (int childId : mTree.getNodeById(nodeId).getChildrenIds()) {
                if (mTree.getNodeById(childId).getRelation().equals(ParseTree.DEP_NOUN_SUBJECT))
                    subjNodeId = childId;
                if (mTree.getNodeById(childId).getRelation().equals(ParseTree.DEP_OBJECTIVE))
                    dobjNodeId = childId;
                if (mTree.getNodeById(childId).getRelation().equals(ParseTree.DEP_INDIRECT_OBJECTIVE))
                    iobjNodeId = childId;
            }
            if (subjNodeId != NOT_EXIST) {
                if (dobjNodeId != NOT_EXIST) {
                    mTree.setNodeParentId(subjNodeId, dobjNodeId);
                    mTree.addChildById(dobjNodeId, subjNodeId);
                    toDelete.add(subjNodeId);
                } else if (iobjNodeId != NOT_EXIST) {
                    mTree.setNodeParentId(subjNodeId, iobjNodeId);
                    mTree.addChildById(iobjNodeId, subjNodeId);
                    toDelete.add(subjNodeId);
                }
            }
            mTree.removeChildrenById(nodeId, toDelete);
        }

    }

    /**
     * Reduce the tree to keep only essential data
     * This should not be called directly, but
     * instead called from the other reduce function.
     */
    private void doReduce() {
        ParseTree.Node root = mTree.getRoot();

        recursiveReduce(mTree.getRootId());            //root Node
        if (root.getFlag() == ParseTree.Flag.DELETE) {
            Iterator<Integer> it = root.getChildrenIds().iterator();
            if (it.hasNext()) {
                int firstId = it.next();
                if (root.getChildrenIds().size() > 1) {
                    ArrayList<Integer> toDemote = new ArrayList<>();
                    int nodeId;
                    while (it.hasNext()) {
                        nodeId = it.next();
                        mTree.setNodeParentId(nodeId, root.getId());
                        toDemote.add(nodeId);
                    }
                    mTree.addChildrenById(firstId, toDemote);
                    mTree.removeChildrenById(root.getId(), toDemote);
                }
                if (mTree.getNodeById(firstId).getChildrenIds().size() > 0) {
                    ArrayList<Integer> toPromote = new ArrayList<>();
                    Iterator<Integer> cIt = mTree.getNodeById(firstId).getChildrenIds().iterator();
                    int cFirstId = cIt.next(), cNodeId;
                    while (cIt.hasNext()) {
                        cNodeId = cIt.next();
                        if (mTree.getNodeById(cNodeId).getRelation().equals(ParseTree.DEP_CONJUNCTION) || mTree.getNodeById(cNodeId).getRelation().equals(ParseTree.DEP_COORDINATING_CONJUNCTION)) {
                            mTree.setNodeParentId(cNodeId, root.getId());
                            toPromote.add(cNodeId);
                        }
                    }
                    mTree.addChildrenById(root.getId(), toPromote);
                    mTree.removeChildrenById(cFirstId, toPromote);
                }

                mTree.changeRoot(root.getChildrenIds().iterator().next());
                mTree.deleteNodeById(root.getId());
            }
        }
        // advmod
        if (root.getChildrenIds().size() > 0) {
            int advmodNodeId = NOT_EXIST;
            for (Integer nodeId : root.getChildrenIds()) {
                if (mTree.getNodeById(nodeId).getRelation().equals(ParseTree.DEP_ADVERB_MODIFIER)) {
                    advmodNodeId = nodeId;
                    break;
                }
            }
            if (advmodNodeId != NOT_EXIST) {
                // swap the contents
                String tmp = mTree.getNodeById(advmodNodeId).getType();
                mTree.setNodeTypeById(advmodNodeId, root.getType());
                root.setType(tmp);
                tmp = mTree.getNodeById(advmodNodeId).getWord();
                mTree.setNodeWordById(advmodNodeId, root.getWord());
                root.setWord(tmp);
                tmp = mTree.getNodeById(advmodNodeId).getRelation();
                mTree.setRelationById(advmodNodeId, root.getRelation());
                root.setRelation(tmp);
                resolveObjectRelation(root.getId());
            }
        }

        removeFlagDelete();

        // TODO: check for a case which *doesn't* use advmod (when or where) but still requires some special handling re. subject and object?
    }


    private void removeFlagDelete() {
        ArrayList<Integer> deleteFlagNodes = new ArrayList<>();
        for (int i = mTree.getNodeList().size() - 1; i > -1; --i) {
            if (mTree.getNodeByIndex(i).getFlag() == ParseTree.Flag.DELETE) {
                mTree.deleteNodeByIndex(i);
            }
        }
    }

    private void setSubTreeRequisite(int nodeId, ParseTree.Requisite requisite) {
        mTree.setNodeRequisiteById(nodeId, requisite);
        if (mTree.getNodeById(nodeId).getChildrenIds() != null) {
            for (int childId : mTree.getNodeById(nodeId).getChildrenIds()) {
                setSubTreeRequisite(childId, requisite);
            }
        }

    }


    public void analyzeRequisite(int nodeId) {
        if (mTree.getNodeById(nodeId).getChildrenIds() != null) {
            for (int childId : mTree.getNodeById(nodeId).getChildrenIds()) {
                analyzeRequisite(childId);
                if (StringUtils.equalsIgnoreCase(mTree.getNodeById(childId).getWord(), "prerequisite") ||
                        StringUtils.equalsIgnoreCase(mTree.getNodeById(childId).getWord(), "pre-requisite")) {
                    setSubTreeRequisite(childId, ParseTree.Requisite.PRE);

                } else if (StringUtils.equalsIgnoreCase(mTree.getNodeById(childId).getWord(), "corequisite") ||
                        StringUtils.equalsIgnoreCase(mTree.getNodeById(childId).getWord(), "co-requisite")) {
                    setSubTreeRequisite(childId, ParseTree.Requisite.CO);
                }
            }
        }

    }
    public void analyzeCourses() {

    }
    public void reduceTree() {

    }
}