FROM debian:bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends openssh-server ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -m -s /bin/bash easyssh \
    && mkdir -p /run/sshd /home/easyssh/.ssh \
    && chown -R easyssh:easyssh /home/easyssh/.ssh \
    && chmod 700 /home/easyssh/.ssh

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 22
ENTRYPOINT ["/entrypoint.sh"]

