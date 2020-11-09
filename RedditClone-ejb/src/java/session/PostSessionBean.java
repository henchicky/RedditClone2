package session;

import entity.Comment;
import entity.Community;
import entity.Post;
import entity.UserEntity;
import exception.UnknownPersistenceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

@Stateless
public class PostSessionBean implements PostSessionBeanLocal {

    @PersistenceContext(unitName = "RedditClone-ejbPU")
    private EntityManager em;

    @Override
    public Long createPost(Post newPost, Long userId, Long communityId) throws UnknownPersistenceException {
        try {
            em.persist(newPost);
            UserEntity user = em.find(UserEntity.class, userId);
            System.out.println("postid = " + newPost.getId());
            System.out.println("userid = " + user.getId());
            newPost.setUser(user);
            newPost.getListOfVoters().put(userId, 0);
            user.getPosts().add(newPost);
            if (communityId != -1) {
                Community community = em.find(Community.class, communityId);
                newPost.setCommunity(community);
                community.getPosts().add(newPost);
                System.out.println("communityid = " + community.getId());
            }
            System.out.println("postid = " + newPost.getId());
            System.out.println("userid = " + user.getId());
            return newPost.getId();
        } catch (PersistenceException ex) {
            throw new UnknownPersistenceException(ex.getMessage());
        }
    }

    @Override
    public Long editPost(Post post) throws UnknownPersistenceException {
        try {
            em.merge(post);
            em.flush();
            System.out.println("postid = " + post.getId());
            return post.getId();
        } catch (PersistenceException ex) {
            throw new UnknownPersistenceException(ex.getMessage());
        }
    }

    @Override
    public Post getPostByPostID(Long postId) throws NoResultException {
        Query query = em.createQuery("SELECT p from Post p WHERE p.id = :inid");
        query.setParameter("inid", postId);
        try {
            return (Post) query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException ex) {
            throw new NoResultException();
        }
    }

    @Override
    public List<Post> getPostsByUserId(Long userId) {
        UserEntity user = em.find(UserEntity.class, userId);
        System.out.println("Userid = " + userId);
        if (user != null && user.getPosts() != null) {
            user.getPosts().size();
            return user.getPosts();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Post> getPostsByCommunityId(Long userId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Post> getAllPosts() {
        Query query = em.createQuery("SELECT p from Post p");
        return query.getResultList();
    }

    @Override
    public Integer upvote(Long userid, Long postid) {
        UserEntity currentUser = em.find(UserEntity.class, userid);
        Post currentPost = em.find(Post.class, postid);
        HashMap<Long, Integer> listOfVoters = currentPost.getListOfVoters();
        Integer votecounts = 0;
        if (listOfVoters.get(currentUser.getId()) == null) {
            listOfVoters.put(currentUser.getId(), 1);
            System.out.println("sessionbean: user have not voted before!");
        } else {
            Integer voteValue = listOfVoters.get(currentUser.getId());
            //check if already upvoted, then remove upvote
            if (voteValue == 1) {
                listOfVoters.replace(userid, 0);
                System.out.println("sessionbean: Remove current upvote");
            } else {
                listOfVoters.replace(userid, 1);
                System.out.println("sessionbean: Either upvote or change downvote to upvote");
            }
        }
        System.out.println("sessionbean: size = " + listOfVoters.size());
        for (Integer i : listOfVoters.values()) {
            votecounts += i;
        }
        currentPost.setNumOfVotes(votecounts);
        return votecounts;
    }

    @Override
    public Integer downvote(Long userid, Long postid) {
        UserEntity currentUser = em.find(UserEntity.class, userid);
        Post currentPost = em.find(Post.class, postid);
        HashMap<Long, Integer> listOfVoters = currentPost.getListOfVoters();
        Integer votecounts = 0;
        if (listOfVoters.get(currentUser.getId()) == null) {
            listOfVoters.put(currentUser.getId(), -1);
            System.out.println("sessionbean: user have not voted before!");
        } else {
            Integer voteValue = listOfVoters.get(currentUser.getId());
            //check if already upvoted, then remove upvote
            if (voteValue == -1) {
                listOfVoters.replace(userid, 0);
                System.out.println("sessionbean: Remove current downvote");
            } else {
                listOfVoters.replace(userid, -1);
                System.out.println("sessionbean: Either downvote or change upvote to downvote");
            }
        }
        System.out.println("sessionbean: size = " + listOfVoters.size());
        for (Integer i : listOfVoters.values()) {
            votecounts += i;
        }
        currentPost.setNumOfVotes(votecounts);
        return votecounts;
    }

    @Override
    public Integer checkVoteStatus(Long userid, Long postid) {
        UserEntity currentUser = em.find(UserEntity.class, userid);
        Post currentPost = em.find(Post.class, postid);
        Integer status;
        /*
        0 - No vote
        1 - upvoted
        -1 - downvoted
         */
        HashMap<Long, Integer> listOfVoters = currentPost.getListOfVoters();
        Integer votecounts = 0;
        if (listOfVoters.get(currentUser.getId()) == null) {
            System.out.println("sessionbean: Not voted before!");
            status = 0;
        } else {
            Integer voteValue = listOfVoters.get(currentUser.getId());
            //check if already downvote, then remove downvote
            if (voteValue == -1) {
                status = -1;
                System.out.println("sessionbean: downvote");
            } else if (voteValue == 1) {
                status = 1;
                System.out.println("sessionbean: upvote");
            } else {
                status = 0;
                System.out.println("sessionbean: No vote");
            }
        }
        System.out.println("sessionbean: size = " + listOfVoters.size());
        for (Integer i : listOfVoters.values()) {
            votecounts += i;
        }
        currentPost.setNumOfVotes(votecounts);
        return status;
    }

    @Override
    public void deletePost(Long postid) {
        Post post = em.find(Post.class, postid);

        UserEntity user = post.getUser();
        user.getPosts().remove(post);
        if (post.getCommunity() != null) {
            Community community = post.getCommunity();
            community.getPosts().remove(post);
        }
        List<Comment> comments = post.getComments();

        for (Comment c : comments) {
            em.remove(c);
        }

        em.remove(post);
    }

}
