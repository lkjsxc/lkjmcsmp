package com.lkjmcsmp.domain.model;

import java.time.Instant;
import java.util.UUID;

public record TpaRequest(UUID from, UUID to, boolean summonHere, Instant expiresAt) {
}
