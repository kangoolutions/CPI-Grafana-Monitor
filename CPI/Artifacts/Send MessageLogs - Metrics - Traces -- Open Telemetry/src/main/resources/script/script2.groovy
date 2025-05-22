/* Refer the link below to learn more about the use cases of script.
https://help.sap.com/viewer/368c481cd6954bdfa5d0435479fd4eaf/Cloud/de-DE/148851bf8192412cba1f9d2c17f4bd25.html

If you want to know more about the SCRIPT APIs, refer the link below
https://help.sap.com/doc/a56f52e1a58e4e2bac7f7adbf45b2e26/Cloud/de-DE/index.html */
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