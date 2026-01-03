# Project 10: Restaurant Ordering System — Requirements Specification

**Course/Project:** OOP Java — Project 10  
**System Name:** Restaurant Ordering System (POS)  

---

## 10.1 Problem Description

A busy restaurant currently handles orders using handwritten notes and verbal communication between waitstaff and kitchen staff. This manual workflow frequently leads to:

- Incorrect orders
- Missing modifications (extra toppings, no spice)
- Inconsistent pricing
- Difficulty tracking sales at the end of the day

There is no centralized menu system, no real-time price calculation, and no login mechanism to separate Staff from Managers.

The upgraded system must operate as a complete point-of-sale (POS) and ordering platform, providing:

- Secure login with role-based permissions
- Menu browsing and item selection
- Modifiers, toppings, and customizations
- Real-time order summary and pricing
- Cart and checkout management
- Search, sorting, and category filters
- Daily report and analytics dashboard
- Persistent storage using sequential files
- A full Java Swing GUI for all workflows

---

## 10.2 Functional Requirements

### FR-0: Login and Authentication

- **FR-0.1:** The system shall provide a login screen requiring username and password.
- **FR-0.2:** The system shall validate credentials using a stored credential file.
- **FR-0.3:** The system shall support at least two roles (**Manager**, **Staff**).
- **FR-0.4:** Only **Managers** may perform price updates or view full-day sales.
- **FR-0.5:** The system shall log all login and logout events.

### FR-1: Menu Browsing and Management

- **FR-1.1:** The system shall display the menu organized into categories (Drinks, Main Dishes, Desserts, etc.).
- **FR-1.2:** The system shall allow searching menu items by name or keyword.
- **FR-1.3:** The system shall allow **Managers** to update menu prices.
- **FR-1.4:** Each menu item shall include **name**, **description**, **base price**, and **category**.

### FR-2: Order Creation and Customization

- **FR-2.1:** The system shall allow selecting menu items to add to the active order.
- **FR-2.2:** The system shall allow adding customizations (e.g., extra cheese, no onions, size options).
- **FR-2.3:** The system shall update the item price based on selected customizations.
- **FR-2.4:** The system shall validate numeric quantity inputs using `try–catch` blocks.
- **FR-2.5:** The system shall update the order subtotal in real time as items or customizations change.

### FR-3: Cart and Checkout

- **FR-3.1:** The system shall display all items in the cart with quantities, customizations, and prices.
- **FR-3.2:** The system shall allow editing or removing items from the cart.
- **FR-3.3:** The system shall compute tax, service fee, and final total automatically.
- **FR-3.4:** The system shall allow confirming the order and generating an order summary.
- **FR-3.5:** The system shall validate that the cart is not empty before checkout.

### FR-4: Additional Core Features

- **FR-4.1:** The system shall allow exporting the daily order list to CSV.
- **FR-4.2:** The system shall allow printing an individual order receipt.
- **FR-4.3:** The system shall classify best-selling items based on sales frequency.
- **FR-4.4:** The system shall maintain an audit log of menu price changes.

### FR-5: Dashboard and Search

- **FR-5.1:** The system shall display all orders in a sortable table (time, total, number of items).
- **FR-5.2:** The system shall filter orders by date, price range, or staff member.
- **FR-5.3:** The dashboard shall display sales analytics including:
  - Total daily revenue
  - Number of orders
  - Average order value
  - Best-selling categories
- **FR-5.4:** The system shall allow keyword search of orders or menu items.

### FR-6: Persistence and GUI Requirements

- **FR-6.1:** The system shall store menu items, customizations, and orders using **Sequential File I/O**.
- **FR-6.2:** The system shall store login credentials in an encoded credential file.
- **FR-6.3:** The GUI shall include tabs for **Menu**, **Orders**, **Cart**, and **Dashboard**.
- **FR-6.4:** The GUI shall update order totals and item prices in real time.
- **FR-6.5:** The GUI shall display validation and error messages via `JOptionPane`.
