    import com.sap.gateway.ip.core.customdev.util.Message;
    import java.util.HashMap;
    import java.sql.Timestamp
    
   import groovy.json.JsonBuilder
   
   import java.security.SecureRandom

def processData(Message message) {
    
    // get configured time zone
    def timeZone = message.getProperties().get("timeZone")
    def env = message.getProperties().get("env")
    def tenant_name = message.getProperties().get("tenant_name")
    
    def xml = new XmlSlurper(false, false).parse(message.getBody(Reader))
    def numberOfTraces = 0

       
    //some items might be filtered
    def blacklist_regex_string = message.getProperties().get("integration_flow_filter_regex")  
    def blacklist = null
    if(blacklist_regex_string) {
        blacklist = ~blacklist_regex_string
    }
    
    def entries = []

    xml.MessageProcessingLog.each { entry ->
    
        //care for the filter
        def ignore = false
        if(blacklist) {
            def matcher = entry.IntegrationArtifact.Id.text() =~ blacklist
            if (matcher) {
                return
            }
        }
        
        TracingData traceData = getTraceDataFromCustomHeaderProperties(entry.CustomHeaderProperties.MessageProcessingLogCustomHeaderProperty)
        
        def traceId = traceData.traceId
        def spanId = traceData.spanId
        def parentSpanId = traceData.parentSpanId
        
        
    
        if( !ignore && entry.status.text() != "PROCESSING" && traceId && spanId && traceId.size() == 32 && spanId.size() == 16) {
            
 
            
            numberOfTraces++
        
            def dateEnd = parseDate(entry.LogEnd.text())        
            def dateStart = parseDate(entry.LogStart.text())

            def start = dateStart.format("yyyy-MM-dd'T'HH:mm:ss.SSS z", TimeZone.getTimeZone(timeZone))
            def end = dateEnd.format("yyyy-MM-dd'T'HH:mm:ss.SSS z", TimeZone.getTimeZone(timeZone))

            entries << [
                attributes: [
                    [
                        key:"integrationArtifactId",
                        value: [
                            stringValue: entry.IntegrationArtifact.Id.text()
                        ]
                    ]
                ],
                startTimeUnixNano: dateStart.getTime()+"000000",
                endTimeUnixNano: dateEnd.getTime()+"000000",
                traceId: traceId,
                spanId: spanId,
                parentSpanId: parentSpanId,
                name: "SAP CI: " + entry.IntegrationArtifact.Id.text(),
                kind: 2,
                droppedAttributesCount: 0,
                events: [],
                droppedEventsCount: 0,
                status: [
                    code: 1
                ]
            ]
        }
    }
    
    println entries
        
    def json = new JsonBuilder()
    json {
        resourceSpans([
            [
                resource: [
                    attributes: [
                        [
                            key: "service.name",
                            value: [
                                 stringValue: tenant_name
                            ]         
                        ],
                        [
                            key: "otel.collector",
                            value: [
                                stringValue: "Kangoolutions Integration Flow"
                            ]
                        ]
                    ]
                ],
                scopeSpans: [              
                    [
                        scope: [
                            name: "SAP Cloud Integration"
                        ],
                        spans: entries
                    ]
                ]
            ]
        ])
    }
            

    def jsonString = json.toPrettyString()
    println jsonString

    message.setBody(jsonString)

    message.setHeader("Content-Type", "application/json")
    message.setProperty("numberOfTraces", numberOfTraces)

    return message
}

class TracingData {
    String traceId
    String spanId
    String parentSpanId
} 

def getTraceDataFromCustomHeaderProperties(properties) {
    TracingData data = new TracingData()
    properties.each {
        property ->
        if(property.Name.text().toLowerCase() == "traceid" ) {
            data.traceId = property.Value.text()
        }
        
        if(property.Name.text().toLowerCase() == "spanid" ) {
            data.spanId = property.Value.text()
        }
        
        if(property.Name.text().toLowerCase() == "parentspanid" ) {
            data.parentSpanId = property.Value.text()
        }
    }
    
    return data
}

def parseDate(String timestamp) {
    if(timestamp.size() == "2023-11-06T08:00:01".size()) {
       timestamp += ".000"
    } 
    timestamp = timestamp.padRight(23,"0")
    def parsedDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS",timestamp)
}

class TraceUtil {
    static SecureRandom secureRandom = new SecureRandom()

    static String generateSpanId() {
        return generateRandomHex(8) // 8 Bytes = 16 Hex-Zeichen
    }

    static String generateTraceId() {
        return generateRandomHex(16) // 16 Bytes = 32 Hex-Zeichen
    }

    private static String generateRandomHex(int byteLength) {
        byte[] bytes = new byte[byteLength]
        secureRandom.nextBytes(bytes)
        StringBuilder hexString = new StringBuilder(byteLength * 2)
        for (byte b : bytes) {
            hexString.append(String.format('%02x', b))
        }
        return hexString.toString()
    }
}