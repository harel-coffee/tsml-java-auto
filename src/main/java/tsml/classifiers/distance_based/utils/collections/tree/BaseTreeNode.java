/*
 * This file is part of the UEA Time Series Machine Learning (TSML) toolbox.
 *
 * The UEA TSML toolbox is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * The UEA TSML toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the UEA TSML toolbox. If not, see <https://www.gnu.org/licenses/>.
 */
 
package tsml.classifiers.distance_based.utils.collections.tree;

import java.util.*;

/**
 * Purpose: node of a tree data structure.
 *
 * Contributors: goastler
 */
public class BaseTreeNode<A> extends AbstractList<TreeNode<A>> implements TreeNode<A> {

    private final List<TreeNode<A>> children = new ArrayList<>();
    private A element;
    private TreeNode<A> parent;
    // skip is a utility to skip calling recursive calls when handling the parent/child relationship of nodes
    private boolean skip;
    
    public BaseTreeNode() {}

    public BaseTreeNode(A element) {
        this(element, null);
    }

    public BaseTreeNode(A element, TreeNode<A> parent) {
        setValue(element);
        setParent(parent);
    }

    @Override public boolean equals(final Object o) {
        return this == o;
    }

    @Override
    public TreeNode<A> getParent() {
        return parent;
    }

    @Override
    public void setParent(TreeNode<A> nextParent) {
        if(parent == nextParent) {
            // do nothing
            return;
        }
        if(parent != null && !skip) {
            // remove this child from the parent
            parent.remove(this);
        }
        if(nextParent != null) {
            // add this child to the new parent
            nextParent.add(this);
        }
        this.parent = nextParent;
    }

    @Override
    public A getValue() {
        return element;
    }

    @Override
    public void setValue(A element) {
        this.element = element;
    }

    /**
     * total number of nodes in the tree, including this one
     * @return
     */
    @Override
    public int size() {
        return children.size();
    }

    @Override public void add(final int i, final TreeNode<A> node) {
        if(skip) return;
        if(node.getParent() == this) {
            throw new IllegalArgumentException("already a child");
        }
        // node is not a child yet
        // add the node to the children
        children.add(i, node);
        // set this node as the parent
        skip = true;
        node.setParent(this);
        skip = false;
    }

    @Override public boolean add(final TreeNode<A> node) {
        int size = children.size();
        add(children.size(), node);
        return size != children.size();
    }

    @Override public TreeNode<A> get(final int i) {
        return children.get(i);
    }

    @Override public TreeNode<A> set(final int i, final TreeNode<A> child) {
        if(skip) return null;
        if(child.getParent() == this) {
            // already a child - cannot house multiple children
            throw new IllegalArgumentException("already a child: " + child);
        }
        // get the previous
        TreeNode<A> previous = children.get(i);
        // overwrite the previous
        children.set(i, child);
        skip = true;
        // remove this as the parent of the overwritten
        previous.removeParent();
        // setup the new node as a child
        child.setParent(this);
        skip = false;
        return previous;
    }

    @Override public TreeNode<A> remove(final int i) {
        if(skip) return null;
        // remove the child
        TreeNode<A> child = children.remove(i);
        // discard the parent
        skip = true;
        child.removeParent();
        skip = false;
        return child;
    }

    @Override public String toString() {
        if(children.isEmpty()) {
            return "BaseTreeNode{" +
                           "location=" + getLocation() +
                           ", element=" + element + "}";
        }
        return "BaseTreeNode{" +
                       "location=" + getLocation() +
                       ", element=" + element +
                       ", children=" + children +
                       '}';
    }

    @Override public void clear() {
        // remove all the children
        for(int i = children.size() - 1; i >= 0; i--) {
            final TreeNode<A> child = children.get(i);
            child.removeParent();
        }
    }
}
