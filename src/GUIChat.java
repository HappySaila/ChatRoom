import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.filechooser.*;
import java.util.Date;
import java.net.URL;
import java.io.IOException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.net.URL;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import java.io.File;



import java.io.*;
import java.net.*;


/**
 * Opens a window that can be used for a two-way network chat.
 * The window can "listen" for a connection request on a port
 * that is specified by the user.  It can request a connection
 * to another GUIChat window on a specified computer and port.
 * The window has an input box where the user can enter
 * messages to be sent over the connection.  A connection
 * can be closed by clicking a button in the window or by
 * closing the window.   It is possible to open additional 
 * windows to support simultaneous chats (or to test the program 
 * by opening a connection from one window to another).
 * This class contains a main() routine, so it can be run as
 * a stand-alone application.
 */
 
public class GUIChat extends JFrame {
private static final long serialVersionUID = 1L;


/**Possible states of the thread that handles the network connection.  */
   private enum ConnectionState { LISTENING, CONNECTING, CONNECTED, CLOSED }
   
   /**
    * Default port number.  This is the initial content of input boxes in
    * the window that specify the port number for the connection. 
    */
   private static String defaultPort = "1501";
   
   /**
    * Default host name.  This is the initial content of the input box that
    * specifies the name of the computer to which a connection request
    * will be sent.
    */
   private static String defaultHost = "localhost";
         
   /**
    * Used to keep track of where on the screen the previous window
    * was opened, so that the next window can be placed at a 
    * different position.
    */
   private static Point previousWindowLocation;
   
   /**
    * The number of windows that are currently open.  If this drops to
    * zero, then the program is terminated by calling System.exit();
    */
   private static int openWindowCount;
   
   /**
    * The number of windows that have been created.  This is used
    * in the title bar of the second and subsequent windows.
    */
    
    private static int OnlineCount = 1;
   
   /**
    * The number of online users.  This is used
    * in the online label.
    */

   private static int windowsCreated;
   
   /**
    * The thread that handles the connection; defined by a nested class.
    */
   private ConnectionHandler connection;
   
   
   /**
    * The main() routine makes it possible to run this class as an
    * application; it just creates a GUIChat window and makes it visible.
    */
   public static void main(String[] args) {
      GUIChat window = new GUIChat();
      window.setVisible(true);
   }
   
      
   /**
    * Control buttons that appear in the window.
    */
   private JButton newButton, listenButton, connectButton, closeButton, attachFileButton, quitButton, attachButton, sendButton;
   
   /**
    * Input boxes for connection information (port numbers and host names).
    */
   private JTextField listeningPortInput, remotePortInput, remoteHostInput;

   /**
    * Input box for messages that will be sent to the other side of the
    * network connection.
    */
   private JTextField messageInput;
   
   
   private JEditorPane transcript;
   
   private HTMLDocument doc;
   
   //States the the font and colour of different
   private SimpleAttributeSet attributes;
   private SimpleAttributeSet attributes1;
   private SimpleAttributeSet attributes2;
   private SimpleAttributeSet attributes3;
   
   
   private HyperlinkListener hyperlinkListener;
   private FileReader reader;
   private JLabel label;
   
   
   /**
    * Contains a transcript of messages sent and received, along with
    * information about the progress and state of the connection.
    */
   //private JEditorPane transcript;
   //transcript.setContentType("text/html;charset=UTF-8");
   
   
   /**
    * Constructor creates a window with a default title.  The
    * constructor does not make the window visible.
    */  
   public GUIChat() 
   {
      this( windowsCreated == 0 ? "Chat Window [Online("+OnlineCount+")]" :
                           "Chat Window #" + (windowsCreated+1)+ "  [Online("+OnlineCount+")]" );
   }
   
   
   
   /**
    * Constructor creates a window with a specified title.  The
    * constructor does not make the window visible.
    */ 
   public GUIChat(String title){
   super(title);  
   
   // Instantiate a Date object
      Date date = new Date();
      
      ActionListener actionHandler = new ActionHandler();
      newButton = new JButton("New");
      newButton.addActionListener(actionHandler);
      listenButton = new JButton("Listen on port:");
      listenButton.addActionListener(actionHandler);
      connectButton = new JButton("Connect to:");
      connectButton.addActionListener(actionHandler);
      closeButton = new JButton("Disconnect");
      closeButton.addActionListener(actionHandler);
      closeButton.setEnabled(false);
      attachFileButton = new JButton("Attach File");
      attachFileButton.addActionListener(actionHandler);
      sendButton = new JButton("Send");
      sendButton.addActionListener(actionHandler);
      sendButton.setEnabled(false);
      attachButton = new JButton("Attach Image");
      attachButton.addActionListener(actionHandler);
      quitButton = new JButton("Quit");
      quitButton.addActionListener(actionHandler);
      messageInput = new JTextField();
      messageInput.addActionListener(actionHandler);
      messageInput.setEditable(false);
     
     //area where text is outputted 
       transcript = new JEditorPane();
       transcript.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
       JScrollPane jsp = new JScrollPane(transcript);
       
    //Enable Pnae to be scrollable
     /*JScrollPane editorScrollPane = new JScrollPane(transcript);
     editorScrollPane.setVerticalScrollBarPolicy(
     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);*/
     transcript.setPreferredSize(new Dimension(200, 200));
     
     doc = (HTMLDocument)transcript.getDocument();
     //reader = new FileReader("form.html");
     //transcript.read(reader, null);
     //Listen for clicks
     transcript.addHyperlinkListener(new HyperlinkListener() {
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
           // Do something with e.getURL() here
        }
    }
});
     
    
    //Styling options to differentiate between chats and system messages    
    attributes = new SimpleAttributeSet();
    StyleConstants.setBold(attributes, true);
    StyleConstants.setItalic(attributes, false); 
    StyleConstants.setFontSize(attributes, 15); 
    StyleConstants.setFontFamily(attributes, "Monospaced");
    StyleConstants.setForeground(attributes, Color.orange); 
    
    attributes1 = new SimpleAttributeSet();
    StyleConstants.setBold(attributes1, true);
    StyleConstants.setItalic(attributes1, false); 
    StyleConstants.setFontSize(attributes1, 12); 
    StyleConstants.setFontFamily(attributes1, "SansSerif");
    StyleConstants.setForeground(attributes1, Color.blue);
    
    attributes3 = new SimpleAttributeSet();
    StyleConstants.setBold(attributes3, true);
    StyleConstants.setItalic(attributes3, false); 
    StyleConstants.setFontSize(attributes3, 12); 
    StyleConstants.setFontFamily(attributes3, "SansSerif");
    StyleConstants.setForeground(attributes3, Color.gray);
    
    attributes2 = new SimpleAttributeSet();
    StyleConstants.setBold(attributes2, true);
    StyleConstants.setItalic(attributes2, false); 
    StyleConstants.setFontSize(attributes2, 15); 
    StyleConstants.setFontFamily(attributes2, "Monospaced");
    StyleConstants.setForeground(attributes2, Color.red);
        

      //transcript.setLineWrap(true);
     // transcript.setWrapStyleWord(true);
     
     
      transcript.setEditable(false);
      listeningPortInput = new JTextField(defaultPort,5);
      remotePortInput = new JTextField(defaultPort,5);
      remoteHostInput = new JTextField(defaultHost,18);
      
      JPanel content = new JPanel();
      content.setLayout(new BorderLayout(3,3));
      content.setBackground(Color.GRAY);
      JPanel topPanel = new JPanel();
      topPanel.setLayout(new GridLayout(2,1,3,3));
      topPanel.setBackground(Color.GRAY);
      JPanel buttonBar = new JPanel();
      buttonBar.setLayout(new FlowLayout(FlowLayout.CENTER,3,3));
      JPanel connectBar = new JPanel();
      connectBar.setLayout(new FlowLayout(FlowLayout.CENTER,3,3));
      JPanel inputBar = new JPanel();
      inputBar.setLayout(new BorderLayout(3,3));
      inputBar.setBackground(Color.GRAY);
      
      content.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
      content.add(topPanel, BorderLayout.NORTH);
      topPanel.add(connectBar);
      topPanel.add(buttonBar);
      content.add(inputBar, BorderLayout.SOUTH);
      content.add(new JScrollPane(transcript));
      
      //Add time and date
      buttonBar.add(new JLabel(date.toString()));
      
      buttonBar.add(newButton);
      buttonBar.add(quitButton);
      buttonBar.add(attachButton);
      buttonBar.add(attachFileButton);
      buttonBar.add(closeButton);
      connectBar.add(listenButton);
      connectBar.add(listeningPortInput);
      connectBar.add(Box.createHorizontalStrut(12));
      connectBar.add(connectButton);
      connectBar.add(remoteHostInput);
      connectBar.add(new JLabel("port:"));
      connectBar.add(remotePortInput);
      inputBar.add(new JLabel("Your Message:"), BorderLayout.WEST);
      inputBar.add(messageInput, BorderLayout.CENTER);
      inputBar.add(sendButton, BorderLayout.EAST);
      
      setContentPane(content);
      
      pack();
      if (previousWindowLocation == null)
         previousWindowLocation = new Point(40,80);
      else {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         previousWindowLocation.x += 50;
         if (previousWindowLocation.x + getWidth() > screenSize.width)
            previousWindowLocation.x = 10;
         previousWindowLocation.y += 30;
         if (previousWindowLocation.y + getHeight() > screenSize.height)
            previousWindowLocation.y = 50;
      }
      setLocation(previousWindowLocation);
      
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      openWindowCount++;
      windowsCreated++;

      addWindowListener( new WindowAdapter() {
         public void windowClosed(WindowEvent evt) {
            if (connection != null && 
                  connection.getConnectionState() != ConnectionState.CLOSED) {
               connection.close();
            }
            openWindowCount--;
            if (openWindowCount == 0) {
               try {
                  System.exit(0);
               }
               catch (SecurityException e) {
               }
            }
         }
      });
      
      
   } // end constructor
   
     /* public void append(String s) {
   try {
      Document doc = transcript.getDocument();
      doc.insertString(doc.getLength(), s, null);
   } catch(BadLocationException exc) {
      exc.printStackTrace();
   }
}*/
      /**
    * Defines responses to buttons, and when the user presses return
    * in the message input box.
    */
   private class ActionHandler implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
         Object source = evt.getSource();
         if (source == newButton) {
            GUIChat window = new GUIChat();
            window.setVisible(true);
         }
         else if (source == listenButton) {
            if (connection == null || 
                  connection.getConnectionState() == ConnectionState.CLOSED) {
               String portString = listeningPortInput.getText();
               int port;
               try {
                  port = Integer.parseInt(portString);
                  if (port < 0 || port > 65535)
                     throw new NumberFormatException();
               }
               catch (NumberFormatException e) {
                  JOptionPane.showMessageDialog(GUIChat.this, 
                        portString + "is not a legal port number.");
                  return;
               }
               connectButton.setEnabled(false);
               listenButton.setEnabled(false);
               closeButton.setEnabled(true);
               connection = new ConnectionHandler(port);
            }
         }
         else if (source == connectButton) {
            if (connection == null || 
                  connection.getConnectionState() == ConnectionState.CLOSED) {
               String portString = remotePortInput.getText();
               int port;
               try {
                  port = Integer.parseInt(portString);
                  if (port < 0 || port > 65535)
                     throw new NumberFormatException();
               }
               catch (NumberFormatException e) {
                  JOptionPane.showMessageDialog(GUIChat.this, 
                        portString +"is not a legal port number.");
                  return;
               }
               connectButton.setEnabled(false);
               listenButton.setEnabled(false);
               connection = new ConnectionHandler(remoteHostInput.getText(),port);
            }
         }
         else if (source == closeButton) {
            if (connection != null)
               connection.close();
         }
         else if (source == attachFileButton) {
            doAttachFile();
         }
         else if (source == quitButton) {
            try {
               System.exit(0);
            }
            catch (SecurityException e) {
            }
         }
         else if (source == attachButton) {
            doAttach();
         }
         
         else if (source == sendButton || source == messageInput) {
            if (connection != null && 
                  connection.getConnectionState() == ConnectionState.CONNECTED) {
               connection.send(messageInput.getText());
               messageInput.selectAll();
               messageInput.requestFocus();
            }
         }
      }
   }
   
   
   public void moveTheFile (String location) {
        try {
            File destDir = new File("Uploads\\");
            File srcFile = new File(location);
            FileUtils.copyFileToDirectory(srcFile, destDir);
        } catch(Exception e) {
        }
    }

   
   
   /**
    * Copies source file to Server
    */
   private void doAttach() {
      JFileChooser fileDialog = new JFileChooser(); 
      File selectedFile; 
      
            //Add a custom file filter and disable the default
	         //(Accept All) file filter.
            fileDialog.addChoosableFileFilter(new ImageFilter());
            fileDialog.setAcceptAllFileFilterUsed(false);
            
             //Add custom icons for file types.
            fileDialog.setFileView(new ImageFileView());

	         //Add the preview pane.
            fileDialog.setAccessory(new ImagePreview(fileDialog));
      
      fileDialog.setDialogTitle("Select image to be sent");
      int option = fileDialog.showDialog(GUIChat.this, "Attach");
      
      
      if (option == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileDialog.getSelectedFile();
                double kilobytes = (selectedFile.length())/(1024);
                connection.send("Image "+selectedFile.getName()+" ("+kilobytes+" KB) ");
                //Copy File to Server ("Uploads Folder")
                String path = selectedFile.getAbsolutePath();
                System.out.println(path);
                
                String path1 = path.replace("\\", "\\\\");
                moveTheFile(path1);
                               
                }else{
                
                }
      if (option != JFileChooser.APPROVE_OPTION)
      
         return;   }
         
         
         
  //Attaches file to be sent       
  private void doAttachFile() {
      JFileChooser fileDialog = new JFileChooser(); 
      File selectedFile;  
      fileDialog.setDialogTitle("Select File to be Sent");
      int option = fileDialog.showDialog(GUIChat.this, "Attach");
      
      
      if (option == JFileChooser.APPROVE_OPTION) {
                
                selectedFile = fileDialog.getSelectedFile();
                double megabytes = (selectedFile.length())/(1024*1024);
                connection.send("File: "+ selectedFile.getName()+" ("+megabytes+" MB) ");
                //Copy File to Server
                
                //Copy File to Server "Uploads Folder"
                String path = selectedFile.getAbsolutePath();
                System.out.println(path);
                
                String path1 = path.replace("\\", "\\\\");
                moveTheFile(path1);
                    
                
                

Element[] roots = doc.getRootElements(); // #0 is the HTML element, #1 the bidi-root
Element body = null;
for( int i = 0; i < roots[0].getElementCount(); i++ ) {
    Element element = roots[0].getElement( i );
    if( element.getAttributes().getAttribute( StyleConstants.NameAttribute ) == HTML.Tag.BODY ) {
        body = element;
        break;
    }
}
                
                try
{
//inserts a hyperlink
    doc.insertAfterStart( body, "<a href=\"http://www.yahoo.com\">Yahoo</a>" );
}
catch(Exception e) { System.out.println(e); }        
                }else{
                
                }
      if (option != JFileChooser.APPROVE_OPTION)
      
         return;   }
       
         
   
   
   /**
    * Add a line of text to the transcript area.
    * @param message text to be added; a line feed is added at the end.
    */
   private void postMessage(String message, SimpleAttributeSet Attributes) {
     // transcript.append(message + '\n');
     try{
     doc.insertString(doc.getLength(),message + '\n' ,Attributes);
         // The following line is a nasty kludge that was the only way I could find to force
         // the transcript to scroll so that the text that was just added is visible in
         // the window.  Without this, text can be added below the bottom of the visible area
         // of the transcript.
         
       } catch (BadLocationException badLocationException) {
      System.err.println("Oops");
      
      transcript.setCaretPosition(transcript.getDocument().getLength());
   }}
   
   
   /**
    * Defines the thread that handles the connection.  The thread is responsible
    * for opening the connection and for receiving messages.  This class contains
    * several methods that are called by the main class, and that are therefore
    * executed in a different thread.  Note that by using a thread to open the
    * connection, any blocking of the graphical user interface is avoided.  By
    * using a thread for reading messages sent from the other side, the messages
    * can be received and posted to the transcript asynchronously at the same
    * time as the user is typing and sending messages.
    */
   private class ConnectionHandler extends Thread {
      
      private volatile ConnectionState state;
      private String remoteHost;
      private int port;
      private ServerSocket listener;
      private Socket socket;
      private PrintWriter out;
      private BufferedReader in;
      
      /**
       * Listen for a connection on a specified port.  The constructor
       * does not perform any network operations; it just sets some
       * instance variables and starts the thread.  Note that the
       * thread will only listen for one connection, and then will
       * close its server socket.
       */
      ConnectionHandler(int port) {
         state = ConnectionState.LISTENING;
         this.port = port;
         postMessage("\nLISTENING ON PORT " + port + "\n",attributes);
         start();
      }
      
      /**
       * Open a connection to specified computer and port.  The constructor
       * does not perform any network operations; it just sets some
       * instance variables and starts the thread.
       */
      ConnectionHandler(String remoteHost, int port) {
         state = ConnectionState.CONNECTING;
         this.remoteHost = remoteHost;
         this.port = port;
         postMessage("\nCONNECTING TO " + remoteHost + " ON PORT " + port + "\n",attributes);
         start();
      }
      
      /**
       * Returns the current state of the connection.  
       */
      synchronized ConnectionState getConnectionState() {
         return state;
      }
      
      /**
       * Send a message to the other side of the connection, and post the
       * message to the transcript.  This should only be called when the
       * connection state is ConnectionState.CONNECTED; if it is called at
       * other times, it is ignored.
       */
      synchronized void send(String message) {
         if (state == ConnectionState.CONNECTED) {
            postMessage("SEND:  " + message,attributes1);
            out.println(message);
            out.flush();
            if (out.checkError()) {
               postMessage("\nERROR OCCURRED WHILE TRYING TO SEND DATA.",attributes2);
               close();
            }
         }
      }
      
      
      synchronized void sendAttachment(String message) {
         if (state == ConnectionState.CONNECTED) {
            postMessage("SEND:  " + message,attributes1);
            Element[] roots = doc.getRootElements(); // #0 is the HTML element, #1 the bidi-root
Element body = null;
for( int i = 0; i < roots[0].getElementCount(); i++ ) {
    Element element = roots[0].getElement( i );
    if( element.getAttributes().getAttribute( StyleConstants.NameAttribute ) == HTML.Tag.BODY ) {
        body = element;
        break;
    }
}
                
                try
{
    doc.insertAfterStart( body, "<a href=\"http://www.yahoo.com\">Yahoo</a>" );
}
catch(Exception e) { System.out.println(e); }        
                }else{
                
                }

            out.println(message);
            out.flush();
            if (out.checkError()) {
               postMessage("\nERROR OCCURRED WHILE TRYING TO SEND DATA.",attributes2);
               close();
            }
         }
      

      
      
      
      
      /**
       * Close the connection. If the server socket is non-null, the
       * server socket is closed, which will cause its accept() method to
       * fail with an error.  If the socket is non-null, then the socket
       * is closed, which will cause its input method to fail with an
       * error.  (However, these errors will not be reported to the user.)
       */
      synchronized void close() {
         state = ConnectionState.CLOSED;
         try {
            if (socket != null)
               socket.close();
            else if (listener != null)
               listener.close();
         }
         catch (IOException e) {
         }
      }
      
      /**
       * This is called by the run() method when a message is received from
       * the other side of the connection.  The message is posted to the
       * transcript, but only if the connection state is CONNECTED.  (This
       * is because a message might be received after the user has clicked
       * the "Disconnect" button; that message should not be seen by the
       * user.)
       */
      synchronized private void received(String message) {
         if (state == ConnectionState.CONNECTED)
            postMessage("RECEIVE:  " + message,attributes3);
            
            
      }
      
      /**
       * This is called by the run() method when the connection has been
       * successfully opened.  It enables the correct buttons, writes a
       * message to the transcript, and sets the connected state to CONNECTED.
       */
      synchronized private void connectionOpened() throws IOException {
         listener = null;
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         out = new PrintWriter(socket.getOutputStream());
         state = ConnectionState.CONNECTED;
         closeButton.setEnabled(true);
         sendButton.setEnabled(true);
         messageInput.setEditable(true);
         messageInput.setText("");
         messageInput.requestFocus();
         postMessage("CONNECTION ESTABLISHED\n", attributes);
         OnlineCount++;
         //setTitle("[Online("+OnlineCount+")]");
      }
      
      /**
       * This is called by the run() method when the connection is closed
       * from the other side.  (This is detected when an end-of-stream is
       * encountered on the input stream.)  It posts a message to the
       * transcript and sets the connection state to CLOSED.
       */
      synchronized private void connectionClosedFromOtherSide() {
         if (state == ConnectionState.CONNECTED) {
            postMessage("\nCONNECTION CLOSED FROM OTHER SIDE\n", attributes2);
            state = ConnectionState.CLOSED;
            OnlineCount--;
         }
      }
      
      /**
       * Called from the finally clause of the run() method to clean up
       * after the network connection closes for any reason.
       */
      private void cleanUp() {
         state = ConnectionState.CLOSED;
         listenButton.setEnabled(true);
         connectButton.setEnabled(true);
         closeButton.setEnabled(false);
         sendButton.setEnabled(false);
         messageInput.setEditable(false);
         postMessage("\n*** CONNECTION CLOSED ***\n", attributes2);
         if (socket != null && !socket.isClosed()) {
               // Make sure that the socket, if any, is closed.
            try {
               socket.close();
            }
            catch (IOException e) {
            }
         }
         socket = null;
         in = null;
         out = null;
         listener = null;
      }
      
      
      /**
       * The run() method that is executed by the thread.  It opens a
       * connection as a client or as a server (depending on which 
       * constructor was used).
       */
      public void run() {
         try {
            if (state == ConnectionState.LISTENING) {
                  // Open a connection as a server.
               listener = new ServerSocket(port);
               socket = listener.accept();
               listener.close();
            }
            else if (state == ConnectionState.CONNECTING) {
                  // Open a connection as a client.
               socket = new Socket(remoteHost,port);
            }
            connectionOpened();  // Set up to use the connection.
            while (state == ConnectionState.CONNECTED) {
                  // Read one line of text from the other side of
                  // the connection, and report it to the user.
               String input = in.readLine();
               if (input == null)
                  connectionClosedFromOtherSide();
               else
                  received(input);  // Report message to user.
            }
         }
         catch (Exception e) {
               // An error occurred.  Report it to the user, but not
               // if the connection has been closed (since the error
               // might be the expected error that is generated when
               // a socket is closed).
            if (state != ConnectionState.CLOSED)
               postMessage("\n\n ERROR:  " + e, attributes2);
         }
         finally {  // Clean up before terminating the thread.
            cleanUp();
         }
      }
      
   } // end nested class ConnectionHandler

}
