import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchService {
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public List<Entry> fetchSimilarEntries(String partialEntryID) {
        List<Entry> entries = new ArrayList<>();
        String query = "SELECT * FROM tblentry WHERE EntryID LIKE ?";;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, partialEntryID + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                entries.add(mapResultSetToEntry(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public List<Entry> fetchExactEntry(String entryID) {
        List<Entry> entries = new ArrayList<>();
        String query = "SELECT * FROM tblentry WHERE EntryID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, entryID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                entries.add(mapResultSetToEntry(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private Entry mapResultSetToEntry(ResultSet rs) throws SQLException {
        return new Entry(
            rs.getInt("EntryID"), rs.getInt("Posted"), rs.getDate("DatePosted"),
            rs.getString("DocNumber"), rs.getString("BusinessCode"),
            rs.getString("LocationCode"), rs.getString("ModuleCode"),
            rs.getString("AccountCode"), rs.getString("NormalBalance"),
            rs.getDouble("Amount"), rs.getDouble("Amount2"),
            rs.getDouble("Credit"), rs.getDouble("Debit"),
            rs.getDouble("FinalAmount")
        );
    }
}