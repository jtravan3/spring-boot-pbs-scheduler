package com.jtravan.datastructures.trees.binary.expressions;


public class BinaryExpressionTree
{
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
                    System.out.println("Invalid OP" + node.item);
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
            System.out.println();
        }
        else
        {
            System.out.println("Tree is empty.");
        }
    }

    private void DisplayInOrder(BinaryNode<Character> node)
    {
        if (node.left != null)
        {
            System.out.print('(');
            DisplayInOrder(node.left);
        }
        System.out.print(node.item);
        if (node.right != null)
        {
            DisplayInOrder(node.right);
            System.out.print(')');
        }
    }

}

