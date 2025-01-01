package com.schemarise.alfa.runtime_int;

import schemarise.alfa.runtime.model.Pair;
import com.schemarise.alfa.runtime.codec.json.IJsonParserWrapper;
import com.schemarise.alfa.runtime.utils.stream.ValueEvent;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventPoller;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public final class RingBufferJsonParserWrapper
        // implements IJsonParserWrapper, EventHandler<ValueEvent>
{
//    private final RingBuffer<ValueEvent> m_ringBuffer;
//    private final JsonParser m_parser;
//    private final EventPoller<ValueEvent> m_poller;
//    private TokenWrapper m_previousToken;
//    private TokenWrapper m_eventToken;
//    private TokenWrapper m_currentToken;
//    private final TokenWrapper TerminalObject = new TokenWrapper();
//    private Disruptor<ValueEvent> m_disruptor;
//    private CountDownLatch m_latch;
//
//    public RingBufferJsonParserWrapper(JsonParser p) throws Exception {
//        m_parser = p;
//
//        m_disruptor = new Disruptor<ValueEvent>(
//                ValueEvent.EVENT_FACTORY,
//                8192,
//                DaemonThreadFactory.INSTANCE,
//                ProducerType.SINGLE,
//                new BusySpinWaitStrategy());
//
//        m_ringBuffer = m_disruptor.getRingBuffer();
//        m_poller = m_ringBuffer.newPoller();
//
//        m_disruptor.handleEventsWith(this);
//
//        m_disruptor.start();
//        Executors.newSingleThreadExecutor().submit(() -> {
//            try {
//                while (p.nextToken() != null )
//                {
//                    deposit(new TokenWrapper(p));
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            // end of tokens
//            deposit(TerminalObject);
//        });
//    }
//
//    long counter = 0;
//    @Override
//    public void onEvent(ValueEvent o, long l, boolean b) throws Exception {
//        counter++;
//
//        if ( counter % 10000000 == 0 )
//            System.out.println(".");
//
////        try {
////            m_latch = new CountDownLatch(1);
////            m_eventToken = (TokenWrapper) o.getValue();
////            m_latch.await();
////        } catch (Exception e) {
////            throw new RuntimeException(e);
////        }
////
////        if ( m_eventToken == TerminalObject ) {
////            m_disruptor.shutdown();
////        }
//    }
//
//    @Override
//    public JsonToken nextToken() {
//        try {
//            return _nextToken();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//    private JsonToken _nextToken() throws Exception {
//
//        while ( m_eventToken == null || m_eventToken == m_currentToken ) {
//            Thread.sleep(0, 10);
//        }
//
//        m_previousToken = m_currentToken;
//        m_currentToken = m_eventToken;
//
//        m_latch.countDown();
//
//        if ( m_currentToken == TerminalObject ) {
//            return null;
//        }
//        else {
//            return m_currentToken.token;
//        }
//    }
//
//    private void deposit(TokenWrapper e) {
//        long sequenceId = m_ringBuffer.next();
//        ValueEvent ve = m_ringBuffer.get(sequenceId);
//        ve.setValue(e);
//
//        m_ringBuffer.publish(sequenceId);
//    }
//
//    @Override
//    public int getIntValue() {
//        return (Integer) m_currentToken.value;
//    }
//
//    @Override
//    public String getText() {
//        return (String) m_currentToken.value;
//    }
//
//    @Override
//    public double getDoubleValue() {
//        return (Double) m_currentToken.value;
//    }
//
//    @Override
//    public short getShortValue() {
//        return (Short) m_currentToken.value;
//    }
//
//    @Override
//    public long getLongValue() {
//        return (Long) m_currentToken.value;
//    }
//
//    @Override
//    public byte getByteValue() {
//        return 0;
//    }
//
//    @Override
//    public byte[] getBinaryValue() {
//        return new byte[0];
//    }
//
//    @Override
//    public char[] getTextCharacters() {
//        return new char[0];
//    }
//
//    @Override
//    public boolean getBooleanValue() {
//        return false;
//    }
//
//    @Override
//    public void pushBackLastToken( {
//
//    }
//
//    @Override
//    public Pair<Integer, Integer> getOffset() {
//        return null;
//    }
//
//    @Override
//    public JsonToken currentToken() {
//        return m_currentToken.token;
//    }
//
//    @Override
//    public void skipChildren() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public String getCurrentName() {
//        return (String) m_currentToken.value;
//    }
//
//    @Override
//    public JsonLocation getCurrentLocation() {
//        return m_currentToken.location;
//    }
//
//    @Override
//    public BigDecimal getBigDecimalValue() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public String getCurrentLocationStr() {
//        return m_currentToken.location.toString();
//    }
//
//
//    public static final class TokenWrapper {
//        private final JsonToken token;
//        private final JsonLocation location;
//        private Object value;
//
//        @Override
//        public String toString() {
//            return "TokenWrapper {" +
//                    "token=" + token +
//                    ", value=" + value +
//                    '}';
//        }
//
//        public JsonToken getToken() {
//            return token;
//        }
//
//        public Object getValue() {
//            return value;
//        }
//
//        public TokenWrapper()  {
//            token = null;
//            location = null;
//            value = null;
//        }
//
//        public JsonLocation getLocation() {
//            return location;
//        }
//
//        public TokenWrapper(JsonParser p) throws IOException {
//            token = p.currentToken();
//
//            this.location = p.currentLocation();
//
//            switch (token) {
//                case VALUE_STRING:
//                    value = p.getText();
//                    break;
//
//                case VALUE_NUMBER_INT:
//                    value = p.getNumberValue();
//                    break;
//
//                case VALUE_NUMBER_FLOAT:
//                    value = p.getFloatValue();
//                    break;
//
//                case FIELD_NAME:
//                    value = p.currentName();
//                    break;
//
//                case VALUE_NULL:
//                    value = null;
//                    break;
//
//                case VALUE_TRUE:
//                    value = true;
//                    break;
//
//                case VALUE_FALSE:
//                    value = false;
//                    break;
//
//                case END_ARRAY:
//                case END_OBJECT:
//                case START_ARRAY:
//                case START_OBJECT:
//                case NOT_AVAILABLE:
//                case VALUE_EMBEDDED_OBJECT:
//            }
//        }
//    }
}
