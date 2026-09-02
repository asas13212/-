package com.ncu.common.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtil
{
    private static String url;
    private static String user;
    private static String password;

    static
    {
        try
        {
            Properties props = new Properties();

            // 优先读本地配置 db-local.properties（自己的真实密码），没有则回退到 db.properties
            InputStream in = JdbcUtil.class.getClassLoader().getResourceAsStream("db-local.properties");
            if (in == null)
            {
                in = JdbcUtil.class.getClassLoader().getResourceAsStream("db.properties");
            }
            props.load(in);

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            // 加载 MySQL 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static Connection connection;

    public static Connection getConnection() throws SQLException
    {
        if (connection == null || connection.isClosed())
        {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    // 关闭连接、语句、结果集
    public static void close(Connection conn, PreparedStatement ps, ResultSet rs)
    {
        try
        {
            if (rs != null) rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try
        {
            if (ps != null) ps.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        try
        {
            if (conn != null) conn.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // 关闭单例连接
    public static void closeConnection()
    {
        try
        {
            if (connection != null && !connection.isClosed())
            {
                connection.close();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
