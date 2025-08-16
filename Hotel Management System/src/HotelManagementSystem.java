import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HotelManagementSystem extends JFrame {
    // ----- DB Config -----
    private static final String DB_URL = "jdbc:mysql://localhost:3310/hotel_db?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    private Connection connection;

    // UI components
    private JTextField guestNameField;
    private JComboBox<Integer> roomCombo;
    private JTextField contactField;
    private JSpinner dateSpinner;        // Check-in
    private JSpinner checkOutSpinner;    // Checkout
    private JTextField idField;

    private JTable reservationsTable;
    private DefaultTableModel tableModel;

    private JButton reserveBtn, updateBtn, deleteBtn, getRoomBtn;
    private JButton checkoutBtn, showAvailableRoomsBtn; // New
    private JComboBox<String> viewSelector;             // New (Current vs All)

    private final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");

    public HotelManagementSystem() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            showErrorAndExit("Database connection failed: " + e.getMessage());
            return;
        }

        initUI();
        loadRoomsIntoCombo();
        loadReservationsToTable(); // Will respect selector if already built
    }

    private void initUI() {
        setTitle("Hotel Reservation System");
        setSize(1080, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(245, 247, 250));
        setContentPane(root);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(52, 152, 219));
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Hotel Reservation Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        // Right side: view selector (Current vs All)
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);
        JLabel viewLbl = new JLabel("View:");
        viewLbl.setForeground(new Color(230, 240, 255));
        viewLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewSelector = new JComboBox<>(new String[]{"Current Reservations", "All Reservations"});
        viewSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewSelector.addActionListener(e -> loadReservationsToTable());

        rightHeader.add(viewLbl);
        rightHeader.add(viewSelector);

        header.add(title, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        JLabel sub = new JLabel("Manage bookings — Add • View • Update • Delete • Checkout");
        sub.setForeground(new Color(230, 240, 255));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        header.add(sub, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);

        // Split: left form, right table
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createFormPanel(), createTablePanel());
        split.setDividerLocation(420);
        split.setOneTouchExpandable(true);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("Tip: Select a row to auto-fill fields for update/delete/checkout. 'Current' shows ACTIVE reservations only.");
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.setForeground(new Color(99, 110, 114));
        JPanel footP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footP.setBackground(new Color(245, 247, 250));
        footP.add(footer);
        root.add(footP, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createFormPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(10, 10));
        p.setBackground(new Color(245, 247, 250));
        p.setBorder(new CompoundBorder(new LineBorder(new Color(220, 224, 230), 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel formTitle = new JLabel("Reservation Controls");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(new Color(33, 37, 41));
        p.add(formTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // Guest Name
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        grid.add(new JLabel("Guest Name:"), gbc);
        guestNameField = new JTextField();
        guestNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        grid.add(guestNameField, gbc);

        // Room combo
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        grid.add(new JLabel("Room Number:"), gbc);
        roomCombo = new JComboBox<>();
        roomCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        grid.add(roomCombo, gbc);

        // Contact
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        grid.add(new JLabel("Contact Number:"), gbc);
        contactField = new JTextField();
        contactField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        grid.add(contactField, gbc);

        // Check-in Date
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        grid.add(new JLabel("Check-in Date:"), gbc);
        dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor inEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(inEditor);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        grid.add(dateSpinner, gbc);

        // Checkout Date (NEW)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        grid.add(new JLabel("Checkout Date:"), gbc);
        checkOutSpinner = new JSpinner(new SpinnerDateModel(new Date(System.currentTimeMillis() + 24L*60*60*1000), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor coEditor = new JSpinner.DateEditor(checkOutSpinner, "yyyy-MM-dd");
        checkOutSpinner.setEditor(coEditor);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        grid.add(checkOutSpinner, gbc);

        // Reservation ID
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        grid.add(new JLabel("Reservation ID (for update/delete/checkout):"), gbc);
        idField = new JTextField();
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        grid.add(idField, gbc);

        p.add(grid, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttons = new JPanel(new GridLayout(3, 2, 12, 12));
        buttons.setBackground(new Color(245, 247, 250));
        reserveBtn = styledButton("Reserve", new Color(46, 204, 113));
        updateBtn  = styledButton("Update", new Color(241, 196, 15));
        deleteBtn  = styledButton("Delete", new Color(231, 76, 60));
        getRoomBtn = styledButton("Get Room No.", new Color(149, 165, 166));
        checkoutBtn = styledButton("Checkout", new Color(155, 89, 182));               // New
        showAvailableRoomsBtn = styledButton("Show Available Rooms", new Color(52, 73, 94)); // New

        buttons.add(reserveBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(getRoomBtn);
        buttons.add(checkoutBtn);
        buttons.add(showAvailableRoomsBtn);

        p.add(buttons, BorderLayout.SOUTH);

        // Listeners
        reserveBtn.addActionListener(e -> reserveRoom());
        updateBtn.addActionListener(e -> updateReservation());
        deleteBtn.addActionListener(e -> deleteReservation());
        getRoomBtn.addActionListener(e -> getRoomNumber());
        checkoutBtn.addActionListener(e -> checkoutReservation());
        showAvailableRoomsBtn.addActionListener(e -> showAvailableRooms());

        return p;
    }

    private JPanel createTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(255, 255, 255));
        p.setBorder(new EmptyBorder(6, 6, 6, 6));

        JLabel ttitle = new JLabel("Reservations");
        ttitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        ttitle.setBorder(new EmptyBorder(8, 8, 8, 8));
        p.add(ttitle, BorderLayout.NORTH);

        // Table model includes checkout_date & status
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Guest Name", "Room", "Contact", "Check-in", "Checkout", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        reservationsTable = new JTable(tableModel);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationsTable.getTableHeader().setReorderingAllowed(false);
        reservationsTable.setRowHeight(24);
        reservationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reservationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        reservationsTable.getTableHeader().setBackground(new Color(236, 240, 241));
        reservationsTable.setShowGrid(true);

        reservationsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = reservationsTable.getSelectedRow();
                if (r >= 0) {
                    idField.setText(String.valueOf(tableModel.getValueAt(r, 0)));
                    guestNameField.setText(String.valueOf(tableModel.getValueAt(r, 1)));
                    Object roomObj = tableModel.getValueAt(r, 2);
                    if (roomObj instanceof Integer) {
                        roomCombo.setSelectedItem((Integer) roomObj);
                    } else {
                        try { roomCombo.setSelectedItem(Integer.parseInt(roomObj.toString())); } catch (Exception ignore) {}
                    }
                    contactField.setText(String.valueOf(tableModel.getValueAt(r, 3)));
                    String inStr = String.valueOf(tableModel.getValueAt(r, 4));
                    String outStr = String.valueOf(tableModel.getValueAt(r, 5));
                    try {
                        // Try full timestamp first
                        Date inD = java.sql.Timestamp.valueOf(inStr);
                        dateSpinner.setValue(inD);
                    } catch (Exception ex1) {
                        try {
                            Date inD = new SimpleDateFormat("yyyy-MM-dd").parse(inStr);
                            dateSpinner.setValue(inD);
                        } catch (Exception ignore) {}
                    }
                    try {
                        Date outD = java.sql.Timestamp.valueOf(outStr);
                        checkOutSpinner.setValue(outD);
                    } catch (Exception ex2) {
                        try {
                            Date outD = new SimpleDateFormat("yyyy-MM-dd").parse(outStr);
                            checkOutSpinner.setValue(outD);
                        } catch (Exception ignore) {}
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(reservationsTable);
        scroll.setBorder(new CompoundBorder(new LineBorder(new Color(220, 224, 230), 1, true),
                new EmptyBorder(8, 8, 8, 8)));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private JButton styledButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        return b;
    }

    // ---------------- DB Operations ----------------

    private void loadRoomsIntoCombo() {
        SwingUtilities.invokeLater(() -> {
            roomCombo.removeAllItems();
            String sql = "SELECT room_number FROM rooms ORDER BY room_number";
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                boolean any = false;
                while (rs.next()) {
                    roomCombo.addItem(rs.getInt("room_number"));
                    any = true;
                }
                if (!any) {
                    JOptionPane.showMessageDialog(this,
                            "No rooms found in 'rooms' table. Please insert rooms.",
                            "No rooms", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                showError("Error loading rooms: " + e.getMessage());
            }
        });
    }

    private void reserveRoom() {
        String guest = guestNameField.getText().trim();
        Integer room = (Integer) roomCombo.getSelectedItem();
        String contact = contactField.getText().trim();
        Date checkIn = (Date) dateSpinner.getValue();
        Date checkOut = (Date) checkOutSpinner.getValue();

        String inDateOnly = dateOnlyFormat.format(checkIn);
        String outDateOnly = dateOnlyFormat.format(checkOut);

        if (guest.isEmpty() || contact.isEmpty() || room == null) {
            showError("Please fill Guest name, contact and choose a room.");
            return;
        }
        if (!checkOut.after(checkIn)) {
            showError("Checkout date must be after Check-in date.");
            return;
        }

        // Check overlapping booking for the room (only against ACTIVE reservations)
        String overlapSql =
                "SELECT reservation_id FROM reservations " +
                "WHERE room_number = ? AND status = 'ACTIVE' " +
                "AND (checkout_date > ? AND reservation_date < ?)"; // overlap condition
        try (PreparedStatement ps = connection.prepareStatement(overlapSql)) {
            ps.setTimestamp(1, new java.sql.Timestamp(checkIn.getTime())); // we will reset params right below, fix order:
        } catch (SQLException ignore) {}

        try (PreparedStatement ps = connection.prepareStatement(overlapSql)) {
            ps.setInt(1, room);
            ps.setTimestamp(2, new java.sql.Timestamp(checkIn.getTime()));
            ps.setTimestamp(3, new java.sql.Timestamp(checkOut.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    showError("Room " + room + " is not available between " + inDateOnly + " and " + outDateOnly + ".");
                    return;
                }
            }
        } catch (SQLException e) {
            showError("Error checking availability: " + e.getMessage());
            return;
        }

        String insert = "INSERT INTO reservations (guest_name, room_number, contact_number, reservation_date, checkout_date, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, guest);
            ps.setInt(2, room);
            ps.setString(3, contact);
            ps.setTimestamp(4, new java.sql.Timestamp(checkIn.getTime()));
            ps.setTimestamp(5, new java.sql.Timestamp(checkOut.getTime()));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Reservation successful for room " + room + " from " + inDateOnly + " to " + outDateOnly + "!");
                clearForm();
                loadReservationsToTable();
            } else {
                showError("Failed to save reservation.");
            }
        } catch (SQLException e) {
            showError("Error creating reservation: " + e.getMessage());
        }
    }

    private void loadReservationsToTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);

            boolean showCurrent = viewSelector != null && "Current Reservations".equals(viewSelector.getSelectedItem());
            String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date, checkout_date, status " +
                         "FROM reservations ";
            if (showCurrent) {
                sql += "WHERE status = 'ACTIVE' ";
            }
            sql += "ORDER BY reservation_date DESC";

            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt("reservation_id");
                    String guest = rs.getString("guest_name");
                    int room = rs.getInt("room_number");
                    String contact = rs.getString("contact_number");
                    Timestamp inTs = rs.getTimestamp("reservation_date");
                    Timestamp outTs = rs.getTimestamp("checkout_date");
                    String status = rs.getString("status");

                    String inStr  = (inTs  != null) ? inTs.toString().replace(".0", "") : "";
                    String outStr = (outTs != null) ? outTs.toString().replace(".0", "") : "";
                    tableModel.addRow(new Object[]{id, guest, room, contact, inStr, outStr, status});
                }
            } catch (SQLException e) {
                showError("Error loading reservations: " + e.getMessage());
            }
        });
    }

    private void getRoomNumber() {
        String idTxt = idField.getText().trim();
        if (idTxt.isEmpty()) {
            showError("Enter Reservation ID first.");
            return;
        }
        try {
            int id = Integer.parseInt(idTxt);
            String sql = "SELECT room_number, guest_name, reservation_date, checkout_date FROM reservations WHERE reservation_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int room = rs.getInt("room_number");
                        String guest = rs.getString("guest_name");
                        Timestamp inTs = rs.getTimestamp("reservation_date");
                        Timestamp outTs = rs.getTimestamp("checkout_date");
                        String inStr = (inTs != null) ? inTs.toString().replace(".0", "") : "";
                        String outStr = (outTs != null) ? outTs.toString().replace(".0", "") : "";
                        JOptionPane.showMessageDialog(this,
                                "Reservation ID: " + id +
                                "\nGuest: " + guest +
                                "\nRoom: " + room +
                                "\nCheck-in: " + inStr +
                                "\nCheckout: " + outStr);
                    } else {
                        showError("No reservation found for ID " + id);
                    }
                }
            }
        } catch (NumberFormatException nfe) {
            showError("Invalid Reservation ID.");
        } catch (SQLException e) {
            showError("Error fetching room: " + e.getMessage());
        }
    }

    private void updateReservation() {
        String idTxt = idField.getText().trim();
        if (idTxt.isEmpty()) {
            showError("Enter Reservation ID to update.");
            return;
        }
        try {
            int id = Integer.parseInt(idTxt);
            String guest = guestNameField.getText().trim();
            Integer room = (Integer) roomCombo.getSelectedItem();
            String contact = contactField.getText().trim();
            Date checkIn = (Date) dateSpinner.getValue();
            Date checkOut = (Date) checkOutSpinner.getValue();

            if (guest.isEmpty() || contact.isEmpty() || room == null) {
                showError("Please fill Guest name, contact and choose a room.");
                return;
            }
            if (!checkOut.after(checkIn)) {
                showError("Checkout date must be after Check-in date.");
                return;
            }

            // Prevent overlapping with other ACTIVE reservations
            String checkSql =
                    "SELECT reservation_id FROM reservations " +
                    "WHERE room_number = ? AND status = 'ACTIVE' AND reservation_id <> ? " +
                    "AND (checkout_date > ? AND reservation_date < ?)";
            try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
                ps.setInt(1, room);
                ps.setInt(2, id);
                ps.setTimestamp(3, new java.sql.Timestamp(checkIn.getTime()));
                ps.setTimestamp(4, new java.sql.Timestamp(checkOut.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        showError("Room " + room + " is already booked in the given date range.");
                        return;
                    }
                }
            }

            String sql = "UPDATE reservations SET guest_name = ?, room_number = ?, contact_number = ?, reservation_date = ?, checkout_date = ? WHERE reservation_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, guest);
                ps.setInt(2, room);
                ps.setString(3, contact);
                ps.setTimestamp(4, new java.sql.Timestamp(checkIn.getTime()));
                ps.setTimestamp(5, new java.sql.Timestamp(checkOut.getTime()));
                ps.setInt(6, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Reservation updated.");
                    clearForm();
                    loadReservationsToTable();
                } else {
                    showError("No reservation found with ID " + id);
                }
            }
        } catch (NumberFormatException nfe) {
            showError("Invalid Reservation ID.");
        } catch (SQLException e) {
            showError("Error updating reservation: " + e.getMessage());
        }
    }

    private void deleteReservation() {
        String idTxt = idField.getText().trim();
        if (idTxt.isEmpty()) {
            showError("Enter Reservation ID to delete.");
            return;
        }
        try {
            int id = Integer.parseInt(idTxt);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete reservation " + id + " ? This cannot be undone.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            String sql = "DELETE FROM reservations WHERE reservation_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Reservation deleted.");
                    clearForm();
                    loadReservationsToTable();
                } else {
                    showError("No reservation found with ID " + id);
                }
            }
        } catch (NumberFormatException nfe) {
            showError("Invalid Reservation ID.");
        } catch (SQLException e) {
            showError("Error deleting reservation: " + e.getMessage());
        }
    }

    // ---------------- New Features ----------------

    // 1) Show available rooms (between selected check-in & checkout), with type and price
    private void showAvailableRooms() {
        Date checkIn = (Date) dateSpinner.getValue();
        Date checkOut = (Date) checkOutSpinner.getValue();

        if (!checkOut.after(checkIn)) {
            showError("Checkout date must be after check-in date.");
            return;
        }

        String sql =
                "SELECT r.room_number, r.room_type, r.price_per_night " +
                "FROM rooms r " +
                "WHERE r.room_number NOT IN ( " +
                "  SELECT room_number FROM reservations " +
                "  WHERE status = 'ACTIVE' AND (checkout_date > ? AND reservation_date < ?) " +
                ") " +
                "ORDER BY r.room_number";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, new java.sql.Timestamp(checkIn.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(checkOut.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder("Available Rooms (")
                        .append(dateOnlyFormat.format(checkIn))
                        .append(" to ")
                        .append(dateOnlyFormat.format(checkOut))
                        .append(")\n\n");
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    int roomNo = rs.getInt("room_number");
                    String type = rs.getString("room_type");
                    double price = rs.getDouble("price_per_night");
                    sb.append("Room: ").append(roomNo)
                      .append(" | Type: ").append(type)
                      .append(" | Price/Night: ₹").append(String.format("%.2f", price))
                      .append("\n");
                }
                if (!any) sb.append("(No rooms available for the selected dates)");
                JOptionPane.showMessageDialog(this, sb.toString(), "Available Rooms", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            showError("Error fetching available rooms: " + e.getMessage());
        }
    }

    // 2) Checkout with bill popup (18% GST) and mark as CHECKED_OUT
    private void checkoutReservation() {
        String idTxt = idField.getText().trim();
        if (idTxt.isEmpty()) {
            showError("Enter Reservation ID to checkout.");
            return;
        }
        try {
            int id = Integer.parseInt(idTxt);

            String sql =
                    "SELECT r.guest_name, r.room_number, r.contact_number, r.reservation_date, r.checkout_date, r.status, rm.price_per_night " +
                    "FROM reservations r " +
                    "JOIN rooms rm ON r.room_number = rm.room_number " +
                    "WHERE r.reservation_id = ?";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        showError("No reservation found for ID " + id);
                        return;
                    }
                    String status = rs.getString("status");
                    if (!"ACTIVE".equalsIgnoreCase(status)) {
                        showError("Reservation " + id + " is not ACTIVE (current status: " + status + ").");
                        return;
                    }

                    String guest = rs.getString("guest_name");
                    String contact = rs.getString("contact_number");
                    int room = rs.getInt("room_number");
                    Timestamp inTs = rs.getTimestamp("reservation_date");
                    Timestamp outTs = rs.getTimestamp("checkout_date");
                    double price = rs.getDouble("price_per_night");

                    if (inTs == null || outTs == null || !outTs.after(inTs)) {
                        showError("Invalid check-in/checkout dates for this reservation.");
                        return;
                    }

                    long nights = computeNights(inTs, outTs);
                    if (nights <= 0) nights = 1;

                    double subtotal = price * nights;
                    double gst = subtotal * 0.18;
                    double total = subtotal + gst;

                    String bill =
                            "Checkout Summary\n" +
                            "----------------\n" +
                            "Reservation ID : " + id + "\n" +
                            "Guest          : " + guest + "\n" +
                            "Contact        : " + contact + "\n" +
                            "Room           : " + room + "\n" +
                            "Check-in       : " + inTs.toString().replace(".0", "") + "\n" +
                            "Checkout       : " + outTs.toString().replace(".0", "") + "\n" +
                            "Nights         : " + nights + "\n" +
                            "Price/Night    : ₹" + String.format("%.2f", price) + "\n" +
                            "Subtotal       : ₹" + String.format("%.2f", subtotal) + "\n" +
                            "GST (18%)      : ₹" + String.format("%.2f", gst) + "\n" +
                            "Grand Total    : ₹" + String.format("%.2f", total);

                    int confirm = JOptionPane.showConfirmDialog(this, bill + "\n\nConfirm checkout?",
                            "Checkout", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try (PreparedStatement upd = connection.prepareStatement(
                                "UPDATE reservations SET status = 'CHECKED_OUT' WHERE reservation_id = ?")) {
                            upd.setInt(1, id);
                            upd.executeUpdate();
                            JOptionPane.showMessageDialog(this, "Checkout successful!");
                            clearForm();
                            loadReservationsToTable();
                        }
                    }
                }
            }
        } catch (NumberFormatException nfe) {
            showError("Invalid Reservation ID.");
        } catch (SQLException e) {
            showError("Error during checkout: " + e.getMessage());
        }
    }

    // ---------------- Helpers ----------------

    private long computeNights(Timestamp checkIn, Timestamp checkOut) {
        // Truncate to date (midnight) to avoid partial-day confusion
        try {
            Date inDate = dateOnlyFormat.parse(dateOnlyFormat.format(checkIn));
            Date outDate = dateOnlyFormat.parse(dateOnlyFormat.format(checkOut));
            long diffMs = outDate.getTime() - inDate.getTime();
            long nights = diffMs / (24L * 60 * 60 * 1000);
            return Math.max(nights, 1);
        } catch (Exception e) {
            long diffMs = checkOut.getTime() - checkIn.getTime();
            long nights = (long) Math.ceil(diffMs / (24.0 * 60 * 60 * 1000));
            return Math.max(nights, 1);
        }
    }

    private void clearForm() {
        guestNameField.setText("");
        contactField.setText("");
        idField.setText("");
        dateSpinner.setValue(new Date());
        checkOutSpinner.setValue(new Date(System.currentTimeMillis() + 24L*60*60*1000));
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showErrorAndExit(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Fatal Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    // ---------------- main ----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(HotelManagementSystem::new);
    }
}

