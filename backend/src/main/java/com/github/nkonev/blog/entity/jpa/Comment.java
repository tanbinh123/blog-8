package com.github.nkonev.blog.entity.jpa;

import com.github.nkonev.blog.Constants;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment", schema = Constants.Schemas.POSTS)
@DynamicInsert
@DynamicUpdate
public class Comment {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String text;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private UserAccount owner;

    private long postId;

    @Generated(GenerationTime.INSERT)
    private LocalDateTime createDateTime;

    private LocalDateTime editDateTime;

    public Comment() { }

    public Comment(Long id, String text, long postId) {
        this.id = id;
        this.text = text;
    }

    public Comment(Long id, String text, long postId, UserAccount owner) {
        this(id, text, postId);
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getEditDateTime() {
        return editDateTime;
    }

    public void setEditDateTime(LocalDateTime editDateTime) {
        this.editDateTime = editDateTime;
    }
}
