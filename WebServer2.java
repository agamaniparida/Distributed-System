import java.io.*;
import java.net.*;
import java.util.*;
import javax.tools.FileObject;
import java.text.SimpleDateFormat;
import java.nio.file.*;

public class WebServer2 
{
	// *************************************Comment Starts*********************************//
	// The below lines will contain all the static variables which are used in
	// the Program//
	// ************************************Comment  Ends***********************************//
	private final static String ServerName = "Agamani Web Server";
	private static int PortNumber;
	private static String FilePath;
	private static boolean connectionKeepAlive;
	public static int counter=0;
    public static ServerSocket serverSocket;

	// *************************************Comment Starts*********************************//
	// The below lines will contain different file types that the web server
	// will handle variables which are used in the Program//
	// ************************************Comment Ends***********************************//
	private static final Map<String, String> fileMap = new HashMap<String, String>() 
	{
		{
			put("ico", "application/ico");
			put("jpg", "image/jpg");
			put("jpeg", "image/jpeg");
			put("css", "text/css");
			put("png", "image/png");
			put("js", "application/js");
			put("html", "text/html");
		}
	};
	// *************************************Comment Starts*********************************//
	// The below lines creates a function to display success and error message
	// when program runs//
	// ************************************Comment Ends***********************************//
	private static void display(String code, String mime, int length, DataOutputStream out, String time)throws Exception 
	{
		System.out.println(code);
		out.writeBytes("HTTP Code:" + code + " \r\n");
		out.writeBytes("Content-Type: " + fileMap.get(mime) + "\r\n");
		out.writeBytes("Content-Length: " + length + "\r\n");
		out.writeBytes("Date: " + time + "\r\n");
		out.writeBytes(ServerName);
		out.writeBytes("\r\n\r\n");
	}

	// *************************************Comment Starts*********************************//
	// The below lines creates a core function for the web server where we
	// validate different condition to display the out put of the web server.
	// We are covering the success criteria as when the page loads successfully
	// and the failure criteria are//
	// 404(File not found) 403(Access Forbidden) 400 Bad Request//
	// ************************************Comment Ends***********************************//
	private static void response(String inStrReadline, DataOutputStream outPut, Socket connectionSocket, BufferedReader inPut,String currentTime) throws Exception 
	{
		String method = inStrReadline.substring(0, inStrReadline.indexOf("/") - 1);
		String file = inStrReadline.substring(inStrReadline.indexOf("/") + 1, inStrReadline.lastIndexOf("/") - 5);

		// *************************************Comment Starts*********************************//
		// When the argument for the file path is null but the file is accessed
		// from the same directory as the class file, then the program should
		// run as expected.
		// To handle this in the below line we are checking the same.
		// ************************************Comment Ends***********************************//
		if (file.equals(""))
			file = "index.html";
		// ************************************Comment Starts***********************************//
		// In below variable mime will verify the file extension and will make
		// sure the extension is a valid option from the above map declaration
		// ************************************Comment Ends***********************************//

		String mime = file.substring(file.indexOf(".") + 1);
		// ************************************Comment Starts***********************************//
		// In the below line we will check if the request consists of any bad
		// string or contains any unacceptable characters
		// ************************************Comment Ends***********************************//

		if (file.contains("#") || file.contains(";") || file.contains("*")) 
		{
			System.out.println(" (Drop Connection : Bad Request)");
			return;
		}

		// ************************************Comment Starts***********************************//
		// In the below lines we will check multiple conditions where we will
		// verify the request is coming form the correct path and
		// we are not requesting anything which is out of the server path
		// ************************************Comment Ends***********************************//
		Path relativePath = Paths.get(FilePath, file);

		// ************************************Comment Starts***********************************//
		// In the below line we will check if the request has empty path in
		// document url but class and the index.html are in same folder then the
		// page should load properly
		// ************************************Comment Ends***********************************//
		CharSequence cs1 = "/";
		boolean retval = relativePath.toString().contains(cs1);
		if (!relativePath.startsWith(FilePath)) 
		{
			if (retval) {
				System.out.println("Error Details  : Connection Dropped Due to File not Present");
				return;
			}
		}
		// ************************************Comment Starts***********************************//
		// Post Method//
		// ************************************Comment Ends***********************************//
		if (method.equals("POST")) 
		{
			String responseString = "400 Bad Request";
			display("400: BAD REQUEST", "html", responseString.length(), outPut, currentTime);
			outPut.write(responseString.getBytes());
			return;
		}
		// ************************************Comment Starts***********************************//
		// Get Method//
		// ************************************Comment Ends***********************************//
		if (method.equals("GET")) {
			try 
			{
				// ************************************Comment Starts***********************************//
				// The below lines of code ensures the persistent connect to be open by issuing token 
				//for each request so that the next request will know to keep its open and active Method//
				// ************************************Comment Ends***********************************//
				StringTokenizer tokenizedLine = new StringTokenizer(inStrReadline);
				while (tokenizedLine.hasMoreTokens())
				{
					String token = tokenizedLine.nextToken();
					if (token.equals("Connection:"))
					if (tokenizedLine.nextToken().equals("keep-alive")) 
					connectionKeepAlive = true;
				}
				// ************************************Comment Starts***********************************//
				// The below block check for the HTTP Protocol the browser
				// request and if the protocol is different from HTTP 1.0 or 1.1
				// then it will throw error//
				// ************************************Comment Ends***********************************//
				if ((!inStrReadline.contains("HTTP/1.0")) && (!inStrReadline.contains("HTTP/1.1"))) 
				{
					System.out.println("Error Details: Not Supported HTTP Protocol - Dropping connection)");
					throw new IOException();
				}
				// ************************************Comment Starts***********************************//
				// In the below line we check if the input argument is '/' then
				// refer to the root directory//
				// ************************************Comment Ends***********************************//
				if (file.endsWith("/"))
					file = file + "index.html";
				File fileObject = new File(FilePath + file);

				// ************************************Comment Starts***********************************//
				// In the below line we will check if the file exists but the
				// user do not have write access on the file then the below
				// error should occur.//
                // We have checked for multiple condition, if file is read only, user has write access and we have restricted the file type
                // as .html then they will get 403 error. But we have commented that line for generic purpose
				// ************************************Comment Ends***********************************//

                //if (fileObject.exists() && (fileObject.canWrite() == true) && file.endsWith(".html"))

				if (fileObject.exists() && (fileObject.canWrite() == false))
				{
					String responseString = " 403 forbidden";
					display("Response: 403 - ACCESS FORBIDDEN", "html", responseString.length(), outPut, currentTime);
					outPut.write(responseString.getBytes());
					return;
				}

				// ************************************Comment Starts***********************************//
				// In the below line we will read the file using the buffer
				// stream and the n display the content of the file//
				// ************************************Comment Ends***********************************//
				byte[] fileBytes = null;
				InputStream is = new FileInputStream(FilePath + file);
				fileBytes = new byte[is.available()];
				is.read(fileBytes);
				display("Response: 200-HTTP OK", mime, fileBytes.length, outPut, currentTime);
				outPut.write(fileBytes);
            }

            catch (FileNotFoundException ex1)
			{
				try
				{
					// ************************************Comment Starts***********************************//
					// In the below line we will we will validate if the file is
					// correct and have content//
					// ************************************Comment Ends***********************************//
					byte[] fileBytes = null;
					InputStream is = new FileInputStream(FilePath + "404.html");
					fileBytes = new byte[is.available()];
					is.read(fileBytes);
					System.out.println("Error Details: File is either nulll or no valid content present");
					display("Response: 404 - FILE NOT FOUND", "html", fileBytes.length, outPut, currentTime);
					outPut.write(fileBytes);
				} 
				catch (FileNotFoundException ex2) 
				{
					// ************************************Comment Starts***********************************//
					// In the below line we will we will validate if the file is
					// present in the directory//
					// ************************************Comment Ends***********************************//
					String responseString = "404 File Not Found";
					System.out.println("Error DEtails: File Not Found in the Requested Directory");
					display("Response: 404 - FILE NOT FOUND", "html", responseString.length(), outPut, currentTime);
					outPut.write(responseString.getBytes());
				}
			} 
			catch (IOException ex3) 
			{
				// ************************************Comment Starts***********************************//
				// In the below line we will we will validate if there is any
				// input out put exception occurs//
				// ************************************Comment Ends***********************************//
				String responseString = " 400 Bad Request";
				display("Response: 400 - BAD REQUEST", "html", responseString.length(), outPut, currentTime);
				outPut.write(responseString.getBytes());
			}

                // ************************************Comment Starts***********************************//
                // In the below line we are trying to add timeout for http/1.1 connection to make it persistent

                // ************************************Comment Ends***********************************//
            finally
			{
				if (inStrReadline.contains("HTTP/1.0") && connectionKeepAlive == false)
				{
					connectionSocket.close();
                    System.out.println("Connection Alive Status is false and Connection is closed : " + connectionSocket.isClosed());
					return;
				} 
				
				else if (inStrReadline.contains("HTTP/1.1"))
				{
					connectionSocket.setKeepAlive(true);
					System.out.println("Client Keep Alive Status : " + connectionSocket.getKeepAlive());
					serverSocket.setSoTimeout(50000);
					System.out.println("Request Time Out Value : " + serverSocket.getSoTimeout() + " MiliSeconds");
					System.out.print("Request # "+ counter +" Complete"+"\n");

				}
			}

		}

	}

	private static class WorkerRunnable implements Runnable 
	{
		protected Socket socket = null;
		BufferedReader inputStream;
		DataOutputStream outputStream;
		String inStrReadLine;

		public WorkerRunnable(Socket connectionSocket) throws Exception 
		{
			try
			{
				this.socket = connectionSocket;
			}
			catch (Exception e) 
			{
				System.out.println("Error Occured : #001 Unable to instantiate the socket");
			}
		}
		// *************************************Comment Starts*********************************//
		// The below lines will start each request when requested from the new thread call and will request the response method to execute
		// ************************************Comment Ends***********************************//
		public void run() 
		{
			try 
			{
				while (true) 
				{
					
					// *************************************Comment Starts*********************************//
					//get the input out request holder
					// ************************************Comment Ends***********************************//
					this.inputStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
					this.outputStream = new DataOutputStream(this.socket.getOutputStream());
					this.inStrReadLine = this.inputStream.readLine();
					// ************************************Comment Starts***********************************//
					// Current Date Time Print for the console
					// ************************************Comment Ends***********************************//
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					String cuurentTime = "[" + sdf.format(cal.getTime()) + "] ";
					
					
					try 
					{
						if (this.inputStream != null)
						{
							// ************************************Comment Starts***********************************//
							//print all the request detail, time
							// ************************************Comment Ends***********************************//
							counter++;
							System.out.print("\n"+"Request # "+ counter +"\n"); 
							System.out.print("Requested Item : " + this.inStrReadLine +"\n"); 
							System.out.print("Time of request : " + cuurentTime +"\n"); 
							System.out.print("Inet Address : " + this.socket.getInetAddress().toString()+"\n");
							// ************************************Comment Starts***********************************//
							// if the input stream has certain request the actual response method will be called and executed
							// ************************************Comment Ends***********************************//
							response(this.inStrReadLine, this.outputStream, this.socket, this.inputStream, cuurentTime);
						}
					} 
					catch (Exception e) 
					{
						//e.printStackTrace();
						System.out.println("Eror Occured: #002 Unable to close the connection");
					}
					finally
					{
						// ************************************CommentStarts**********************************//
						// Reset,flush and close the input and out put variable
						// ************************************Comment Ends***********************************//
						this.inputStream.reset();
						this.outputStream.flush();
						this.inputStream.close();
						this.outputStream.close();
                        
						
					}
				}

			} 
			catch (Exception e) 
			{
				try 
				{
					// ************************************Comment Starts***********************************//
					// Close input and out put objects
					// ************************************Comment Ends***********************************//
					this.inputStream.close();
					this.outputStream.close();
                    this.socket.close();
				} 
				catch (IOException exc) 
				{
					exc.printStackTrace();
					System.out.println("Error Occured : #003 Unable to close the connection");
				}
			}
		}
	}

	public static void main(String args[]) throws Exception 
	{
		try
		{
			int i = 0;
			System.out.println("*********Input Arguments Starts**********");
			for (i = 0; i < args.length; i++) 
			{
				if (i == 1) 
				{
					System.out.println("Document Root : " + args[i]);
					FilePath = args[1];
				} else if (i == 3) 
				{
					System.out.println("Port Number :" + args[i]);
					PortNumber = Integer.valueOf(args[3]);
				}
			}
			System.out.println("*********Input Arguments Ends**********");
            serverSocket = new ServerSocket(PortNumber);
			// ************************************Comment Starts***********************************//
			// In the below line we will keep the server alive for infinite period
			// of time unless interrupted manually//
			// ************************************Comment Ends***********************************//
			System.out.println("Web Server Started");
			while (true) 
			{	
				Socket connectionSocket = serverSocket.accept();
				new Thread(new WorkerRunnable(connectionSocket)).start();
			}
		}
        catch(SocketTimeoutException s)
        {
            System.out.println("Error Occured : Socket Timed Out");
        }
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error Occured : #004 Main Block Unable to Excute");
		}
	}
}
