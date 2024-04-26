package org.quickdev.domain.datasource.repository;

import java.util.Map;

import org.quickdev.domain.datasource.model.TokenBasedConnection;
import org.quickdev.domain.datasource.model.TokenBasedConnectionDO;
import org.quickdev.domain.encryption.EncryptionService;
import org.quickdev.domain.plugin.service.DatasourceMetaInfoService;
import org.quickdev.infra.mongo.MongoUpsertHelper;
import org.quickdev.sdk.models.DatasourceConnectionConfig;
import org.quickdev.sdk.models.TokenBasedConnectionDetail;
import org.quickdev.sdk.plugin.common.DatasourceConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public class TokenBasedConnectionRepository {

    @Autowired
    private TokenBasedConnectionDORepository tokenBasedConnectionDORepository;

    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    public Mono<TokenBasedConnection> findByDatasourceId(String datasourceId, String datasourceType) {
        return tokenBasedConnectionDORepository.findByDatasourceId(datasourceId)
                .map(connectionDO -> convertToDatasourceConnection(connectionDO, datasourceType));
    }

    public Mono<Void> saveConnection(TokenBasedConnection tokenBasedConnection, String datasourceId) {

        tokenBasedConnection.getTokenDetail().doEncrypt(encryptionService::encryptString);

        TokenBasedConnectionDO result = new TokenBasedConnectionDO();
        result.setDatasourceId(tokenBasedConnection.getDatasourceId());
        result.setTokenDetail(tokenBasedConnection.getTokenDetail().toMap());
        result.setId(tokenBasedConnection.getId());
        result.setCreatedAt(tokenBasedConnection.getCreatedAt());
        result.setUpdatedAt(tokenBasedConnection.getUpdatedAt());
        result.setCreatedBy(tokenBasedConnection.getCreatedBy());
        result.setModifiedBy(tokenBasedConnection.getModifiedBy());
        return mongoUpsertHelper.upsertWithAuditingParams(result, "datasourceId", datasourceId)
                .doOnNext(__ -> tokenBasedConnection.getTokenDetail().doDecrypt(encryptionService::decryptString))
                .then();
    }

    private TokenBasedConnection convertToDatasourceConnection(TokenBasedConnectionDO tokenBasedConnectionDO,
            String datasourceType) {

        Map<String, Object> tokenDetailMap = tokenBasedConnectionDO.getTokenDetail();
        TokenBasedConnectionDetail tokenDetail = getDatasourceConnector(datasourceType).resolveTokenDetail(tokenDetailMap);
        tokenDetail.doDecrypt(encryptionService::decryptString);

        TokenBasedConnection result = new TokenBasedConnection();
        result.setDatasourceId(tokenBasedConnectionDO.getDatasourceId());
        result.setTokenDetail(tokenDetail);
        result.setId(tokenBasedConnectionDO.getId());
        result.setCreatedAt(tokenBasedConnectionDO.getCreatedAt());
        result.setUpdatedAt(tokenBasedConnectionDO.getUpdatedAt());
        result.setCreatedBy(tokenBasedConnectionDO.getCreatedBy());
        result.setModifiedBy(tokenBasedConnectionDO.getModifiedBy());
        return result;
    }

    private DatasourceConnector<Object, ? extends DatasourceConnectionConfig> getDatasourceConnector(String datasourceType) {
        return datasourceMetaInfoService.getDatasourceConnector(datasourceType);
    }
}
