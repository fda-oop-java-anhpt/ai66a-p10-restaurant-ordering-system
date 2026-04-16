BEGIN;

-- =====================================================
-- 1. ROLES
-- =====================================================
INSERT INTO roles (id, name) VALUES
(1, 'MANAGER'),
(2, 'STAFF'),
(3, 'SERVER'),
(4, 'KITCHEN')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 2. USERS
-- (password hiện để plain text cho đồ án, sau này hash)
-- =====================================================
INSERT INTO users (id, username, password, role_id) VALUES
(1, 'manager', 'manager123', 1),
(2, 'staff1',  'staff123',   2),
(3, 'staff2',  'staff123',   2)
ON CONFLICT DO NOTHING;

-- =====================================================
-- 3. MENU CATEGORIES
-- =====================================================
INSERT INTO menu_categories (id, name) VALUES
(1, 'Drinks'),
(2, 'Main Dishes'),
(3, 'Desserts')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 4. MENU ITEMS
-- =====================================================
INSERT INTO menu_items (id, name, description, image_path, base_price, category_id) VALUES
(1, 'Coca Cola',      'Cold soft drink',      '/assets/menu/coca-cola.png',      15000, 1),
(2, 'Orange Juice',   'Fresh orange juice',   '/assets/menu/orange-juice.png',   20000, 1),
(3, 'Beef Burger',    'Grilled beef burger',  '/assets/menu/beef-burger.png',    50000, 2),
(4, 'Fried Chicken',  'Crispy fried chicken', '/assets/menu/fried-chicken.png',  45000, 2),
(5, 'Chocolate Cake', 'Sweet chocolate cake', '/assets/menu/chocolate-cake.png', 30000, 3)
ON CONFLICT DO NOTHING;

-- =====================================================
-- 5. CUSTOMIZATION OPTIONS
-- =====================================================
INSERT INTO customization_options (id, name, price_delta, menu_item_id) VALUES
-- Burger
(1, 'Extra Cheese',  10000, 3),
(2, 'No Onions',         0, 3),

-- Chicken
(3, 'Extra Spicy',       0, 4),
(4, 'Large Size',    15000, 4),

-- Drinks
(5, 'Less Ice',          0, 1),
(6, 'Large Cup',      5000, 1)
ON CONFLICT DO NOTHING;

-- =====================================================
-- 6. DINING TABLES
-- =====================================================
INSERT INTO dining_tables (id, table_number, capacity, status) VALUES
(1, 1, 2, 'AVAILABLE'),
(2, 2, 4, 'OCCUPIED'),
(3, 3, 4, 'RESERVED'),
(4, 4, 6, 'CLEANING'),
(5, 5, 4, 'AVAILABLE')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 7. ORDERS (Dynamic lookup using staff usernames)
-- =====================================================
INSERT INTO orders (staff_id, table_number, payment_method, order_status, subtotal, tax, service_fee, total)
SELECT 
  u.id AS staff_id,
  2 AS table_number,
  'CARD' AS payment_method,
  'PAID' AS order_status,
  80000 AS subtotal,
  8000 AS tax,
  2000 AS service_fee,
  90000 AS total
FROM users u
WHERE u.username = 'staff1'
  AND NOT EXISTS (
    SELECT 1 FROM orders
    WHERE staff_id = u.id
      AND total = 90000
      AND table_number = 2
      AND payment_method = 'CARD'
  )
UNION ALL
SELECT 
  u.id AS staff_id,
  3 AS table_number,
  'CASH' AS payment_method,
  'PAID' AS order_status,
  45000 AS subtotal,
  4500 AS tax,
  1500 AS service_fee,
  51000 AS total
FROM users u
WHERE u.username = 'staff2'
  AND NOT EXISTS (
    SELECT 1 FROM orders
    WHERE staff_id = u.id
      AND total = 51000
      AND table_number = 3
      AND payment_method = 'CASH'
  );

-- =====================================================
-- 8. ORDER ITEMS (Dynamic lookup by menu item names)
-- =====================================================
-- Order 1: Beef Burger (from staff1)
INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price)
SELECT 
  (SELECT MIN(o.id) FROM orders o JOIN users u ON o.staff_id = u.id WHERE u.username = 'staff1'),
  (SELECT id FROM menu_items WHERE name = 'Beef Burger' LIMIT 1),
  1 AS quantity,
  (SELECT base_price FROM menu_items WHERE name = 'Beef Burger' LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM order_items oi
  JOIN orders o ON oi.order_id = o.id
  JOIN users u ON o.staff_id = u.id
  WHERE u.username = 'staff1' AND oi.menu_item_id = (SELECT id FROM menu_items WHERE name = 'Beef Burger' LIMIT 1)
);

-- Order 1: Coca Cola x2 (from staff1)
INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price)
SELECT 
  (SELECT MIN(o.id) FROM orders o JOIN users u ON o.staff_id = u.id WHERE u.username = 'staff1'),
  (SELECT id FROM menu_items WHERE name = 'Coca Cola' LIMIT 1),
  2 AS quantity,
  (SELECT base_price FROM menu_items WHERE name = 'Coca Cola' LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM order_items oi
  JOIN orders o ON oi.order_id = o.id
  JOIN users u ON o.staff_id = u.id
  WHERE u.username = 'staff1' AND oi.menu_item_id = (SELECT id FROM menu_items WHERE name = 'Coca Cola' LIMIT 1) AND oi.quantity = 2
);

-- Order 2: Fried Chicken (from staff2)
INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price)
SELECT 
  (SELECT MIN(o.id) FROM orders o JOIN users u ON o.staff_id = u.id WHERE u.username = 'staff2'),
  (SELECT id FROM menu_items WHERE name = 'Fried Chicken' LIMIT 1),
  1 AS quantity,
  (SELECT base_price FROM menu_items WHERE name = 'Fried Chicken' LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM order_items oi
  JOIN orders o ON oi.order_id = o.id
  JOIN users u ON o.staff_id = u.id
  WHERE u.username = 'staff2' AND oi.menu_item_id = (SELECT id FROM menu_items WHERE name = 'Fried Chicken' LIMIT 1)
);

-- =====================================================
-- 9. ORDER ITEM CUSTOMIZATIONS (Dynamic lookup by item/customization names)
-- =====================================================
-- Beef Burger + Extra Cheese (from staff1's order)
INSERT INTO order_item_customizations (order_id, menu_item_id, customization_id)
SELECT 
  (SELECT MIN(o.id) FROM orders o JOIN users u ON o.staff_id = u.id WHERE u.username = 'staff1'),
  (SELECT id FROM menu_items WHERE name = 'Beef Burger' LIMIT 1),
  (SELECT id FROM customization_options WHERE name = 'Extra Cheese' LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM order_item_customizations oic
  WHERE oic.menu_item_id = (SELECT id FROM menu_items WHERE name = 'Beef Burger' LIMIT 1)
    AND oic.customization_id = (SELECT id FROM customization_options WHERE name = 'Extra Cheese' LIMIT 1)
);

-- Coca Cola + Large Cup (from staff1's order)
INSERT INTO order_item_customizations (order_id, menu_item_id, customization_id)
SELECT 
  (SELECT MIN(o.id) FROM orders o JOIN users u ON o.staff_id = u.id WHERE u.username = 'staff1'),
  (SELECT id FROM menu_items WHERE name = 'Coca Cola' LIMIT 1),
  (SELECT id FROM customization_options WHERE name = 'Large Cup' LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM order_item_customizations oic
  WHERE oic.menu_item_id = (SELECT id FROM menu_items WHERE name = 'Coca Cola' LIMIT 1)
    AND oic.customization_id = (SELECT id FROM customization_options WHERE name = 'Large Cup' LIMIT 1)
);

-- =====================================================
-- 10. LOGIN LOGS
-- =====================================================
INSERT INTO login_logs (id, user_id, action) VALUES
(1, 1, 'LOGIN'),
(2, 2, 'LOGIN'),
(3, 2, 'LOGOUT')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 11. AUDIT LOGS (PRICE CHANGE)
-- =====================================================
INSERT INTO audit_logs (id, manager_id, menu_item_id, old_price, new_price) VALUES
(1, 1, 3, 45000, 50000)
ON CONFLICT DO NOTHING;

-- =====================================================
-- 12. SYNC SERIAL SEQUENCES (IMPORTANT)
-- Tranh duplicate key khi code insert du lieu moi sau khi seed
-- =====================================================
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM roles;
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM users;
SELECT setval(pg_get_serial_sequence('login_logs', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM login_logs;
SELECT setval(pg_get_serial_sequence('menu_categories', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM menu_categories;
SELECT setval(pg_get_serial_sequence('menu_items', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM menu_items;
SELECT setval(pg_get_serial_sequence('customization_options', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM customization_options;
SELECT setval(pg_get_serial_sequence('dining_tables', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM dining_tables;
SELECT setval(pg_get_serial_sequence('orders', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM orders;
SELECT setval(pg_get_serial_sequence('order_items', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM order_items;
SELECT setval(pg_get_serial_sequence('order_item_customizations', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM order_item_customizations;
SELECT setval(pg_get_serial_sequence('audit_logs', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM audit_logs;

COMMIT;
