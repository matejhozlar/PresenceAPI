import express from "express";
import jwt from "jsonwebtoken";

const app = express();
const PORT = 5000;
const JWT_SECRET = "CHANGE-ME-must-be-at-least-32-chars"; // Must match your mod config

// Middleware to parse JSON
app.use(express.json());

// JWT verification middleware
function verifyJWT(req, res, next) {
  const authHeader = req.headers["authorization"];

  if (!authHeader) {
    return res.status(401).json({ error: "Missing Authorization header" });
  }

  const token = authHeader.split(" ")[1];

  if (!token) {
    return res.status(401).json({ error: "Invalid Authorization format" });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (error) {
    return res
      .status(403)
      .json({ error: "Invalid or expired token", details: error.message });
  }
}

// Presence endpoint
app.post("/api/presence", verifyJWT, (req, res) => {
  const {
    minecraftUsername,
    uuid,
    state,
    displayName,
    gamemode,
    dimension,
    position,
    health,
    experienceLevel,
    ipAddress,
    serverId,
    timestamp,
  } = req.body;

  // Log the received data
  console.log("\n=== Player Presence Event ===");
  console.log(`Server ID: ${serverId}`);
  console.log(`Player: ${minecraftUsername} (${uuid})`);
  console.log(`State: ${state}`);
  console.log(`Display Name: ${displayName || "N/A"}`);
  console.log(`Gamemode: ${gamemode || "N/A"}`);
  console.log(`Dimension: ${dimension || "N/A"}`);

  if (position) {
    console.log(`Position: X=${position.x}, Y=${position.y}, Z=${position.z}`);
  }

  if (health !== undefined) {
    console.log(`Health: ${health}`);
  }

  if (experienceLevel !== undefined) {
    console.log(`Experience Level: ${experienceLevel}`);
  }

  if (ipAddress) {
    console.log(`IP Address: ${ipAddress}`);
  }

  console.log(`Timestamp: ${new Date(timestamp).toISOString()}`);
  console.log("============================\n");

  // Respond with success
  res.status(200).json({
    success: true,
    message: `Presence recorded for ${minecraftUsername}`,
    receivedAt: new Date().toISOString(),
  });
});

// Heartbeat endpoint
// Matches POST /api/presence/heartbeat in Createrington/app PresenceController.heartbeat()
// Expects: { players: Array<{ uuid, username }>, serverId?: number, timestamp?: number }
// Returns the same shape as the real controller so the mod's response handling works unchanged
app.post("/api/presence/heartbeat", verifyJWT, (req, res) => {
  const { players, serverId, timestamp } = req.body;

  if (!Array.isArray(players)) {
    return res.status(400).json({ error: "players must be an array" });
  }

  const uuidRegex =
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

  const onlinePlayers = players.filter(
    (p) => p.uuid && p.username && uuidRegex.test(p.uuid),
  );

  const targetServerId =
    serverId != null ? parseInt(String(serverId), 10) : null;

  console.log("\n=== Heartbeat Received ===");
  console.log(
    `Server ID: ${targetServerId != null ? targetServerId : "not specified"}`,
  );
  console.log(
    `Players reported: ${onlinePlayers.length} (${players.length - onlinePlayers.length} filtered out)`,
  );

  for (const p of onlinePlayers) {
    console.log(`  - ${p.username} (${p.uuid})`);
  }

  if (timestamp) {
    console.log(`Mod timestamp: ${new Date(timestamp).toISOString()}`);
  }

  console.log("=========================\n");

  res.status(200).json({
    success: true,
    message: "Heartbeat processed",
    data: {
      serverId: targetServerId,
      playersReported: onlinePlayers.length,
      receivedAt: new Date().toISOString(),
    },
  });
});

// Health check endpoint
app.get("/health", (req, res) => {
  res.status(200).json({ status: "ok", timestamp: new Date().toISOString() });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error("Error:", err);
  res
    .status(500)
    .json({ error: "Internal server error", details: err.message });
});

const testToken = jwt.sign({}, JWT_SECRET);
console.log("Use this token for PowerShell testing:\nBearer " + testToken + "\n");
// Start server
app.listen(PORT, () => {
  console.log(`Presence API Server running on http://localhost:${PORT}`);
  console.log(`Endpoints:`);
  console.log(`  POST http://localhost:${PORT}/api/presence          (join/leave events)`);
  console.log(`  POST http://localhost:${PORT}/api/presence/heartbeat (heartbeat / manual sync)`);
  console.log(`  GET  http://localhost:${PORT}/health`);
  console.log(`JWT Secret: ${JWT_SECRET}`);
  console.log(`\nWaiting for player events...\n`);
});
