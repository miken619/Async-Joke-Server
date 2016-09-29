/*--------------------------------------------------------

 1. Michael Nguyen  3/4/15:

 2. Java version 1.8.0_25


 3. Precise command-line compilation examples / instructions:

  In a cmd shell open the directory to this program and type in:
  
 >javac *.java


 4. Precise examples / instructions to run this program:

 In separate CMD shell windows:

 > java  AsyncJokeClient
 > java  AsyncJokeServer
 > java  AsyncJokeClientAdmin


 This is one part of a three part java file. The  AsyncJokeServer
 receives data from the  AsyncJokeClient and  AsyncJokeClientAdmin. For
 the  AsyncJokeClient the  AsyncJokeServer send back either a proverb
 or a joke depending on the mode. For the  AsyncJokeClientAdmin 
 the server set the mode (Joke,Proverb,Maintenance) base on
 the input.

 5. List of files needed for running the program.

 a.  AsyncJokeServer.java
 b.  AsyncJokeClient.java
 c.  AsyncJokeClientAdmin.java

 6. Notes: When switching from to different  mode
 the server will need to finished displaying the current 
 mode if a request has been made before switching.


 ----------------------------------------------------------*/
import java.io.*; //Get the Input Output libraries
import java.net.*; //Get the Java networking libraries
import java.util.HashMap; //Get the Java HashMap libraries

import java.util.concurrent.TimeUnit;     //Get the Java TimeUnit libraries

class AsyncJokeWorker extends Thread { // Class definition
	
	static String mode = "joke-mode";              //Store current mode, intialize to default
	private DatagramSocket udpSocket;              //UDP Socket for data transfer
	private static InetAddress udpAddress;         //Store Inet address of "localhost" 
	private static int udpPort = 4555;             //store default port of client

	private byte[] size;                           //Use in DatagramPacket, store receive package, will be initialize to arbitrary value
	private byte[] sendSize;                       //Use in DatagramPacket, determine size of package being send in byte[]
	private int time;                              //Store waiting time based on single or multiple server
	private String send;                           //String that will be send to the client
	private DatagramPacket udpPackage;             //Package send over or received through the DatagramSocket
	
	boolean wait;                                   //Loop condition, always true, statement inside will break loop if package is received
    static boolean admin = true;                   //Loop condition for the Async Admin, will stop flow of event to change mode
    
    
	AsyncJokeWorker(DatagramSocket u, int t) {
		//initialize value
		wait = true;
		size = new byte[512];
		time = t;                               //Value send from AsyncJokeServer class
		try {
			
			//initialize value
			udpAddress = InetAddress.getByName("localhost");
			
			
			udpSocket = u;                    //Value send from AsyncJokeServer class
			udpSocket.setSoTimeout(180);      //Initialize DatagramSocket Time out so received package doens't stall waiting for server  
			
			TimeUnit.SECONDS.sleep(time);             //Sleep for either 40 or 70 seconds   
			System.out.println("Establishing connection port at " + udpSocket.getLocalPort() +"\n");    //Display connection port to console
			
		} catch (Exception s) {
			System.out.println("Unable to estabalish new server port. Try again\n");}                  //Error, need to restart the program
		
		
		
	}

	public void run() {
		
		try {
			String name = null;               //Store the client name
			String getRandom = null;         //Store the random char send by client to determine which joke or proverb to send back
			while (true) {
				
				//Condition that will stop the flow of event so admin can safely change the mode
				setAdmin();
				while (AdminWorker.getChange()) {TimeUnit.MILLISECONDS.sleep(30);}
				setAdmin();
				
				
				//Send mode to client and wait for ack to break loop
				while (wait) {
					try {
					
					
						if (getMode().equals("joke-mode")) {
							send = new String("Press enter to hear a joke ");
							sendSize = send.getBytes();
							udpPackage = new DatagramPacket(sendSize,
									sendSize.length, udpAddress, udpPort);
							udpSocket.send(udpPackage);
							
						} else if (getMode().equals("proverb-mode")) {
							send = new String("Press enter to hear a proverb ");
							sendSize = send.getBytes();
							udpPackage = new DatagramPacket(sendSize,
									sendSize.length, udpAddress, udpPort);
							udpSocket.send(udpPackage);
							
						} else if (getMode().equals("maintenance-mode")) {
							
							send = new String(
									"The server is temporarily unavailable -- check-back shortly.");
							sendSize = send.getBytes();
							udpPackage = new DatagramPacket(sendSize,
									sendSize.length, udpAddress, udpPort);
							udpSocket.send(udpPackage);
							
						}
						udpPackage = new DatagramPacket(size, size.length);
						udpSocket.receive(udpPackage);
						udpPort = udpPackage.getPort();
						
						break;
					} catch (SocketTimeoutException s) {
						continue;
					} 
				}
				
				//Client send name of user, and char value base on the mode
				//Server store name and char for joke and proverb methods
				while (wait) {
					try {

						udpPackage = new DatagramPacket(size, size.length);
						udpSocket.receive(udpPackage);
						name = new String(udpPackage.getData(), 0,
								udpPackage.getLength());
						
						udpPackage = new DatagramPacket(size, size.length);
						udpSocket.receive(udpPackage);
						getRandom = new String(udpPackage.getData(), 0,
								udpPackage.getLength());
						udpPort = udpPackage.getPort();
						break;
					} catch (SocketTimeoutException s) {continue;}
				}
				
				//Call method based on mode
				if (getMode().contains("joke"))
					printJokes(name, getRandom,udpSocket);
				else if (getMode().contains("proverb"))
					printProverbs(name, getRandom,udpSocket);
				
				//No method call, send back error
				else if (getMode().contains("maintenance")) {
					System.out.println("The server is in maintenance mode.");
					send = new String(
							"The server is temporarily unavailable -- check-back shortly.");
					sendSize = send.getBytes();
					udpPackage = new DatagramPacket(sendSize,
							sendSize.length, udpAddress, udpPort);
					udpSocket.send(udpPackage);
					
				}

			}
			

		} catch (IOException x) { // Input/Output exception call:
									
			System.out.println("Server read error");
			x.printStackTrace(); // trace error
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

	}

	//Methods used by admin to safely change the mode
	//These methods pauses the run() method until the mode is change, and also ensure
	//the mode is only change when the run() loop is at the top
	static void setMode(String newMode) {
		mode = newMode;
	}
	static String getMode() {
		return mode;
	}
	static boolean getAdmin(){
		return admin;
	}
	static void setAdmin() {
		admin = !admin;
	}

	
	

	// Store the HashMap of jokes and stream to the AsyncJokeClient 
	static void printJokes(String name, String getRandom,DatagramSocket udpSocket) {
		
		//Variable use to send joke to client
		byte[] size;
		DatagramPacket udpPackage;
		String send;
         
		//jokes variable initialize to 5 jokes each with a char key
		HashMap<String, String> jokes = new HashMap<String, String>(5);
		jokes.put(
				"A",
				"A SEO couple had twins. For the first time they are happy with duplicate content");
		jokes.put("B",
				"In order to understand recursion you must first understand recursion.");
		jokes.put("C",
				"Why do Java developers wear glasses? Because they can't C#");
		jokes.put("D",
				"There's a band called 1023MB. They haven't had any gigs yet.");
		jokes.put("E", "What do you call 8 hobbits? A hobbyte");

		try {
			
			System.out.println("Looking up Joke " + getRandom + " for " + name); 		//Print joke key and user name to server console														
			send = new String("Looking up Joke " + getRandom + " for " + name           //String that will be send to client
					+ "\n" + jokes.get(getRandom) + "\n");
			size = send.getBytes();                                                     //Convert String to bytes
			udpPackage = new DatagramPacket(size, size.length, udpAddress,              //Initialize DatagramPacket to be send to the client  
					udpPort);
			udpSocket.send(udpPackage);                                                 //Send package

		} catch (IOException ex) { // exception: socket error
			System.out.println("Socket error ");
		}
	}

	// Store the HashMap of proverb and stream to the AsyncJokeClient the current one
	static void printProverbs(String name, String getRandom,DatagramSocket udpSocket) {
		
		//Variable use to send proverb to client
		byte[] size;
		DatagramPacket udpPackage;
		String send;
		
		//proverbs variable initialize to 5 proverbs each with a char key
		HashMap<String, String> proverbs = new HashMap<String, String>(5);
		proverbs.put("A",
				"Experience is a comb which nature gives us when we are bald. ");
		proverbs.put("B",
				"A ship in the harbor is safe, but that is not what ships are built for.");
		proverbs.put("C",
				"You cannot get to the top by sitting on your bottom.");
		proverbs.put("D", "Tomorrow is often the busiest day of the week.");
		proverbs.put("E", "Drop by drop - a whole lake becomes. ");

		try {
			System.out.println("Looking up Proverb " + getRandom + " for "+ name);       //Print proverb key and user name to server console
			send = new String("Looking up Proverb " + getRandom + " for "                //String that will be send to client
					+ name + "\n" + proverbs.get(getRandom) + "\n");
			size = send.getBytes();                                                      //Convert String to bytes 
			udpPackage = new DatagramPacket(size, size.length, udpAddress,               //Initialize DatagramPacket to be send to the client  
					udpPort);
			udpSocket.send(udpPackage);                                                  //Send package
		} catch (IOException ex) { // exception: socket error
			System.out.println("Socket error ");
		}
	}

}

// Class use to control the mode
class AdminWorker extends Thread {
	Socket sock;                                //Open connection to admin port
	static boolean ready=false;
	AdminWorker(Socket s) {
		sock = s;
	} // Constructor, assign arg to local sock

	public void run() {
		//Variable to read and write stream to admin 
		PrintStream out = null; 
		BufferedReader in = null; 
		
		String mode = "joke-mode";            //Initialize to default mode
		
		// Get I/O streams in/out from the socket:
		try {
			
			in = new BufferedReader(                                   // Initialize "in" to client input
					new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());            // Initialize "out" to client output

			// set the mode
			try {
				mode = in.readLine();
				setChange();                      //Change loop condition in AsyncJokeWorker class and wait for loop to reach the top
			
				while(AsyncJokeWorker.getAdmin()){TimeUnit.MILLISECONDS.sleep(30);}             //Once the AsyncJokeWorker class loop reaches the top it set
				                                                                                //condition getAdmin() to be false
				//Change the mode in AsyncJokeWorker class based on the use input
				if (mode.equals("joke-mode")) {
					AsyncJokeWorker.setMode(mode);
					out.println("The Server is now set to Joke Mode");
				} else if (mode.equals("proverb-mode")) {
					AsyncJokeWorker.setMode(mode);
					out.println("The Server is now set to Proverb Mode");
				} else if (mode.equals("maintenance-mode")) {
					AsyncJokeWorker.setMode(mode);
					out.println("The Server is now set to Maintenance Mode");
				}
				
				
				setChange();      //Change condition back to normal, loop in AsyncJokeWorker class no longer stall
			} catch (IOException x) { // Input/Output exception call: invalid

				System.out.println("Server read error");
				x.printStackTrace(); // trace error
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sock.close(); // close this connection, but not the server;
		} catch (IOException ioe) {
			System.out.println(ioe);
		} // Input/Output exception call: invalid input/output

	}
	
	//Use by the AsyncJokeWorker class to inform admin the mode is ready to be change
	static void setChange() {
		ready = !ready;
	}

	static boolean getChange() {
		return ready;
	};
}

public class AsyncJokeServer {
	
	public static void main(String a[]) throws IOException {
		int q_len = 6; //Not interesting. Number of requests for OpSys to queue
		int port = 4250;           //Standard connection port
		Socket sock;               // Will accepts a quick connection from the client before closing
		ServerSocket servsock = new ServerSocket(port, q_len); // Opens a ServerSocket connection for sock
		DatagramSocket sSocket = null;              //DatagramSocket that will reconnect with the client after Socket close.
		
	
		AdminLooper AL = new AdminLooper(); // create a DIFFERENT thread for admin
		Thread t = new Thread(AL);
		t.start(); // ...and start it, waiting for administration input
		System.out
				.println("\n\nMike's Async Server 1.8 starting up, listening at port 4250. \n"); // Server Intro
		
		while(true){
			sock = servsock.accept(); // wait for the next client connection
			sock.close();             //Close connection right away
			
			//Start multiple server thread
			if(a.length>0){
				for(int z = 0; z <a.length;z++){
					try{
					new Thread(new AsyncJokeWorker( new DatagramSocket(Integer.parseInt(a[z])),70)).start();      // Spawn worker to handle request
					}catch(SocketException se){continue;}
				}
			//Start single thread
			}else{
				try{
					sSocket = new DatagramSocket(port);
			
					new AsyncJokeWorker(sSocket,40).start();                  // Spawn worker to handle it
				}catch(SocketException se){continue;}
			}
		
		}
		

	}
}

class AdminLooper implements Runnable {
	public static boolean adminControlSwitch = true;

	public void run() { // RUNning the Admin listen loop
		System.out.println("\n\nIn the admin looper thread");

		int q_len = 6; /* Number of requests for OpSys to queue */
		int port = 4565; // We are listening at a different port for Admin
							// clients
		Socket sock;

		try {
			ServerSocket servsock = new ServerSocket(port, q_len);
			while (adminControlSwitch) {
				// wait for the next ADMIN client connection:
				sock = servsock.accept();
				new AdminWorker(sock).start();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

}
