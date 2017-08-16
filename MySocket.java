package com.agamani.dht_Final;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author shipra
 */
public class MySocket {
	private Socket socket;
	BufferedReader reader;
	PrintWriter writer;

	/**
	 * Here is already created socket is passed to constructor and input and
	 * output streams are created and managed from here.
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public MySocket(Socket socket) throws IOException {
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		writer = new PrintWriter(this.socket.getOutputStream(), true);

	}

	/**
	 *This creates a client side node. It connects to the given node. This connection is from client
	 * to socket.
	 * 
	 * @param node
	 * @throws IOException
	 */
	public MySocket(Node node) throws IOException {
		// this method is equivalent to creating socket + connect()

		// client side socket(server port number, hostname)
		this.socket = new Socket(node.hostName, Integer.parseInt(node.portNumber));
		reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		writer = new PrintWriter(this.socket.getOutputStream(), true);

	}

	public String readLine() throws IOException {
		return reader.readLine();
	}

	public void writeLine(String message) {
		writer.println(message);
	}

	public void close() {
		try {
			reader.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
