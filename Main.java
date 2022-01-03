package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Kaito Garcia
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (errorCheckBoolean(args)) {
            errorCheckPrint(args);
            return;
        }
        if (!new File(System.getProperty("user.dir") + "/.gitlet").exists()) {
            repoHelper();
        }
        repo = Utils.readObject(new File(".gitlet/repo"), Repo.class);
        switch (args[0]) {
        case "init":
            repo.init();
            break;
        case "add":
            repo.add(args[1]);
            break;
        case "commit":
            repo.commit(args[1]);
            break;
        case "rm":
            repo.rm(args[1]);
            break;
        case "log":
            repo.log();
            break;
        case "global-log":
            repo.globalLog();
            break;
        case "find":
            repo.find(args[1]);
            break;
        case "status":
            repo.status();
            break;
        case "checkout":
            repo.checkout(args);
            break;
        case "branch":
            repo.branch(args[1]);
            break;
        case "rm-branch":
            repo.rmBranch(args[1]);
            break;
        case "reset":
            repo.reset(args[1]);
            break;
        case "merge":
            repo.merge(args[1]);
            break;
        default:
            methodCallsEC(args);
        }
        Utils.writeObject(new File(".gitlet/repo"), repo);
    }

    /**
     * method calls helper.
     * @param args -- args
     */
    public static void methodCallsEC(String... args) {
        switch (args[0]) {
        case "add-remote":
            repo.addRemote(args);
            break;
        case "rm-remote":
            repo.rmRemote(args);
            break;
        case "push":
            repo.push(args);
            break;
        case "fetch":
            repo.fetch(args);
            break;
        case "pull":
            repo.pull(args);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }
    /**
     * Repo helper.
     */
    public static void repoHelper() {
        repo = new Repo();
        Utils.writeObject(new File(".gitlet/repo"), repo);
    }

    /**
     * Returns true if bad.
     * @param args -- main args
     * @return true if bad
     */
    public static boolean errorCheckBoolean(String... args) {
        if (args == null || args.length == 0) {
            return true;
        } else if (!argChecker(args)) {
            return true;
        } else if (!exists && !args[0].equals("init")) {
            return true;
        }
        return false;
    }

    /**
     * System print.
     * @param args -- main args
     */
    public static void errorCheckPrint(String... args) {
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (!argChecker(args)) {
            System.out.println("Incorrect operands.");
        } else if (!exists && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * Helper function to determine if args are valid.
     * @param args input
     * @return true if valid
     */
    public static boolean argChecker(String... args) {
        String cmd = args[0];
        int len = args.length;
        if (cmd.equals("init") && len != 1) {
            return false;
        } else if (cmd.equals("add") && len != 2) {
            return false;
        } else if (cmd.equals("commit") && len != 2) {
            return false;
        } else if (cmd.equals("rm") && len != 2) {
            return false;
        } else if (cmd.equals("log") && len != 1) {
            return false;
        } else if (cmd.equals("global-log") && len != 1) {
            return false;
        } else if (cmd.equals("find") && len != 2) {
            return false;
        } else if (cmd.equals("status") && len != 1) {
            return false;
        } else if (cmd.equals("checkout") && checkoutChecker(args)) {
            return false;
        } else if (cmd.equals("branch") && len != 2) {
            return false;
        } else if (cmd.equals("rm branch") && len != 2) {
            return false;
        } else if (cmd.equals("reset") && len != 2) {
            return false;
        } else if (cmd.equals("add-remote") && len != 3) {
            return false;
        } else if (cmd.equals("rm-remote") && len != 2) {
            return false;
        } else if (cmd.equals("push") && len != 3) {
            return false;
        } else if (cmd.equals("fetch") && len != 3) {
            return false;
        } else if (cmd.equals("pull") && len != 3) {
            return false;
        }
        return !cmd.equals("merge") || len == 2;
    }

    /** Checks checkout arguments to ensure operands are correct.
     @param args array input of type string
     @return true if checkout args are invalid
     */
    public static boolean checkoutChecker(String... args) {
        int len = args.length;
        if (len == 2 && args[1].equals("--")) {
            return true;
        }
        if (len == 3 && !args[1].equals("--")) {
            return true;
        }
        if (len == 4 && !args[2].equals("--")) {
            return true;
        }
        return false;
    }


    /** Repository. */
    private static Repo repo;

    /** String user. */
    private static boolean exists = new File(System.getProperty("user.dir")
            + "/.gitlet").exists();
}

