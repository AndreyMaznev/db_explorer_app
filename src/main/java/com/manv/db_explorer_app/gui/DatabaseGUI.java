package com.manv.db_explorer_app.gui;

import com.manv.db_explorer_app.service.DatabaseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;



public class DatabaseGUI extends JFrame {

    private final DatabaseService databaseService;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private List<Map<String, Object>> fullData; // Все данные из таблицы
    private int currentPage = 0; // Текущая страница
    private int pageSize = 10; // Размер страницы
    private JComboBox<String> columnComboBox;
    private JComboBox<String> tableComboBox; // Выпадающий список для выбора таблицы


    public DatabaseGUI(DatabaseService databaseService) {
        this.databaseService = databaseService;
        setTitle("Database Viewer");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        tableComboBox = new JComboBox<>();
        try {
            List<String> tableNames = databaseService.getTableNames();
            for (String tableName : tableNames) {
                tableComboBox.addItem(tableName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при получении списка таблиц: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }


        JComboBox<String> tableComboBox = new JComboBox<>();
        JButton loadButton = new JButton("Load Data");
        JTextField filterField = new JTextField(20);
        JButton filterButton = new JButton("Filter");

        columnComboBox = new JComboBox<>();

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            filterField.setText(""); // Очищаем поле фильтра
            applyFilter("", (String) columnComboBox.getSelectedItem()); // Применяем пустой фильтр
        });

        // Создаем таблицу с пустой моделью
        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        // Кнопки пагинации
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");

        try {
            List<String> tableNames = databaseService.getTableNames();
            for (String tableName : tableNames) {
                tableComboBox.addItem(tableName);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при получении списка таблиц: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        loadButton.addActionListener(e -> loadData(tableComboBox.getSelectedItem().toString()));
        filterButton.addActionListener(e -> {
            String selectedColumn = (String) columnComboBox.getSelectedItem();
            applyFilter(filterField.getText(), selectedColumn);
        });
        prevButton.addActionListener(e -> navigatePage(-1));
        nextButton.addActionListener(e -> navigatePage(1));

        JPanel topPanel = new JPanel();
        topPanel.add(tableComboBox);
        topPanel.add(loadButton);
        topPanel.add(new JLabel("Filter by Column:"));
        topPanel.add(columnComboBox);
        topPanel.add(new JLabel("Value:"));
        topPanel.add(filterField);
        topPanel.add(filterButton);

        JPanel paginationPanel = new JPanel();
        paginationPanel.add(prevButton);
        paginationPanel.add(nextButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(paginationPanel, BorderLayout.SOUTH);


        add(panel);
    }

    private void loadData(String tableName) {
        try {
            fullData = databaseService.getDataFromTable(tableName);

            if (fullData.isEmpty()) {
                JOptionPane.showMessageDialog(this, "The table is empty", "Information", JOptionPane.INFORMATION_MESSAGE);
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);
                columnComboBox.removeAllItems(); // Очищаем выпадающий список
                return;
            }

            // Очищаем предыдущие данные
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            columnComboBox.removeAllItems(); // Очищаем старые значения

            // Получаем заголовки столбцов
            Map<String, Object> firstRow = fullData.get(0);
            for (String columnName : firstRow.keySet()) {
                tableModel.addColumn(columnName);
                columnComboBox.addItem(columnName); // Добавляем столбец в выпадающий список
            }

            // Заполняем таблицу данными
            currentPage = 0;
            updateTableData();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTableData() {
        tableModel.setRowCount(0);
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullData.size());

        for (int i = start; i < end; i++) {
            Map<String, Object> row = fullData.get(i);
            tableModel.addRow(row.values().toArray());
        }
    }

    private void applyFilter(String filterText, String columnName) {
        if (filterText == null || filterText.trim().isEmpty()) {
            // Если фильтр пустой, загружаем все данные заново
            String selectedTable = (String) tableComboBox.getSelectedItem();
            fullData = databaseService.getDataFromTable(selectedTable);
        } else {
            // Очищаем пробелы и преобразуем текст в нижний регистр
            String normalizedFilterText = filterText.trim().toLowerCase();

            // Фильтруем данные по выбранному столбцу
            fullData.removeIf(row -> {
                Object columnValue = row.get(columnName); // Получаем значение из выбранного столбца
                if (columnValue == null) {
                    return true; // Удаляем строки, где значение равно null
                }

                // Преобразуем значение в строку и нормализуем его
                String normalizedColumnValue = columnValue.toString().trim().toLowerCase();

                // Сравниваем значения
                return !normalizedColumnValue.matches(".*" + normalizedFilterText + ".*");
            });
        }

        currentPage = 0; // Возвращаемся на первую страницу
        updateTableData(); // Обновляем отображение данных
    }




    private void navigatePage(int direction) {
        int totalPages = (int) Math.ceil((double) fullData.size() / pageSize);

        if (direction == -1 && currentPage > 0) {
            currentPage--;
        } else if (direction == 1 && currentPage < totalPages - 1) {
            currentPage++;
        }

        updateTableData();
    }
}