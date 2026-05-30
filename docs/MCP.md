# MCP Setup Notes

No MCP servers were exposed to the active Codex runtime during implementation, so local filesystem tools and official web/Maven/npm sources were used instead.

Use `.codex/mcp.example.toml` as a starting point for a future Codex restart/session. Secret-backed MCPs should be enabled only after the relevant environment variables exist.

Required local tools found:

- `node`, `npm`, `npx`
- `docker`
- `git`
- `gh`, but not logged in

Not found:

- `uv` / `uvx`
- active MCP resources/templates in the current session

Recommended enablement order:

1. Filesystem MCP restricted to this project path.
2. Fetch MCP after installing `uvx` or an equivalent trusted fetch server.
3. GitHub MCP after setting `GITHUB_PERSONAL_ACCESS_TOKEN` or logging in with a compatible GitHub MCP auth flow.
4. Docker MCP only if Docker Desktop MCP tooling is available.
5. PostgreSQL MCP only when the project adds a backend database.
6. Brave Search MCP only after setting `BRAVE_API_KEY`.

