package com.dbdky.caci2;

import java.sql.*;

//����CMA��DBConnection
public class DBConnection { 
	  
	 private String sql ;
	 private ResultSet result ;
	 private PreparedStatement preStatement;
	 private Connection conn ;
	
	 public void connect(String sWhere,String sAccount,String sPWD)
	 {
		 /*@@@@@@ open finally 
		 try
		 {
			 Class.forName("com.mysql.jdbc.Driver").newInstance();
			 conn = DriverManager.getConnection(sWhere,sAccount,sPWD);
		 }catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }*/
		 //temp sql server
		 String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";  //����JDBC����
		 //String dbURL = "jdbc:sqlserver://192.168.1.11:1433; DatabaseName=MONITORDB";  //���ӷ����������ݿ�sample
		 //Connection dbConn;

		 try {
			 Class.forName(driverName);
			 conn = DriverManager.getConnection(sWhere, sAccount, sPWD);
			 System.out.println("Connection Successful!");  //������ӳɹ� ����̨���Connection Successful!
		  } catch (Exception e) {
		   e.printStackTrace();
		  }
		
	 }
	  
	 public void setPreStatement() throws SQLException
	 {
		 this.preStatement = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY); 
	 }

	 public DBConnection(String sWhere,String sAccount,String sPWD) 
	 {  
   	connect(sWhere,sAccount,sPWD);
	 }
   
	 public DBConnection()
	 {
  
	 }
   
	 public void setSql(String sql) 
	 {
		 this.sql = sql ;
	 }
   
	 public String getSql() {
		 return sql ;
	 }
   
	 public void selectSql() {
		 try {
			 result = preStatement.executeQuery() ;
		 } catch (SQLException e) {
			 e.printStackTrace();
		 }
   }
   
   public void executeSql() {
   	try {
   		preStatement.executeUpdate() ;
   	} catch (SQLException e) {
   		e.printStackTrace();
   	}
   }  
   public ResultSet getResult() {
   	return result ;
   }
}