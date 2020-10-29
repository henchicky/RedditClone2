package session;

import entity.Community;
import entity.UserEntity;
import exception.UnknownPersistenceException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

@Stateless
public class CommunitySessionBean implements CommunitySessionBeanLocal {

    @PersistenceContext(unitName = "RedditClone-ejbPU")
    private EntityManager em;

    @Override
    public Long createCommunity(Community newCommunity, Long userid) throws UnknownPersistenceException {
        try {
            em.persist(newCommunity);
            em.flush();
            UserEntity user = em.find(UserEntity.class, userid);
            newCommunity.getUsers().add(user);
            user.getCommunities().add(newCommunity);
            System.out.println("communityid = " + newCommunity.getId());
            return newCommunity.getId();
        } catch (PersistenceException ex) {
            throw new UnknownPersistenceException(ex.getMessage());
        }
    }

    @Override
    public Long editCommunity(Community community) throws UnknownPersistenceException {
        try {
            em.persist(community);
            em.flush();
            System.out.println("communityid = " + community.getId());
            return community.getId();
        } catch (PersistenceException ex) {
            throw new UnknownPersistenceException(ex.getMessage());
        }
    }

    @Override
    public List<Community> getAllCommunities() {
        Query query = em.createQuery("SELECT c from Community c");
        return query.getResultList();
    }

    @Override
    public Community getCommunityById(Long communityId) {
        return em.find(Community.class, communityId);
    }

    @Override
    public void joinCommunity(Long communityId, Long userid) {
        UserEntity user = em.find(UserEntity.class, userid);
        Community community = em.find(Community.class, communityId);
        community.getUsers().add(user);
        user.getCommunities().add(community);
    }

    @Override
    public void leaveCommunity(Long communityId, Long userid) {
        UserEntity user = em.find(UserEntity.class, userid);
        Community community = em.find(Community.class, communityId);
        community.getUsers().remove(user);
        user.getCommunities().remove(community);
    }

    @Override
    public List<Community> getUserCreatedCommunities(Long userid) {
        UserEntity user = em.find(UserEntity.class, userid);
        List<Community> userCreatedCommunitiesList = new ArrayList<>();
        List<Community> allCommunitiesList = getAllCommunities();
        //allCommunitiesList.size();
        for (int i = 0; i < allCommunitiesList.size(); i++) {
            //the first user of the community is the creator
            if (allCommunitiesList.get(i).getUsers().get(0).getId() == userid) {
                userCreatedCommunitiesList.add(allCommunitiesList.get(i));
            }
        }
        return userCreatedCommunitiesList;
    }

    @Override
    public List<Community> getUserJoinedCommunities(Long userid) {
        UserEntity user = em.find(UserEntity.class, userid);
        return user.getCommunities();
    }

}
