package com.cit.ids.utilities;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SSocketHandler extends Service {

	private ServerSocket vServerSocket = null;
	private Socket vSocket = null;
	int vServerPort = 900;
	
	public SSocketHandler(int vServerPort) {
		this.vServerPort = vServerPort;
	}
	
	public void startServer() {
		new Thread(new SSocketThread()).start();
	}
	
	public void sendData(String vMessage) {
		try {
			PrintWriter vPrintWriter = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(vSocket.getOutputStream())), true);
			vPrintWriter.println(vMessage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	class SSocketThread implements Runnable {
		@Override
		public void run() {
			try {
				vServerSocket = new ServerSocket(vServerPort);
				vSocket = vServerSocket.accept();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent vIntent) {
		return null;
	}
}