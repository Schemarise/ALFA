package com.schemarise.alfa.runtime;


public interface MessagingSupport {
    <T extends AlfaObject> void publish(String queueName, T alfaObj);
}
