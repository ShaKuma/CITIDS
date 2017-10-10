package com.cit.ids.utilities;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CSocketHandler extends Service {

	private Socket vSocket = null;
	String vServerIP = null;
	int vServerPort = 900;
	
	public CSocketHandler(String vServerIP, int vServerPort) {
		this.vServerIP = vServerIP;
		this.vServerPort = vServerPort;
	}
	
	public void connect() {
		new Thread(new CSocketThread()).start();
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

	class CSocketThread implements Runnable {
		@Override
		public void run() {
			try {
				InetAddress vServerAddress = InetAddress.getByName(vServerIP);
				vSocket = new Socket(vServerAddress, vServerPort);
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