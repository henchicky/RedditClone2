package managedbean;

import entity.Community;
import entity.Post;
import entity.UserEntity;
import exception.UnknownPersistenceException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import session.CommunitySessionBeanLocal;

@Named(value = "communityManagedBean")
@SessionScoped
public class CommunityManagedBean implements Serializable {

    @EJB
    private CommunitySessionBeanLocal communitySessionBeanLocal;

    private String communityName;
    private String description;
    private Date creationDate;

    private List<Community> communities;

    private Community community;

    private List<Post> posts;

    private List<UserEntity> users;

    private boolean isOwnerOfCommunity;
    private boolean hasJoinedCommunity;
    private boolean isSignedIn;

    public CommunityManagedBean() {
    }

    @PostConstruct
    public void init() {
        communities = communitySessionBeanLocal.getAllCommunities();
    }

    public void setToNull() {
        communityName = null;
        description = null;
        creationDate = null;
        community = null;
    }

    public void createCommunity() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Long userid = Long.parseLong(params.get("userid"));
        Community newCommunity = new Community();
        newCommunity.setCommunityName(communityName);
        newCommunity.setCreationDate(new Date());
        newCommunity.setDescription(description);
        try {
            context.addMessage(null, new FacesMessage("Success", communityName + " created successfully!"));
            communitySessionBeanLocal.createCommunity(newCommunity, userid);
            setToNull();
        } catch (UnknownPersistenceException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "There was a unknown error when creating community. Please try again."));
            setToNull();
        }
    }

    public String openCommunity(Long communityid, Long userid) {
        System.out.println("communityid = " + communityid);
        community = communitySessionBeanLocal.getCommunityById(communityid);
        posts = community.getPosts();
        System.out.println("Num of posts = " + posts.size());
        if (userid != null) {
            System.out.println("userid = " + userid);
            users = community.getUsers();
            checkOwnerOfCommunity(userid);
            hasJoinedCommunity(userid);
            isSignedIn = true;
        } else {
            isSignedIn = false;
        }
        return "/viewCommunity2.xhtml?faces-redirect=true";
    }

    public void joinCommunity(Long userID) {
        communitySessionBeanLocal.joinCommunity(community.getId(), userID);
        community = communitySessionBeanLocal.getCommunityById(community.getId());
        users = community.getUsers();
        hasJoinedCommunity(userID);
    }

    public void leaveCommunity(Long userID) {
        communitySessionBeanLocal.leaveCommunity(community.getId(), userID);
        community = communitySessionBeanLocal.getCommunityById(community.getId());
        users = community.getUsers();
        hasJoinedCommunity(userID);
    }

    private void checkOwnerOfCommunity(Long userid) {
        System.out.println("userid = " + userid);
        System.out.println("community owner userid = " + users.get(0).getId());
        System.out.println("are they the same = " + (users.get(0).getId() == userid));
        if (users.get(0).getId() == userid) {
            isOwnerOfCommunity = true;
        } else {
            isOwnerOfCommunity = false;
        }
    }

    private void hasJoinedCommunity(Long userid) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == userid) {
                hasJoinedCommunity = true;
            } else {
                hasJoinedCommunity = false;
            }
        }
    }

    public String loadAllCommunity() {
        communities = communitySessionBeanLocal.getAllCommunities();
        return "/viewallcommunities.xhtml?faces-redirect=true";
    }

    public void onCommunityChange() {

    }

    public CommunitySessionBeanLocal getCommunitySessionBeanLocal() {
        return communitySessionBeanLocal;
    }

    public void setCommunitySessionBeanLocal(CommunitySessionBeanLocal communitySessionBeanLocal) {
        this.communitySessionBeanLocal = communitySessionBeanLocal;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<Community> getCommunities() {
        return communities;
    }

    public void setCommunities(List<Community> communities) {
        this.communities = communities;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }

    public boolean isIsOwnerOfCommunity() {
        return isOwnerOfCommunity;
    }

    public void setIsOwnerOfCommunity(boolean isOwnerOfCommunity) {
        this.isOwnerOfCommunity = isOwnerOfCommunity;
    }

    public boolean isHasJoinedCommunity() {
        return hasJoinedCommunity;
    }

    public void setHasJoinedCommunity(boolean hasJoinedCommunity) {
        this.hasJoinedCommunity = hasJoinedCommunity;
    }

    public boolean isIsSignedIn() {
        return isSignedIn;
    }

    public void setIsSignedIn(boolean isSignedIn) {
        this.isSignedIn = isSignedIn;
    }
}
