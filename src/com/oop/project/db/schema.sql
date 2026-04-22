-- =====================
-- Role
-- =====================
CREATE TABLE roles (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL
);

-- =====================
-- Users
-- =====================
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(60) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  role_id INTEGER NOT NULL,
  CONSTRAINT fk_user_role
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- =====================
-- Login log
-- =====================
CREATE TABLE login_logs (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL,
  action VARCHAR(30) NOT NULL,
  logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_loginlog_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================
-- Menu category
-- =====================
CREATE TABLE menu_categories (
  id SERIAL PRIMARY KEY,
  name VARCHAR(80) UNIQUE NOT NULL
);

-- =====================
-- Menu item
-- =====================
CREATE TABLE menu_items (
  id SERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(255),
  image_path VARCHAR(255),
  base_price NUMERIC(12,2) NOT NULL DEFAULT 0,
  category_id INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_menuitem_category
    FOREIGN KEY (category_id) REFERENCES menu_categories(id)
);

-- =====================
-- Customization option
-- =====================
CREATE TABLE customization_options (
  id SERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  price_delta NUMERIC(12,2) NOT NULL DEFAULT 0,
  menu_item_id INTEGER NOT NULL,
  CONSTRAINT fk_customopt_menuitem
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
    ON DELETE CASCADE
);

-- =====================
-- Dining tables
-- =====================
CREATE TABLE dining_tables (
  id SERIAL PRIMARY KEY,
  table_number INTEGER NOT NULL UNIQUE CHECK (table_number > 0),
  capacity INTEGER NOT NULL DEFAULT 4 CHECK (capacity > 0),
  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
    CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'CLEANING'))
);

-- =====================
-- Orders
-- =====================
CREATE TABLE orders (
  id SERIAL PRIMARY KEY,
  staff_id INTEGER NOT NULL,
  table_number INTEGER,
  payment_method VARCHAR(10) NOT NULL DEFAULT 'CASH'
    CHECK (payment_method IN ('CARD', 'CASH')),
  order_status VARCHAR(20) NOT NULL DEFAULT 'PAID'
    CHECK (order_status IN ('OPEN', 'SENT_TO_KITCHEN', 'PAID', 'VOID')),
  subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
  tax NUMERIC(12,2) NOT NULL DEFAULT 0,
  service_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
  total NUMERIC(12,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_staff
    FOREIGN KEY (staff_id) REFERENCES users(id),
  CONSTRAINT fk_order_table_number
    FOREIGN KEY (table_number) REFERENCES dining_tables(table_number)
);

-- =====================
-- Order items
-- =====================
CREATE TABLE order_items (
  id SERIAL PRIMARY KEY,
  order_id INTEGER NOT NULL,
  menu_item_id INTEGER NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  unit_price NUMERIC(12,2) NOT NULL DEFAULT 0,
  note TEXT NOT NULL DEFAULT '',
  CONSTRAINT fk_orderitem_order
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_orderitem_menuitem
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- =====================
-- Order item customization
-- =====================
CREATE TABLE order_item_customizations (
  id SERIAL PRIMARY KEY,
  order_id INTEGER NOT NULL,
  order_item_id INTEGER,
  menu_item_id INTEGER NOT NULL,
  customization_id INTEGER NOT NULL,
  CONSTRAINT fk_oic_order
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_oic_order_item
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
  CONSTRAINT fk_oic_menuitem
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
  CONSTRAINT fk_oic_customopt
    FOREIGN KEY (customization_id) REFERENCES customization_options(id)
);

CREATE INDEX idx_oic_order_item_id ON order_item_customizations(order_item_id);

CREATE OR REPLACE FUNCTION set_oic_order_item_id()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.order_item_id IS NULL THEN
    SELECT oi.id
    INTO NEW.order_item_id
    FROM order_items oi
    WHERE oi.order_id = NEW.order_id
      AND oi.menu_item_id = NEW.menu_item_id
    ORDER BY oi.id DESC
    LIMIT 1;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_oic_order_item_id
BEFORE INSERT ON order_item_customizations
FOR EACH ROW
EXECUTE FUNCTION set_oic_order_item_id();

CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_staff_created_at ON orders(staff_id, created_at);
CREATE INDEX idx_orders_status ON orders(order_status);

-- =====================
-- Audit log
-- =====================
CREATE TABLE audit_logs (
  id SERIAL PRIMARY KEY,
  manager_id INTEGER NOT NULL,
  menu_item_id INTEGER NOT NULL,
  old_price NUMERIC(12,2) NOT NULL,
  new_price NUMERIC(12,2) NOT NULL,
  changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_manager FOREIGN KEY (manager_id) REFERENCES users(id),
  CONSTRAINT fk_audit_menuitem FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);
