import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.regex.*;

public class ScientificCalculator {
    private JFrame frame;
    private JTextField textField;
    private String expression = "";

    public ScientificCalculator() {
        frame = new JFrame("Scientific Calculator");
        frame.setSize(400, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(50, 50, 50));

        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.BOLD, 24));
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setEditable(false);
        frame.add(textField, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.weightx = 1;
        gbc.weighty = 1;

        String[] buttons = {
            "7", "8", "9", "/", "sin",
            "4", "5", "6", "*", "cos",
            "1", "2", "3", "-", "tan",
            "0", ".", "=", "+", "log",
            "C", "⌫", "sqrt", "^", "(", ")"
        };

        int row = 0, col = 0;
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBackground(new Color(80, 80, 80));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(new ButtonClickListener());

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(100, 100, 100));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(80, 80, 80));
                }
            });

            gbc.gridx = col;
            gbc.gridy = row;
            panel.add(button, gbc);

            col++;
            if (col > 4) {
                col = 0;
                row++;
            }
        }

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (command.equals("C")) {
                expression = "";
                textField.setText("");
            } else if (command.equals("⌫")) {
                if (!expression.isEmpty()) {
                    expression = expression.substring(0, expression.length() - 1);
                    textField.setText(expression);
                }
            } else if (command.equals("=")) {
                try {
                    double result = evaluateExpression(expression);
                    textField.setText(String.valueOf(result));
                    expression = String.valueOf(result);
                } catch (Exception ex) {
                    textField.setText("Error");
                }
            } else {
                expression += command;
                textField.setText(expression);
            }
        }
    }

    private double evaluateExpression(String exp) {
        try {
            return new ExpressionEvaluator().evaluate(exp);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ScientificCalculator::new);
    }
}

class ExpressionEvaluator {
    public double evaluate(String expression) {
        return evaluatePostfix(infixToPostfix(expression));
    }

    private String infixToPostfix(String expression) {
        StringBuilder result = new StringBuilder();
        Stack<String> stack = new Stack<>();
        Pattern pattern = Pattern.compile("sin|cos|tan|log|sqrt|[-+*/^()]|\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String token = matcher.group();

            if (token.matches("\\d+(\\.\\d+)?")) {
                result.append(token).append(' ');
            } else if (isFunction(token)) {
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    result.append(stack.pop()).append(' ');
                }
                stack.pop();
                if (!stack.isEmpty() && isFunction(stack.peek())) {
                    result.append(stack.pop()).append(' ');
                }
            } else {
                while (!stack.isEmpty() && precedence(token) <= precedence(stack.peek())) {
                    result.append(stack.pop()).append(' ');
                }
                stack.push(token);
            }
        }

        while (!stack.isEmpty()) {
            result.append(stack.pop()).append(' ');
        }
        return result.toString();
    }

    private double evaluatePostfix(String postfix) {
        Stack<Double> stack = new Stack<>();
        Scanner scanner = new Scanner(postfix);

        while (scanner.hasNext()) {
            if (scanner.hasNextDouble()) {
                stack.push(scanner.nextDouble());
            } else {
                String token = scanner.next();
                if (isFunction(token)) {
                    double a = stack.pop();
                    switch (token) {
                        case "sin" -> stack.push(Math.sin(Math.toRadians(a)));
                        case "cos" -> stack.push(Math.cos(Math.toRadians(a)));
                        case "tan" -> stack.push(Math.tan(Math.toRadians(a)));
                        case "log" -> stack.push(Math.log10(a));
                        case "sqrt" -> stack.push(Math.sqrt(a));
                    }
                } else {
                    double b = stack.pop();
                    double a = stack.isEmpty() ? 0 : stack.pop();
                    switch (token) {
                        case "+" -> stack.push(a + b);
                        case "-" -> stack.push(a - b);
                        case "*" -> stack.push(a * b);
                        case "/" -> stack.push(a / b);
                        case "^" -> stack.push(Math.pow(a, b));
                    }
                }
            }
        }
        scanner.close();
        return stack.pop();
    }

    private int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            default -> 0;
        };
    }

    private boolean isFunction(String token) {
        return token.equals("sin") || token.equals("cos") || token.equals("tan") ||
               token.equals("log") || token.equals("sqrt");
    }
}
