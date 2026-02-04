package hr.abysalto.hiring.api.junior.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private boolean dataInitialized = false;

	public boolean isDataInitialized() {
		return this.dataInitialized;
	}

	public void initialize() {
		initTables();
		initData();
		this.dataInitialized = true;
	}

	private void initTables() {
		this.jdbcTemplate.execute("DROP TABLE IF EXISTS \"ORDER_ITEM\"");
		this.jdbcTemplate.execute("DROP TABLE IF EXISTS \"ORDERS\"");
		this.jdbcTemplate.execute("DROP TABLE IF EXISTS \"REFRESH_TOKEN\"");
		this.jdbcTemplate.execute("DROP TABLE IF EXISTS \"BUYER_ADDRESS\"");
		this.jdbcTemplate.execute("DROP TABLE IF EXISTS \"BUYER\"");
		this.jdbcTemplate.execute("DROP TABLE IF EXISTS \"ITEM\"");
		this.jdbcTemplate.execute("DROP TABLE IF EXISTS \"USER_INFO\"");

		this.jdbcTemplate.execute("""
			 CREATE TABLE user_info (
				 id BIGINT AUTO_INCREMENT PRIMARY KEY,
				 username VARCHAR(100) NOT NULL UNIQUE,
				 email VARCHAR(255) NOT NULL UNIQUE,
				 password VARCHAR(255) NOT NULL
			 );
			""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE refresh_token (
				 id BIGINT AUTO_INCREMENT PRIMARY KEY,
				 token VARCHAR(255) NOT NULL,
				 expiry_date TIMESTAMP NOT NULL,
				 user_id BIGINT NOT NULL,
				 CONSTRAINT FK_refresh_token_to_user FOREIGN KEY (user_id) REFERENCES user_info (id)
			 );
			""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE buyer (
				 id BIGINT AUTO_INCREMENT PRIMARY KEY,
				 first_name VARCHAR(100) NOT NULL,
				 last_name VARCHAR(100) NOT NULL,
				 title VARCHAR(100) NULL,
				 user_id BIGINT NULL,
				 CONSTRAINT FK_buyer_to_user FOREIGN KEY (user_id) REFERENCES user_info (id)
			 );
			""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE buyer_address (
				 id BIGINT AUTO_INCREMENT PRIMARY KEY,
				 city VARCHAR(100) NOT NULL,
				 street VARCHAR(100) NOT NULL,
				 home_number VARCHAR(100) NULL,
				 buyer_id BIGINT NOT NULL,
				 CONSTRAINT FK_buyer_address_to_buyer FOREIGN KEY (buyer_id) REFERENCES buyer (id)
			 );
			""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE item (
				 id BIGINT AUTO_INCREMENT PRIMARY KEY,
				 item_number SMALLINT NOT NULL,
				 name VARCHAR(100) NOT NULL,
				 price DECIMAL(19, 2) NULL
			 );
			""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE orders (
				 id BIGINT AUTO_INCREMENT PRIMARY KEY,
				 buyer_id BIGINT NOT NULL,
				 order_status VARCHAR(32) NOT NULL,
				 order_time TIMESTAMP NOT NULL,
				 payment_option VARCHAR(32) NULL,
				 delivery_address_id BIGINT NOT NULL,
				 contact_number VARCHAR(100) NULL,
				 order_note VARCHAR(500) NULL,
				 total_price DECIMAL(19, 2) NULL,
				 currency VARCHAR(50) NULL,
				 version BIGINT DEFAULT 0,
				 CONSTRAINT FK_orders_to_buyer FOREIGN KEY (buyer_id) REFERENCES buyer (id),
				 CONSTRAINT FK_orders_to_delivery_address FOREIGN KEY (delivery_address_id) REFERENCES buyer_address (id)
			 );
			""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE order_item (
				 id BIGINT AUTO_INCREMENT PRIMARY KEY,
				 order_id BIGINT NOT NULL,
				 item_id BIGINT NOT NULL,
				 snapshot_price DECIMAL(19, 2) NULL,
				 quantity SMALLINT NOT NULL,
				 CONSTRAINT FK_order_item_to_orders FOREIGN KEY (order_id) REFERENCES orders (id),
				 CONSTRAINT FK_order_item_to_item FOREIGN KEY (item_id) REFERENCES item (id)
			 );
			""");
	}

	private void initData() {
		// password = "password", BCrypt encoded
		this.jdbcTemplate.execute("INSERT INTO user_info (username, email, password) VALUES ('demo', 'demo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')");

		this.jdbcTemplate.execute("INSERT INTO refresh_token (token, expiry_date, user_id) VALUES ('init-refresh-token-demo', TIMESTAMPADD('DAY', 7, CURRENT_TIMESTAMP), 1)");

		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Jabba', 'Hutt', 'the')");
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title, user_id) VALUES ('Anakin', 'Skywalker', NULL, 1)");
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Jar Jar', 'Binks', NULL)");
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Han', 'Solo', NULL)");
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Leia', 'Organa', 'Princess')");

		this.jdbcTemplate.execute("INSERT INTO buyer_address (city, street, home_number, buyer_id) VALUES ('Mos Eisley', 'Dune Street', '1', 1)");
		this.jdbcTemplate.execute("INSERT INTO buyer_address (city, street, home_number, buyer_id) VALUES ('Tatooine', 'Lars Homestead', '1', 2)");
		this.jdbcTemplate.execute("INSERT INTO buyer_address (city, street, home_number, buyer_id) VALUES ('Naboo', 'Theed', '42', 3)");
		this.jdbcTemplate.execute("INSERT INTO buyer_address (city, street, home_number, buyer_id) VALUES ('Corellia', 'Solo Lane', '7', 4)");
		this.jdbcTemplate.execute("INSERT INTO buyer_address (city, street, home_number, buyer_id) VALUES ('Alderaan', 'Palace Ave', '1', 5)");

		this.jdbcTemplate.execute("INSERT INTO item (item_number, name, price) VALUES (1, 'Lightsaber', 299.99)");
		this.jdbcTemplate.execute("INSERT INTO item (item_number, name, price) VALUES (2, 'Blaster', 149.50)");
		this.jdbcTemplate.execute("INSERT INTO item (item_number, name, price) VALUES (3, 'Holocron', 499.00)");
		this.jdbcTemplate.execute("INSERT INTO item (item_number, name, price) VALUES (4, 'Medpac', 24.99)");
		this.jdbcTemplate.execute("INSERT INTO item (item_number, name, price) VALUES (5, 'Datapad', 89.00)");

		this.jdbcTemplate.execute("""
			 INSERT INTO orders (buyer_id, order_status, order_time, payment_option, delivery_address_id, contact_number, order_note, total_price, currency, version)
			 VALUES (1, 'WAITING_FOR_CONFIRMATION', CURRENT_TIMESTAMP, 'CASH', 1, '+1234567890', 'May the Force be with you.', 749.48, 'USD', 0)
			""");

		this.jdbcTemplate.execute("INSERT INTO order_item (order_id, item_id, snapshot_price, quantity) VALUES (1, 1, 299.99, 2)");
		this.jdbcTemplate.execute("INSERT INTO order_item (order_id, item_id, snapshot_price, quantity) VALUES (1, 2, 149.50, 1)");
	}
}
