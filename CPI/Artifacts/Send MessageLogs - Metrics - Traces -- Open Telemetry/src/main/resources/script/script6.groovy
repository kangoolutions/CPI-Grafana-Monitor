import com.sap.gateway.ip.core.customdev.util.Message;

import groovy.json.JsonBuilder

def getPropertyValueByName(properties, targetName) {
    def match = properties.find { it.Name.text() == targetName }
    return match ? match.Value.text() : null
}

def processData(Message message) {

// get configured time zone
def timeZone = message.getProperties().get("timeZone")

def xml = new XmlSlurper(false, false).parse(message.getBody(Reader))
def numberOfLogs = 0

//some items might be filtered
def blacklist_regex_string = message.getProperties().get("integration_flow_filter_regex")  
def blacklist = null
if(blacklist_regex_string) {
    blacklist = ~blacklist_regex_string
}


def logEntries = xml.MessageProcessingLog.collect { entry ->

    //care for the filter
    def ignore = false
    if(blacklist) {
        def matcher = entry.IntegrationArtifact.Id.text() =~ blacklist
        if (matcher) {
            ignore = true
        }
    }

    if( !ignore && entry.status.text() != "PROCESSING") {
        numberOfLogs++

        def durationInMS = calculateDuration(entry.LogStart.text(), entry.LogEnd.text())

        def dateEnd = parseDate(entry.LogEnd.text())        
        def dateStart = parseDate(entry.LogStart.text())

        def start = dateStart.format("yyyy-MM-dd'T'HH:mm:ss.SSS z", TimeZone.getTimeZone(timeZone))
        def end = dateEnd.format("yyyy-MM-dd'T'HH:mm:ss.SSS z", TimeZone.getTimeZone(timeZone))
        
        
            
        def resourceLog = [
            resource: [
                attributes:[
                    [key: "service.name", value: [stringValue: message.getProperty('tenant_name')]],
                    [key: "service.namespace", value: [stringValue: "CPI"]],
                    [key: "otel.collector", value: [stringValue: message.getProperty('otel_collector')]],
                    [key: "deployment.environment", value: [stringValue: message.getProperty('env')]],
                    [key: "service.instance.id", value: [stringValue: "${entry.IntegrationArtifact.Id.text()}"]]
                ]
            ],
            scopeLogs: [
                [
                    scope: {},
                    logRecords: [
                        [
                            timeUnixNano: dateEnd.getTime()+"000000",
                            severityNumber: 9,
                            severityText: "Info",
                            body: [stringValue: "name=${entry.IntegrationArtifact.Id.text()} status=${entry.Status.text()} custom_status=${entry.CustomStatus.text()} log_level=${entry.LogLevel.text()} log_start=${start} log_end=${end} duration_ms=${durationInMS} application_message_id=${entry.ApplicationMessageId.text()} correlation_id=${entry.CorrelationId.text()} sender=${entry.Sender.text()} receiver=${entry.Receiver.text()} package_id=${entry.IntegrationArtifact.PackageId.text()} web_link=${entry.AlternateWebLink.text()} transaction_id=${entry.TransactionId.text()} message_guid=${entry.MessageGuid.text()} application_message_type=${entry.ApplicationMessageType.text()}${parseCustomHeaderProperties(entry.CustomHeaderProperties.MessageProcessingLogCustomHeaderProperty)}"],
                            attributes: [
                                [key: "app", value: [stringValue: "server"]],
                                [key: "status", value: [stringValue: "${entry.Status.text()}"]],
                                [key: "start_time", value: [stringValue: "${start}"]],
                                [key: "end_time", value: [stringValue: "${end}"]],
                                [key: "duration_ms", value: [stringValue: "${durationInMS}"]],
                                [key: "spanId", value: [stringValue: entry.ApplicationMessageId.text()]],
                                [key: "traceId", value: [stringValue: entry.CorrelationId.text()]]
                            ],
                            droppedAttributesCount: 1,
                            
                            
                        ]
                    ]
                ]
            ]
        ]

        return resourceLog
    }
}.flatten()

def json = new JsonBuilder()
json {
    resourceLogs logEntries
}

def jsonString = json.toPrettyString()
println jsonString

message.setBody(jsonString)
message.setHeader("Content-Type", "application/json")
message.setProperty("numberOfLogs", numberOfLogs.toString())
return message
}

def parseDate(String timestamp) {
    if(timestamp.size() == "2023-11-06T08:00:01".size()) {
       timestamp += ".000"
    } 

    timestamp = timestamp.padRight(23,"0")
    def parsedDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS",timestamp)
}

def calculateDuration(start, end) {
    def formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    def dateStart = formatter.parse(start)
    def dateEnd = formatter.parse(end)

    (dateEnd.time - dateStart.time)
}

def parseCustomHeaderProperties(properties) {
    properties.collect { prop ->
        " ${prop.Name.text()}=${prop.Value.text()}"
    }.join("")
    
}
