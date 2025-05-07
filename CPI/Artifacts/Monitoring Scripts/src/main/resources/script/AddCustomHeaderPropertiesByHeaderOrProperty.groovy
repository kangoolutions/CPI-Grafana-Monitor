import com.sap.gateway.ip.core.customdev.util.Message;
//by Dominic Beckbauer - Kangoolutions GmbH 2024

def Message processData(Message message) {
    
    def additionalTrackingObjects = ["traceid", "traceparent", "requestid" ]

    def messageLog = messageLogFactory.getMessageLog(message);

    def iterationElementsLists = [message.getProperties(), message.getHeaders()]
                                 

    if(messageLog != null) {
        iterationElementsLists.each { elements ->
                                      elements.each { key, value ->
	        if (key.toLowerCase().startsWith("ch_")) 
					{
		        println("$key - $value")
	          messageLog.addCustomHeaderProperty(key.substring(3), value);
	        }
	
	        if(additionalTrackingObjects.contains(key.toLowerCase().replaceAll("_","").replaceAll("-",""))) 
					{
		        println("$key - $value")
	          messageLog.addCustomHeaderProperty(key, value);
	        }
	      }
      }
    }
    return message;
}