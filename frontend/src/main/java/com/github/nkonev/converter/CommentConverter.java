package com.github.nkonev.converter;

import com.github.nkonev.dto.CommentDTO;
import com.github.nkonev.dto.CommentDTOExtended;
import com.github.nkonev.dto.CommentDTOWithAuthorization;
import com.github.nkonev.dto.UserAccountDetailsDTO;
import com.github.nkonev.entity.jpa.Comment;
import com.github.nkonev.entity.jpa.Permissions;
import com.github.nkonev.exception.BadRequestException;
import com.github.nkonev.security.BlogSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Service
public class CommentConverter {

    @Autowired
    private BlogSecurityService blogSecurityService;

    public Comment convertFromDto(CommentDTO commentDTO, long postId, Comment forUpdate) {
        Assert.notNull(commentDTO, "commentDTO can't be null");
        checkLength(commentDTO.getText());
        if (forUpdate == null) {
            forUpdate = new Comment();
            forUpdate.setPostId(postId);
        }
        forUpdate.setText(commentDTO.getText());
        return forUpdate;
    }

    private void checkLength(String comment) {
        String trimmed = StringUtils.trimWhitespace(comment);
        final int MIN_COMMENT_LENGTH = 1;
        if (trimmed == null || trimmed.length() < MIN_COMMENT_LENGTH) {
            throw new BadRequestException("comment too short, must be longer than " + MIN_COMMENT_LENGTH);
        }
    }

    public CommentDTOWithAuthorization convertToDto(Comment comment, UserAccountDetailsDTO userAccount) {
        Assert.notNull(comment, "comment can't be null");

        return new CommentDTOWithAuthorization(
                comment.getId(),
                comment.getText(),
                UserAccountConverter.convertToUserAccountDTO(comment.getOwner()),
                blogSecurityService.hasCommentPermission(comment, userAccount, Permissions.EDIT),
                blogSecurityService.hasCommentPermission(comment, userAccount, Permissions.DELETE)
        );
    }

    public CommentDTO convertToDto(Comment comment) {
        Assert.notNull(comment, "comment can't be null");

        return new CommentDTO(
                comment.getId(),
                comment.getText()
        );

    }

    public CommentDTOExtended convertToDtoExtended(Comment comment, UserAccountDetailsDTO userAccount, long countInPost) {
        return new CommentDTOExtended(
                comment.getId(),
                comment.getText(),
                UserAccountConverter.convertToUserAccountDTO(comment.getOwner()),
                blogSecurityService.hasCommentPermission(comment, userAccount, Permissions.EDIT),
                blogSecurityService.hasCommentPermission(comment, userAccount, Permissions.DELETE),
                countInPost
        );
    }
}