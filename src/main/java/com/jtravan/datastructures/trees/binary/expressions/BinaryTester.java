package com.jtravan.datastructures.trees.binary.expressions;

class BinaryTester
{
    public static void main(String[] args)
    {

//        String expression = "(1+2)";
        String expression = "((3*(8-2))-(1+9))";
        System.out.println("Expression: " + expression);
        BinaryExpressionTree bet = new BinaryExpressionTree(expression);
        System.out.println("Infix: ");
        bet.DisplayInOrder();
        System.out.println("Postfix: ");
        bet.DisplayPostOrder();
        System.out.println("Prefix: ");
        bet.DisplayPreOrder();
        System.out.println(expression + " = " +bet.Eval());
    }
}
