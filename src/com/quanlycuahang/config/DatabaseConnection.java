package com.quanlycuahang.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quan ly ket noi JDBC den SQL Server bang Windows Authentication.
 * Tu dong thu TCP/IP truoc, roi Named Pipes.
 */
public class DatabaseConnection {

    // ----------------------------------------------------------------
    // === CAU HINH — KHONG CAN SUA, TU DONG THU NHIEU CACH ===
    // ----------------------------------------------------------------
    private static final String DATABASE = "QuanLyCuaHangDienThoai";

    // 4 chuoi ket noi: 2 instance x 2 protocol
    // Thu lan luot den khi thanh cong
    private static final String[] URLS = {
        // 1) Default instance (MSSQLSERVER) qua TCP/IP
        "jdbc:sqlserver://localhost:1433;"
            + "databaseName=" + DATABASE + ";"
            + "integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=8;",

        // 2) Named instance LONGQUAN qua TCP/IP
        "jdbc:sqlserver://localhost\\LONGQUAN;"
            + "databaseName=" + DATABASE + ";"
            + "integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=8;",

        // 3) Default instance qua Named Pipes
        "jdbc:sqlserver://;serverName=localhost;"
            + "databaseName=" + DATABASE + ";"
            + "integratedSecurity=true;encrypt=true;trustServerCertificate=true;"
            + "namedPipe=true;loginTimeout=8;",

        // 4) Named instance LONGQUAN qua Named Pipes
        "jdbc:sqlserver://;serverName=localhost\\LONGQUAN;"
            + "databaseName=" + DATABASE + ";"
            + "integratedSecurity=true;encrypt=true;trustServerCertificate=true;"
            + "namedPipe=true;loginTimeout=8;",
    };

    private static final String[] URL_LABELS = {
        "TCP/IP → localhost (MSSQLSERVER)",
        "TCP/IP → localhost\\LONGQUAN",
        "Named Pipes → localhost (MSSQLSERVER)",
        "Named Pipes → localhost\\LONGQUAN",
    };
    // ----------------------------------------------------------------

    private static String connectedVia = ""; // luu lai cach ket noi thanh cong gan nhat

    private DatabaseConnection() {}

    /**
     * Tra ve mot ket noi moi.
     * Cac DAO dang dung try-with-resources nen moi thao tac phai so huu connection rieng,
     * tranh viec man hinh nay dong connection trong khi man hinh khac dang doc du lieu.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Khong tim thay JDBC Driver! Them mssql-jdbc.jar vao classpath.", e);
        }

        SQLException lastError = null;
        for (int i = 0; i < URLS.length; i++) {
            try {
                Connection connection = DriverManager.getConnection(URLS[i]);
                connectedVia = URL_LABELS[i];
                return connection;
            } catch (SQLException e) {
                lastError = e;
            }
        }
        throw new SQLException(
            "Khong the ket noi SQL Server sau khi thu tat ca cac phuong phap!\n\n"
            + "Hay lam theo:\n"
            + "1. Mo 'SQL Server Configuration Manager'\n"
            + "2. SQL Server Network Configuration\n"
            + "   → Protocols for MSSQLSERVER → Bat TCP/IP\n"
            + "   → Protocols for LONGQUAN    → Bat TCP/IP\n"
            + "3. Services → Restart SQL Server\n"
            + "4. Kiem tra database 'QuanLyCuaHangDienThoai' tren instance nao?",
            lastError
        );
    }

    public static String getConnectedVia() { return connectedVia; }

    public static void closeConnection() {
        // Khong giu connection dung chung nua. DAO tu dong dong connection bang try-with-resources.
    }

    public static boolean isConnected() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
