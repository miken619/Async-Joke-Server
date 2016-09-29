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


 This is one part of a three part java file. The AsyncJokeClientAdmin
controls the mode (Joke,Proverb,Maintenance) and tell the JokeServer
to set it.

 5. List of files needed for running the program.

 a. AsyncJokeServer.java
 b. AsyncJokeClient.java
 c. AsyncJokeClientAdmin.java

 6. Notes: When switching from to different  mode
 the server will need to finished displaying the current 
 mode if a request has been made before switching.



 ----------------------------------------------------------*/
import java.io.*;  //Get the Input Output Libraries
import java.net.*; //Get the Java networking libraries


public class AsyncJokeClientAdmin {
	public static void main(String args[]){
		String servermode;                            //Class member, servermode, local to JokeClientAdmin.
		if(args.length<1) servermode= "localhost";    //If no servermode is pass through the agr, use "localhost"
		else servermode =args[0];                     //else use arg as for the servermode
		 
		System.out.println("Mike's Async Joke Client Administration, 1.8. \n");   //intro
		System.out.println("Using server: " + servermode + ", Port: 4565");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try{
			char mode;        //Class member, mode, local to InetClient.
			
			System.out.println("Welcome to Mike's Async Client Administration. ");
			
			do{
				System.out.println("");                          //asking user to set the mode
				System.out.print ("What mode would you like the server to be in? \n" +
				                  "(A) - Joke Mode \n" +
						          "(B) - Proverb mode \n" +
				                  "(C) - Maintenance Mode \n" +
				                  "(D) - Quit \n\n");        
				System.out.flush(); 
				mode=in.readLine().charAt(0);    //initialize "mode" to user input
				mode=Character.toUpperCase(mode);
				if (mode =='A' || mode == 'B' || mode == 'C') {   //call setMode(); method if user didn't input "quit"
					
					setMode(mode,servermode);
					
				}
			}while(mode!='D');   //quitSmoking.com?               //user quit the program
		    System.out.println("Cancelled by user request.");
		}catch(IOException x){x.printStackTrace();}                            //Input/Output exception: invalid input, error is trace
	}
	
	
	static void setMode(char mode, String servermode){
		Socket sock;                                        //Method member, sock, local to setMode().
		BufferedReader fromServer;                          //Method member, fromServer, local to setMode().
		PrintStream toServer;                               //Method member, toServer, local to setMode().
		String textFromServer;                              //Method member, textFromServer, local to setMode().
		
		try{                                                //switch statement to initialize the mode to send to the JokeServer
			String sendMode="joke-mode";
			switch(mode){
			
				case 'A':
					sendMode="joke-mode";
					break;
				case 'B':
					sendMode="proverb-mode";
					break;
				case 'C':
					sendMode="maintenance-mode";
					break;
			
			}
			/*Open our connection to server port*/
			sock = new Socket (servermode, 4565);
			
			// Create filter I/O streams for the socket;
			fromServer = 
					new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			// Send machine mode or IP address to server:
			
			toServer.println(sendMode); toServer.flush();
			
			// Read 1 lines of response form the server,
			// and block while synchronously waiting:
			for(int i = 0; i < 1; i++){
				textFromServer = fromServer.readLine();
				if (textFromServer != null) System.out.println(textFromServer);
			}
			sock.close();
		}catch (IOException x){                                     //Input/Output exception: invalid stream from/to InetServer, 
			System.out.println("Socket error.");                    //error is trace
			x.printStackTrace();
		}
	}
}