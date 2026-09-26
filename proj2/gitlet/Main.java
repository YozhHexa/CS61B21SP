package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args == null) {
            return;
        }

        try {
            String firstArg = args[0];
            switch(firstArg) {
                case "init":
                    if (args.length != 1) {
                        return;
                    }
                    Repository.init();
                    break;
                case "add":
                    if (args.length != 2) {
                        return;
                    }
                    Repository.add(args[1]);
                    break;
                case "commit":
                    if (args.length != 2 || args[1].isEmpty()) {
                        System.out.println("Please enter a commit message.");
                        return;
                    }
                    Repository.commit(args[1]);
                    break;
                case "rm":
                    if (args.length != 2) {
                        return;
                    }
                    Repository.rm(args[1]);
                    break;
                case "log":
                    if (args.length != 1) {
                        return;
                    }
                    Repository.log();
                    break;
                case "global-log":
                    if (args.length != 1) {
                        return;
                    }
                    Repository.globalLog();
                    break;
                case "find":
                    if (args.length != 2) {
                        return;
                    }
                    Repository.find(args[1]);
                    break;
                case "checkout":
                    if (args.length != 2 && args.length != 3 && args.length != 4) {
                        return;
                    }
                    Repository.checkout(args);
                    break;
                case "status":
                    if (args.length != 1) {
                        return;
                    }
                    Repository.status();
                    break;
                    // TODO: FILL THE REST IN

            }

        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }


    }
}
