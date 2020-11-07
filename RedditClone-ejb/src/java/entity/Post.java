package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Lob
    private String body;
    private String imgURL;
    private String vidURL;

    // can be positive or negative
    private int numOfVotes;

    private HashMap<Long, Integer> listOfVoters = new HashMap<>();

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeOfPost;

    @ManyToOne
    private UserEntity user = new UserEntity();
    @OneToMany //(mappedBy = "")
    private List<Comment> comments = new ArrayList<>();
    @ManyToOne
    private Community community;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getNumOfVotes() {
        return numOfVotes;
    }

    public void setNumOfVotes(int numOfVotes) {
        this.numOfVotes = numOfVotes;
    }

    public HashMap<Long, Integer> getListOfVoters() {
        return listOfVoters;
    }

    public void setListOfVoters(HashMap<Long, Integer> listOfVoters) {
        this.listOfVoters = listOfVoters;
    }

    public Date getTimeOfPost() {
        return timeOfPost;
    }

    public void setTimeOfPost(Date timeOfPost) {
        this.timeOfPost = timeOfPost;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Post)) {
            return false;
        }
        Post other = (Post) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Post[ id=" + id + " ]";
    }

}
