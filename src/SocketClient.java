import java.io.*;
import java.util.Scanner;
import java.net.*;
// params:giant.utdallas.edu 1026
public class SocketClient {
    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;

    public void communicate() {
        boolean exit = false;
        Scanner sc = new Scanner(System.in);
        String name = "";
        String isValidUName = "";
        while (isValidUName.isEmpty()) {
            System.out.println("Enter your name: ");
            name = sc.nextLine();

            //Send data over socket
            out.println(name);
            //Receive text from server
            try {
                isValidUName = in.readLine();
                if (!isValidUName.isEmpty() && !isValidUName.equals("EXIT"))
                    System.out.println("Text received: " + isValidUName);
                else if (isValidUName.isEmpty()) {
                    System.out.println("Name Already Exists, Please try with a new Name " + isValidUName);
                    isValidUName = "";
                }
            } catch (IOException e) {
                System.out.println("Read failed");
                System.exit(1);
            }
        }
        while (!exit) {
            System.out.println("1.\tDisplay the names of all known users.\n" +
                "2.\tDisplay the names of all currently connected users.\n" +
                "3.\tSend a text message to a particular user.\n" +
                "4.\tSend a text message to all currently connected users.\n" +
                "5.\tSend a text message to all known users.\n" +
                "6.\tGet my messages.\n" +
                "7.\tExit.\n");
            switch (sc.nextLine()) {
                case "1":
                    out.println("1");
                    try {
                        String line = in.readLine();
                        System.out.println("Known users:\n " + line.replace("[", "").replace("]", "").replace(",", "\n"));
                    } catch (IOException e) {
                        System.out.println("Read failed");
                        System.exit(1);
                    }
                    break;
                case "2":
                    out.println("2");
                    try {
                        String line = in.readLine();
                        System.out.println("Currently Connected Users:\n " + line.replace("[", "").replace("]", "").replace(",", "\n"));
                    } catch (IOException e) {
                        System.out.println("Read failed");
                        System.exit(1);
                    }
                    break;
                case "3":
                    //Send a text message to a particular user.
                    System.out.println("Enter recipient's name");
                    String receiver = sc.nextLine();
                    System.out.println("Enter a message");
                    String message = sc.nextLine();
                    out.println("3-" + receiver + "-" + message);
                    try {
                        String line = in.readLine();
                        System.out.println(line + "\n");
                    } catch (IOException e) {
                        System.out.println("Read failed");
                        System.exit(1);
                    }
                    break;
                case "4":
                    //Send a text message to all currently connected users.
                    System.out.println("Enter a message");
                    String messageConUsers = sc.nextLine();
                    out.println("4-" + messageConUsers);
                    try {
                        String line = in.readLine();
                        System.out.println(line + "\n");
                    } catch (IOException e) {
                        System.out.println("Read failed");
                        System.exit(1);
                    }
                    break;
                case "5":
                    //Send a text message to all known users.
                    System.out.println("Enter a message");
                    String messageAll = sc.nextLine();
                    out.println("5-" + messageAll);
                    try {
                        String line = in.readLine();
                        System.out.println(line + "\n");
                    } catch (IOException e) {
                        System.out.println("Read failed");
                        System.exit(1);
                    }
                    break;
                case "6":
                    //get messages
                    out.println("6");
                    try {
                        String line = in.readLine();
                        System.out.println("Your messages:");
                        System.out.println(line.isEmpty() ? "NO NEW MESSAGES\n" : line.replace(";", "\n"));
                        System.out.println();
                    } catch (IOException e) {
                        System.out.println("Read failed");
                        System.exit(1);
                    }
                    break;
                case "7":
                    //exit
                    out.println("7");
                    try {
                        String line = in.readLine();
                        if (line.equals("EXIT"))
                            System.exit(1);
                    } catch (IOException e) {
                        System.out.println("Read failed");
                        System.exit(1);
                    }
                    break;
                default:
                    //invalid case
                    System.out.println("Enter a valid choice");
            }
        }

    }

    public void listenSocket(String host, int port) {
        //Create socket connection
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("No I/O");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage:  client hostname port");
            System.exit(1);
        }
        SocketClient client = new SocketClient();
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        System.out.println("Connecting to " + args[0] + ": " + args[1]);
        client.listenSocket(host, port);
        client.communicate();
    }
}