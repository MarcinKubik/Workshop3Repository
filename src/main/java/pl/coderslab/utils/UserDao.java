package pl.coderslab.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Arrays;

public class UserDao {
    private static final String CREATE_USER_QUERY =
            "INSERT INTO users(username, email, password) VALUES (?, ?, ?)";

    private static final String READ_USER_QUERY =
            "SELECT * FROM users";

    private static final String UPDATE_USER_QUERY =
            "UPDATE users SET email =?, username = ?, password = ? WHERE id =?";

    private static final String DELETE_USER_QUERY =
            "DELETE FROM users WHERE id = ?";

    public void showAllUsers() {
        try (Connection connection = DbUtil.getConnection()) {

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id"));
                System.out.println(resultSet.getString("username"));
                System.out.println(resultSet.getString("email"));
                System.out.println(resultSet.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public User create(User user) {
        try (Connection connection = DbUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getEmail());
            statement.setString(3, hashPassword(user.getPassword()));
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                user.setId(resultSet.getInt(1));

            }

            return user;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User read(int userId) {
        try (Connection connection = DbUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(READ_USER_QUERY);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt("id") == userId) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setEmail(resultSet.getString("email"));
                    user.setUserName(resultSet.getString("username"));
                    user.setPassword(resultSet.getString("password"));
                    /*System.out.println(user.getId());
                    System.out.println(user.getEmail());
                    System.out.println(user.getUserName());
                    System.out.println(user.getPassword());*/
                    return user;
                }
                if (resultSet.getInt("id") > userId) {
                    System.out.println("Użytkownik o podanym id nie istnieje w tabeli.");
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;  // tu jeszcze potrzebny???
    }

    public void update(User user) {
        try (Connection connection = DbUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(UPDATE_USER_QUERY);
            PreparedStatement statement1 = connection.prepareStatement("SELECT * FROM users");
            ResultSet resultSet = statement1.executeQuery();
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getUserName());
            while (resultSet.next()) {
                if (resultSet.getInt("id") == user.getId()) {
                    if (user.getPassword().equals(resultSet.getString("password"))) {  //sprawdzam czy hasło zostało zmienione, jeżeli nie,
                        statement.setString(3, user.getPassword());                 // nie będzie zasolone
                        break;
                    }
                    else{
                        statement.setString(3, hashPassword(user.getPassword()));
                        break;
                    }
                }
            }
            statement.setInt(4, user.getId());
            statement.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int userId) {
        try (Connection connection = DbUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(DELETE_USER_QUERY);
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User[] findAll() {
        User[] userArray = new User[0];
        try (Connection connection = DbUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(READ_USER_QUERY);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                userArray = Arrays.copyOf(userArray, userArray.length + 1);
                userArray[userArray.length - 1] = new User();
                userArray[userArray.length - 1].setId(resultSet.getInt("id"));
                userArray[userArray.length - 1].setEmail(resultSet.getString("email"));
                userArray[userArray.length - 1].setUserName(resultSet.getString("username"));
                userArray[userArray.length - 1].setPassword(resultSet.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userArray;
    }
}
