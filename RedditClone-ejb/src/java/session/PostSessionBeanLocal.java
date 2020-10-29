package session;

import entity.Post;
import exception.UnknownPersistenceException;
import java.util.List;
import javax.ejb.Local;
import javax.persistence.NoResultException;

@Local
public interface PostSessionBeanLocal {

    public Long createPost(Post newPost, Long userId, Long communityId) throws UnknownPersistenceException;

    public Long editPost(Post newPost) throws UnknownPersistenceException;

    public Post getPostByPostID(Long postId) throws NoResultException;

    public List<Post> getPostsByUserId(Long userId);

    public List<Post> getPostsByCommunityId(Long userId);

    public List<Post> getAllPosts();

    public Integer upvote(Long userid, Long postid);

    public Integer downvote(Long userid, Long postid);

    public Integer checkVoteStatus(Long userid, Long postid);

    public void deletePost(Long postid);
}
