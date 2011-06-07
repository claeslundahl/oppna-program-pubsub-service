/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.SubscriberTimeoutNotifier;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.content.AtomSerializer;

@Entity
@Table(name="TOPICS")
public class DefaultTopic extends AbstractEntity<URI> implements Topic {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultTopic.class);

    
    @Id
    @GeneratedValue
    @SuppressWarnings("unused") // only used by JPA
    private Long pk;
    
    @Column(nullable=false, unique=true)
    @Index(name="topic_url")
    private String url;
    
    @Transient
    private List<Subscriber> subscribers = new ArrayList<Subscriber>();
    
    @Transient
    private SubscriberTimeoutNotifier subscriberTimeoutNotifier;

    @Transient
    private PublicationRetryer publicationRetryer;

    // For JPA
    protected DefaultTopic() {
    }
    
    public DefaultTopic(URI url, SubscriberTimeoutNotifier subscriberTimoutNotifier, 
            PublicationRetryer publicationRetryer) {
        Assert.notNull(url);
        Assert.notNull(subscriberTimoutNotifier);
        
        this.url = url.toString();
        this.subscriberTimeoutNotifier = subscriberTimoutNotifier;
        this.publicationRetryer = publicationRetryer;
    }
    
    @Override
    public URI getId() {
        return getUrl();
    }

    
    @Override
    public URI getUrl() {
        return URI.create(url);
    }

    @Override
    public synchronized void publish(Feed feed) {
        LOG.info("Publishing on topic {}", url);

        if(!subscribers.isEmpty()) {
            // if all publications success, purge until now
            DateTime lastUpdatedSubscriber = new DateTime();
            for(Subscriber subscriber : subscribers) {
                try {
                	
                    publish(subscriber, feed);
                } catch (Exception e) {
                    LOG.warn("Subscriber failed: {}", e.getMessage());
                    
                    if(publicationRetryer != null) {
                        publicationRetryer.addRetry(this, subscriber, feed);
                    }
                    lastUpdatedSubscriber = subscriber.getLastUpdated();
                }
            }
        } else {
            LOG.info("No subscribers for topic {}, publication dropped", url);
        }
        // TODO purge old entries based on lastUpdatedSubscriber
    }
    
    protected synchronized void publish(Subscriber subscriber, Feed feed) throws PublicationFailedException {
        LOG.info("Publishing to {}", subscriber);
        subscriber.publish(feed);
    }

    @Override
    public synchronized void addSubscriber(Subscriber subscriber) {
        Assert.notNull(subscriber);
        
        removeSubscriber(subscriber);
        
        subscriberTimeoutNotifier.addSubscriber(subscriber);
        subscribers.add(subscriber);
    }

    @Override
    public synchronized void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    protected void setSubscriberTimeoutNotifier(SubscriberTimeoutNotifier subscriberTimoutNotifier) {
        this.subscriberTimeoutNotifier = subscriberTimoutNotifier;
    }
}
