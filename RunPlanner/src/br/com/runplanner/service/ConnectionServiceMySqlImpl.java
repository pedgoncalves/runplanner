package br.com.runplanner.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionServiceMySqlImpl implements ConnectionService {
	private final String DRIVER_NAME = "database.driver";
	private final String URL = "database.url";
	private final String USERNAME = "database.username";
	private final String PASSWORD= "database.password";

	public Connection getConnection() {
		Connection connection = null;

		try {
			Properties props = new Properties();  
			InputStream in = getClass().getClassLoader().getResourceAsStream("/application.properties");   
			props.load(in);  
			in.close();     
			
			String url = props.getProperty(URL);
			String driver = props.getProperty(DRIVER_NAME);
			String user = props.getProperty(USERNAME);
			String pass = props.getProperty(PASSWORD);
			
			// Load the JDBC driver
			Class.forName(driver);

			connection = DriverManager.getConnection(url, user, pass);
		} catch (ClassNotFoundException e) {
			// Could not find the database driver
			e.printStackTrace();
		} catch (SQLException e) {
			// Could not connect to the database
			e.printStackTrace();
		} catch (IOException e) {
			// Could not connect to the database
			e.printStackTrace();
		}

		return connection;
	}

}
