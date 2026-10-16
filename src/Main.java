import java.util.*;

public class Main {
    private static final String EPSILON = "eps";

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("Computation Theory Project");
        System.out.println("DFA Simulator and NFA to DFA Converter");
        System.out.println("========================================");
        System.out.println("1) Phase 1 - DFA Simulator");
        System.out.println("2) Phase 2 - NFA to DFA Converter");
        System.out.print("Choose phase: ");

        String choice = input.nextLine().trim();

        if (choice.equals("1")) {
            runDfaSimulator(input);
        } else if (choice.equals("2")) {
            runNfaToDfaConverter(input);
        } else {
            System.out.println("Invalid choice.");
        }

        input.close();
    }

    // =====================================================
    // Phase 1: DFA Simulator
    // =====================================================
    private static void runDfaSimulator(Scanner input) {
        System.out.println("\n========================================");
        System.out.println("PHASE 1: DFA SIMULATOR");
        System.out.println("========================================");

        System.out.print("Enter number of states: ");
        int stateCount = Integer.parseInt(input.nextLine().trim());

        System.out.println("Enter states separated by space:");
        Set<String> states = readSet(input.nextLine());

        while (states.size() != stateCount) {
            System.out.println("Number of states is not correct. Enter states again:");
            states = readSet(input.nextLine());
        }

        System.out.println("Enter alphabet symbols separated by space (example: a b):");
        Set<String> alphabet = readSet(input.nextLine());

        Map<String, Map<String, String>> transitionTable = new LinkedHashMap<>();
        for (String state : states) {
            transitionTable.put(state, new LinkedHashMap<>());
        }

        System.out.print("Enter number of transitions: ");
        int transitionCount = Integer.parseInt(input.nextLine().trim());

        System.out.println("Enter transitions in this format: from symbol to");
        System.out.println("Example: q0 a q1");

        for (int i = 0; i < transitionCount; i++) {
            String line = input.nextLine().trim();
            String[] parts = line.split("\\s+");

            if (parts.length != 3) {
                System.out.println("Invalid format. Enter transition again.");
                i--;
                continue;
            }

            String from = parts[0];
            String symbol = parts[1];
            String to = parts[2];

            if (!states.contains(from)) {
                System.out.println("Invalid transition: source state is not in Q. Enter again.");
                i--;
                continue;
            }
            if (!alphabet.contains(symbol)) {
                System.out.println("Invalid transition: symbol is not in Sigma. Enter again.");
                i--;
                continue;
            }
            if (!states.contains(to)) {
                System.out.println("Invalid transition: destination state is not in Q. Enter again.");
                i--;
                continue;
            }
            if (transitionTable.get(from).containsKey(symbol)) {
                System.out.println("Invalid DFA transition: this state and symbol already have a destination. Enter again.");
                i--;
                continue;
            }

            transitionTable.get(from).put(symbol, to);
        }

        System.out.print("Enter start state: ");
        String startState = input.nextLine().trim();

        System.out.println("Enter final states separated by space:");
        Set<String> finalStates = readSet(input.nextLine());

        boolean validDfa = validateDfa(states, alphabet, transitionTable, startState, finalStates);
        if (!validDfa) {
            System.out.println("\nThe DFA definition is not complete or not valid. Please run the program again with correct input.");
            return;
        }

        printDfaDefinition(states, alphabet, transitionTable, startState, finalStates);

        System.out.print("Enter number of test strings: ");
        int testCount = Integer.parseInt(input.nextLine().trim());

        List<String> testStrings = new ArrayList<>();
        System.out.println("Enter each test string on a separate line.");
        System.out.println("For empty string, type: empty");
        for (int i = 0; i < testCount; i++) {
            String testString = input.nextLine().trim();
            if (testString.equalsIgnoreCase("empty") || testString.equals("epsilon") || testString.equals("ε")) {
                testString = "";
            }
            testStrings.add(testString);
        }

        System.out.println("\n========================================");
        System.out.println("DFA SIMULATION TRACE");
        System.out.println("========================================");

        Map<String, String> summary = new LinkedHashMap<>();
        for (String testString : testStrings) {
            String result = simulateDfa(transitionTable, alphabet, startState, finalStates, testString);
            summary.put(testString.isEmpty() ? "empty" : testString, result);
        }

        System.out.println("\n========================================");
        System.out.println("SUMMARY OF TEST STRINGS");
        System.out.println("========================================");
        System.out.printf("%-15s %-10s%n", "String", "Result");
        System.out.println("---------------------------");
        for (String text : summary.keySet()) {
            System.out.printf("%-15s %-10s%n", text, summary.get(text));
        }
    }

    private static boolean validateDfa(Set<String> states,
                                       Set<String> alphabet,
                                       Map<String, Map<String, String>> transitionTable,
                                       String startState,
                                       Set<String> finalStates) {
        boolean valid = true;

        if (!states.contains(startState)) {
            System.out.println("Error: start state is not a member of Q.");
            valid = false;
        }

        for (String finalState : finalStates) {
            if (!states.contains(finalState)) {
                System.out.println("Error: final state " + finalState + " is not a member of Q.");
                valid = false;
            }
        }

        // In a complete DFA, every state must have exactly one transition for every input symbol.
        for (String state : states) {
            for (String symbol : alphabet) {
                if (!transitionTable.get(state).containsKey(symbol)) {
                    System.out.println("Error: missing transition for state " + state + " with symbol " + symbol + ".");
                    valid = false;
                }
            }
        }

        return valid;
    }

    private static void printDfaDefinition(Set<String> states,
                                           Set<String> alphabet,
                                           Map<String, Map<String, String>> transitionTable,
                                           String startState,
                                           Set<String> finalStates) {
        System.out.println("\n========================================");
        System.out.println("DFA DEFINITION");
        System.out.println("========================================");
        System.out.println("M = (Q, Sigma, delta, q0, F)");
        System.out.println("Q     = " + formatSet(states));
        System.out.println("Sigma = " + formatSet(alphabet));
        System.out.println("q0    = " + startState);
        System.out.println("F     = " + formatSet(finalStates));
        printDfaTransitionTable(alphabet, transitionTable);
    }

    private static void printDfaTransitionTable(Set<String> alphabet,
                                                Map<String, Map<String, String>> transitionTable) {
        System.out.println("\nDFA Transition Table:");
        System.out.printf("%-15s", "State");
        for (String symbol : alphabet) {
            System.out.printf("%-15s", symbol);
        }
        System.out.println();
        System.out.println("--------------------------------------------------");

        for (String state : transitionTable.keySet()) {
            System.out.printf("%-15s", state);
            for (String symbol : alphabet) {
                System.out.printf("%-15s", transitionTable.get(state).get(symbol));
            }
            System.out.println();
        }
    }

    private static String simulateDfa(Map<String, Map<String, String>> transitionTable,
                                      Set<String> alphabet,
                                      String startState,
                                      Set<String> finalStates,
                                      String testString) {
        String currentState = startState;
        String printedString = testString.isEmpty() ? "empty" : testString;

        System.out.println("\nInput string: " + printedString);
        System.out.println("Start state: " + currentState);

        // In this beginner-friendly project, each input symbol is one character.
        for (int i = 0; i < testString.length(); i++) {
            String symbol = String.valueOf(testString.charAt(i));

            if (!alphabet.contains(symbol)) {
                System.out.println("Symbol " + symbol + " is not in Sigma.");
                System.out.println("Result: Reject");
                return "Reject";
            }

            String nextState = transitionTable.get(currentState).get(symbol);
            System.out.println(currentState + " --" + symbol + "--> " + nextState);
            currentState = nextState;
        }

        System.out.println("Final stopped state: " + currentState);

        if (finalStates.contains(currentState)) {
            System.out.println("Result: Accept");
            return "Accept";
        } else {
            System.out.println("Result: Reject");
            return "Reject";
        }
    }

    // =====================================================
    // Phase 2: NFA to DFA Converter
    // =====================================================
    private static void runNfaToDfaConverter(Scanner input) {
        System.out.println("\n========================================");
        System.out.println("PHASE 2: NFA TO DFA CONVERTER");
        System.out.println("========================================");
        System.out.println("Use eps for epsilon transitions.");

        System.out.print("Enter number of NFA states: ");
        int stateCount = Integer.parseInt(input.nextLine().trim());

        System.out.println("Enter NFA states separated by space:");
        Set<String> states = readSet(input.nextLine());

        while (states.size() != stateCount) {
            System.out.println("Number of states is not correct. Enter states again:");
            states = readSet(input.nextLine());
        }

        System.out.println("Enter alphabet symbols separated by space, without eps (example: a b):");
        Set<String> alphabet = readSet(input.nextLine());

        Map<String, Map<String, Set<String>>> nfaTransitions = new LinkedHashMap<>();
        for (String state : states) {
            nfaTransitions.put(state, new LinkedHashMap<>());
        }

        System.out.print("Enter number of NFA transitions: ");
        int transitionCount = Integer.parseInt(input.nextLine().trim());

        System.out.println("Enter transitions in this format: from symbol toStates");
        System.out.println("Examples:");
        System.out.println("q0 a q0,q1");
        System.out.println("q0 eps q2");

        for (int i = 0; i < transitionCount; i++) {
            String line = input.nextLine().trim();
            String[] parts = line.split("\\s+");

            if (parts.length != 3) {
                System.out.println("Invalid format. Enter transition again.");
                i--;
                continue;
            }

            String from = parts[0];
            String symbol = normalizeSymbol(parts[1]);
            Set<String> toStates = readCommaSet(parts[2]);

            if (!states.contains(from)) {
                System.out.println("Invalid transition: source state is not in Q. Enter again.");
                i--;
                continue;
            }

            if (!symbol.equals(EPSILON) && !alphabet.contains(symbol)) {
                System.out.println("Invalid transition: symbol is not in Sigma. Enter again.");
                i--;
                continue;
            }

            boolean allToStatesAreValid = true;
            for (String to : toStates) {
                if (!states.contains(to)) {
                    allToStatesAreValid = false;
                    break;
                }
            }

            if (!allToStatesAreValid) {
                System.out.println("Invalid transition: at least one destination state is not in Q. Enter again.");
                i--;
                continue;
            }

            nfaTransitions.get(from)
                    .computeIfAbsent(symbol, key -> new TreeSet<>())
                    .addAll(toStates);
        }

        System.out.print("Enter NFA start state: ");
        String startState = input.nextLine().trim();

        System.out.println("Enter NFA final states separated by space:");
        Set<String> finalStates = readSet(input.nextLine());

        boolean validNfa = validateNfa(states, startState, finalStates);
        if (!validNfa) {
            System.out.println("\nThe NFA definition is not valid. Please run the program again with correct input.");
            return;
        }

        printNfaDefinition(states, alphabet, nfaTransitions, startState, finalStates);
        convertNfaToDfa(nfaTransitions, alphabet, startState, finalStates);
    }

    private static boolean validateNfa(Set<String> states,
                                       String startState,
                                       Set<String> finalStates) {
        boolean valid = true;

        if (!states.contains(startState)) {
            System.out.println("Error: NFA start state is not a member of Q.");
            valid = false;
        }

        for (String finalState : finalStates) {
            if (!states.contains(finalState)) {
                System.out.println("Error: NFA final state " + finalState + " is not a member of Q.");
                valid = false;
            }
        }

        return valid;
    }

    private static void printNfaDefinition(Set<String> states,
                                           Set<String> alphabet,
                                           Map<String, Map<String, Set<String>>> nfaTransitions,
                                           String startState,
                                           Set<String> finalStates) {
        System.out.println("\n========================================");
        System.out.println("NFA DEFINITION");
        System.out.println("========================================");
        System.out.println("N = (Q, Sigma, delta, q0, F)");
        System.out.println("Q     = " + formatSet(states));
        System.out.println("Sigma = " + formatSet(alphabet));
        System.out.println("q0    = " + startState);
        System.out.println("F     = " + formatSet(finalStates));
        printNfaTransitionTable(alphabet, nfaTransitions);
    }

    private static void printNfaTransitionTable(Set<String> alphabet,
                                                Map<String, Map<String, Set<String>>> nfaTransitions) {
        System.out.println("\nNFA Transition Table:");
        System.out.printf("%-15s", "State");
        for (String symbol : alphabet) {
            System.out.printf("%-15s", symbol);
        }
        System.out.printf("%-15s%n", EPSILON);
        System.out.println("----------------------------------------------------------------");

        for (String state : nfaTransitions.keySet()) {
            System.out.printf("%-15s", state);
            for (String symbol : alphabet) {
                Set<String> nextStates = nfaTransitions.get(state).getOrDefault(symbol, new TreeSet<>());
                System.out.printf("%-15s", formatSet(nextStates));
            }
            Set<String> epsilonStates = nfaTransitions.get(state).getOrDefault(EPSILON, new TreeSet<>());
            System.out.printf("%-15s%n", formatSet(epsilonStates));
        }
    }

    private static void convertNfaToDfa(Map<String, Map<String, Set<String>>> nfaTransitions,
                                        Set<String> alphabet,
                                        String nfaStartState,
                                        Set<String> nfaFinalStates) {
        System.out.println("\n========================================");
        System.out.println("EPSILON-CLOSURE INFORMATION");
        System.out.println("========================================");
        for (String state : nfaTransitions.keySet()) {
            System.out.println("epsilon-closure(" + state + ") = " + formatSet(epsilonClosure(setOf(state), nfaTransitions)));
        }

        Set<String> dfaStartState = epsilonClosure(setOf(nfaStartState), nfaTransitions);

        System.out.println("\n========================================");
        System.out.println("SUBSET CONSTRUCTION STEPS");
        System.out.println("========================================");
        System.out.println("Start DFA state = epsilon-closure(" + nfaStartState + ") = " + formatSet(dfaStartState));

        Queue<Set<String>> queue = new LinkedList<>();
        Set<Set<String>> dfaStates = new LinkedHashSet<>();
        Map<Set<String>, Map<String, Set<String>>> dfaTransitions = new LinkedHashMap<>();

        queue.add(dfaStartState);
        dfaStates.add(dfaStartState);

        while (!queue.isEmpty()) {
            Set<String> currentDfaState = queue.poll();
            dfaTransitions.putIfAbsent(currentDfaState, new LinkedHashMap<>());

            System.out.println("\nProcessing DFA state: " + formatSet(currentDfaState));

            for (String symbol : alphabet) {
                Set<String> moveResult = move(currentDfaState, symbol, nfaTransitions);
                Set<String> nextDfaState = epsilonClosure(moveResult, nfaTransitions);

                System.out.println("  With input " + symbol + ":");
                System.out.println("    move(" + formatSet(currentDfaState) + ", " + symbol + ") = " + formatSet(moveResult));
                System.out.println("    epsilon-closure(" + formatSet(moveResult) + ") = " + formatSet(nextDfaState));

                dfaTransitions.get(currentDfaState).put(symbol, nextDfaState);

                if (!dfaStates.contains(nextDfaState)) {
                    dfaStates.add(nextDfaState);
                    queue.add(nextDfaState);
                    System.out.println("    New DFA state discovered: " + formatSet(nextDfaState));
                } else {
                    System.out.println("    This DFA state already exists.");
                }
            }
        }

        Set<Set<String>> dfaFinalStates = new LinkedHashSet<>();
        for (Set<String> dfaState : dfaStates) {
            if (containsAnyFinalState(dfaState, nfaFinalStates)) {
                dfaFinalStates.add(dfaState);
            }
        }

        printDfaConversionResult(dfaStates, alphabet, dfaTransitions, dfaStartState, dfaFinalStates);
    }

    // epsilon-closure means all states that can be reached using only epsilon transitions.
    private static Set<String> epsilonClosure(Set<String> startStates,
                                              Map<String, Map<String, Set<String>>> nfaTransitions) {
        Set<String> closure = new TreeSet<>(startStates);
        Stack<String> stack = new Stack<>();

        for (String state : startStates) {
            stack.push(state);
        }

        while (!stack.isEmpty()) {
            String currentState = stack.pop();
            Set<String> epsilonNextStates = nfaTransitions
                    .getOrDefault(currentState, new LinkedHashMap<>())
                    .getOrDefault(EPSILON, new TreeSet<>());

            for (String nextState : epsilonNextStates) {
                if (!closure.contains(nextState)) {
                    closure.add(nextState);
                    stack.push(nextState);
                }
            }
        }

        return closure;
    }

    // move means all states reachable from a set of states using one input symbol.
    private static Set<String> move(Set<String> states,
                                    String symbol,
                                    Map<String, Map<String, Set<String>>> nfaTransitions) {
        Set<String> result = new TreeSet<>();

        for (String state : states) {
            Set<String> nextStates = nfaTransitions
                    .getOrDefault(state, new LinkedHashMap<>())
                    .getOrDefault(symbol, new TreeSet<>());
            result.addAll(nextStates);
        }

        return result;
    }

    private static boolean containsAnyFinalState(Set<String> dfaState, Set<String> nfaFinalStates) {
        for (String state : dfaState) {
            if (nfaFinalStates.contains(state)) {
                return true;
            }
        }
        return false;
    }

    private static void printDfaConversionResult(Set<Set<String>> dfaStates,
                                                 Set<String> alphabet,
                                                 Map<Set<String>, Map<String, Set<String>>> dfaTransitions,
                                                 Set<String> dfaStartState,
                                                 Set<Set<String>> dfaFinalStates) {
        System.out.println("\n========================================");
        System.out.println("NFA TO DFA CONVERSION RESULT");
        System.out.println("========================================");

        System.out.println("\nDFA States:");
        for (Set<String> state : dfaStates) {
            if (state.isEmpty()) {
                System.out.println(formatSet(state) + "  (Dead State)");
            } else {
                System.out.println(formatSet(state));
            }
        }

        System.out.println("\nDFA Start State:");
        System.out.println(formatSet(dfaStartState));

        System.out.println("\nDFA Final States:");
        if (dfaFinalStates.isEmpty()) {
            System.out.println("No final state was produced.");
        } else {
            for (Set<String> state : dfaFinalStates) {
                System.out.println(formatSet(state));
            }
        }

        System.out.println("\nDFA Transition Table:");
        System.out.printf("%-20s", "State");
        for (String symbol : alphabet) {
            System.out.printf("%-20s", symbol);
        }
        System.out.println();
        System.out.println("--------------------------------------------------------------");

        for (Set<String> state : dfaStates) {
            System.out.printf("%-20s", formatSet(state));
            for (String symbol : alphabet) {
                Set<String> nextState = dfaTransitions.get(state).get(symbol);
                System.out.printf("%-20s", formatSet(nextState));
            }
            System.out.println();
        }
    }

    // =====================================================
    // Helper methods
    // =====================================================
    private static Set<String> readSet(String line) {
        Set<String> result = new LinkedHashSet<>();
        if (line.trim().isEmpty()) {
            return result;
        }

        String[] parts = line.trim().split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(part.trim());
            }
        }

        return result;
    }

    private static Set<String> readCommaSet(String text) {
        Set<String> result = new TreeSet<>();
        String[] parts = text.trim().split(",");

        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                result.add(part.trim());
            }
        }

        return result;
    }

    private static Set<String> setOf(String state) {
        Set<String> result = new TreeSet<>();
        result.add(state);
        return result;
    }

    private static String normalizeSymbol(String symbol) {
        if (symbol.equals("epsilon") || symbol.equals("e") || symbol.equals("ε")) {
            return EPSILON;
        }
        return symbol;
    }

    private static String formatSet(Set<String> states) {
        if (states.isEmpty()) {
            return "{}";
        }
        return "{" + String.join(",", states) + "}";
    }
}
