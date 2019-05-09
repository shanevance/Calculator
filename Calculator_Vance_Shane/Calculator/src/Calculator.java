import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Stack;

/**
 * 
 * @author Shane Vance
 *
 */

public class Calculator {
	
	/**
	 * 
	 * This method calculates a string input using Infix notation. Infix, is how we humans
	 * do mathematical evaluating of expressions using the PEMDAS (Parentheses, Exponents,
	 * Multiplication, Division, Addition, Subtraction) notation. This method goes through
	 * the string does its operations by finding the operand and applying it to and operator.
	 * It will then pop the operator and operand off the stack so that it can continue evaluating.
	 * It will also throw an error if there is division by zero, since this cannot be done. 
	 * 
	 * @param exp String expression
	 * @return BigDecimal that way large real numbers can be handled
	 * 
	 */
	
	public static BigDecimal eval(String exp) {

		char[] expression = exp.toCharArray();
		Stack<BigDecimal> operand = new Stack<BigDecimal>();
		Stack<Character> operator = new Stack<Character>();

		for (int i = 0; i < expression.length; i++) {

			if (isOperand(expression[i]) || expression[i] == '.') {

				String s = "";
				
				while (i < expression.length) {
					
					if (isOperand(expression[i]) || expression[i] == '.') {
					
						s += expression[i++];
				
					} else {
					
						break;
					
					}
					
				}
				
				if (i == expression.length) {
				
					i = i - 1;
				
				}
				
				operand.push(new BigDecimal(s));
			
			} 
			
			if (isOpen(expression[i])) {

				operator.push(expression[i]);

			} 
			
			if (isClose(expression[i])) {

				while (operator.peek() != '(') {

					operand.push(applyOperator(operator.pop(), operand.pop(), operand.pop()));
					
				}
				
				operator.pop();

			} 
			
			if (isOperator(expression[i])) {
				
				while (!operator.empty() && hasPrecedence(expression[i], operator.peek())) {

					operand.push(applyOperator(operator.pop(), operand.pop(), operand.pop()));

				}
				
				operator.push(expression[i]);

			}
			
		}

		while (!operator.empty()) {
			
			operand.push(applyOperator(operator.pop(), operand.pop(), operand.pop()));

		}
		
		return operand.pop();

	}
	
	private static boolean hasPrecedence(char c, char operator) {
		
		return !(isOpen(operator) || isClose(operator) || ((c == '*' || c == '/' || c == '^') && (operator == '+' || operator == '-')));
	
	}

	/**
	 * This goes through and apply the operator as needed
	 * @param operator
	 * @param b
	 * @param a
	 * @return
	 */
	
	private static BigDecimal applyOperator(char operator, BigDecimal b, BigDecimal a) {

		switch (operator) {

		case '+':

			return a.add(b);

		case '-':

			return a.subtract(b);

		case '*':

			return a.multiply(b);

		case '^':
			
			try {
				
				// Checks to see if the exponent contains a negative power or a double exponent
				if (b.toString().contains(Character.toString('-')) || b.toString().contains(Character.toString('.'))) {
				
					BigDecimal result;
					double a1 = a.doubleValue();
					double b1 = b.doubleValue();
					double pow = Math.pow(a1, b1);
					String str = "" + pow;
					result = new BigDecimal(str);
					return result;
					
				} else {
					
					// This will hand large exponents and throw exceptions if it cannot
					try {
									
						 int n = b.intValueExact();
						 return a.pow(n);		
					
					} catch(UnsupportedOperationException e) {
						
						throw new UnsupportedOperationException("Power must be a double");

					} catch (ArithmeticException e) {
						
						throw new ArithmeticException("Error: invalid input");
						
					}
					
				}
				
			} catch(UnsupportedOperationException e) {

				throw new UnsupportedOperationException("Exponent not supported");
				
			}
		
		case '/':

			if (b.toString().equals("0")) {

				throw new UnsupportedOperationException("Cannot divide by zero");

			} else {
				
				return a.divide(b, MathContext.DECIMAL128); // Allows for repeating digits

			}

		}

		return BigDecimal.ZERO;

	}	
	
	/**
	 * 
	 * This will convert a string into a readable representation for the evaluator
	 * to evaluate.
	 * 
	 * @param e
	 * @return
	 */
	
	public static String convertToProperExpression(String e) {

		String str = "";

		ArrayList<String> temp = new ArrayList<String>();

		ArrayList<Character> c = new ArrayList<Character>();

		for (char ch : e.toCharArray()) {
			
			if (isOpen(ch)) {
				
				c.add('(');
				
			} else if (isClose(ch)) {
				
				c.add(')');
				
			} else {
				
				c.add(ch);
				
			}
			
		}
		
		try {
			
			c = removeOpen(c);
			
		} catch (IndexOutOfBoundsException ex) {
			
			throw new IndexOutOfBoundsException("Error: invalid input");
			
		}

		for (int i = 0; i < c.size(); i++) {

			temp.add(Character.toString(c.get(i)));
			
			if (i + 1 < c.size()) {
				
				if (isClose(c.get(i)) && isOperand(c.get(i + 1))) {

					temp.add("*");

				}

				
				if (isClose(c.get(i)) && isOpen(c.get(i + 1))) {

					temp.add("*");

				}

				if (isOperand(c.get(i)) && isOpen(c.get(i + 1))) {

					temp.add("*");

				}
				
				// Converts two minus' into a plus
				if (c.get(i) == '-' && c.get(i + 1) == '-') {
					
					temp.remove(temp.size() - 1);
					temp.add("+");
					
					if (i == 0 && c.get(i) == '-' && c.get(i + 1) == '-') {
						
						temp.remove(temp.size() - 1);
						i++;
						continue;
					
					}
					
					if (i != 0 && (isOpen(c.get(i - 1)) && isOpen(c.get(i + 2))) || (isOpen(c.get(i - 1)) && isOperand(c.get(i + 2)))) {
						
						temp.remove(temp.size() - 1);
						i++;
						continue;
					
					}
					
					i++;
					continue;
					
				}
				
				if (c.get(i) == '+' && c.get(i + 1) == '-') {
					
					temp.remove(temp.size() - 1);
					temp.add("-");
					i++;
					continue;
					
				}				
				
				// Checks to see if the current value is negative
				if (c.get(i) == '-') {					
					
					if (i == 0 || (i != 0 && c.get(i - 1) == '+') || isOpen(c.get(i + 1)) || isOpen(c.get(i - 1))){
			
						temp.remove(temp.size() - 1);
						temp.add("0-");
						
					}
					
					if (i != 0) {
						
						if (isClose(c.get(i - 1))) {
							
							temp.remove(temp.size() - 1);
							temp.add("+0-");
							
						}
						
						if (i != 0 && (c.get(i - 1) == '*' || c.get(i - 1) == '/')) {						
							
							temp.remove(temp.size() - 1);
							temp.add("1+0-");
							
						}	
						
					} 					
					
				}

			}

		}
		
		// Builds the string and collapses any whitespace
		for (int i = 0; i < temp.size(); i++) {
			
			if (temp.get(i).equals(" ")) {			
				
				continue;				
				
			} else {
			
				str += temp.get(i);
			
			}
			
		}

		return str;

	}

	// Goes through and checks to see if there are any open or close braces 
	private static ArrayList<Character> removeOpen(ArrayList<Character> c) {

		for (int i = 0; i < c.size(); i++) {

			if (isOpen(c.get(i)) && isClose(c.get(i + 1))) {

				c.remove(i);
				c.remove(i);
				return removeOpen(c);

			}

		}

		return c;

	}

	// Evaluates whether the string is an equation
	public static boolean isEquation(String e) {

		return isEquationHelper(e.toCharArray(), 0, new Stack<Character>(), false);

	}

	// Recursive helper method for the equation checker
	// Parses through to see if the string is in the correct format to be evaluated
	private static boolean isEquationHelper(char[] c, int index, Stack<Character> open, boolean result) {

		if (index == c.length && open.empty()) {

			return result;

		} else if (index == c.length && !open.empty() || !(isDecimal(c[index]) || isOperand(c[index])
				|| isOperator(c[index]) || isOpen(c[index]) || isClose(c[index]))) {

			return false;

		} else if (isDecimal(c[index]) && (isOperator(c[index + 1]) || isOpen(c[index + 1]) || isClose(c[index + 1]))) {

			return false;

		} else if (isOpen(c[index])) {

			open.push(c[index]);
			return isEquationHelper(c, index + 1, open, result);

		} else if (open.empty() && isClose(c[index])) {

			return false;

		} else if (!open.empty() && isClose(open, c[index])) {

			open.pop();
			return isEquationHelper(c, index + 1, open, true);

		} else if (isOperator(c[index])) {

			if (c[index] == '-'
					&& !(c[index + 1] == '/' || c[index + 1] == '*' || c[index + 1] == '+' || c[index + 1] == '/')) {

				return isEquationHelper(c, index + 1, open, true);

			} else if (index == 0 || index == (c.length - 1) || isOperator(c[index + 1]) && c[index + 1] != '-'
					|| isOpen(c[index - 1]) && isOperand(c[index + 1])
					|| isClose(c[index + 1]) && isOperand(c[index - 1])) {

				return false;

			} else {

				return isEquationHelper(c, index + 1, open, true);

			}

		} else {

			return isEquationHelper(c, index + 1, open, true);

		}

	}

	private static boolean isDecimal(char c) {

		return c == '.';

	}

	private static boolean isOperand(char c) {

		return c >= '0' && c <= '9';

	}

	private static boolean isOpen(char c) {

		return c == '(' || c == '{' || c == '[';

	}

	private static boolean isClose(char c) {

		return c == ')' || c == '}' || c == ']';

	}

	private static boolean isClose(Stack<Character> open, char c) {

		return open.peek() == '(' && c == ')' || open.peek() == '{' && c == '}' || open.peek() == '[' && c == ']';

	}

	private static boolean isOperator(char c) {

		return c == '+' || c == '-' || c == '/' || c == '*' || c == '^';

	}

}