-- =====================
-- SEED STATIC DATA
-- =====================

-- =====================
-- Roles
-- =====================
INSERT INTO roles (name)
VALUES
('MANAGER'),
('STAFF');

-- =====================
-- Users
-- =====================
-- Giả sử:
-- MANAGER id = 1
-- STAFF   id = 2
INSERT INTO users (username, password, role_id)
VALUES
('admin', 'admin123', 1),
('staff1', 'staff123', 2);

-- =====================
-- Menu Categories
-- =====================
INSERT INTO menu_categories (name)
VALUES
('Coffee'),
('Tea'),
('Dessert');

-- =====================
-- Menu Items
-- =====================
-- category_id:
-- Coffee  = 1
-- Tea     = 2
-- Dessert = 3
INSERT INTO menu_items (name, description, base_price, category_id)
VALUES
('Espresso', 'Strong black coffee', 30000, 1),
('Latte', 'Milk coffee', 40000, 1),
('Milk Tea', 'Classic milk tea', 35000, 2),
('Cookie Cream', 'Signature dessert', 25000, 3);

-- =====================
-- Customization Options
-- =====================
-- menu_item_id:
-- Espresso     = 1
-- Latte        = 2
-- Milk Tea     = 3
-- CookieCream  = 4
INSERT INTO customization_options (name, price_delta, menu_item_id)
VALUES
('Extra Shot', 10000, 1),
('Less Sugar', 0, 3),
('More Ice', 0, 3),
('Whipped Cream', 5000, 4);
