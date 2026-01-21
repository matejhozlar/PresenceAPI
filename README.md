# PresenceAPI

PresenceAPI is a lightweight NeoForge server-side mod that tracks player presence and synchronizes it with an external backend via configurable HTTP requests.

Built for server networks, dashboards, analytics platforms, and custom integrations, PresenceAPI provides a clean and reliable way to react to player activity in real time.

## ✨ Features

Detects player join and leave events
Sends HTTP/HTTPS requests to external services
Fully configurable endpoints, headers, and payloads
Server-side only — no client installation required
Lightweight and performance-friendly
How It Works

When a player joins or leaves the server, PresenceAPI sends an HTTP request to your configured endpoint containing customizable player and server data.

This allows external systems to instantly react to server activity.

## Configuration

PresenceAPI includes a flexible configuration system that allows you to:

Define request URLs
Customize headers (authentication, tokens, etc.)
Configure request payload data (player, server, event type)
Enable or disable specific events

All behavior can be adjusted without modifying code.

## Use Cases

Player presence tracking
Multi-server network coordination
Web dashboards and status pages
Discord bots and webhook integrations
Analytics, logging, and monitoring
