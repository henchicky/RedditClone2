package session;

import entity.Community;
import exception.UnknownPersistenceException;
import java.util.List;
import javax.ejb.Local;

@Local
public interface CommunitySessionBeanLocal {

    public Long createCommunity(Community newCommunity, Long userid) throws UnknownPersistenceException;

    public Long editCommunity(Community community) throws UnknownPersistenceException;

    public List<Community> getAllCommunities();

    public Community getCommunityById(Long communityId);

    public void joinCommunity(Long communityId, Long userid);

    public void leaveCommunity(Long communityId, Long userid);

    public List<Community> getUserCreatedCommunities(Long userid);

    public List<Community> getUserJoinedCommunities(Long userid);
}
