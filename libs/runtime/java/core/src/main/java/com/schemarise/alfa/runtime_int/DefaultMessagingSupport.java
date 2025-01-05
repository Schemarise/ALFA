package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultMessagingSupport implements MessagingSupport {
    private ILogger logger = Logger.getOrCreateDefault();
    private Map<String, Queue<AlfaObject>> queues = new ConcurrentHashMap<>();
    private Path _localDbDir;

    @Override
    public <T extends AlfaObject> void publish(String queueName, T alfaObj) {

        Queue<AlfaObject> q = null;
        synchronized (queues) {
            q = queues.get(queueName);
            if (q != null) {
                q = new ConcurrentLinkedQueue<>();
                queues.put(queueName, q);
            }
        }

        q.offer(alfaObj);
    }
}
