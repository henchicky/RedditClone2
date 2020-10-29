package session;

import entity.Comment;
import entity.Post;
import exception.UnknownPersistenceException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class CommentSessionBean implements CommentSessionBeanLocal {

    @PersistenceContext(unitName = "RedditClone-ejbPU")
    private EntityManager em;

    @Override
    public Comment createComment(Long postid, Comment newComment) throws UnknownPersistenceException {
        Post post = em.find(Post.class, postid);
        post.getComments().add(newComment);
        em.persist(newComment);
        return newComment;
    }

    @Override
    public Comment getCommentByCommentId(Long commentId) {
        return em.find(Comment.class, commentId);
    }

    @Override
    public Comment updateComment(Comment editedcomment) {
        Comment comment = em.find(Comment.class, editedcomment.getId());
        comment.setText(editedcomment.getText());
        return comment;
    }

    @Override
    public Comment replyComment(Long postid, Long parentcommentid, Comment replyComment) {
        Comment parentComment = em.find(Comment.class, parentcommentid);
        replyComment.setReplyComment(parentComment);
        System.out.println("session bean : " + replyComment.getText());
        em.persist(replyComment);
        parentComment.setReplyComment(replyComment);
        return parentComment;
    }
}
