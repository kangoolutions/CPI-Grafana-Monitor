import com.sap.gateway.ip.core.customdev.util.Message;
def Message processData(Message message) {
    
    message.setHeader("traceparent", message.getProperties().get("traceparent"))

    return message;
}