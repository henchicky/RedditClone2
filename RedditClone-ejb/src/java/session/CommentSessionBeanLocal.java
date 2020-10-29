package session;

import entity.Comment;
import exception.UnknownPersistenceException;
import javax.ejb.Local;

@Local
public interface CommentSessionBeanLocal {

    public Comment createComment(Long postid, Comment newComment) throws UnknownPersistenceException;

    public Comment getCommentByCommentId(Long commentId);

    public Comment updateComment(Comment comment);

    public Comment replyComment(Long postid, Long parentcommentid, Comment replyComment);

}
