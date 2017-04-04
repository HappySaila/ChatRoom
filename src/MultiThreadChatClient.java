//Example 25

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MultiThreadChatClient implements Runnable {

  // The client socket
  private static Socket clientSocket = null;
  // The output stream
  private static PrintStream os = null;
  // The input stream
  private static DataInputStream is = null;

  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  public static void main(String[] args) {

    // The default port.
    int portNumber = 2222;
    // The default host.
    String host = "137.158.58.20";

    if (args.length < 2) {
      System.out
          .println("Usage: java MultiThreadChatClient <host> <portNumber>\n"
              + "Now using host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
    try {
      clientSocket = new Socket(host, portNumber); //(TA) open a socket to the server "host" at port "portNumber"
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());// (TA) use the clientSocket to make a new output stream so we can sent stuff to server
      is = new DataInputStream(clientSocket.getInputStream());// (TA) use the clientSocket to make a new input stream so we can get data from the server
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    /*
     * If everything has been initialized then we want to write some data to the
     * socket we have opened a connection to on the port portNumber.
     */
    if (clientSocket != null && os != null && is != null) {
      try {

        /* Create a thread to read from the server. */
        //(TA) here you should make a new instance of MultiThreadChatClient to run in its own Thread. Note that this will be of type Thread so after calling .start() the "run" method gets called.
        //(TA) also, this is a bit hacky/subtle but this is being done in the static main method of the MultiThreadChatClient class which runs in its own "thread", separate
        //(TA) from the "Thread" instance of the class you must instantiate here. The idea is that writing server (output socket) is done in the main thread (while loop below) and 
        //(TA) reading from the server (input stream) is done in the Thread we instantiate here, which runs its code from the "run" method below. Got it? Great :) 
        
        new Thread(new MultiThreadChatClient()).start();

        while (!closed) {
          //(TA) as long as the clientSocket is not closed, we want to read from the client and send that information to the server (output stream)
          //(TA) put here the code that does this.

          os.println(inputLine.readLine().trim());
        }
        /*
         * Close the output stream, close the input stream, close the socket.
         */
        os.close();
        is.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

  /*
   * Create a thread to read from the server. (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {
    /*
     * Keep on reading from the socket till we receive "Bye" from the
     * server. Once we received that then we want to break.
     */
    String responseLine;
    try {
      while ((responseLine = is.readLine()) != null) {
        System.out.println(responseLine);
        if (responseLine.indexOf("*** Bye") != -1)
          break;
      }
      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }
}
