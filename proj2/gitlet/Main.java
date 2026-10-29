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

        try {
            if (args.length == 0) {
                throw Utils.error("Please enter a command.");
            }

            String firstArg = args[0];

            if (!firstArg.equals("init") && !Repository.GITLET_DIR.exists()) {
                throw Utils.error("Not in an initialized Gitlet directory.");
            }
            switch(firstArg) {
                case "init":
                    if (args.length != 1) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.init();
                    break;
                case "add":
                    if (args.length != 2) {
                        throw Utils.error("Incorrect operands.");
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
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.rm(args[1]);
                    break;
                case "log":
                    if (args.length != 1) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.log();
                    break;
                case "global-log":
                    if (args.length != 1) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.globalLog();
                    break;
                case "find":
                    if (args.length != 2) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.find(args[1]);
                    break;
                case "checkout":
                    if (args.length != 2 && args.length != 3 && args.length != 4) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.checkout(args);
                    break;
                case "status":
                    if (args.length != 1) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.status();
                    break;
                case "branch":
                    if (args.length != 2) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.branch(args[1]);
                    break;
                case "rm-branch":
                    if (args.length != 2) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.rmBranch(args[1]);
                    break;
                case "reset":
                    if (args.length != 2) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.reset(args[1]);
                    break;
                case "merge":
                    if (args.length != 2) {
                        throw Utils.error("Incorrect operands.");
                    }
                    Repository.merge(args[1]);
                    break;
                default:
                    throw Utils.error("No command with that name exists.");
            }

        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }


    }
}
