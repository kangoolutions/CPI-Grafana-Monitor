import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.sql.Timestamp

import groovy.json.JsonSlurper

def processData(Message message) {

    def jsonValue = message.getBody(java.lang.String)
    
    def jsonSlurper = new JsonSlurper()
    def jsonObject = jsonSlurper.parseText(jsonValue)
    
    def metricValue = jsonObject."resource-usage"[0].value
    def totalAvailable = jsonObject.'total-available'
    
    def String metricString =  ""
    
    jsonObject."resource-usage".each {
        line ->
        metricString +=  "CPI_TOTAL_MESSAGES,platform=cpi,env=${message.getProperties().get('env')},tenant_name=${message.getProperties().get('tenant_name')},iflow=${line.context.replaceAll('mpl:','')} metric=${line.value}\n"
   
    }
    
    message.setBody(metricString)

    return message
}