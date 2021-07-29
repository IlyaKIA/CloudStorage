package UsersDB;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class DB_Handler extends DB_Config {
    Connection dbConnection;

    public Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connactionString = "jdbc:mysql://" + dbHost + ":"
                + dbPort + "/" + dbName;
        Class.forName("com.mysql.cj.jdbc.Driver");
        dbConnection = DriverManager.getConnection(connactionString, dbUser, dbPass);
        return dbConnection;
    }
    public void signUpUser (String login, String password, String firstName, String lastName) throws SQLException {
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
        }catch (ClassNotFoundException SQL_Er) {
            log.debug("Error: {}", SQL_Er.getClass());
        }
    }
    public ResultSet getUser (User user) {
        ResultSet resSet = null;
        String select = "SELECT * FROM " + DB_Const.USER_TABLE + " WHERE " +
                DB_Const.USER_LOGIN + "=? AND " + DB_Const.USER_PASSWORD + "=?";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            prSt.setString(1, user.getLogin());
            prSt.setString(2, user.getPassword());
            resSet = prSt.executeQuery();
        }catch (SQLException | ClassNotFoundException SQL_Er) {
            if (SQL_Er.getClass().equals(java.sql.SQLIntegrityConstraintViolationException.class)) log.debug("Error: {}", "User already exists");
            else log.debug("Error: {}", SQL_Er.getClass());
        }
        return resSet;
    }
}
