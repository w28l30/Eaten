import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//created by Xiaowen Jiang
//run this method to create a server database named eaten
public class CreateDB {
	   // JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost/";

	   //  Database credentials
	   static final String USER = "root";
	   static final String PASS = "";
	   static final String DBNAME = "eaten";
	   
	   public static void main(String[] args) {
	   Connection conn = null;
	   Statement stmt = null;
	   try{
	      //STEP 2: Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");

	      //STEP 3: Open a connection
	      System.out.println("Connecting to database...");
	      conn = DriverManager.getConnection(DB_URL, USER, PASS);

	      //STEP 4: Execute a query
	      System.out.println("Creating database...");
	      stmt = conn.createStatement();
	      
	      //create the database if not exists
	      String sql = "CREATE DATABASE IF NOT EXISTS "+DBNAME;      
	      stmt.executeUpdate(sql);
	      
	      //create the tables if not exists
	      if(conn!=null)
	      {
	    	  stmt.close();
	    	  conn.close();
	      }
	      conn = DriverManager.getConnection(DB_URL+DBNAME,USER,PASS);
	      stmt = conn.createStatement();
	      
	      //create user account table
	      sql = "CREATE TABLE IF NOT EXISTS account" +
	              "(Ind INTEGER not NULL AUTO_INCREMENT, " +
	              " Name VARCHAR(255), " + 
	              " Password VARCHAR(255), " +
	              " Username VARCHAR(255), " +
	              " Email VARCHAR(255), "+
	              " Gender INTEGER, "+
	              " Rate INTEGER, "+
	              " PRIMARY KEY ( Ind ))"; 
	      stmt.executeUpdate(sql);
	      
	      //create appointment table
	      sql = "CREATE TABLE IF NOT EXISTS appointment" +
	              "(Ind INTEGER not NULL AUTO_INCREMENT, " +
	              " Time DATETIME, " + 
	              " Property INTEGER, " +
	              " Name VARCHAR(255), " +
	              " Category VARCHAR(255), "+
	              " Address VARCHAR(255), "+
	              " Username VARCHAR(255), "+
	              " LimitNum INTEGER, " +
	              " Appoint_num INTEGER, " +
	              " latitude DOUBLE, "+
	              " longitude DOUBLE, "+
	              " memo VARCHAR(1000), "+
	              " url VARCHAR(100), "+
	              " Telephone VARCHAR(20), "+
	              " PRIMARY KEY ( Ind ))";
	      stmt.executeUpdate(sql);
	      
	      System.out.println("Database created successfully...");
	   }catch(SQLException se){
	      //Handle errors for JDBC
	      se.printStackTrace();
	   }catch(Exception e){
	      //Handle errors for Class.forName
	      e.printStackTrace();
	   }finally{
	      //finally block used to close resources
	      try{
	         if(stmt!=null)
	            stmt.close();
	      }catch(SQLException se2){
	      }// nothing we can do
	      try{
	         if(conn!=null)
	            conn.close();
	      }catch(SQLException se){
	         se.printStackTrace();
	      }//end finally try
	   }//end try
	   System.out.println("Goodbye!");
	}//end main
}
