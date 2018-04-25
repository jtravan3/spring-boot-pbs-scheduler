package com.jtravan.datastructures.trees.binary.expressions;

public class BinaryNode<T> {
    public T item;
    public BinaryNode<T> left;
    public BinaryNode<T> right;

    public BinaryNode() {
    }

    public BinaryNode(T element) {
        item = element;
    }

    public BinaryNode(T element, BinaryNode<T> leftNode, BinaryNode<T> rightNode) {
        item = element;
        left = leftNode;
        right = rightNode;
    }

    public boolean isLeaf() {
        return (left == null && right == null);
    }

    public String toString() {
        return item.toString();
    }
}
