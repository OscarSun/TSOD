package com.lbs.tsod.taxi1.osm;

import java.sql.*;
import java.util.Properties;

/**
 * PostgreSQL source for connecting and querying a PostgreSQL databases.
 */
public class PostgresSource {
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    private Connection connection = null;

    /**
     * Creates a {@link PostgresSource} object.
     *
     * @param host     Host name of the database server.
     * @param port     Port of the database server.
     * @param database Name of the database.
     * @param user     User for accessing the database.
     * @param password Password of the user.
     */
    public PostgresSource(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    /**
     * Checks if the database connection has been established.
     *
     * @return True if database connection is established, false otherwise.
     */
    public boolean isOpen() {
        try {
            if (connection != null && connection.isValid(5)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Connects to the database.
     *
     * @throws SQLException thrown if opening database connection failed.
     */
    public void open() throws Exception {
        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            // props.setProperty("ssl","true");
            connection = DriverManager.getConnection(url, props);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new SQLException("Opening PostgreSQL connection failed: " + e.getMessage());
        }
    }

    /**
     * Closes database connection.
     *
     * @throws SQLException thrown if closing database connection failed.
     */
    public void close() throws Exception {
        try {
            connection.close();
            connection = null;
        } catch (SQLException e) {
            throw new SQLException("Closing PostgreSQL connection failed: " + e.getMessage());
        }
    }

    /**
     * Executes a query that is specified as a query string.
     *
     * @param query Query string statement.
     * @return Result of the query as {@link ResultSet} object.
     * @throws SQLException thrown if execution of query failed.
     */
    public ResultSet execute(String query) throws Exception {
        ResultSet query_result = null;

        if (!isOpen()) {
            throw new Exception("PostgreSQL connection is closed or invalid.");
        }

        try {
            Statement statement = connection.createStatement();
            statement.setFetchSize(100);
            query_result = statement.executeQuery(query);

        } catch (SQLException e) {
            throw new SQLException("Executing PostgreSQL query failed: " + e.getMessage());
        }

        return query_result;
    }
}
