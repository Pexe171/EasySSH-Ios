#!/usr/bin/env sh
set -eu

if [ -z "${AUTHORIZED_KEYS:-}" ]; then
  echo "AUTHORIZED_KEYS is required" >&2
  exit 1
fi

printf '%s\n' "$AUTHORIZED_KEYS" > /home/easyssh/.ssh/authorized_keys
chown easyssh:easyssh /home/easyssh/.ssh/authorized_keys
chmod 600 /home/easyssh/.ssh/authorized_keys

exec /usr/sbin/sshd -D -e

