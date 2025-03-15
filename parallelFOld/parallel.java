package parallel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class parallel extends javax.swing.JFrame {
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private DefaultTableModel tableModel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/dbentries";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public parallel() {
        initComponents();
        setupTable();
    }

    private void setupTable() {
        String[] columnNames = {
            "EntryID", "Posted", "DatePosted", "DocNumber", "BusinessCode",
            "LocationCode", "ModuleCode", "AccountCode", "NormalBalance",
            "Amount", "Amount2", "Credit", "Debit", "FinalAmount"
        };
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(columnNames);
        result.setModel(tableModel);
        SearchParallel.addActionListener(e -> searchParallel());
    }

private void searchParallel() {
    String entryID = textSearch.getText().trim();
    if (entryID.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter an EntryID", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    long startTime = System.currentTimeMillis();
    int totalRows = 2379393;
    int batchSize = 500000;
    int numBatches = (int) Math.ceil((double) totalRows / batchSize);

    List<CompletableFuture<List<Object[]>>> tasks = new ArrayList<>();
    for (int i = 0; i < numBatches; i++) {
        final int rangeStart = i * batchSize;
        final int rangeEnd = Math.min((i + 1) * batchSize, totalRows);
        final int batchNumber = i + 1;

        tasks.add(CompletableFuture.supplyAsync(() -> {
            String threadName = Thread.currentThread().getName();
            long threadStartTime = System.currentTimeMillis();
            
            System.out.println(batchNumber + " using " + threadName);

            List<Object[]> results = fetchEntriesWithRange(entryID, rangeStart, rangeEnd);

            long threadEndTime = System.currentTimeMillis();
            long elapsedTime = threadEndTime - threadStartTime;
            
            // Print execution time immediately after searching
            System.out.println(batchNumber + " completed in " + elapsedTime + " ms using " + threadName);
            return results;
        }, executor));
    }

    // Combine results and update table
    CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
        .thenApply(v -> tasks.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList()))  // Merge results from all threads
        .thenAccept(results -> {
            SwingUtilities.invokeLater(() -> {
                updateTable(results); // Update table with search results
                long totalTime = System.currentTimeMillis() - startTime;
                Time.setText(totalTime + " ms");
                System.out.println("Total execution time: " + totalTime + " ms.");
            });
        });
}




    private List<Object[]> fetchEntriesWithRange(String partialEntryID, int rangeStart, int rangeEnd) {
        List<Object[]> results = new ArrayList<>();
        String query = "SELECT * FROM tblentry WHERE EntryID LIKE ? AND EntryID BETWEEN ? AND ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + partialEntryID + "%");
            stmt.setInt(2, rangeStart);
            stmt.setInt(3, rangeEnd);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private Object[] mapRow(ResultSet rs) throws SQLException {
        return new Object[]{
            rs.getInt("EntryID"), rs.getInt("Posted"), rs.getDate("DatePosted"),
            rs.getString("DocNumber"), rs.getString("BusinessCode"),
            rs.getString("LocationCode"), rs.getString("ModuleCode"),
            rs.getString("AccountCode"), rs.getString("NormalBalance"),
            rs.getDouble("Amount"), rs.getDouble("Amount2"),
            rs.getDouble("Credit"), rs.getDouble("Debit"),
            rs.getDouble("FinalAmount")
        };
    }

    private void updateTable(List<Object[]> entries) {
        tableModel.setRowCount(0);
        entries.forEach(tableModel::addRow);
        result.revalidate();
        result.repaint();
    }

    private void initComponents() {
        textSearch = new javax.swing.JTextField();
        SearchParallel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        result = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        Time = new javax.swing.JLabel();
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, Color.BLUE, getWidth(), getHeight(), Color.GREEN);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parallel Search");

        SearchParallel.setText("Parallel Search");
        SearchParallel.setFont(new Font("Arial", Font.BOLD, 16));
        SearchParallel.setForeground(Color.WHITE);
        SearchParallel.setBackground(new Color(0, 102, 204));

        textSearch.setFont(new Font("Arial", Font.PLAIN, 18));
        textSearch.setPreferredSize(new Dimension(350, 40));

        result.setFont(new Font("Arial", Font.PLAIN, 16));
        result.setRowHeight(30);
        jScrollPane1.setViewportView(result);

        jLabel1.setText("Time:");
        jLabel1.setFont(new Font("Arial", Font.BOLD, 18));
        jLabel1.setForeground(Color.WHITE);

        Time.setText("- ms");
        Time.setFont(new Font("Arial", Font.BOLD, 18));
        Time.setForeground(Color.WHITE);

        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(1000, 600));

        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        searchPanel.add(textSearch);
        searchPanel.add(SearchParallel);
        searchPanel.add(jLabel1);
        searchPanel.add(Time);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(jScrollPane1, BorderLayout.CENTER);

        getContentPane().add(panel);
        pack();
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new parallel().setVisible(true));
    }

    private javax.swing.JButton SearchParallel;
    private javax.swing.JLabel Time, jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable result;
    private javax.swing.JTextField textSearch;
    private JPanel panel;
}
