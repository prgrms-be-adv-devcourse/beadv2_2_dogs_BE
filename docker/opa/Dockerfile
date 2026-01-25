FROM openpolicyagent/opa:0.63.0
WORKDIR /app
COPY baro-cloud/opa/opa-config.yaml /app/opa-config.yaml

EXPOSE 8181
ENTRYPOINT ["/opa", "run", "--server", "--config-file=/app/opa-config.yaml"]
