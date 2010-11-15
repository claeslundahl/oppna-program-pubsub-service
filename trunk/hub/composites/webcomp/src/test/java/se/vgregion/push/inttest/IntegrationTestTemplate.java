package se.vgregion.push.inttest;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.servlet.DispatcherServlet;

import se.vgregion.push.types.Feed;

public class IntegrationTestTemplate {

    private Server server;
    protected BlockingQueue<Boolean> verifications = new LinkedBlockingQueue<Boolean>();
    protected BlockingQueue<Feed> publishedFeeds = new LinkedBlockingQueue<Feed>();
    protected Publisher publisher;
    protected URI hubUrl;
    
    @Before
    public void setUpComponents() throws Exception {
        server = new Server(8080);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
 
        context.getInitParams().put("contextConfigLocation", "classpath*:services-config.xml");
        
        DispatcherServlet servlet = new DispatcherServlet();
        
        servlet.setContextConfigLocation("classpath:hub-servlet.xml, classpath*:services-config.xml");
        
        servlet.init(new MockServletConfig());
        
        context.addServlet(new ServletHolder(servlet),"/*");
        server.start();
        
        hubUrl = URI.create("http://localhost:8080");
        
        publisher = new Publisher();
        
        Subscriber subscriber = new Subscriber(createSubscriberResult());
        subscriber.addListener(new SubscriberListener() {
            @Override
            public void published(Feed feed) {
                publishedFeeds.add(feed);
            }

            @Override
            public void verified() {
                verifications.add(true);                
            }
        });
        
        subscriber.subscribe(hubUrl, publisher.getUrl());
        
    }
    
    protected SubscriberResult createSubscriberResult() {
        return null;
    }
    
    @After
    public void stopServer() throws Exception {
        server.stop();
    }
}
