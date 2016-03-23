import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;






import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import javax.security.auth.callback.PasswordCallback;

import org.apache.commons.collections.map.StaticBucketMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

//created by Haotian Zhu

public class SocketThread implements Runnable {  
    
	private Socket s = null;  
    private BufferedReader br = null;  
    private OutputStream os =null;
    //Database settings
    private Connection mSQL;
    private String sql;
    private Statement stmt;
    private Statement stmt1;
    private int result;
    private ResultSet resultSet;
    private JSONObject report;
    private JSONArray array=new JSONArray(); 
    private long threadID;
    private Date date;
    private boolean running=true;
    private String usernameGlobal;
    
    
    
    public SocketThread(Socket s) throws IOException {  
        this.s = s;    
        try {
        	Class.forName("com.mysql.jdbc.Driver");
        	mSQL=(Connection) java.sql.DriverManager.getConnection("jdbc:mysql://localhost/eaten?useUnicode=true&characterEncoding=GBK", "root", ""); 
        	stmt=(Statement) mSQL.createStatement();
        	stmt1=(Statement) mSQL.createStatement();
        	os = s.getOutputStream();
		} catch (Exception e) {
			// TODO: handle exception
		}
    }  
  
    
    @Override  
    public void run() {   
    	threadID=Thread.currentThread().getId();
		try
		{
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			
			String content = null;
		    //����ѭ�����ϴ�Socket�ж�ȡ�ͻ��˷��͹���������
			while(running){
				while((content = readFromClient()) != null && running )
				{
					
	                //������ͨIO����
	                JSONObject json=JSONObject.fromObject(content);
	                
	                String username="";
	                String password;
	                String name;
	                String eMail;
	                String gender;
	                String nameRest;
	                String catagory;
	                String property;
	                String address;
	                String limit;
	                String latitude;
	                String longitude;
	                String memo;
	                String time;
	                String url;
	                String teleNum;
	                int Appoint_num=0;
	                String currentTime;
	                ResultSet eachRecord;
					JSONArray member;
					JSONArray resultArray;
	                
	                switch ((int)json.get("Command")) 
	                {
	                
	                
	                
	                //case 1 is for the register
	                //return: 	1.successful: ("result", "1")
	                //			2.fail:("result", "0")
					case 1:
						username=(String)json.get("Username");
						name=(String)json.get("Name");
						password=(String)json.get("Password");
						eMail=(String)json.get("Email");
						gender=json.get("Gender").toString();
						//Q1: ����ͬ���û�  Q2�� ����   
						sql="SELECT * FROM account WHERE Username='"+username+"'";
						resultSet=stmt.executeQuery(sql);
						if(resultSet.next()){
							System.out.println("User already exist!");
							report=new JSONObject();
							report.put("result", (int)0);
							
						}else{
							sql= "INSERT INTO account (Name, Password, Username, Email, Gender,  Rate) "
									+ "VALUES('"+name+"', '"+password+"', '"+username+"', '"+eMail+"', '"+gender+"', '5') ";
							result=stmt.executeUpdate(sql);
							if(result==0){
								System.out.println("Register failed!	"+threadID+"	"+username);
								report=new JSONObject();
								report.put("result", 0);
							}else{
								System.out.println("Register successful!	"+threadID+"	"+username);
								report=new JSONObject();
								report.put("result", 1);
							}
						}
						os.flush();
						os.write((report.toString()+"\n").getBytes());
						break;
						
						
					//case 2 is for log in
					//		1. username does not exist: ("result", "-1")
					//		2. password is wrong: ("result", "0")
					//		3. successful: ("result", "1")
					case 2:
						username=(String)json.get("Username");
						password=(String)json.get("Password");
						//password="123456";
						sql="SELECT Password FROM account WHERE Username='"+username+"'";
						resultSet=stmt.executeQuery(sql);
						if(resultSet.next()){
							if(resultSet.getString(1).equals(password)){
								System.out.println("Login successful	"+threadID+"	"+username);
								report=new JSONObject();
								report.put("result", (int)1);
								if(MyServer.userList.containsKey(username)){
									MyServer.userList.get(username).close();
									MyServer.userList.remove(username);
								}
								MyServer.userList.put(username, s);
							}else{
								System.out.println("Login failed	"+threadID+"	"+username);
								report=new JSONObject();
								report.put("result", (int)0);
							}
						}else{
							System.out.println("Username does not exist!	"+threadID+"	"+username);
							report=new JSONObject();
							report.put("result", (int)-1);
						}
						os.flush();
						os.write((report.toString()+"\n").getBytes());
						//os.close();
						usernameGlobal=username;
						break;
						
						
					//case 3 is for request user's data
					//		1. username does not exist: ("result", "does't exist")
					//		2. successful: return email, name, gender,rate
					case 3:
						username=(String)json.get("Username");
						sql="SELECT * FROM account WHERE Username='"+username+"'";
						resultSet=stmt.executeQuery(sql);
						if(resultSet.next()){
							System.out.println("User information sent!	"+threadID+"	"+username);
							report=new JSONObject();
							report.put("result", (int)1 );
							report.put("Username", username);
							report.put("Email", resultSet.getString(5));
							report.put("Name", resultSet.getString(2));
							report.put("Gender", resultSet.getInt(6));
							report.put("Rate", resultSet.getInt(7));
						}else{
							System.out.println("Username does not exist!	"+threadID+"	"+username);
							report=new JSONObject();
							report.put("result", (int)0);
						}
						os.flush();
						os.write((report.toString()+"\n").getBytes());
						break;
						
						
					//case 4: set up dinner	
					//		result 1: successfully set up the dinner
					//		result 2: set up failed
					case 4:
						username=(String)json.get("Username");
						nameRest=(String)json.get("NameRest");
						catagory=(String)json.get("Catagory");
						address=(String)json.get("Address");
						limit=json.get("Limit").toString();
						latitude=(String)json.get("latitude");
						longitude=(String)json.get("longitude");
						memo=(String)json.get("memo");
						time=(String)json.get("Time");
						url=json.getString("url").toString();
						teleNum=json.getString("Telephone").toString();
						
						sql="SELECT MAX(Appoint_num) FROM appointment";
						resultSet=stmt.executeQuery(sql);
						resultSet.next();
						Appoint_num= 1+resultSet.getInt(1);
						System.out.println(username+"	"+ nameRest+"	"+ catagory+"	"+ address+"	"+ limit+"	"+latitude+"	"+ longitude+"	"+ memo+"	"+ time);
						
						sql="INSERT INTO appointment ( `Time`, `Property`, `Name`, `Category`, `Address`, `Username`, `LimitNum`, `Appoint_num`, `latitude`, `longitude`, `memo`, `url`, `Telephone`) "
								+ "VALUES( '"+time+"', 0, '"+nameRest+"', '"+catagory+"', '"+address+"', '"+username+"', "+limit+","+ Appoint_num +" , "+latitude+", "+longitude+", '"+memo+"','"+ url +"','"+teleNum+"')";
						
						result=stmt.executeUpdate(sql);
						if(result==0){
							System.out.println("Set up failed	"+threadID+"	"+username);
							report=new JSONObject();
							report.put("result", (int)0);
						}else{
							System.out.println("Register successful!	"+threadID+"	"+username);
							report=new JSONObject();
							report.put("result", (int)1);
						}
						os.flush();
						os.write((report.toString()+"\n").getBytes());
						break;
						
					//case 5 is for join a dinner
					//
					//
						
						
					case 5:
						username=json.get("Username").toString();
						Appoint_num=(int)json.get("Appoint_num");
						array=new JSONArray();
						report=new JSONObject();
						
						
						sql="SELECT Username FROM appointment WHERE Appoint_num="+Appoint_num;
						resultSet=stmt.executeQuery(sql);
						HashSet<String> userList=new HashSet<String>();
						while(resultSet.next()){
							userList.add(resultSet.getString(1));
						}
						if(userList.contains(username)){
							report.put("result", (int)-2);
							System.out.println("User has already in the list!	"+	threadID);
						}
						else{
							int appointNum=0;//means current member in the appointment
							int limitApp = 0;
							
							sql="SELECT COUNT(Appoint_num) AS CurrentNum FROM appointment "
									+ "WHERE Appoint_num='"+Appoint_num+"'";
							resultSet=stmt.executeQuery(sql);
							if(resultSet.next()){
								appointNum = resultSet.getInt(1);
								sql="SELECT LimitNum, Time FROM appointment WHERE Property=0 AND Appoint_num="+Appoint_num;
								resultSet=stmt.executeQuery(sql);
								resultSet.next();
								limitApp=resultSet.getInt(1);
								time=resultSet.getObject(2).toString();
								
								if(limitApp<=appointNum){//means the appoint is already full
									report.put("result", (int)0);
									System.out.println("Dinner is full!	"+	threadID);
								}
								else {
									sql="SELECT * FROM appointment WHERE Property=0 AND Appoint_num="+Appoint_num; 
									resultSet=stmt.executeQuery(sql);
									resultSet.next();
									sql="INSERT INTO `appointment` ( `Time`, `Property`, `Name`, `Category`, `Address`, `Username`, `LimitNum`, `Appoint_num`,`latitude`,`longitude`,  `memo`, `url`, `Telephone`) VALUES"
											+"('"+time+"', 1, '"+replacepie(resultSet.getString(4))+"', '"+resultSet.getString(5)+"', '"+resultSet.getString(6)+"', '"+username+"', "+resultSet.getInt(8)+", "+Appoint_num+", "+resultSet.getDouble(10)+","+resultSet.getDouble(11)+",'"+replacepie(resultSet.getString(12))+"', '"+resultSet.getString(13)+"', '"+resultSet.getString(14)+"')";
									
									System.out.println("Join successful!	"+	threadID);
									result=stmt.executeUpdate(sql);
									report.put("result", (int)1);
									pushNotice(username, Appoint_num);
								}
								
							}else{
								report.put("result", (int)-1);  //means unknown error
								System.out.println("Unknown error!	"+	threadID);
							}
							
						}
						array.add(report);
						os.write((array.toString()+"\n").getBytes());
						os.flush();
						break;
						
						
						
						
					//		return appointments by locations
					//		
					//
					case 6:
						latitude=json.get("latitude").toString();
						longitude=json.get("longitude").toString();
						//System.out.println(latitude+"         "+longitude);
						double lon=Double.parseDouble(longitude);
						array=new JSONArray();
						double threshold=1;//0.009009 means 1km
						
						
						sql="SELECT * FROM appointment WHERE longitude <" + (lon+threshold) + " AND longitude > "+(lon-threshold)+"AND Time>NOW() AND Property=0 ORDER BY Time ASC";
						resultSet=stmt.executeQuery(sql);
						
						int ind=0;
						
						
						
						while(resultSet.next()){
							if( Math.abs(resultSet.getDouble(10)-Double.parseDouble(latitude) )<threshold){
								JSONObject sub=new JSONObject();
								int appnum=resultSet.getInt(9);
								
								
								sql="SELECT COUNT(Appoint_num) AS CurrentNum FROM appointment "
										+ "WHERE Appoint_num='"+appnum+"'";
								eachRecord=stmt1.executeQuery(sql);
								eachRecord.next();
								nameRest = resultSet.getObject(4).toString();
								memo = resultSet.getObject(12).toString();
								
								sub.put("Time", resultSet.getObject(2).toString());
								sub.put("Name", nameRest );
								sub.put("Catagory", resultSet.getObject(5));
								sub.put("Address", resultSet.getObject(6));
								sub.put("Username", resultSet.getObject(7));
								sub.put("Limit", resultSet.getObject(8));
								sub.put("latitude", resultSet.getObject(10));
								sub.put("longitude", resultSet.getObject(11));
								sub.put("Appoint_num", appnum);
								sub.put("memo", memo );
								sub.put("Current", eachRecord.getInt(1));
								
								
								sql="SELECT Username FROM appointment WHERE Appoint_num="+resultSet.getInt(9);
								eachRecord=stmt1.executeQuery(sql);
								member=new JSONArray();
								while(eachRecord.next()){
									member.add(eachRecord.getObject(1));
								}
								sub.put("Member", member);
								sub.put("url", resultSet.getObject(13));
								sub.put("Telephone", resultSet.getString(14));
								array.add(sub);
								ind++;
							}	
						}
						System.out.println("Query successful!	"+ind+" result found!	"+threadID);
						JSONObject sub=new JSONObject();
						
//						sub.put("length", array.toString().length());
//						os.write((sub.toString()+"\r\n").getBytes());
						report=new JSONObject();
						report.put("Big", (int)1);
						os.write((report.toString()+"\r\n").getBytes());
						os.flush();
						os.write((array.toString()+"\r\n").getBytes());
						os.flush();
						
						
//						PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
//                            os)), true);
//						
//	                    out.println(array.toString()+"\r\n");
//						out.flush();
						break;
	
						
						
						
//						search the history of the user
//							
//					
					
					case 7:
						username=json.get("Username").toString();
						array=new JSONArray();
						ind=0;
						
						
						sql="SELECT * FROM appointment WHERE Username='"+username+"' AND Time < NOW() ORDER BY Time DESC";
						resultSet=stmt.executeQuery(sql);
						
						resultArray=new JSONArray();
						while(resultSet.next()){
							
							JSONObject person=new JSONObject();
							sql="SELECT Username FROM appointment WHERE Appoint_num="+resultSet.getInt(9);
							eachRecord=stmt1.executeQuery(sql);
							member=new JSONArray();
							while(eachRecord.next()){
								member.add(eachRecord.getObject(1));
							}
							sql="SELECT url FROM appointment WHERE Appoint_num="+resultSet.getInt(9)+" AND Property=0";
							eachRecord=stmt1.executeQuery(sql);
							eachRecord.next();
							person.put("url", eachRecord.getString(1));
							person.put("Member", member);
							person.put("Property", resultSet.getObject(3));
							person.put("Name", resultSet.getObject(4));
							person.put("Address", resultSet.getString(6));
							person.put("Time", resultSet.getObject(2).toString());
							resultArray.add(person);
							ind++;
						}
						System.out.println("history query successful!	"+ind+" result found!	"+threadID);
						report=new JSONObject();
						report.put("Big", (int)1);
						os.write((report.toString()+"\r\n").getBytes());
						os.flush();
						os.write((resultArray.toString()+"\r\n").getBytes());
						
						os.flush();
						
						break;
	
				
						
						
						
					case 8:
						username=json.get("Username").toString();
						array=new JSONArray();
						ind=0;
						
						sql="SELECT * FROM appointment WHERE Username='"+username+"' AND Time >NOW() ORDER BY Time ASC";
						resultSet=stmt.executeQuery(sql);
						
						resultArray=new JSONArray();
						while(resultSet.next()){
							
							JSONObject person=new JSONObject();
							sql="SELECT Username FROM appointment WHERE Appoint_num="+resultSet.getInt(9);
							eachRecord=stmt1.executeQuery(sql);
							member=new JSONArray();
							while(eachRecord.next()){
								member.add(eachRecord.getObject(1));
							}
							person.put("Member", member);
							sql="SELECT url FROM appointment WHERE Appoint_num="+resultSet.getInt(9)+" AND Property=0";
							eachRecord=stmt1.executeQuery(sql);
							eachRecord.next();
							person.put("url", eachRecord.getString(1));
							person.put("Property", resultSet.getInt(3));
							person.put("Name", resultSet.getString(4));
							person.put("Time", resultSet.getObject(2).toString());
							person.put("Memo", resultSet.getString(12));
							person.put("Address", resultSet.getString(6));
							person.put("Telephone", resultSet.getString(14));
							person.put("Appoint_num", resultSet.getInt(9));
							resultArray.add(person);
							ind++;
						}
						System.out.println("future query successful!	"+ind+" result found!	"+threadID);
						report=new JSONObject();
						report.put("Big", (int)1);
						os.write((report.toString()+"\r\n").getBytes());
						os.flush();
						os.write((resultArray.toString()+"\r\n").getBytes());
						os.flush();
						
						break;
						
						
					case 9:
						Appoint_num=json.getInt("Appoint_num");
						username=json.getString("Username");
						property=json.get("Property").toString();
						
						report=new JSONObject();
						
						if(property.equals("0")){//0 means create
							sql="DELETE FROM appointment WHERE Appoint_num="+Appoint_num+" AND Username='"+username+"'";
							result=stmt.executeUpdate(sql);
							if(result==0){
								report.put("result", (int)0);
								System.out.println("Delete failed!!");
							}else{
								sql="SELECT * FROM appointment WHERE Appoint_num="+Appoint_num;
								resultSet=stmt.executeQuery(sql);
								if(resultSet.next()){
									username=resultSet.getString(7);
									sql="UPDATE appointment SET Property=0 WHERE Appoint_num="+Appoint_num+" AND Username='"+username+"'";
									result=stmt.executeUpdate(sql);
									if(result==0){
										report.put("result", (int)0);
										System.out.println("Delete failed!!");
									}else{
										report.put("result", (int)1);
										System.out.println("Delete successfully!!");
										pushNoticeLeave(username, Appoint_num);
										pushNoticeCreater(username, Appoint_num);
									}
								
								}else{
									report.put("result", (int)1);
									System.out.println("Delete successfully!!");
									pushNoticeLeave(username, Appoint_num);
								}
							}
						}else if(property.equals("1")){
							sql="DELETE FROM appointment WHERE Appoint_num="+Appoint_num+" AND Username='"+username+"'";
							result=stmt.executeUpdate(sql);
							if(result==0){
								report.put("result", (int)0);
								System.out.println("Delete failed!!");
							}else{
								report.put("result", (int)1);
								System.out.println("Delete successfully!!");
								pushNoticeLeave(username, Appoint_num);
							}
						}
						os.write((report.toString()+"\r\n").getBytes());
						os.flush();
						
						break;
						
					case 20:
						br.close();
						os.close();
						MyServer.socketList.remove(s);
						running=false;
						MyServer.userList.remove(username);
						Thread.sleep(4000);
						System.out.println("Client closed!");
						
						
						
					default:
						break;
					}
	                
				}
			}
		}
		catch(IOException | SQLException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				br.close();
				os.close();
				MyServer.socketList.remove(s);
				MyServer.userList.remove(usernameGlobal);
				Thread.sleep(4000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			System.out.println("Client closed!");
			
		}
		System.out.println("Client ended!     Thread ID:"+threadID);
		MyServer.socketList.remove(s);
    }  
  
    
    
    
    //read the command from client
    private String readFromClient() throws IOException {  
        try  
        {  
            return br.readLine();  
        }  
        //�����׽���쳣,������Socket��Ӧ�Ŀͻ����Ѿ��ر�  
        catch(IOException e)  
        {  
            //ɾ����Socket  
        	running=false;
            MyServer.socketList.remove(s);
            os.close();
            System.out.println("Client disconnected!!");
        }  
        return null;  
    }  
    
    public static String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }
    
    
    private String replacepie(String word) {
        char guess = '\'';
        int index = word.indexOf(guess);
        while (index >= 0) {
            System.out.println(index);
            word = word.substring(0, index) + '\\' + word.substring(index, word.length());
            index = word.indexOf(guess, index + 2);
        }
        return word;
    }
    
    
    public void pushNotice(String userName, int appointNumber){
    	try {
    		System.out.println("Trying to push");
			Statement statement=(Statement) mSQL.createStatement();
			ResultSet rS;
			String SQL="SELECT * FROM appointment WHERE Appoint_num="+appointNumber+" AND Username<>'"+userName+"'";
			rS=statement.executeQuery(SQL);
			OutputStream outStr;
			JSONObject push;
			while(rS.next()){
				System.out.println("Found other users");
				if(MyServer.userList.containsKey(rS.getString(7))){
					outStr=MyServer.userList.get(rS.getString(7)).getOutputStream();
					push=new JSONObject();
					push.put("action", (int)1);
					push.put("join", userName);
					push.put("Time", rS.getObject(2).toString());
					push.put("Name", rS.getString(4));
					outStr.write((push.toString()+"\r\n").getBytes() );
					outStr.flush();
					System.out.println("Notice pushed!");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public void pushNoticeLeave(String userName, int appointNumber){
    	try {
    		System.out.println("Trying to push");
			Statement statement=(Statement) mSQL.createStatement();
			ResultSet rS;
			String SQL="SELECT * FROM appointment WHERE Appoint_num="+appointNumber;
			rS=statement.executeQuery(SQL);
			OutputStream outStr;
			JSONObject push;
			while(rS.next()){
				System.out.println("Found other users");
				if(MyServer.userList.containsKey(rS.getString(7))){
					outStr=MyServer.userList.get(rS.getString(7)).getOutputStream();
					push=new JSONObject();
					push.put("action",  (int)2);
					push.put("join", userName);
					push.put("Time", rS.getObject(2).toString());
					push.put("Name", rS.getString(4));
					outStr.write((push.toString()+"\r\n").getBytes() );
					outStr.flush();
					System.out.println("Notice pushed!");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    
    public void pushNoticeCreater(String userName, int appointNumber){
    	try {
    		System.out.println("Trying to push");
			Statement statement=(Statement) mSQL.createStatement();
			ResultSet rS;
			String SQL="SELECT * FROM appointment WHERE Appoint_num="+appointNumber+" AND Username='"+userName+"'";
			rS=statement.executeQuery(SQL);
			OutputStream outStr;
			JSONObject push;
			while(rS.next()){
				System.out.println("Found other users");
				if(MyServer.userList.containsKey(rS.getString(7))){
					outStr=MyServer.userList.get(rS.getString(7)).getOutputStream();
					push=new JSONObject();
					push.put("action",  (int)3);
					push.put("join", userName);
					push.put("Time", rS.getObject(2).toString());
					push.put("Name", rS.getString(4));
					outStr.write((push.toString()+"\r\n").getBytes() );
					outStr.flush();
					System.out.println("Notice pushed!");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    
}
