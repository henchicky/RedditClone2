package managedbean;

import entity.Community;
import entity.Post;
import entity.UserEntity;
import exception.InvalidLoginCredentialException;
import exception.UnknownPersistenceException;
import exception.UserNotFoundException;
import exception.UsernameExistException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.primefaces.model.file.UploadedFile;
import session.CommunitySessionBeanLocal;
import session.PostSessionBeanLocal;
import session.UserSessionBeanLocal;

@Named(value = "userManagedBean")
@SessionScoped
public class UserManagedBean implements Serializable {

    @EJB
    private UserSessionBeanLocal userSessionBean;
    @EJB
    private PostSessionBeanLocal postSessionBeanLocal;
    @EJB
    private CommunitySessionBeanLocal communitySessionBeanLocal;

    private Long userId;
    private UserEntity currentUser;

    private String username;
    private String name;
    private String email;
    private String password;
    private String newPassword;
    private String profileimgurl = "avatar.png";
    private UploadedFile uploadedfile;
    private boolean isSignedOut = true;

    private List<Post> userPosts = new ArrayList<>();

    private List<Community> userJoinedCommunity = new ArrayList<>();
    private List<Community> userCreatedCommunity = new ArrayList<>();

    private UserEntity otherUsers;
    private List<Post> otherUsersPosts;
    private Long userid;

    //private List<Comment> comments = new ArrayList<>();
    public UserManagedBean() {
    }

    /*For Authentication Purposes*/
    private void setToNull() {
        username = null;
        password = null;
        newPassword = null;
        email = null;
        name = null;
        userId = (long) -1;
        isSignedOut = true;
        currentUser = null;
    }

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            currentUser = userSessionBean.userLogin(username, password);
            userId = currentUser.getId();
            email = currentUser.getEmail();
            if (currentUser.getName() != null) {
                name = currentUser.getName();
            } else {
                name = currentUser.getUsername();
            }
            isSignedOut = false;
            //return "/secret/secret.xhtml?faces-redirect=true";
            userPosts = postSessionBeanLocal.getPostsByUserId(currentUser.getId());
            userJoinedCommunity = communitySessionBeanLocal.getUserJoinedCommunities(currentUser.getId());
            userCreatedCommunity = communitySessionBeanLocal.getUserCreatedCommunities(currentUser.getId());
            return "index.xhtml?faces-redirect=true";
        } catch (InvalidLoginCredentialException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid password. Please try again"));
            setToNull();
            return "index.xhtml";
        } catch (Exception ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Username does not exist. Please create a new account!"));
            setToNull();
            return "index.xhtml";
        }
    }

    public String registerNewUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setJoinDate(new Date());

        try {
            currentUser = userSessionBean.createUserEntity(newUser);
            name = currentUser.getUsername();
            isSignedOut = false;
            return "index.xhtml?faces-redirect=true";
        } catch (UsernameExistException ex) {
            setToNull();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Username or email already exist. Please use a different username or email."));
            return "index.xhtml";
        } catch (UnknownPersistenceException ex) {
            setToNull();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "There was a problem register. Please try again."));
            return "index.xhtml";
        } catch (Exception ex) {
            return "index.xhtml";
        }
    }

    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        setToNull();
        context.addMessage(null, new FacesMessage("Success", "Logged out successfully"));
        return "index.xhtml?faces-redirect=true";
    }

    /*upload profile image*/
    public void upload() throws IOException {
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        //get the deployment path
        String UPLOAD_DIRECTORY = ctx.getRealPath("/") + "profileimg/";
        System.out.println("#UPLOAD_DIRECTORY : " + UPLOAD_DIRECTORY);

        //debug purposes
        setProfileimgurl(uploadedfile.getFileName());
        System.out.println("filename: " + uploadedfile.getFileName());
        //---------------------

        //replace existing file
        Path path = Paths.get(UPLOAD_DIRECTORY + uploadedfile.getFileName());
        InputStream bytes = uploadedfile.getInputStream();
        Files.copy(bytes, path, StandardCopyOption.REPLACE_EXISTING);
    }

    public void updateUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        currentUser.setProfileimgurl(profileimgurl);

        try {
            userSessionBean.editProfile(currentUser);
            context.addMessage(null, new FacesMessage("Success", "Updated successfully"));

        } catch (Exception e) {
            //show with an error icon
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update user"));
        }
    }

    public void changePassword() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (!currentUser.getPassword().equals(password)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Current password is invalid"));
            return;
        } else {
            currentUser.setPassword(newPassword);
            userSessionBean.changePassword(currentUser);
            context.addMessage(null, new FacesMessage("Success", "Updated successfully"));
        }
    }

    public String getPostsAndComByUserId() {
        userPosts = postSessionBeanLocal.getPostsByUserId(currentUser.getId());
        userJoinedCommunity = communitySessionBeanLocal.getUserJoinedCommunities(currentUser.getId());
        userCreatedCommunity = communitySessionBeanLocal.getUserCreatedCommunities(currentUser.getId());
        return "/myprofile.xhtml?faces-redirect=true";
    }

    public void retrievePostsAndComByUserIdForOtherUser() {
        /*Map<String, String> params
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Long userid = Long.parseLong(params.get("userid"));*/
        System.out.println("managedbean : " + userid);
        try {
            otherUsers = userSessionBean.retrieveUserById(userid);
        } catch (UserNotFoundException ex) {
            Logger.getLogger(UserManagedBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        userPosts = postSessionBeanLocal.getPostsByUserId(userid);
        userJoinedCommunity = communitySessionBeanLocal.getUserJoinedCommunities(userid);
        userCreatedCommunity = communitySessionBeanLocal.getUserCreatedCommunities(userid);
        //return "/otheruserprofile.xhtml?faces-redirect=true";
    }

    public String retrieveNumOfPost() {
        try {
            currentUser = userSessionBean.retrieveUserById(userId);
        } catch (UserNotFoundException ex) {
            Logger.getLogger(UserManagedBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "" + currentUser.getPosts().size();
    }

    // get other users community
    public void retrieveComByUserID() {
        Map<String, String> params
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Long userid = Long.parseLong(params.get("userid"));
        userJoinedCommunity = communitySessionBeanLocal.getUserJoinedCommunities(userid);
        userCreatedCommunity = communitySessionBeanLocal.getUserCreatedCommunities(userid);
    }

    public UserSessionBeanLocal getUserSessionBean() {
        return userSessionBean;
    }

    public void setUserSessionBean(UserSessionBeanLocal userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    public PostSessionBeanLocal getPostSessionBeanLocal() {
        return postSessionBeanLocal;
    }

    public void setPostSessionBeanLocal(PostSessionBeanLocal postSessionBeanLocal) {
        this.postSessionBeanLocal = postSessionBeanLocal;
    }

    public CommunitySessionBeanLocal getCommunitySessionBeanLocal() {
        return communitySessionBeanLocal;
    }

    public void setCommunitySessionBeanLocal(CommunitySessionBeanLocal communitySessionBeanLocal) {
        this.communitySessionBeanLocal = communitySessionBeanLocal;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserEntity getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserEntity currentUser) {
        this.currentUser = currentUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getProfileimgurl() {
        return profileimgurl;
    }

    public void setProfileimgurl(String profileimgurl) {
        this.profileimgurl = profileimgurl;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public UploadedFile getUploadedfile() {
        return uploadedfile;
    }

    public void setUploadedfile(UploadedFile uploadedfile) {
        this.uploadedfile = uploadedfile;
    }

    public boolean isIsSignedOut() {
        return isSignedOut;
    }

    public void setIsSignedOut(boolean isSignedOut) {
        this.isSignedOut = isSignedOut;
    }

    public List<Post> getUserPosts() {
        return userPosts;
    }

    public void setUserPosts(List<Post> userPosts) {
        this.userPosts = userPosts;
    }

    public UserEntity getOtherUsers() {
        return otherUsers;
    }

    public void setOtherUsers(UserEntity otherUsers) {
        this.otherUsers = otherUsers;
    }

    public List<Post> getOtherUsersPosts() {
        return otherUsersPosts;
    }

    public void setOtherUsersPosts(List<Post> otherUsersPosts) {
        this.otherUsersPosts = otherUsersPosts;
    }

    public List<Community> getUserJoinedCommunity() {
        return userJoinedCommunity;
    }

    public void setUserJoinedCommunity(List<Community> userJoinedCommunity) {
        this.userJoinedCommunity = userJoinedCommunity;
    }

    public List<Community> getUserCreatedCommunity() {
        return userCreatedCommunity;
    }

    public void setUserCreatedCommunity(List<Community> userCreatedCommunity) {
        this.userCreatedCommunity = userCreatedCommunity;
    }
}
