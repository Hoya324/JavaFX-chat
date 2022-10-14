package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    Socket socket;

     public Client(Socket socket) {
         this.socket = socket;
         receive();
     }

     // 클라이언트로부터 메세지를 전달 받는 메소드.
     public void receive() {
         Runnable tread = new Runnable() {
             @Override
             public void run() {
                 try {
                     while(true) {
                         InputStream in = socket.getInputStream();
                         byte[] buffer = new byte[512];
                         int length = in.read(buffer);
                         while(length == -1) throw  new IOException(); // 입력 오류 호출
                         System.out.println("[메세지 수신 성공] "
                                 + socket.getRemoteSocketAddress() // 현재 접속한 클라이언트 주소 출력
                                 + ": " + Thread.currentThread().getName()); // 쓰레드 고유값 출력
                         String message = new String(buffer, 0, length, "UTF-8"); // 위의 내용 한글로 출력
                         // 클라이언트에게 받은 내용을 다른 클라이언트에게도 보내기.
                         for(Client client : Main.clients) {
                             client.send(message);
                         }
                     }
                 } catch (Exception e) {
                     try {
                         System.out.println("[메세지 수신 오류] "
                                 + socket.getRemoteSocketAddress()
                                 + ": " + Thread.currentThread().getName());
                     } catch (Exception e2) {
                         e2.printStackTrace(); // 에러의 발생근원지를 찾아서 단계별로 에러를 출력
                     }
                 }
             }
         };
         Main.threadPool.submit(tread); // 접손한 클라이언트의 쓰레드를 안정적으로 관리
     }

     // 클라이언트에게 메세지를 전송하는 메소드.
     public void send(String message) {
         Runnable tread = new Runnable() {
             @Override
             public void run() {
                 try {
                     OutputStream out = socket.getOutputStream();
                     byte[] buffer = message.getBytes("UTF-8");
                     out.write(buffer); // 오류가 없으면 출력
                     out.flush();
                 } catch (Exception e) {
                     try {
                         System.out.println("[메세지 송신 오류]"
                            + socket.getRemoteSocketAddress()
                            + ": " + Thread.currentThread().getName());
                         Main.clients.remove(Client.this); // 오류가 생긴 클라이언트를 서버에서 제거
                         socket.close();
                     } catch (Exception e2) {
                         e2.printStackTrace();
                     }
                 }
             }
         };
         Main.threadPool.submit(tread);
     }

}
