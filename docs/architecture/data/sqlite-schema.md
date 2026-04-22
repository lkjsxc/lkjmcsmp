# SQLite Schema Contract

## Tables

### `player_points`

- `player_uuid` TEXT PRIMARY KEY
- `balance` INTEGER NOT NULL
- `updated_at` TEXT NOT NULL

### `points_ledger`

- `id` TEXT PRIMARY KEY
- `player_uuid` TEXT NOT NULL
- `delta` INTEGER NOT NULL
- `reason_code` TEXT NOT NULL
- `meta_json` TEXT NOT NULL
- `created_at` TEXT NOT NULL

### `homes`

- `player_uuid` TEXT NOT NULL
- `home_name` TEXT NOT NULL
- `world` TEXT NOT NULL
- `x` REAL NOT NULL
- `y` REAL NOT NULL
- `z` REAL NOT NULL
- `yaw` REAL NOT NULL
- `pitch` REAL NOT NULL
- PRIMARY KEY (`player_uuid`, `home_name`)

### `warps`

- `warp_name` TEXT PRIMARY KEY
- `world` TEXT NOT NULL
- `x` REAL NOT NULL
- `y` REAL NOT NULL
- `z` REAL NOT NULL
- `yaw` REAL NOT NULL
- `pitch` REAL NOT NULL
- `created_by` TEXT NOT NULL

### `party_members`

- `party_id` TEXT NOT NULL
- `player_uuid` TEXT PRIMARY KEY
- `role` TEXT NOT NULL

### `party_invites`

- `party_id` TEXT NOT NULL
- `target_uuid` TEXT PRIMARY KEY
- `invited_by` TEXT NOT NULL
- `expires_at` TEXT NOT NULL

### `party_homes`

- `party_id` TEXT PRIMARY KEY
- `world` TEXT NOT NULL
- `x` REAL NOT NULL
- `y` REAL NOT NULL
- `z` REAL NOT NULL
- `yaw` REAL NOT NULL
- `pitch` REAL NOT NULL

### `achievement_state`

- `player_uuid` TEXT NOT NULL
- `achievement_key` TEXT NOT NULL
- `status` TEXT NOT NULL
- `progress_value` INTEGER NOT NULL
- `updated_at` TEXT NOT NULL
- PRIMARY KEY (`player_uuid`, `achievement_key`)

### `economy_overrides`

- `item_key` TEXT PRIMARY KEY
- `points_cost` INTEGER NOT NULL
- `quantity` INTEGER NOT NULL
- `updated_by` TEXT NOT NULL
- `updated_at` TEXT NOT NULL

### `audit_log`

- `id` TEXT PRIMARY KEY
- `actor_uuid` TEXT NOT NULL
- `target_uuid` TEXT NOT NULL
- `event_key` TEXT NOT NULL
- `before_json` TEXT NOT NULL
- `after_json` TEXT NOT NULL
- `created_at` TEXT NOT NULL

### `first_join_rtp`

- `player_uuid` TEXT PRIMARY KEY
- `completed_at` TEXT NOT NULL

### `temporary_end_instances`

- `instance_id` TEXT PRIMARY KEY
- `world_name` TEXT NOT NULL UNIQUE
- `creator_uuid` TEXT NOT NULL
- `origin_world` TEXT NOT NULL
- `origin_x` REAL NOT NULL
- `origin_y` REAL NOT NULL
- `origin_z` REAL NOT NULL
- `origin_yaw` REAL NOT NULL
- `origin_pitch` REAL NOT NULL
- `creation_time` TEXT NOT NULL
- `expiration_time` TEXT NOT NULL
- `state` TEXT NOT NULL

### `temporary_end_participants`

- `instance_id` TEXT NOT NULL
- `player_uuid` TEXT NOT NULL
- `return_world` TEXT NOT NULL
- `return_x` REAL NOT NULL
- `return_y` REAL NOT NULL
- `return_z` REAL NOT NULL
- `return_yaw` REAL NOT NULL
- `return_pitch` REAL NOT NULL
- PRIMARY KEY (`instance_id`, `player_uuid`)
