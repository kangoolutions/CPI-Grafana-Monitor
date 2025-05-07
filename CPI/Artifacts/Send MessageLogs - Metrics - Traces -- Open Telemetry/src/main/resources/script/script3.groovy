import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.sql.Timestamp

import groovy.json.JsonSlurper
import com.sap.gateway.ip.core.customdev.util.Message
import java.time.Instant

Message processData(Message message) {
    def jsonValue = message.getBody(String)
    def metricName = message.getProperties().get("metric_name")
    def unit = message.getProperties().get("unit")
    def iflowPrefix = message.getProperties().get("iflow_prefix")
    def env = message.getProperties().get("env")
    def tenant = message.getProperties().get("tenant_name")
    def currentTimeNano = Instant.now().toEpochMilli() * 1_000_000

    def jsonSlurper = new JsonSlurper()
    def jsonObject = jsonSlurper.parseText(jsonValue)

    def dataPoints = []

    // Loop over "resource-usage" items
    jsonObject."resource-usage".each { line ->
        def iflow = line.context ? line.context.replaceAll(iflowPrefix, "") : null



        def attributes = [
            [ key: "service.namespace", value: [ stringValue: "CPI" ] ],
            [ key: "deployment.environment", value: [ stringValue: env ] ],
            [ key: "service.name", value: [ stringValue: tenant ] ]
        ]

        if (iflow) {
            attributes << [ key: "iflow", value: [ stringValue: iflow ] ]
        }

        dataPoints << [
            attributes   : attributes,
            timeUnixNano : currentTimeNano,
            asDouble     : line.value
        ]
    }

    // Add total-available metric as a separate entry
    def totalAvailablePoints = []
    if (jsonObject.'total-available') {
        totalAvailablePoints << [
            attributes   : [
                [ key: "service.namespace", value: [ stringValue: "CPI" ] ],
                [ key: "deployment.environment", value: [ stringValue: env ] ],
                [ key: "service.name", value: [ stringValue: tenant ] ]
            ],
            timeUnixNano : currentTimeNano,
            asDouble     : jsonObject.'total-available'
        ]
    }

    // Build metrics array
    def metrics = []

    if (!dataPoints.isEmpty()) {
        metrics << [
            name        : metricName,
            description : "Per-iflow metric from resource-usage",
            unit        : unit,
            gauge       : [ dataPoints: dataPoints ]
        ]
    }

    if (!totalAvailablePoints.isEmpty()) {
        metrics << [
            name        : metricName + "_total",
            description : "Total available value across all iFlows",
            unit        : unit,
            gauge       : [ dataPoints: totalAvailablePoints ]
        ]
    }

    // Full OTLP wrapper
    def otelJson = [
        resourceMetrics: [
            [
                resource: [
                    attributes: [
                        [ key: "service.name", value: [ stringValue: "sap-cpi" ] ]
                    ]
                ],
                scopeMetrics: [
                    [
                        scope: [
                            name   : "cpi.metrics.exporter",
                            version: "1.0"
                        ],
                        metrics: metrics
                    ]
                ]
            ]
        ]
    ]

    def jsonText = groovy.json.JsonOutput.toJson(otelJson)
    message.setBody(jsonText.getBytes("UTF-8"))
    message.setHeader("Content-Type", "application/json")

    return message
}