import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
def Message processData(Message message) {
    //cpu_usage,app=Trigger_for_Grafana,platform=cpi,env=dev metric=0.5
    
      def xml = new XmlSlurper().parse(message.getBody(Reader))

        def entries = []
        xml.IntegrationRuntimeArtifact.each { it ->
            def metric = 0
            if(it.Status.text() == "STARTED") {
                metric = 1
            }
            if(it.Status.text() == "STARTING") {
                metric = 0.5
            }
            entries << "INTEGRATION_FLOW_IS_RUNNING,iflow=${it.Id.text()},platform=cpi,env="+message.getProperties().get("env")+",tenant_name="+message.getProperties().get("tenant_name")+" metric=${metric}"
        }
        
        message.setBody(entries.join('\n'))

    return message;
}