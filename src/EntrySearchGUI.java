import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EntrySearchGUI extends JFrame {
    private JTextField entryIDField;
    private JButton searchButton;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel processTimeLabel;
    private final SearchService searchService = new SearchService();

    public EntrySearchGUI() {
        setTitle("Entry Search System - Parallel Processing");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setupGUI();
    }

    private void setupGUI() {
        // Full-screen gradient panel
        GradientPanel gradientPanel = new GradientPanel();
        gradientPanel.setLayout(new BorderLayout());
        add(gradientPanel, BorderLayout.CENTER);

        // Input panel at the top
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Enter EntryID:"));
        
        entryIDField = new JTextField(20);
        inputPanel.add(entryIDField);

        searchButton = new JButton("Search");
        inputPanel.add(searchButton);

        processTimeLabel = new JLabel("Process Time: - ms");
        inputPanel.add(processTimeLabel);
        gradientPanel.add(inputPanel, BorderLayout.NORTH);

        // Table with scroll pane (fills window)
        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);
        resultTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultTable.setRowHeight(20);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        gradientPanel.add(scrollPane, BorderLayout.CENTER);

        // Column styling
        JTableHeader header = resultTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 12));

        // Column names
        String[] columnNames = {
            "EntryID", "Posted", "DatePosted", "DocNumber", "BusinessCode",
            "LocationCode", "ModuleCode", "AccountCode", "NormalBalance",
            "Amount", "Amount2", "Credit", "Debit", "FinalAmount"
        };
        tableModel.setColumnIdentifiers(columnNames);

        // Event listeners
        searchButton.addActionListener(e -> searchExactEntry());
        entryIDField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { liveSearch(); }
            public void removeUpdate(DocumentEvent e) { liveSearch(); }
            public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });
    }

    private void liveSearch() {
        String partialEntryID = entryIDField.getText().trim();
        if (partialEntryID.isEmpty()) {
            tableModel.setRowCount(0);
            return;
        }

        CompletableFuture.supplyAsync(() -> searchService.fetchSimilarEntries(partialEntryID))
            .thenAccept(entries -> SwingUtilities.invokeLater(() -> updateTable(entries)));
    }

    private void searchExactEntry() {
        String entryIDText = entryIDField.getText().trim();
        if (entryIDText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an EntryID", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long startTime = System.currentTimeMillis();
        CompletableFuture.supplyAsync(() -> searchService.fetchExactEntry(entryIDText))
            .thenAccept(entries -> SwingUtilities.invokeLater(() -> {
                updateTable(entries);
                processTimeLabel.setText("Process Time: " + (System.currentTimeMillis() - startTime) + " ms");
            }));
    }

    private void updateTable(List<Entry> entries) {
        tableModel.setRowCount(0);
        entries.forEach(entry -> tableModel.addRow(entry.toObjectArray()));
        resultTable.revalidate();
        resultTable.repaint();
    }
}
