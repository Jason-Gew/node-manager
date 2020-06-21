package gew.nodemanager.server.repository;

import gew.nodemanager.common.model.ServiceNodeInfo;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Repository
public interface NodeInfoRepository extends KeyValueRepository<ServiceNodeInfo, String> {

    List<ServiceNodeInfo> findServiceNodeInfoByName(final String name);
}
