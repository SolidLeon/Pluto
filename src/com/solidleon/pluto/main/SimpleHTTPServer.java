package com.solidleon.pluto.main; 

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class SimpleHTTPServer implements Runnable {

    private int port = 2345;
    private String webRootDir = ".";

	public static void main(String[] args) {
        int port = 2345;
        String webRootDir = ".";

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                System.err.println("Error parsing command line");
                System.err.println("USAGE: java -jar Pluto.jar <port> <baseDir>");
            }
        }

        if (args.length > 1) {
            webRootDir = args[1];
        }

        new Thread(new SimpleHTTPServer(port, webRootDir)).start();
	}

    public SimpleHTTPServer(int port, String webRootDir) {
        this.port = port;
        this.webRootDir = webRootDir;
    }

    @Override
    public void run() {

        File baseDirFile = new File(webRootDir);
        if (!baseDirFile.exists() || !baseDirFile.isDirectory()) {
            throw new RuntimeException("Web root must be an existing directory!");
        }


        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("SERVER BOUND TO " + port);
            System.out.println("Web-Root: '" + baseDirFile.getAbsolutePath() + "'");
            while (true) {
                try (Socket socket = server.accept()) {

                    System.out.println("READ REQUEST");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = in.readLine();
                    System.out.println(line);

                    String requestFile = line.substring("GET ".length(), line.length() - " HTTP/1.1".length());

                    System.out.println("REQUEST FILE:  '" + requestFile + "'");
                    try (BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
                        File file = new File(baseDirFile, requestFile);
                        if (file.exists()) {
                            out.write(	("HTTP/1.1 200 OK \r\n"+
                                        "Content-Type: text/plain\r\n"+
                                        "Content-Length: "+file.length()+"\r\n"+
                                        "Connection: close\r\n\r\n")
                                    .getBytes());
                            Files.copy(file.toPath(), out);
                        } else {
                            String msg = "File not found";
                            out.write(	("HTTP/1.1 404 Not Found \r\n"+
                                        "Content-Type: text/plain\r\n"+
                                        "Content-Length: "+msg.length()+"\r\n"+
                                        "Connection: close\r\n\r\n")
                                    .getBytes());
                            out.write(msg.getBytes());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
