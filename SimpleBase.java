/**
 * Created by Luke Levis.
 */
import java.io.*;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

public class SimpleBase {

    public TreeMap<String, Integer> data = new TreeMap<String, Integer>();
    public Stack<Stack> history = new Stack<Stack>();

    /**
     * set() sets the name to the specified value.
     * Running time: O(log n).
     * @param name is the name of the variable
     * @param value is the integer value to be stored.
     */
    public void set(String name, int value) {
        // check if first instance of transaction
        int size = history.size();
        if (size == 0) {
            begin();
        }
        try {
            Integer toInsert = data.get(name);
            // update history with current info
            Object[] command = {"SET", name, toInsert};
            history.peek().push(command);
            exec("SET", name, value);
        } catch (ClassCastException e1) {
            System.out.println("Object is not of comparable type.");
        } catch (NullPointerException e2) {
            System.out.println("Value is not declared.");
        }
        if (size == 0) {
            commit();
        }
    }

    /**
     * gets() prints the value associated with name if it exists.
     * prints NULL otherwise.
     * Running time: O(log n).
     * @param name is the name of the variable to look for
     */
    public void gets(String name) {
        try {
            Integer value = data.get(name);
            System.out.println(value);
        } catch (NullPointerException e3) {
            System.out.println("NULL");
        }
    }

    /**
     * unset() removes the binding between name and its variable.
     * If name was not bound, nothing is done.
     * Running time: O(log n).
     * @param name is the name of the variable to look for
     */
    public void unset(String name) {
        // check if first instance of transaction
        int size = history.size();
        if (size == 0) {
            begin();
        }
        if (data.get(name) != null) {
            int val = data.get(name);
            // update history
            System.out.println("VALUE= " + val);
            Object[] command = {"UNSET", name, val};
            history.peek().push(command);
            // set value to null;
            exec("SET", name, null);
        } else {
            return;
        }
        if (size == 0) {
            commit();
        }
    }

    /**
     * numEqualTo() returns the number of items in the database with
     * the specified value. Returns 0 if none exist.
     * @param value is the value which we look for
     */
    public void numEqualTo(int value) {
        // assuming number will not exceed Integer.MAX_VALUE
        int total = 0;

        // iterate over the entrySet
        for (Map.Entry<String,Integer> entry : data.entrySet()) {
            if (entry.getValue() == value) {
                total++;
            }
        }
        System.out.println(total);
    }

    /**
     * end() exists the program immediately.
     */
    public static void end() {
        System.exit(0);
    }

    /**
     * exec() simply performs the remove or insert command without updating history.
     * Used when performing rollbacks to ensure no corruption of history
     * Running time: O(log n)
     * @param func is the function to perform
     * @param name is the name to look for
     * @param value is the value to exec
     */
    public void exec(String func, String name, Integer value) {
        if (func.equals("SET") && value != null) {
            data.put(name, value);
        } else {
            data.remove(name);
        }
    }

    /**
     * begin() opens a new transaction history and continues to operate
     * on the same database.
     * Running time: O(1).
     */
    public void begin() {
        // Open new Stack and push to the stack.
        Stack<Object[]> commandStack = new Stack<Object[]>();
        history.push(commandStack);
    }

    /**
     * rollBack() reverses all of the current operations in the current transaction
     * and closes that transaction.
     * Running time: O(log n)
     */
    public void rollBack() {
        if (history.size() == 0) {
            System.out.println("NO TRANSACTION");
            return;
        }

        // undo all operations from most recent transaction
        Stack<Object[]> recent = history.pop();
        Object[] commands;
        //String func;
        String name;
        Integer value;
        while (recent.size() != 0) {
            commands = recent.pop();
            //func = (String) commands[0];
            name = (String) commands[1];
            if (commands[2] != null) {
                value = (Integer) commands[2];
            } else {
                value = null;
            }
            exec("SET", name, value);
        }
    }

    /**
     * commit() closes all open transaction blocks and permanently applies changes made by them.
     * print NO TRANSACTION if no block currently open
     * Running time: O(1)
     */
    public void commit() {
        if (history.size() == 0) {
            System.out.println("NO TRANSACTION");
        } else {
            // garbage collection will take care of leftover data
            history = new Stack<Stack>();
        }
    }

    public static void main(String[] args) throws IOException {
        // create initial SimpleBase
        SimpleBase data = new SimpleBase();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;

        String[] commands;
        String func, name;
        int value;

        while (!((s = in.readLine()).equals("END")) && s.length() != 0) {
            commands = s.split(" ");
            func = commands[0];

            if (func.equals("SET")) {
                name = commands[1];
                value = Integer.parseInt(commands[2]);
                data.set(name, value);
            } else if (func.equals("UNSET")) {
                name = commands[1];
                data.unset(name);
            } else if (func.equals("GET")) {
                name = commands[1];
                data.gets(name);
            } else if (func.equals("NUMEQUALTO")) {
                value = Integer.parseInt(commands[1]);
                data.numEqualTo(value);
            } else if (func.equals("BEGIN")) {
                data.begin();
            } else if (func.equals("ROLLBACK")) {
                data.rollBack();
            } else if (func.equals("COMMIT")) {
                data.commit();
            } else {
                System.out.println("Invalid Commands");
            }
        }

        // terminate or throw error
        commands = s.split(" ");
        if (commands[0].equals("END")) {
            end();
        } else {
            System.out.println("Illegal Arguments");
        }
    }
}
