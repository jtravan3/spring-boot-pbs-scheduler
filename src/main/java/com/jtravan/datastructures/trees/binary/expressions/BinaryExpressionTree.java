package com.jtravan.datastructures.trees.binary.expressions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryExpressionTree
{
    private Logger LOG = LoggerFactory.getLogger("BinaryExpressionTree");

    public int Count;
    public BinaryNode<Character> rootNode;
    private String expression;

    public BinaryExpressionTree(String expression)
    {
        this.expression = expression;
        rootNode = Parse();
    }

    public BinaryNode<Character> Parse()
    {
        char next;
        next = expression.charAt(0);

        if (next == '(') {
            expression = expression.substring(1);

            BinaryNode<Character> left = Parse();
            expression = expression.substring(1);

            char operator = expression.charAt(0);
            expression = expression.substring(1);

            BinaryNode<Character> right = Parse();
            expression = expression.substring(1);

            return new BinaryNode<Character>(operator,left, right);
        } else {

            return new BinaryNode<Character>(next);
        }
    }

    public void DisplayPreOrder()
    {
        // TODO create code to perform a preorder traversal
    }

    public void DisplayPostOrder()
    {
        // TODO create code to perform a postorder traversal
    }

    public double Eval()
    {
        return Eval(rootNode);
    }

    private double Eval(BinaryNode<Character> node)
    {
        double value = 0;

        if (node.isLeaf())
        {
            // cheap way to convert a single digit number to an integer
            value = node.item - '0';
        }
        else {
            double left = Eval(node.left);
            double right = Eval(node.right);
            switch (node.item)
            {
                case '+':
                    // evaluate operator
                    value = left + right;
                    break;
                case '-':
                    // evaluate operator
                    value = left - right;
                    break;
                case '*':
                    // evaluate operator
                    value = left * right;
                    break;
                case '/':
                    // evaluate operator
                    value = left / right;
                    break;
                default:
                    LOG.info("Invalid OP" + node.item);
                    value = 0;
                    break;
            }
        }

        return value;
    }

    public void DisplayInOrder()
    {
        if (rootNode != null)
        {
            DisplayInOrder(rootNode);
        }
        else
        {
            LOG.info("Tree is empty.");
        }
    }

    private void DisplayInOrder(BinaryNode<Character> node)
    {
        if (node.left != null)
        {
            LOG.info("(");
            DisplayInOrder(node.left);
        }
        LOG.info(node.item.toString());
        if (node.right != null)
        {
            DisplayInOrder(node.right);
            LOG.info(")");
        }
    }

}

