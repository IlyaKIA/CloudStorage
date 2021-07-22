package UsersDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DB_Handler extends DB_Config{
    Connection dbConnection;

    public Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connactionString = "jdbc:mysql://" + dbHost + ":"
                + dbPort + "/" + dbName;
        Class.forName("com.mysql.jdbc.Driver");
        dbConnection = DriverManager.getConnection(connactionString, dbUser, dbPass);
        return dbConnection;
    }
    public void signUpUser (String login, String password, String firstName, String lastName){
        String insert = "INSERT INTO " + DB_Const.USER_TABLE + "(" + DB_Const.USER_FIRSTNAME + ","
                + DB_Const.USER_LASTNAME + "," + DB_Const.USER_LOGIN + "," + DB_Const.USER_PASSWORD + ")"
                + "VALUES(?,?,?,?)";
        try {

            PreparedStatement prSt = getDbConnection().prepareStatement(insert);
            prSt.setString(1,firstName);
            prSt.setString(2, lastName);
            prSt.setString(3, login);
            prSt.setString(4, password);
            prSt.executeUpdate();
        }catch (SQLException | ClassNotFoundException SQL_Er) {
            SQL_Er.printStackTrace();
        }
    }
}
