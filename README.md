# Electricity Bill Management System

A console-based Java application for managing electricity customers, generating
bills, recording payments, and producing summary reports. Built with plain
Java (no external database or libraries required) so it compiles and runs
anywhere a JDK is installed.

## Features

- **Admin Login** — access is protected by an admin username/password
  (default: `admin` / `admin123` — change this after first login via
  Admin Settings).
- **Customer Management** — add, view, search, update, and delete customer
  records (name, address, phone, meter number, meter readings).
- **Billing** — generate a bill for any customer based on units consumed,
  using a slab-based tariff:
  | Units          | Rate (Rs/unit) |
  |----------------|----------------|
  | 0 – 100        | 5.0            |
  | 101 – 200      | 7.0            |
  | 201 – 400      | 10.0           |
  | 401+           | 12.0           |

  Plus a fixed service charge of Rs. 50 on every bill.
- **Payments** — record a payment against any unpaid bill (cash, card, UPI,
  net banking) and view payment history.
- **Reports** — summary report (totals billed/collected/pending), pending
  bills list, month-wise report, and top-consumers report.

## Project Structure

```
ElectricityBillManagementSystem/
├── src/
│   ├── Main.java                 # Application entry point
│   ├── model/                    # Data classes
│   │   ├── Customer.java
│   │   ├── Bill.java
│   │   ├── Payment.java
│   │   └── Admin.java
│   ├── service/                  # Business logic
│   │   ├── CustomerService.java
│   │   ├── BillingService.java
│   │   ├── PaymentService.java
│   │   ├── ReportService.java
│   │   └── AuthService.java
│   ├── util/                     # Shared helpers
│   │   ├── FileStorage.java      # Reads/writes plain-text data files
│   │   └── IdGenerator.java
│   └── ui/
│       └── ConsoleUI.java        # Menu-driven console interface
├── data/                         # Auto-created at runtime (customers.txt, bills.txt, etc.)
├── .gitignore
└── README.md
```

## Database & How It Works (JDBC Architecture)

1. **Database Storage**: The application connects to **MySQL Server** (`electricity_db`) via **JDBC** (`com.mysql.cj.jdbc.Driver`).
   - Connector JAR: `lib/mysql-connector-j-9.2.0.jar`
   - Connection URL: `jdbc:mysql://localhost:3306/electricity_db`
   - Database tables (`admins`, `customers`, `bills`, `payments`) are automatically created on first startup if they do not exist.
2. **Layered OOP Design**:
   - `model` — plain data classes (`Customer`, `Bill`, `Payment`, `Admin`).
   - `util` — JDBC connection manager ([DBConnection.java](file:///c:/Users/KiTE/Desktop/OOPS%20ASSIGNMENT/src/util/DBConnection.java)) and ID sequence generator ([IdGenerator.java](file:///c:/Users/KiTE/Desktop/OOPS%20ASSIGNMENT/src/util/IdGenerator.java)).
   - `service` — all business rules and SQL PreparedStatement operations (`CustomerService`, `BillingService`, `PaymentService`, `ReportService`, `AuthService`).
   - `ui` — [ConsoleUI.java](file:///c:/Users/KiTE/Desktop/OOPS%20ASSIGNMENT/src/ui/ConsoleUI.java) menu-driven interface.

## Getting Started

### Prerequisites
- JDK 17 or later
- MySQL Server (running on port 3306)
- MySQL Connector/J (located in `lib/`)

### Compile and Run (command line)

```powershell
# From the project root
# 1. Compile with the JDBC driver in classpath
javac -cp "lib/*" -d out src/Main.java (Get-ChildItem -Recurse src/*.java | ForEach-Object { $_.FullName })

# 2. Run the application
java -cp "out;lib/*" Main
```

### Compile and Run (IDE)
1. Open the project folder as a Java project.
2. Mark `src` as the source root.
3. Run `Main.java`.

On first run, the app seeds a default admin account (`admin` / `admin123`)
and creates the `data/` folder automatically.

## Team Workflow / Version Control

- Each member works on a feature branch (e.g. `feature/customer-management`,
  `feature/billing`, `feature/reports`) and opens a pull request into `main`.
- Commit messages should be descriptive (e.g. `Add slab-based bill calculation logic`),
  so the commit history reflects each member's contribution.
- All team members are added as collaborators on the repository so commits,
  branches, and pull requests are attributed individually.

## Sample Menu Flow

```
1. Customer Management  -> Add Customer, View All, Search, Update, Delete
2. Billing               -> Generate Bill, View All Bills, View by Customer
3. Payments              -> Record Payment, View All, View by Bill
4. Reports               -> Summary, Pending Bills, Monthly, Top Consumers
5. Admin Settings        -> Change Password
0. Exit
```
