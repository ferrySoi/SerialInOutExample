package nl.hhs.challenge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertIntoSQL {

    /**
     * Connect to the vb1.db database
     *
     * @return the Connection object
     */
    private Connection connect() {
        Connection conn = null;
        String driver = "com.mysql.cj.jdbc.Driver";
        // MySQL connection string, pas zonodig het pad aan:
        String connection = "jdbc:mysql://localhost:3306/vb1?serverTimezone=UTC";
        String user = "microbit";
        String password = "geheim";
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(connection, user, password);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Insert a new row into the tbl1 table
     *
     * @param tijdstip
     * @param temperatuur
     */
    public void insert(String tijdstip, float temperatuur) {
        String sql = "INSERT INTO tbl1(tijdstip, temperatuur) VALUES(?,?)";
        try {
            PreparedStatement preparedStatement = connect().prepareStatement(sql);
            preparedStatement.setString(1, tijdstip);
            preparedStatement.setFloat(2, temperatuur);
            preparedStatement.executeUpdate();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
