import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


public class PackagingServer {
	
	static String user = "root";
	static String password = "123456";
	static String url = "jdbc:mysql://localhost:3306/mydb";
	static String driver = "com.mysql.jdbc.Driver";
	static Connection con = null;
	static Statement stmt = null;
	static ResultSet rs = null;
	
	private ServerSocket server;
	public static void main(String[] args)
	{
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, user, password);
			stmt = con.createStatement();
			rs = null;
			//启动服务器的socket
			new PackagingServer().OpenServer();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void OpenServer()
	{
		try{
			server = new ServerSocket(10001);//监听10001端口
			
			Socket socket;
			while((socket = server.accept()) != null)
			{
				new Users(socket).start();
			}
		}catch(Exception e)
		{}
	}
	
	public class Users extends Thread {
		private Socket socket;
		
		public Users(Socket socket)
		{
			this.socket = socket;
		}
		public void run() {
			try{
				DataInputStream minput = new DataInputStream(socket.getInputStream());
				DataOutputStream moutput = new DataOutputStream(socket.getOutputStream());
				
				String hello;
				hello = minput.readUTF();
				
				if(hello.equals("HELLO"))
				{
					String sayHello = "HELLO\r\n";
					moutput.writeUTF(sayHello);
					boolean flag = true;
					String msg = "";
					while(flag){
						msg = minput.readUTF();
						System.out.println(msg);
						flag = decodeMsg(msg);
					}
				}
			}
			catch(Exception e){}
		}
		public boolean decodeMsg(String s)
		{
			boolean flag = true;
			
			String[] msg = s.split(" ");
			if(msg[0].equals("REG"))//注册
			{
				try {
					String sqlStr = "insert into users values (null,null,null,"+ msg[1] +","+ msg[2]+",null)";
					stmt.executeUpdate(sqlStr);
					sendResponse("1");
				} catch (SQLException e) {
					sendResponse("0");
				}
			}
			else if(msg[0].equals("LOGIN"))//登陆
			{
				try {
					String sqlStr = "select * from users";
					rs = stmt.executeQuery(sqlStr);
					while(rs.next()){
						if(rs.getString(4).equals(msg[1]))
						{
							if(rs.getString(5).equals(msg[2]))
								sendResponse("1");
						}
					}
					sendResponse("0");
				} catch (SQLException e) {
					sendResponse("0");
				}
			}
			else if(msg[0].equals("UPDATE"))//更新信息
			{
				try {
					String sqlStr = "update users set name = " + msg[3] + ",sex = " + msg[4] + ",intro = " + msg[5] + "where phone = " + msg[1];
					stmt.executeUpdate(sqlStr);
					sendResponse("1");
				} catch (SQLException e) {
					sendResponse("0");
				}
			}
			else if(msg[0].equals("ADD"))//增加订单
			{
				try {
					String sqlStr = "insert into oreders values ("+msg[3]+","+msg[4]+","+msg[5]+","+msg[6]+","+msg[1]+","+msg[2]+",null,null)";
					stmt.executeUpdate(sqlStr);
					sendResponse("1");
				} catch (SQLException e) {
					sendResponse("0");
				}
			}
			else if(msg[0].equals("UPDATEORDER"))//更新订单
			{
				try {
					String sqlStr = "update order set bphone = " + msg[3] + ",bname = " + msg[4] + "where phone = " + msg[1] + ",foodname = " + msg[2];
					stmt.executeUpdate(sqlStr);
					sendResponse("1");
				} catch (SQLException e) {
					sendResponse("0");
				}
			}
			else if(msg[0].equals("GET"))//请求订单
			{
				try {
					String sqlStr = "select * from order";
					rs = stmt.executeQuery(sqlStr);
					String str = "ORDER";
					while(rs.next()){
						str += " " + rs.getString(0);
						str += " " + rs.getString(1);
						str += " " + rs.getString(2);
						str += " " + rs.getString(3);
						str += " " + rs.getString(4);
						str += " " + rs.getString(5);
						str += " " + rs.getString(6);
						str += " " + rs.getString(7);
						str += " " + rs.getString(8);
						str += "\r\n";
					}
					sendResponse(str);
				} catch (SQLException e) {
					sendResponse("0");
				}
			}
			else if(msg[0].equals("LOGOUT"))//退出
			{
				flag = false;
				sendResponse("1");
			}
			
			return flag;
		}
		public void sendResponse(String response)
		{
			try {
				DataOutputStream moutput = new DataOutputStream(socket.getOutputStream());
				String sayresp = "";
				sayresp += "RESPONSE";
				sayresp += response;
				sayresp += "\r\n";
				
				moutput.writeUTF(sayresp);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
