import com.sap.gateway.ip.core.customdev.util.Message;
import java.security.SecureRandom

/*
By Kangoolutions GmbH, Germany

Adds traceparent header to Custom Header Property to convert to Trace
*/

def Message processData(Message message) {
   
   def traceparent = message.getHeaders().get("traceparent")
   
    String traceId = null
    String parentSpanId = null
    String traceFlags = "00"
    
    String spanId = TraceUtil.generateSpanId()
   
   
   if(traceparent) {
       
        def traceparentParts = traceparent.split("-") 
        
        traceId = traceparentParts[1]
        parentSpanId = traceparentParts[2]
        spanId = TraceUtil.generateSpanId()
        traceFlags = traceparentParts[3]

        
   }
   
   if(!traceId) {
       traceId = TraceUtil.generateTraceId()
   }
   

        
    def newTraceparent = "00-$traceId-$spanId-$traceFlags"
    def messageLog = messageLogFactory.getMessageLog(message);
    messageLog.addCustomHeaderProperty("generated traceparent", newTraceparent);
    messageLog.addCustomHeaderProperty("traceId", traceId);
    if(parentSpanId && traceparent) {
       messageLog.addCustomHeaderProperty("parentSpanId", parentSpanId);     
       messageLog.addCustomHeaderProperty("incoming traceparent", traceparent);
    }

    messageLog.addCustomHeaderProperty("spanId", spanId);
    messageLog.addCustomHeaderProperty("traceFlags", traceFlags);
    
    message.setHeader("traceparent", newTraceparent)
    message.setProperty("traceparent", newTraceparent)
    
        


    return message;
}

class TraceUtil {
    static SecureRandom secureRandom = new SecureRandom()

    static String generateSpanId() {
        return generateRandomHex(8) // 8 Bytes = 16 Hex
    }

    static String generateTraceId() {
        return generateRandomHex(16) // 16 Bytes = 32 Hex
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
