/*--------------------------------------------------------

 1. Michael Nguyen  3/4/15:

 2. Java version 1.8.0_25


 3. Precise command-line compilation examples / instructions:
  
  In a cmd shell open the directory to this program and type in:
  
 >javac *.java


 4. Precise examples / instructions to run this program:

 In separate CMD shell windows:

 > java AsyncJokeClient
 > java AsyncJokeServer
 > java AsyncJokeClientAdmin


 This is one part of a three part java file. The AsyncJokeClient
 is the interface with which the user will interact with
 The user will enter his/her name once and the AsyncJokeClient
 will connect with the AsyncJokeServer to request jokes or proverb
 and send that back to the AsyncJokeClient which will display it to the 
 user.

 5. List of files needed for running the program.

 a. AsyncJokeServer.java
 b. AsyncJokeClient.java
 c. AsyncJokeClientAdmin.java

 6. Notes: When switching from to different  mode
 the server will need to finished displaying the current 
 mode if a request has been made before switching.



 ----------------------------------------------------------*/

import java.io.*; //Get the Input Output Libraries
import java.net.*; //Get the Java networking libraries
import java.util.HashMap; //Get the HashMap Libraries
import java.util.Random; //Get the Random Libraries

public class AsyncJokeClient {
     static int g = 0;            //Condition use in the getjokesProverbProverbs() method to prevent start up sequence to occur more than once 
	public static void main(String args[]) {
		String serverName = "localhost";   //Name of address for the Socket variable
		
	
	

		System.out.println("\n\nMike's Async Joke Client, 1.8. \n"); // intro
		System.out.println("Using server: " + serverName + ", Port: 4250");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			String name = null;          //Store the name of the user
									
			String name2;                //Store use input, use to determine to continue or quit program

			do {//Intro, get user name once
				System.out.println("");
				if (name == null) {
					System.out.print("Enter your name or quit to end program: "); 
					System.out.flush();
					name2 = in.readLine();
					name = name2;
					System.out.println("");
				} else {
					//Ask user to continue or quit after showing five jokes or proverbs
					System.out
							.print("Press enter to continue or quit to end program: ");
					System.out.flush();
					name2 = in.readLine();
					System.out.println("");
				}

				if (name2.indexOf("quit") < 0) {  //User decides to continue
					getjokesProverbProverbs(name, serverName,args);

				}
			} while (name2.indexOf("quit") < 0); //User decides to quit
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {
			x.printStackTrace();
		} 
	}
     
	
	//Method takes in name of the user, serverName of the address use for the Socket, and a[] argument from command line
	static void getjokesProverbProverbs(String name, String serverName,String[] a) {
		Socket sock; //Opens a quick connection to Server at serverName, port 4250

		String textFromServer; //Store string data from server
		HashMap<String, Boolean> jokes = new HashMap<String, Boolean>(5);     //Store state of jokes, determine what joke to ask server					
		HashMap<String, Boolean> proverbs = new HashMap<String, Boolean>(5);  //Store state of proverbs, determine what joke to ask server	
		HashMap<String, Integer> multServer = new HashMap<String, Integer>(a.length); //Store server user input from command line
        
		//Variable randomize the joke/proverb request
		Random random = new Random(); 
		String abcde = "ABCDE"; 

		String enterKey;                        //Store user input from console input
		int c = 0, c2 = 0;                      //Loop condition, breaks loop if 5 jokes or proverbs are display
		
		String randomChar = "B";                //Store the joke or proverb request that will be send to the server
		
        char first = 'A';                       //Use as a key in the multServer variable, increment based on a[] length
        String sec;                             //Use along with first, convert first into a string for multServer variable
        
		DatagramSocket udpSocket;               //UDP socket that will replace doorbell socket   
		InetAddress udpAddress;                 //Store "localhost" converted to InetAddress
		int udpPort;                            //Store either 4250 or command line argument for DatagramSocket port
		byte[] sendSize;                        //Use in DatagramPacket, determine size of package being send in byte[]
		DatagramPacket udpPackage,temp;         //Package send over or received through the DatagramSocket
		byte[] size = new byte[512];            //Use in DatagramPacket, store receive package, initialize to arbitrary value
		
		BufferedReader in;                      //Read console input
		Thread t = null;                        //Thread for addNum class, use while waiting for server respond
		boolean wait = true;                    //Loop condition, always true, statement inside will break loop if package is received
		AddNums AN = null;
		// initialize jokes in order to maintain state
		jokes.put("A", false);
		jokes.put("B", false);
		jokes.put("C", false);
		jokes.put("D", false);
		jokes.put("E", false);
		// initialize proverbs in order to maintain state
		proverbs.put("A", false);
		proverbs.put("B", false);
		proverbs.put("C", false);
		proverbs.put("D", false);
		proverbs.put("E", false);

		try {
			//Initialize multServer variable if command line was provided
			if(a.length>0){
				System.out.println("Asynchronous JokeClient started with bindings:");
				for(int b= 0; b <a.length;b++){
					first += b;           //Char value increment, bases on a[] length
					sec = ""+ first;      //Convert char to String
					System.out.println("Server " + first + " at "+ a[b]);         //Display to console
					multServer.put(sec,Integer.parseInt(a[b]));                   //Store command line argument
					first ='A';                                                   //reinitialize first
				}
				System.out.println();
			}
			
		    //variable g ensure doorbell socket only make a connection once
			if(g==0){
				sock = new Socket(serverName, 4250); // open connection to
				sock.close();
				g++;
			}
			udpAddress = InetAddress.getByName(serverName);           //initialize udpAddress to only use "localhost"
			udpPort = 4250;                                            //Standard port if no command line is given
			udpSocket = new DatagramSocket(4555);                      //initialize client DatagramSocket to port 4555
			udpSocket.setSoTimeout(180);                               //Initialize DatagramSocket Time out so received package doens't stall waiting for server
			in = new BufferedReader(new InputStreamReader(System.in));  //Initialize in variable to read user input from console
			udpPackage = new DatagramPacket(size, size.length);        //Initialize DatagramPacket to initialize to arbitrary value
			while (true) {
				if (c == 5 || c2 == 5)                //Loop condition, breaks if 5 jokes or proverbs are display
					break;
				else if (c == 0 && c2 == 0) {
                    
					//Initial start condition, after getting 5 jokes or proverb will not reactivate unless the program is restarted
					//Opens up a thread to AddNums class to do addition while waiting for server
					AN = new AddNums();
					t = new Thread(AN);
					t.start(); 
					
				
				}
				//Initial start condition, waits for all server to responds, will not reactivate unless the program is restarted
				//Only activate if there is command line argument, wait for message from server and send back ack
				if(g==1 && a.length>0){
					for (int x = 0; x < a.length;) {
						try {
							udpAddress = InetAddress.getByName("localhost");   
							udpPackage = new DatagramPacket(size, size.length);
							udpSocket.receive(udpPackage);
							temp = udpPackage;                                   //I added the multiple server option last and use "udpPackage" for 
							                                                     //a lot of my codes, so using temp allow me to not have to go back and change my code
							udpPort= udpPackage.getPort();
							sendSize = name.getBytes();
							udpPackage = new DatagramPacket(sendSize,
									sendSize.length, udpAddress, udpPort);
							udpSocket.send(udpPackage);                         //send back ack, server will not use data for anything, just an ack
							udpPackage = temp;                                  //Restore udpPackage to received data, at this point store the current mode 
							x++;
							
						} catch (Exception s) {
							continue;
						}

					}				
					
					  g++;
					  
					
				}else { 
					while (wait) {
						try {
							//This loop will take over after the initial 5 jokes or proverb,
							//Loop received current mode and send back ack to the server
							udpPackage = new DatagramPacket(size, size.length);
							udpSocket.receive(udpPackage);
							temp = udpPackage;
							udpPort= udpPackage.getPort();
							sendSize = name.getBytes();
							udpPackage = new DatagramPacket(sendSize,
									sendSize.length, udpAddress, udpPort);
							udpSocket.send(udpPackage);
							udpPackage = temp;
							break;
						} catch (SocketTimeoutException s) {
							continue;
						}

					}
					
				}
					
				
				//Wait for the AddNum thread to finish before continuing
				if (AddNums.checkCon) {
					AddNums.checkCon = false;
					try {
						t.join();
					} catch (InterruptedException e) {e.printStackTrace();}
				}
				
				//Convert the package received to String, package usually hold the current mode
				String mode = new String(udpPackage.getData(), 0,
						udpPackage.getLength());
				
				//Display the multiple server and check input
				//Loop until input matches one of the server (ie, A,B,C)
				if(a.length > 0 && !mode.contains("unavailable")){
				
					do {
						System.out.print("Enter ");
						for (int y = 0; y < a.length;y++) {
							first +=y;
							System.out.print(first + " ");
							first = 'A';
							if(y < a.length-1)
								System.out.print("or ");
 
						}				
						System.out.print("to get a joke or proverb: ");
               
						enterKey = in.readLine();
						enterKey = enterKey.toUpperCase();
						System.out.println("");
						
					} while (multServer.get(enterKey) == null);
					
					udpPort = multServer.get(enterKey);        //change server port to match the input
					
				//Display single server mode, server port does not change
			    //User input "enter", loop until correct input
				}else{
					do {
						System.out.print(mode);

						enterKey = in.readLine();
						System.out.println("");
						System.out.flush();
					} while (!enterKey.equals(""));
				
				}
				
				
				
				//Generate a random char value, to send to server, jokes variable is updated and maintain states
				if (mode.contains("joke")) {
					while (true) {
						if (jokes.get(randomChar = ""
								+ abcde.charAt(random.nextInt(5))) != true) {
							jokes.put(randomChar, true);
							c++;
							break;
						}
					}
					
					//Generate a random char value, to send to server, proverbs variable is updated and maintain states
				} else if (mode.contains("proverb")) {
					while (true) {
						if (proverbs.get(randomChar = ""
								+ abcde.charAt(random.nextInt(5))) != true) {
							proverbs.put(randomChar, true);
							c2++;
							break;
						}
					}
				 //If server is in maintenance mode, c = 5 will cause the loop to break and go back to the main() method 
				}else{c=5;};

				while (wait) {
					try {
						//Send user name to server
						sendSize = name.getBytes();
						udpPackage = new DatagramPacket(sendSize,
								sendSize.length, udpAddress, udpPort);
						udpSocket.send(udpPackage);
						
						//Send random char value to server
						sendSize = randomChar.getBytes();
						udpPackage = new DatagramPacket(sendSize,
								sendSize.length, udpAddress, udpPort);
						udpSocket.send(udpPackage);
						
						//Received joke/proverb from server
						udpPackage = new DatagramPacket(size, size.length);
						udpSocket.receive(udpPackage);
						textFromServer = new String(udpPackage.getData(), 0,
								udpPackage.getLength());
					    
						//Display only jokes or proverbs
						if (textFromServer != null && !textFromServer.contains("unavailable"))
							System.out.println(textFromServer);
						break;
					} catch (SocketTimeoutException s) {
						continue;
					}

				}
				

				
			}

			udpSocket.close(); //Close DatagramSocket

		} catch (IOException x) { // Input/Output exception: invalid stream
									
			System.out.println("Socket error."); // error is trace
			x.printStackTrace();
		}
		
	}
	
	

}
//AddNums class called by getjokesProverbProverbs() to do addition while waiting for server responds
//Only called once, unless program is restarted
class AddNums extends Thread {
	static boolean checkCon = true;    //Loop condition, value will be change by calling method
	BufferedReader in;                //Read user input from console
	String[] addNum;                  //Store the int value from user input
   
	public void run() {
		in = new BufferedReader(new InputStreamReader(System.in));  //Initialize to read user input
		
		while (checkCon) {

			System.out.print("Enter a string of number to add up: "); //Intro
		   
			try {
				addNum = in.readLine().split(" ");                 //The user input is broken up by its' spaces and store in addNum[]
				int sum = 0;                                       //Store total addition value
				for (int e = 0; e < addNum.length; e++) {          //loop through addnum[], parsing String into int, and adding together 
					sum += Integer.parseInt(addNum[e]);            
				}
				System.out.println("Your total is: " + sum + "\n"); //Dipslay sum to user
			} catch (IOException e1) {                              //Read error
				e1.printStackTrace();
			} catch (NumberFormatException n) {                     //Invalid input
								continue;
			}

		}

	}
	
	           
		         
		
		

}
