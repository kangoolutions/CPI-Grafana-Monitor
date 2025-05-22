import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.sql.Timestamp

import groovy.json.JsonSlurper

def processData(Message message) {

    def jsonValue = message.getBody(java.lang.String)
    def metric_name = message.getProperties().get("metric_name")
    def iflow_prefix =  message.getProperties().get("iflow_prefix")
    
    def jsonSlurper = new JsonSlurper()
    def jsonObject = jsonSlurper.parseText(jsonValue)

    def totalAvailable = jsonObject.'total-available'
    
    def String metricString =  ""
    
    jsonObject."resource-usage".each {
        line ->
        def context = ""
        if(line.context) {
            context = ",iflow=${line.context.replaceAll(iflow_prefix,'')}"
        }
        metricString +=  "${metric_name},platform=cpi,env=${message.getProperties().get('env')},tenant_name=${message.getProperties().get('tenant_name')}$context metric=${line.value}\n"
   
    }
    
    if(jsonObject."total-available") {
    metricString +=  "${metric_name + '_total'},platform=cpi,env=${message.getProperties().get('env')},tenant_name=${message.getProperties().get('tenant_name')} metric=${jsonObject.'total-available'}\n"
        
    
    }
    
    message.setBody(metricString)

    return message
}