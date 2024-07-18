| [README](README.md) | [Whats new?](whats_new.md) | [Getting Started](getting_started.md) | [How to use](how_to_use.md) | [Contribute](contribute.md) |     |
| ------------------- | -------------------------- | ------------------------------------- | --------------------------- | --------------------------- | --- |

# Tracing

## How does it work

The "traceparent" http header is used to provide tracing and parentspan id. The Cloud Integration will send tracing information via Open Telemetry protocol to your tracing tool like Grafana Cloud. CustomHeaderProperties are used to transport all necessary information.

## How to Use

1. "traceparent" needs to be an allowed header in your Integration Flow, in case your sending system sends this header
2. Use the script "" in the beginning of your Integratin Flow to read incoming traceparen header and replace with own traceparent. It will create some CustomHeaderProperties which are necessary.
3. To propagate tracing to following systems, make sure the (overwritten) traceparent header is sent to following systems or Integration Flows.
4. Activate sending traces and fill out OTel credentials in main Integration Flow.

You can use ProcessDirect and trace multiple Integration Flows. Just make sure to follow 1-4 with each Integration Flow that should be traced

## Troubleshooting

- Make sure traceparent is correct
- spanId must be 16 hex
- traceId must be 32 hex
