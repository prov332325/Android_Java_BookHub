// package java2;

// import java.io.BufferedReader;
// import java.io.BufferedWriter;
// // import java.io.DataInputStream;
// // import java.io.DataOutputStream;
// import java.io.IOException;
// import java.io.InputStreamReader; 
// import java.io.OutputStreamWriter;
// import java.net.*;
// import java.nio.charset.StandardCharsets;
// import java.sql.Connection; 
// import java.sql.DriverManager;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// // import java.sql.SQLException;
// // import java.sql.Time;
// import java.time.LocalTime;
// import java.util.HashMap; 
// import java.sql.Statement;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;
// import com.google.gson.JsonSyntaxException;


// public class Socket_server {

//     // DB 연결 
//     private static final String URL = "jdbc:mysql://3.36.26.111:3306/book_hub";
//     private static final String USERNAME = "eunsil1011";
//     private static final String PASSWORD = "Monica6451!";

//     private static final int PORT = 9999; // 서버 포트 번호

//     // 클라이언트로부터 받은 메시지, 보내는 사람, 받는 사람
//     String login_user_emailId = null;
//     String this_user_emailID = null;
//     String message = null;
//     String message_time = null; 

//     // 서버 접속한 클라이언트들을 hash map 에 넣을 예정
//     private HashMap<String, BufferedWriter> clientMap = new HashMap<>();

//     public static void main(String[] args) {
    
//              new Socket_server().startServer();  
//     }

//     public void startServer() {

//         ServerSocket serverSocket = null;
//         Socket socket = null;

//         try {
//                // DB 연결, JDBC 드라이버 로드 
//                Class.forName("com.mysql.cj.jdbc.Driver"); 
//                System.out.println("데이터베이스 연결시도");
//                // 데이터 베이스 연결 
//                Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//                System.out.println("자바랑 데이터베이스 연결 성공");
  
//                // 연결 확인
//                  // 테스트 쿼리 실행
//             Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT VERSION()");

//             if (rs.next()) {
//                 System.out.println("Database version: " + rs.getString(1));
//             }

//             // 연결 종료
//             connection.close();

               
//             serverSocket = new ServerSocket(PORT);
//             System.out.println("서버가 시작");

//             // 서버 접속하는 클라마다 그에 대응하는 서버 소켓 생성해주기
//             while (true) {
//                 socket = serverSocket.accept();
//                 System.out.println("[" + socket.getInetAddress() + ": " + socket.getPort() + "] 에서 접속하였습니다");

//                 // 새로운 서버 스레드 생성해줌.
//                 ServerReceiver thread = new ServerReceiver(socket); // 클라이언트가 무사히 접속하면, 새로운 클라이언트용 서버 스레드 생성됨.
//                 thread.start(); // 스레드 바로 시작함. 이때 run 메소드 실행됨.

//             }
//         } catch (ClassNotFoundException | SQLException e) {
//             e.printStackTrace();
//             System.out.println("서버연결 코드 catch문 ");
//         } catch (Exception e) {
//             e.printStackTrace();
//             System.out.println("서버연결 코드 catch문 ");
//         }  
//          finally {
//             try {
//                 if (serverSocket != null) {
//                     serverSocket.close();
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }

//     } // server starter 끝

//     // 내부 클래스 - server receiver- 별도 스레드 !!!
//     // 클라이언트가 새롭게 서버와 연결되면 새로운 !! new 스레드가 생성됨 !!!!
//     // 별도로 처리됨.

//     class ServerReceiver extends Thread {
//         // 유저가 입력한 메시지 받는 메소드

//         Socket socket;
//         // DataInputStream in;
//         // DataOutputStream out;

//         BufferedReader reader;
//         BufferedWriter writer;

//         // 방 입장, 퇴장할 때 사용할 string 변수들.
//         // String client_nickname="";
//         // String room_number = "";


//         // 현재 로그인 중인 클라이언트의 이메일 아이디 
//         String clientEmail;

//         ServerReceiver(Socket socket) { // 클라이언트가 연결될때마다 새로운 스레드 생성하기. \
//             this.socket = socket;
//             try {
//                 System.out.println("ServerReceiver 들어옴 ");
//                 // in = new DataInputStream(socket.getInputStream());
//                 // out = new DataOutputStream(socket.getOutputStream());
//                 reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
//                 writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

//             } catch (IOException ie) {
//                 ie.printStackTrace();
//             }
//         } // server receiver 생성자 끝

//         // run
//         @Override
//         public void run() {
       
//             // 이 곳은 서버 리시버. 안드로이드 클라이언트로부터 메시지를 받는 리시버임. 
//             // 여기서는 

//             try {
//                 String msg;
//                 msg = reader.readLine(); // 클라이언트가 처음 접속해서 보낸 첫번째 메시지. 를 담음.
//                 clientEmail = msg;
//                 // if (msg != null) { // 처음 접속시 한번 실행되어야 하는 코드. !!!
//                 // 왜 필요한가 ?
//                 // 아무것도 입력하지 않은

//                 clientMap.put(msg, writer); // 이건 특정 클라이언트가 서버에 처음 접속했을때 한번만 호출돼야 함!
//                 // 현재 해시맵에 들어있는 유저, 새로 입장할때마다 출력하기
//                 LocalTime now = LocalTime.now();
//                 System.out.println("전체 유저 목록 !!!, 시간: " + now.toString());
//                 for (String key : clientMap.keySet()) {
//                     System.out.println("키:" + key + "값: " + clientMap.get(key));
//                 }

//                 // System.out.println("이멜아이디: " + msg + ", 버퍼writer: " + writer.toString());
//                  while ((msg = reader.readLine()) != null) { // != null) { // 클라이언트가 연결된 상태에서의 메시지를 주고 받는 while 문 !!
//                     System.out.println("메시지받음: " + msg);

//                     if (msg != null && isValidJson(msg)) {

//                         // json 파싱
//                         try { 
//                             JsonObject jsonObject = JsonParser.parseString(msg).getAsJsonObject();
//                             login_user_emailId = jsonObject.get("login_user_emailId").getAsString();
//                             this_user_emailID = jsonObject.get("this_user_emailID").getAsString();
//                             message = jsonObject.get("message").getAsString();
//                             message_time = jsonObject.get("message_time").getAsString();
                            
//                             System.out.println("로그인 사용자 이메일: " + login_user_emailId);
//                             System.out.println("대상 사용자 이메일: " + this_user_emailID); // key
//                             System.out.println("메시지: " + message);
//                             System.out.println("메시지 시간 : " + message_time);

//                         } catch (JsonSyntaxException je) {
//                             System.out.println("json 파싱 에러 !! ");
//                             je.printStackTrace();

//                         }

//                         // writer.write(message + "\n"); // 클라이언트에게 다시 보냄 !!!
//                         // writer.flush();

//                         // 여기서 메시지가 있을때에만 작업 하기
//                         // 1. 상대 클라이언트가 소켓 연결 중인 경우 = 로그인 중인 경우 = 클라이언트로 메시지 전송 writer
//                         // key = 상대 아이디 value = 상대의 writer임
//                         BufferedWriter this_user_out = clientMap.get(this_user_emailID); // 이미 map 에 저장할때 인코딩을 한 상태이기
//                                                                                          // 때문에 utf -8 안해줘도 됨
//                         System.out.println("대상 사용자의 writer 보낼준비중. . : " + this_user_out); // key

//                         // message + 시간 json 으로 만들기 
//                         JsonObject jObject = new JsonObject();
//                         jObject.addProperty("sent_message", message);
//                         jObject.addProperty("sent_time", message_time);
                        
//                         String sentToClient = jObject.toString();
//                         System.out.println("상대 클라에게 보낼메시지 json to string: " + sentToClient);

//                         if (this_user_out != null) {
//                             // 상대 클라이언트가 소켓 연결 중인 경우 클라에게 다시 보냄.
//                             try {
//                                 this_user_out.write(sentToClient + "\n");
//                                 this_user_out.flush();
//                                 System.out.println("상대 클라에게 메시지 전송 성공: " + sentToClient);
//                             } catch (IOException ie) {
//                                 ie.printStackTrace();
//                             }

//                         } else {
//                             // db 에 저장하기
//                         }

//                         // 2. 상대 클라이언트가 소켓 연결 안하고 있는 경우 = 로그아웃인 경우 = db 저장

//                     } else {
//                         System.out.println("서버로 넘어온 msg json아님, message 내용: " + msg); 
//                     }

//                 }
//                 // } // if 문 끝

//             } catch (IOException ie) {
//                 ie.printStackTrace();
//             } finally { // 소켓이 종료 된 후에 map 에서 지워줌. 
//                 // finally 는 try catch 문이 종료되어야 실행됨. 즉 소켓이 끊기거나 했을때 read line 이 null 을 반환할때 등. 
//                 try {
//                         if(clientEmail != null) {
//                     clientMap.remove(clientEmail); // 현재 접속중인 클라이언트의 이메일 아이디를 키 값으로 갖는 접속 멤버 리스트 맵이기 때문에
//                     // 이 값을 가지고 map 에서 remove 할 수 있음. 
//                     System.out.println("클라이언트 연결 종료: " + clientEmail); 

//                 } 
//                 if ( reader != null) {
//                     reader.close();
//                 } 
//                 if (writer != null) {
//                     writer.close();
//                 }
//                 if (socket != null) {
//                     socket.close();
//                 }
//                 } catch (IOException e) {
//                     e.printStackTrace();
//                 }
//             }
//         } // run 끝 

//         // db 저장 로직 

//         // json 확인 
//         private boolean isValidJson (String jsonString) {
//             try {
//                 JsonParser.parseString(jsonString).getAsJsonObject();
//                 return true;
//             } catch (JsonSyntaxException je) {
//                 je.printStackTrace();
//                 return false; 
//             }
//         }

//     } // server receiver 클래스 끝 





// }