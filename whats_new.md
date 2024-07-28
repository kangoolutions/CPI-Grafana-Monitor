| [README](README.md) | [Whats new?](whats_new.md) | [Getting Started](getting_started.md) | [How to use](how_to_use.md) | [Contribute](contribute.md) |     |
| ------------------- | -------------------------- | ------------------------------------- | --------------------------- | --------------------------- | --- |

# Release Notes

!! As long as we haven't reached version 1.0.0, there might be breaking changes between the versions. Data might be collected differently and data collection for some logs and metrics might be reset.

## 0.3.0

### Integration Flow (CPI) 1.3.0

[Feature] Send error status with traces

### Additional Scripts (CPI) 1.3.0

[Feature] Add new traceparent header if no traceparent header found

## 0.2.0

### Integration Flow (CPI) 1.2.0

[Improvement] No more initial time settings anymore
[Feature] Sending Traces for Integration Flows to open telemetry endpoint.
[Feature] Filter Integration Flows with regex for logs
[Fix] Last run date now directly in beginning of flow

### Additional Scripts (CPI) 1.2.0

[Feature] Script to read traceparent header and replace with new one for traces

## 0.1.1

This is the first public release.

### Dashboards

- Initial Version of Messages Dashboard
  - Shows statistics and list of messages in all connected tenants
- Initial Version of Integration Flows Dashboads
  - Shows statistics about deployed Integration Flows and Integration Flows with deployment errors.

### Integration Flow (1.0.18)

- Initial Version of the log and metrics collecting Integration Flow
