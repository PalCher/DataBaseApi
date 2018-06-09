package ru.geekbrains.java3.lesson2;




import javax.xml.transform.Result;
import java.sql.*;
import java.util.Scanner;

public class MainClass {
    private static Connection connection;
    private static Statement stmt;
    private static Scanner sc = new Scanner(System.in);
    private static PreparedStatement psInsert, psUpdateCostByName, psSelect, psSelectByMinMaxCost;

    public static void main(String[] args) {
        try{
        connect();
        prepareTable();
        prepareStatements();
        prepareData();
        mainLoop();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private static void mainLoop() throws SQLException{
        while(true){
            String str = sc.nextLine();
            String[] cmd = str.split(" ");
            if (cmd[0].startsWith("/")){
                switch (cmd[0]){
                    case "/помощь":
                        System.out.println("Команды:\n" +
                                "/цена имятовара - узнать цену товара по имени\n"+
                                "/сменитьцену имятовара новаяцена - изменить цену товара\n" +
                                "/товарыпоцене отсумма досумма - вывести товары в заданном ценовом диапазоне\n");
                        break;
                    case "/цена":
                        getCostByName(cmd[1]);
                        break;
                    case "/сменитьцену":
                        updateCost(cmd[1], Integer.parseInt(cmd[2]));
                        break;
                    case "/товарыпоцене":
                        printProducts(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
                        break;
                    case "/выход":
                        disconnect();
                        break;
                }
            } else System.out.println("Введите команду");

        }
    }

    private static void prepareStatements() throws SQLException{
        psInsert = connection.prepareStatement("INSERT INTO products (prodID, title, cost) VALUES (?, ?, ?)");
        psSelect = connection.prepareStatement("SELECT cost FROM products WHERE title = ?");
        psSelectByMinMaxCost = connection.prepareStatement("SELECT title FROM products WHERE cost >= ? AND cost <= ?");
        psUpdateCostByName = connection.prepareStatement("UPDATE products SET cost = ? WHERE title = ?");
    }

    private static void prepareTable() throws SQLException{
        stmt.execute("CREATE TABLE IF NOT EXISTS products(ID INTEGER PRIMARY KEY AUTOINCREMENT, ProdID INTEGER, title VARCHAR(20), cost INTEGER);");
        stmt.execute("DELETE FROM products");
    }

    private static void prepareData() throws SQLException{
        connection.setAutoCommit(false);
        for (int i = 1; i <= 10000; i++) {
            psInsert.setInt(1, i);
            psInsert.setString(2, "товар" + i);
            psInsert.setInt(3, (i * 10));
            psInsert.addBatch();
        }
        psInsert.executeBatch();
        connection.setAutoCommit(true);
    }

    private static void getCostByName(String name) throws SQLException {
        psSelect.setString(1, name);
        ResultSet rs = psSelect.executeQuery();
        boolean b = false;
        while(rs.next()){
           b = true;
            System.out.println(name + " - цена: " + rs.getInt("cost"));
        }
        if(!b) System.out.println("Такого товар нет в базе");
    }

    private static void updateCost(String name, int newCost) throws SQLException{
        psUpdateCostByName.setInt(1, newCost);
        psUpdateCostByName.setString(2, name);
        if (psUpdateCostByName.executeUpdate() == 1){
            System.out.println("Стоимость товара обновлена");
        } else{
            System.out.println("Что-то пошло не так");
        }
    }

    private static void printProducts(int minCost, int maxCost) throws SQLException {
        psSelectByMinMaxCost.setInt(1, minCost);
        psSelectByMinMaxCost.setInt(2, maxCost);
        ResultSet rs = psSelectByMinMaxCost.executeQuery();
        System.out.println("Товары по цене от " + minCost + " до " + maxCost);
        while (rs.next()){
            System.out.println(rs.getString(1));
        }
    }



    private static void connect(){
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            stmt = connection.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}