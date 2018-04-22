

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

class ClientWorker implements Runnable {
    private Socket client;
    UserMap userMap;

    ClientWorker(Socket client, UserMap userMap) {
        this.client = client;
        //map to maintain user details
        this.userMap = userMap;
    }

    public void run() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
        String line;
        String userName = "";
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("in or out failed");
            System.exit(-1);
        }
        boolean exit = false;
        while (!exit) {
            try {
                // Receive text from client
                line = in.readLine();
                //return list of known users
                if (line.equals("1")) {
                    System.out.println(dateFormat.format(new Date()) + ", " + userName + " displays all known users");
                    line = userMap.knownUsers.keySet().toString();
                }
                //return the list of connected users
                else if (line.startsWith("2")) {
                    System.out.println(dateFormat.format(new Date()) + ", " + userName + " displays all currently connected users");
                    line = userMap.connectedUsers.keySet().toString();
                }
                //send message to a particular user 3-receivername-msg
                else if (line.startsWith("3-")) {

                    //index 1 is receiver name and 2 is message
                    String[] data = line.split("-");
                    System.out.println(dateFormat.format(new Date()) + ", " + userName + " posts a message for " + data[1]);

                    //make an uknown user a known user and set to false as its not connected

                    if (!userMap.knownUsers.containsKey(data[1])) {
                        userMap.knownUsers.put(data[1], false);
                        synchronized (userMap.userReceivedMessages) {
                            userMap.userReceivedMessages.put(data[1], new LinkedList<String>());
                        }
                    }

                    if (userMap.knownUsers.containsKey(data[1]) && userMap.userReceivedMessages.containsKey(data[1])) {
                        List<String> msgSent;

                        //add message to received messages map
                        synchronized (userMap.userReceivedMessages) {
                            msgSent = userMap.userReceivedMessages.get(data[1]);
                            msgSent.add("From " + userName + ", " + dateFormat.format(new Date()) + ", " + data[2]);
                            userMap.userReceivedMessages.put(data[1], msgSent);
                        }
                        line = "Message Posted to " + data[1];
                    } else {
                        line = "Couldn't send message to a invalid user";
                    }
                }
                //send message to all the currently connected user
                else if (line.startsWith("4-")) {
                    System.out.println(dateFormat.format(new Date()) + ", " + userName + " posts a message for all connected users");
                    String[] connUsers = line.split("-");
                    synchronized (userMap.userReceivedMessages) {
                        for (Map.Entry<String, Boolean> mp : userMap.connectedUsers.entrySet()) {
                            if (!mp.getKey().equals(userName)) {
                                List<String> messagesConn = userMap.userReceivedMessages.get(mp.getKey());
                                messagesConn.add("From " + userName + ", " + dateFormat.format(new Date()) + ", " + connUsers[1]);
                                userMap.userReceivedMessages.put(mp.getKey(), messagesConn);
                            }
                        }
                        line = "Message Posted to All Connected Users";
                    }
                }
                //send message to all known users
                else if (line.startsWith("5-")) {
                    String[] addMessage = line.split("-");
                    System.out.println(dateFormat.format(new Date()) + ", " + userName + " posts a message for all known users");
                    synchronized (userMap.userReceivedMessages) {
                        for (Map.Entry<String, Boolean> mp : userMap.knownUsers.entrySet()) {
                            if (!mp.getKey().equals(userName)) {
                                List<String> messages = userMap.userReceivedMessages.get(mp.getKey());
                                String msg = "From " + userName + ", " + dateFormat.format(new Date()) + ", " + addMessage[1];
                                messages.add(msg);
                                userMap.userReceivedMessages.put(mp.getKey(), messages);
                            }
                        }
                    }
                    line = "Message Posted to All Known Users";
                }
                //get users messages
                else if (line.equals("6")) {
                    System.out.println(dateFormat.format(new Date()) + ", " + userName + " gets messages");
                    //line = StringUtils.join(userMap.userReceivedMessages.get(userName), ";");
                    line = "";
                    synchronized (userMap.userReceivedMessages) {
                        for (String msg : userMap.userReceivedMessages.get(userName)) {
                            line += msg + ";";
                        }
                        //resetting the inbox
                        userMap.userReceivedMessages.put(userName, new LinkedList<String>());
                    }
                }
                //user exits
                else if (line.equals("7")) {
                    System.out.println(dateFormat.format(new Date()) + ", " + userName + " exits");
                    //mark the user disconnected
                    if (userMap.knownUsers.containsKey(userName))
                        userMap.knownUsers.put(userName, false);
                    //remove from the user list
                    userMap.connectedUsers.remove(userName);
                    exit = true;
                    line = "EXIT";
                }
                // Send response back to client when client logs initially logs in
                else if (userName.length() == 0) {
                    userName = line;
                    if (userMap.knownUsers.containsKey(userName) && !userMap.knownUsers.get(userName)) {
                        System.out.println(dateFormat.format(new Date()) + ", Connection by known user " + userName);
                        userMap.knownUsers.put(userName, true);
                        userMap.connectedUsers.put(userName, true);
                        line = "Hi " + userName;
                    } else if (!userMap.knownUsers.containsKey(userName)) {
                        System.out.println(dateFormat.format(new Date()) + ", Connection by unknown user " + userName);
                        userMap.knownUsers.put(userName, true);
                        userMap.connectedUsers.put(userName, true);
                        synchronized (userMap.userReceivedMessages) {
                            userMap.userReceivedMessages.put(userName, new LinkedList<String>());
                        }
                        line = "Hi " + userName;
                    } else if (userMap.knownUsers.containsKey(userName) && userMap.knownUsers.get(userName)) {
                        System.out.println(dateFormat.format(new Date()) + ", Duplicate connection requested for " + userName);
                        line = "";
                        userName = "";
                    }

                }
                //handle invalid input
                else {
                    System.out.println(dateFormat.format(new Date()) + ", Invalid input entered by " + userName);
                    line = "Invalid Input";
                }
                out.println(line);
            } catch (IOException e) {
                System.out.println("Read failed");
                System.exit(-1);
            }
        }
        try {
            client.close();
        } catch (
            IOException e)

        {
            System.out.println("Close failed");
            System.exit(-1);
        }
    }
}

class SocketThrdServer {
    ServerSocket server = null;

    public void listenSocket(int port, UserMap userMap) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server running on port " + port +
                "," + " use ctrl-C to end");
        } catch (IOException e) {
            System.out.println("Error creating socket");
            System.exit(-1);
        }
        while (true) {
            ClientWorker w;
            try {
                w = new ClientWorker(server.accept(), userMap);
                Thread t = new Thread(w);
                t.start();
            } catch (IOException e) {
                System.out.println("Accept failed");
                System.exit(-1);
            }
        }
    }

    protected void finalize() {
        try {
            server.close();
        } catch (IOException e) {
            System.out.println("Could not close socket");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SocketThrdServer port");
            System.exit(1);
        }
        final UserMap userMap = new UserMap();
        SocketThrdServer server = new SocketThrdServer();
        int port = Integer.valueOf(args[0]);
        server.listenSocket(port, userMap);
    }
}

class UserMap {
    //key is name and value if is connected
    Map<String, Boolean> knownUsers = new HashMap<String, Boolean>();
    //key is ip address and if is connected
    Map<String, Boolean> connectedUsers = new HashMap<String, Boolean>();
    //key is username and value is list of messages
    Map<String, List<String>> userReceivedMessages = new HashMap<String, List<String>>();
}
