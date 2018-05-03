package com.liujie.gmall.config;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

public class ActiveMQUtil {
    PooledConnectionFactory pooledConnectionFactory=null;

    public ConnectionFactory init(String brokerUrl) {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        //加入连接池
        pooledConnectionFactory=new PooledConnectionFactory(factory);
        //出现异常时重新连接
        pooledConnectionFactory.setReconnectOnException(true);
        //
        pooledConnectionFactory.setMaxConnections(5);
        pooledConnectionFactory.setExpiryTimeout(10000);
        return pooledConnectionFactory;
    }

    public ConnectionFactory getConnectionFactory(){
        return pooledConnectionFactory;
    }
    public Connection getConnection(){

        Connection connection = null;
        try {
            connection = pooledConnectionFactory.createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return connection;
    }
}