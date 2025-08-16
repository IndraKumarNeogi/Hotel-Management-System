# 🏨 Hotel Management System

A comprehensive Java-based desktop application for managing hotel reservations with a modern graphical user interface. This system provides complete functionality for hotel staff to manage room bookings, guest information, and billing operations.

## 🌟 Features

### Core Functionality
- **Room Reservation Management** - Add, update, and delete reservations
- **Guest Information Management** - Store and manage guest details
- **Real-time Availability Checking** - Check room availability for specific dates
- **Billing & Checkout System** - Automated billing with GST calculation
- **Reservation Status Tracking** - Active, Checked-out, and historical reservations

### Advanced Features
- **Date Range Validation** - Prevents overlapping bookings
- **Dynamic Room Availability** - Shows available rooms for selected date ranges
- **Automated Billing** - Calculates total including 18% GST
- **Visual Status Indicators** - Color-coded reservation status
- **Data Validation** - Comprehensive input validation and error handling

## 🛠️ Technology Stack

- **Language**: Java 17
- **Database**: MySQL 8.0+
- **GUI Framework**: Java Swing
- **Database Driver**: MySQL Connector/J
- **Build Tool**: Standard Java compilation

## 📋 Prerequisites

- Java 17 or higher
- MySQL Server 8.0+
- MySQL Connector/J (mysql-connector-j-9.1.0.jar included)
- Windows/Linux/Mac OS compatible

## 🚀 Installation & Setup

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE hotel_db;

-- Use the database
USE hotel_db;

-- Create rooms table
CREATE TABLE rooms (
    room_number INT PRIMARY KEY,
    room_type VARCHAR(50) NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL
);

-- Create reservations table
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_name VARCHAR(100) NOT NULL,
    room_number INT NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    reservation_date DATETIME NOT NULL,
    checkout_date DATETIME NOT NULL,
    status ENUM('ACTIVE', 'CHECKED_OUT') DEFAULT 'ACTIVE',
    FOREIGN KEY (room_number) REFERENCES rooms(room_number)
);

-- Insert sample rooms
INSERT INTO rooms (room_number, room_type, price_per_night) VALUES
(101, 'Standard', 2500.00),
(102, 'Deluxe', 3500.00),
(103, 'Suite', 5000.00),
(201, 'Standard', 2500.00),
(202, 'Deluxe', 3500.00),
(203, 'Suite', 5000.00);
```

### 2. Application Setup

1. **Clone or download** the project files
2. **Place MySQL connector** in `lib/` directory (already included)
3. **Configure database connection** in `HotelManagementSystem.java`:
   ```java
   private static final String DB_URL = "jdbc:mysql://localhost:3310/hotel_db?serverTimezone=UTC";
   private static final String DB_USER = "****";
   private static final String DB_PASS = "****";
   ```

### 3. Compilation & Run

```bash
# Compile
javac -cp "lib/mysql-connector-j-9.1.0.jar" src/HotelManagementSystem.java

# Run
java -cp "lib/mysql-connector-j-9.1.0.jar;src" HotelManagementSystem
```

## 🎯 Usage Guide

### Making a Reservation
1. Enter guest name, contact number
2. Select room number from dropdown
3. Choose check-in and checkout dates
4. Click **"Reserve"** to create booking

### Managing Reservations
- **Update**: Select reservation from table, modify details, click **"Update"**
- **Delete**: Select reservation, click **"Delete"** (with confirmation)
- **Checkout**: Select reservation, click **"Checkout"** for automated billing

### Viewing Options
- **Current Reservations**: Shows only active bookings
- **All Reservations**: Shows complete history including checked-out bookings

### Additional Features
- **Show Available Rooms**: Displays available rooms for selected date range
- **Get Room No.**: Retrieves reservation details by ID

## 💰 Billing System

The checkout system automatically calculates:
- **Subtotal**: Room price × number of nights
- **GST**: 18% of subtotal
- **Grand Total**: Subtotal + GST

Example bill format:
```
Checkout Summary
----------------
Reservation ID : 123
Guest          : John Doe
Contact        : 9876543210
Room           : 101
Check-in       : 2024-01-15 14:00:00
Checkout       : 2024-01-17 11:00:00
Nights         : 2
Price/Night    : ₹2500.00
Subtotal       : ₹5000.00
GST (18%)      : ₹900.00
Grand Total    : ₹5900.00
```

## 🏗️ Project Structure

```
Hotel Management System/
├── src/
│   └── HotelManagementSystem.java    # Main application
├── lib/
│   ├── mysql-connector-j-9.1.0.jar  # MySQL driver
│   └── jdk-17.0.12.7-hotspot/     # Java runtime
├── bin/
│   └── HotelManagementSystem.class  # Compiled classes
└── README.md                        # This file
```

## 🔧 Configuration

### Database Configuration
- **Host**: localhost:3310 (adjust as needed)
- **Database**: hotel_db
- **Username**: **** (change as per your MySQL setup)
- **Password**: **** (change as per your MySQL setup)

### Room Pricing Configuration
Modify the `rooms` table to set different pricing:
```sql
UPDATE rooms SET price_per_night = 3000.00 WHERE room_number = 101;
```

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure MySQL is running
   - Check database credentials
   - Verify database exists

2. **No Rooms Found**
   - Run the room insertion SQL script
   - Check if rooms table has data

3. **Compilation Errors**
   - Ensure Java 17+ is installed
   - Verify MySQL connector is in lib directory

4. **Port Issues**
   - Change port in DB_URL if MySQL runs on different port
   - Default: 3306 or 3310

## 🔄 Database Schema

### Rooms Table
| Column | Type | Description |
|--------|------|-------------|
| room_number | INT | Primary key |
| room_type | VARCHAR | Room category |
| price_per_night | DECIMAL | Room rate |

### Reservations Table
| Column | Type | Description |
|--------|------|-------------|
| reservation_id | INT | Auto-increment primary key |
| guest_name | VARCHAR | Guest full name |
| room_number | INT | Foreign key to rooms |
| contact_number | VARCHAR | Guest phone number |
| reservation_date | DATETIME | Check-in date/time |
| checkout_date | DATETIME | Checkout date/time |
| status | ENUM | ACTIVE or CHECKED_OUT |

## 🚀 Future Enhancements

- [ ] Room type filtering
- [ ] Guest history tracking
- [ ] Advanced reporting
- [ ] Multi-user support
- [ ] Backup and restore functionality
- [ ] Email notifications
- [ ] Mobile responsive design

## 🖊 Author

Indra Kumar Neogi
gmail: indra.kumar.neogi@gmail.com
linkedin: [linkedin.com/in/indra-kumar-neogi](https://www.linkedin.com/in/indra-kumar-neogi-bbb34a268)

## 📞 Support

For issues or questions:
1. Check the troubleshooting section
2. Verify database connectivity
3. Ensure all dependencies are properly installed

## 📄 License

This project is open-source and available for educational and commercial use.

---

**Created with ❤️ for the hospitality industry**
