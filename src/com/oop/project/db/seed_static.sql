BEGIN;

-- =====================================================
-- 1. ROLES
-- =====================================================
INSERT INTO roles (id, name) VALUES
(1, 'MANAGER'),
(2, 'STAFF')
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
INSERT INTO menu_items (id, name, description, base_price, category_id) VALUES
(1, 'Coca Cola',      'Cold soft drink',        15000, 1),
(2, 'Orange Juice',   'Fresh orange juice',     20000, 1),
(3, 'Beef Burger',    'Grilled beef burger',    50000, 2),
(4, 'Fried Chicken',  'Crispy fried chicken',   45000, 2),
(5, 'Chocolate Cake', 'Sweet chocolate cake',   30000, 3)
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
-- 6. ORDERS
-- =====================================================
INSERT INTO orders (id, staff_id, subtotal, tax, service_fee, total) VALUES
(1, 2, 80000,  8000,  2000,  90000),
(2, 3, 45000,  4500,  1500,  51000)
ON CONFLICT DO NOTHING;

-- =====================================================
-- 7. ORDER ITEMS
-- =====================================================
INSERT INTO order_items (id, order_id, menu_item_id, quantity, unit_price) VALUES
-- Order 1
(1, 1, 3, 1, 50000),  -- Beef Burger
(2, 1, 1, 2, 15000),  -- Coca Cola x2

-- Order 2
(3, 2, 4, 1, 45000)   -- Fried Chicken
ON CONFLICT DO NOTHING;

-- =====================================================
-- 8. ORDER ITEM CUSTOMIZATIONS
-- =====================================================
INSERT INTO order_item_customizations (id, order_item_id, customization_id) VALUES
(1, 1, 1),  -- Beef Burger + Extra Cheese
(2, 2, 6)   -- Coca Cola + Large Cup
ON CONFLICT DO NOTHING;

-- =====================================================
-- 9. LOGIN LOGS
-- =====================================================
INSERT INTO login_logs (id, user_id, action) VALUES
(1, 1, 'LOGIN'),
(2, 2, 'LOGIN'),
(3, 2, 'LOGOUT')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 10. AUDIT LOGS (PRICE CHANGE)
-- =====================================================
INSERT INTO audit_logs (id, manager_id, menu_item_id, old_price, new_price) VALUES
(1, 1, 3, 45000, 50000)
ON CONFLICT DO NOTHING;

COMMIT;
