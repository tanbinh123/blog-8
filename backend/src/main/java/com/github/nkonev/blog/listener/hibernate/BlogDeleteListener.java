package com.github.nkonev.blog.listener.hibernate;

import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.services.WebSocketService;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlogDeleteListener implements PostDeleteEventListener {
    private static final long serialVersionUID = 3341150678491703105L;

    private static transient final Logger LOGGER = LoggerFactory.getLogger(BlogDeleteListener.class);

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (event.getEntity() instanceof Post) {
            Post post = (Post) event.getEntity();
            webSocketService.sendDeletePostEvent(post.getId());
            LOGGER.debug("sql delete: {}", post);
        }

    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}