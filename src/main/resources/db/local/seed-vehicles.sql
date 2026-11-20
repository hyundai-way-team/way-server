INSERT INTO vehicle (vehicle_key, created_at, updated_at)
VALUES ('DEMO-VIN-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE vehicle_key = vehicle.vehicle_key;
