package managedbean;

import entity.Comment;
import entity.Post;
import entity.UserEntity;
import exception.UnknownPersistenceException;
import exception.UserNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import session.CommentSessionBeanLocal;
import session.PostSessionBeanLocal;
import session.UserSessionBeanLocal;

@Named(value = "postManagedBean")
@SessionScoped
public class PostManagedBean implements Serializable {

    private long postid = -1;
    private long userid;
    private long communityid;
    private String title = "";
    private String body = "";
    private String imgURL = "";
    private String vidURL = "";
    // can be positive or negative
    private String numOfVotes = "0";
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date timeOfPost;

    private boolean fromUserBoard;
    private Post currentOpenedPost;
    private boolean currentOpenedPostUpvote;
    private boolean currentOpenedPostDownvote;

    private List<Post> allPosts = new ArrayList<>();
    private List<Post> userPosts = new ArrayList<>();

    /*for post comments*/
    private String comment;
    private String replyCommentText;
    private List<Comment> comments = new ArrayList<>();

    private Comment currentComment = new Comment();

    @EJB
    private PostSessionBeanLocal postSessionBeanLocal;
    @EJB
    private UserSessionBeanLocal userSessionBeanLocal;
    @EJB
    private CommentSessionBeanLocal commentSessionBeanLocal;

    public PostManagedBean() {
    }

    @PostConstruct
    public void init() {
        allPosts = postSessionBeanLocal.getAllPosts();
        Collections.reverse(allPosts);
    }

    public void setToNull() {
        postid = -1;
        userid = -1;
        communityid = -1;
        title = "";
        body = "";
        imgURL = "";
        vidURL = "";
        numOfVotes = "0";
        timeOfPost = null;
    }

    public String createNewPost() {
        Map<String, String> params
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        userid = Long.parseLong(params.get("userid"));

        System.out.println("userid = " + userid);
        if (!params.get("communityid").equals("")) {
            System.out.println("communityid = " + params.get("communityid"));
            communityid = Long.parseLong(params.get("communityid"));
        } else {
            communityid = (long) - 1;
        }

        System.err.println("userid = " + userid);
        Post newPost = new Post();
        newPost.setTitle(title);
        newPost.setBody(body);
        newPost.setTimeOfPost(new Date());
        try {
            postSessionBeanLocal.createPost(newPost, userid, communityid);
            userPosts = postSessionBeanLocal.getPostsByUserId(userid);
            setToNull();
            return "/authoriseduser/myprofilePage.xhtml?faces-redirect=true";
        } catch (UnknownPersistenceException ex) {
            setToNull();
            return "/authoriseduser/myprofilePage.xhtml?faces-redirect=true";
        }
    }

    public String opencreatepostpage() {
        Map<String, String> params
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (params.get("userid") == null) {
            fromUserBoard = false;
        } else {
            fromUserBoard = true;
        }
        return "/authoriseduser/newPost.xhtml?faces-redirect=true";
    }

    public String getPostByPostId(long postId) {
        Post post = postSessionBeanLocal.getPostByPostID(postId);
        title = post.getTitle();
        numOfVotes = "" + post.getNumOfVotes();
        body = post.getBody();
        timeOfPost = post.getTimeOfPost();
        imgURL = post.getImgURL();
        vidURL = post.getVidURL();

        return "myprofilePage.xhtml?faces-redirect=true";
    }

    public String getPostsByCommunityId() {
        return "";

    }

    //to be able to be sorted by num of votes
    public String loadAllPost() {
        init();
        return "/index.xhtml?faces-redirect=true";
    }

    public void upvote(Long postID, Long userID) {
        System.out.println("postid = " + postID + ", userID = " + userID);
        numOfVotes = "" + postSessionBeanLocal.upvote(userID, postID);
        checkVoteStatus(postid, userid);
        currentOpenedPost = postSessionBeanLocal.getPostByPostID(postid);
        init();
        System.out.println("num of votes = " + numOfVotes);
    }

    public void downvote(Long postID, Long userID) {
        System.out.println("postid = " + postID + ", userID = " + userID);
        numOfVotes = "" + postSessionBeanLocal.downvote(userID, postID);
        checkVoteStatus(postid, userid);
        currentOpenedPost = postSessionBeanLocal.getPostByPostID(postid);
        init();
        System.out.println("num of votes = " + numOfVotes);
    }

    private void checkVoteStatus(Long postid, Long userid) {
        /*
        0 - No vote
        1 - upvoted
        -1 - downvoted
         */
        Integer status = postSessionBeanLocal.checkVoteStatus(userid, postid);
        if (status == 0) {
            currentOpenedPostUpvote = false;
            currentOpenedPostDownvote = false;
        } else if (status == 1) {
            currentOpenedPostUpvote = true;
            currentOpenedPostDownvote = false;
        } else {
            currentOpenedPostUpvote = false;
            currentOpenedPostDownvote = true;
        }
    }

    public String openPost() {
        Map<String, String> params
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        postid = Long.parseLong(params.get("postid"));

        if (params.get("userid") != null) {
            userid = Long.parseLong(params.get("userid"));
            checkVoteStatus(postid, userid);
        } else {
            currentOpenedPostUpvote = false;
            currentOpenedPostUpvote = false;
        }
        //check if logged in
        if (params.get("currentuserid") == null) {
            currentOpenedPostUpvote = false;
            currentOpenedPostUpvote = false;
        }
        currentOpenedPost = postSessionBeanLocal.getPostByPostID(postid);
        numOfVotes = "" + currentOpenedPost.getNumOfVotes();
        comments = currentOpenedPost.getComments();
        Collections.reverse(comments);
        return "/viewPost2.xhtml?faces-redirect=true";
    }

    public String saveEditPost() {
        try {
            postSessionBeanLocal.editPost(currentOpenedPost);
            return "/viewPost2.xhtml?faces-redirect=true";
        } catch (UnknownPersistenceException ex) {
            return "/viewPost2.xhtml?faces-redirect=true";
        }
    }

    public String deletePost() {
        postSessionBeanLocal.deletePost(postid);
        return "myprofilePage.xhtml/?faces-redirect=true";
    }

    /*For comments of post*/
    public void setCommentToNull() {
        comment = "";
        replyCommentText = "";
    }

    public void submitComment() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Long userID = Long.parseLong(params.get("userid"));
        Comment newComment = new Comment();
        newComment.setTimeOfComment(new Date());
        newComment.setText(comment);
        System.out.println("comment! " + newComment.getText());
        try {
            UserEntity currentUser = userSessionBeanLocal.retrieveUserById(userID);
            newComment.setUser(currentUser);
            commentSessionBeanLocal.createComment(postid, newComment);
            currentOpenedPost = postSessionBeanLocal.getPostByPostID(postid);
            comments = currentOpenedPost.getComments();
            Collections.reverse(comments);
            setCommentToNull();
            System.out.println("comment submitted! " + comments.size());
        } catch (UnknownPersistenceException | UserNotFoundException ex) {
            System.out.println("comment error! " + comments.size());
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "There was a unknown error when creating comment. Please try again."));
        }
    }

    public void replyComment(Long userID, Long postID, Long parentcommentid) {
        Comment replyComment = new Comment();
        replyComment.setTimeOfComment(new Date());
        replyComment.setText(replyCommentText);
        System.out.println("reply comment text = " + replyCommentText);
        UserEntity currentUser;
        try {
            currentUser = userSessionBeanLocal.retrieveUserById(userID);
            replyComment.setUser(currentUser);
        } catch (UserNotFoundException ex) {
            //
        }
        commentSessionBeanLocal.replyComment(postid, parentcommentid, replyComment);

        Post currentCommentedPost = postSessionBeanLocal.getPostByPostID(postID);
        comments = currentCommentedPost.getComments();
        Collections.reverse(comments);
        setCommentToNull();
    }

    public void editComment() {
        System.out.println("currentcomment = " + currentComment.getId());
        commentSessionBeanLocal.updateComment(currentComment);
        Post currentCommentedPost = postSessionBeanLocal.getPostByPostID(postid);
        comments = currentCommentedPost.getComments();
        currentComment = new Comment();
    }

    public void passCommentId() {
        Map<String, String> params
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Long currentCommentId = Long.parseLong(params.get("commentid"));
        System.out.println("currentcommentid = " + currentCommentId);
        currentComment = commentSessionBeanLocal.getCommentByCommentId(currentCommentId);
        System.out.println(currentComment.getText());
    }

    public List<String> completeArea(String query) {
        List<String> results = new ArrayList<String>();

        for (int i = 0; i < 10; i++) {
            results.add(query + i);
        }

        return results;
    }

    public long getPostid() {
        return postid;
    }

    public void setPostid(long postid) {
        this.postid = postid;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public long getCommunityid() {
        return communityid;
    }

    public void setCommunityid(long communityid) {
        this.communityid = communityid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getVidURL() {
        return vidURL;
    }

    public void setVidURL(String vidURL) {
        this.vidURL = vidURL;
    }

    public String getNumOfVotes() {
        return numOfVotes;
    }

    public void setNumOfVotes(String numOfVotes) {
        this.numOfVotes = numOfVotes;
    }

    public Date getTimeOfPost() {
        return timeOfPost;
    }

    public void setTimeOfPost(Date timeOfPost) {
        this.timeOfPost = timeOfPost;
    }

    public boolean isFromUserBoard() {
        return fromUserBoard;
    }

    public void setFromUserBoard(boolean fromUserBoard) {
        this.fromUserBoard = fromUserBoard;
    }

    public Post getCurrentOpenedPost() {
        return currentOpenedPost;
    }

    public void setCurrentOpenedPost(Post currentOpenedPost) {
        this.currentOpenedPost = currentOpenedPost;
    }

    public boolean isCurrentOpenedPostUpvote() {
        return currentOpenedPostUpvote;
    }

    public void setCurrentOpenedPostUpvote(boolean currentOpenedPostUpvote) {
        this.currentOpenedPostUpvote = currentOpenedPostUpvote;
    }

    public boolean isCurrentOpenedPostDownvote() {
        return currentOpenedPostDownvote;
    }

    public void setCurrentOpenedPostDownvote(boolean currentOpenedPostDownvote) {
        this.currentOpenedPostDownvote = currentOpenedPostDownvote;
    }

    public List<Post> getAllPosts() {
        return allPosts;
    }

    public void setAllPosts(List<Post> allPosts) {
        this.allPosts = allPosts;
    }

    public List<Post> getUserPosts() {
        return userPosts;
    }

    public void setUserPosts(List<Post> userPosts) {
        this.userPosts = userPosts;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReplyCommentText() {
        return replyCommentText;
    }

    public void setReplyCommentText(String replyCommentText) {
        this.replyCommentText = replyCommentText;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Comment getCurrentComment() {
        return currentComment;
    }

    public void setCurrentComment(Comment currentComment) {
        this.currentComment = currentComment;
    }

    public PostSessionBeanLocal getPostSessionBeanLocal() {
        return postSessionBeanLocal;
    }

    public void setPostSessionBeanLocal(PostSessionBeanLocal postSessionBeanLocal) {
        this.postSessionBeanLocal = postSessionBeanLocal;
    }

    public UserSessionBeanLocal getUserSessionBeanLocal() {
        return userSessionBeanLocal;
    }

    public void setUserSessionBeanLocal(UserSessionBeanLocal userSessionBeanLocal) {
        this.userSessionBeanLocal = userSessionBeanLocal;
    }

    public CommentSessionBeanLocal getCommentSessionBeanLocal() {
        return commentSessionBeanLocal;
    }

    public void setCommentSessionBeanLocal(CommentSessionBeanLocal commentSessionBeanLocal) {
        this.commentSessionBeanLocal = commentSessionBeanLocal;
    }
}
