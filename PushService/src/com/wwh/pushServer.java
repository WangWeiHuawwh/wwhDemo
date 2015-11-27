package com.wwh;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class pushServer {
	public static final int PORT = 12345;// 监听的端口号
	private InputStream mInputStream;
	private OutputStream outStream = null;
	private PushResponse mPushResponse = new PushResponse();
	byte[] headerBuffer = new byte[PushHead.HEAD_LENGTH];

	public static void main(String[] args) {
		System.out.println("服务器启动...\n");
		pushServer server = new pushServer();
		server.init();
	}

	public void init() {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			while (true) {
				// 一旦有堵塞, 则表示服务器与客户端获得了连接
				Socket client = serverSocket.accept();
				// 处理这次连接
				new HandlerThread(client);
			}
		} catch (Exception e) {
			System.out.println("服务器异常: " + e.getMessage());
		}
	}

	private class HandlerThread implements Runnable {
		private Socket socket;

		public HandlerThread(Socket client) {
			socket = client;
			new Thread(this).start();
		}

		public void run() {
			try {
				// 读取客户端数据
				mInputStream = socket.getInputStream();
				outStream = socket.getOutputStream();
				System.out.println("客户端连接");
				while (true) {
					if (ReadHead()) {
						if (ReadBody()) {
							// 处理客户端数据
							System.out.println("客户端发过来的内容:"
									+ mPushResponse.mPushHead.cmd
									+ mPushResponse.mPushHead.sequence);
							try {
								PushRequest my = new PushRequest(
										mPushResponse.mPushHead.sequence,
										PushRequest.CMD_HEART);
								outStream.write(my.toByteArray());
								outStream.flush();
							} catch (Exception e) {
								System.out.println("error");
							}

						} else {
							// error

						}
					} else {
						// error

					}

				}
				// input.close();
			} catch (Exception e) {
				System.out.println("服务器 run 异常: " + e.getMessage());
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e) {
						socket = null;
						System.out.println("服务端 finally 异常:" + e.getMessage());
					}
				}
			}
		}
	}

	private boolean ReadBody() throws IOException {
		int totalRead = 0;
		int needRead = mPushResponse.mPushHead.bodyLength;
		while (true) {
			int read = 0;
			System.out.println("read body...");
			read = mInputStream.read(mPushResponse.body, totalRead, needRead
					- totalRead);
			if (read == -1) {
				// socket error
				return false;
			}
			totalRead += read;
			if (totalRead == needRead)
				break;
		}
		return true;
	}

	private boolean ReadHead() throws IOException {
		int totalRead = 0;
		int needRead = PushHead.HEAD_LENGTH;
		while (true) {
			int read = 0;
			read = mInputStream.read(headerBuffer, totalRead, needRead
					- totalRead);
			if (read == -1) {
				// socket error
				return false;
			}
			totalRead += read;
			if (totalRead == needRead)
				break;
		}
		mPushResponse.mPushHead.initFromBytes(headerBuffer);
		mPushResponse.body = new byte[mPushResponse.mPushHead.bodyLength];
		return true;
	}
}