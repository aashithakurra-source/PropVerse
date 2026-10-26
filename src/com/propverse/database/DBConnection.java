package com.propverse.database;
import java.io.*; import java.sql.*; import java.util.Properties;
public final class DBConnection {
 private static Properties config; private DBConnection() {}
 public static Connection getConnection() throws SQLException {
  if(config==null){config=new Properties();try(InputStream in=new FileInputStream("config.properties")){config.load(in);}catch(IOException e){throw new SQLException("Copy config.properties.example to config.properties and enter database credentials.",e);}}
  try{Class.forName("com.mysql.cj.jdbc.Driver");}catch(ClassNotFoundException e){throw new SQLException("MySQL Connector/J is missing from the classpath.",e);}
  return DriverManager.getConnection(config.getProperty("db.url"),config.getProperty("db.username"),config.getProperty("db.password"));
 }
}
