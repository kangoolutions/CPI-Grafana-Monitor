/* Refer the link below to learn more about the use cases of script.
https://help.sap.com/viewer/368c481cd6954bdfa5d0435479fd4eaf/Cloud/de-DE/148851bf8192412cba1f9d2c17f4bd25.html

If you want to know more about the SCRIPT APIs, refer the link below
https://help.sap.com/doc/a56f52e1a58e4e2bac7f7adbf45b2e26/Cloud/de-DE/index.html */
import com.sap.gateway.ip.core.customdev.util.Message;
import java.time.Instant
import java.util.HashMap;
def Message processData(Message message) {
    //cpu_usage,app=Trigger_for_Grafana,platform=cpi,env=dev metric=0.5
    
      def xml = new XmlSlurper().parse(message.getBody(Reader))
      def currentTime = Instant.now().toEpochMilli() * 1_000_000 

    def env = message.getProperties().get("env")
    def tenant = message.getProperties().get("tenant_name")

        def metrics = []

    xml.IntegrationRuntimeArtifact.each { it ->
        def status = it.Status.text()
        def iflowId = it.Id.text()
        def metricValue = 0

        if (status == "STARTED") {
            metricValue = 1
        } else if (status == "STARTING") {
            metricValue = 0.5
        } else {
            metricValue = 0
        }


        // Only include metrics with value > 0 (optional)
        def metricJson = """
        {
          "name": "integration_flow_is_running_total",
          "description": "Indicates whether the iFlow is running (1=running, 0.5=starting, 0=stopped)",
          "unit": "1",
          "gauge": {
            "dataPoints": [
              {
                "attributes": [
                  { "key": "iflow", "value": { "stringValue": "$iflowId" } },
                  { "key": "service.namespace", "value": { "stringValue": "cpi" } },
                  { "key": "deployment.environment", "value": { "stringValue": "$env" } },
                  { "key": "service.name", "value": { "stringValue": "$tenant" } },
                  { "key": "status", "value": { "stringValue": "$status" } }
                ],
                "timeUnixNano": "$currentTime",
                "asDouble": $metricValue
              }
            ]
          }
        }
        """
        metrics.add(metricJson)
    }

    // Build full OTLP wrapper
    def finalJson = """
    {
      "resourceMetrics": [
        {
          "resource": {
            "attributes": [
              { "key": "service.name", "value": { "stringValue": "sap-cpi" } }
            ]
          },
          "scopeMetrics": [
            {
              "scope": {
                "name": "cpi.metrics.exporter",
                "version": "1.0"
              },
              "metrics": [
                ${metrics.join(",")}
              ]
            }
          ]
        }
      ]
    }
    """
    
    def jsonBytes = finalJson.getBytes("UTF-8")
    message.setBody(jsonBytes)

    message.setHeader("Content-Type", "application/json")
    message.setHeader("Accept", "application/json")

    return message;
}