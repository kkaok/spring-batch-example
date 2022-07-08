package eblo.example.batch.elasticsearch.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eblo.example.batch.elasticsearch.exception.ElasticSearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ESIndexHandler {

    private final RestHighLevelClient restHighLevelClient;

    /**
     * Index 생성
     * 
     * @param indexName
     * @return
     * @throws ElasticSearchException 
     */
    public boolean addIndex(String indexName){
        log.debug("addIndex start");
        try {
            // 인덱스가 존재한다면 삭제 
            deleteIndex(indexName);
            log.debug("addIndex 기존인덱스 삭제, 인덱스명 : {}", indexName);
            CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createRequest, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        } catch (Exception e) {
            throw new ElasticSearchException("인덱스 생성에 실패했습니다", e.getLocalizedMessage(), e);
        } finally {
            log.debug("addIndex end");
        }
    }
    
    /**
     * 색인 존재 여부 확인 
     * @param indexName
     * @return
     * @throws IOException
     */
    public boolean existsIndex(String indexName) {
        log.debug("existsIndex start");
        try {
            return restHighLevelClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticSearchException("인덱스 존재 여부 확인 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        } finally {
            log.debug("existsIndex end");
        }
    }

    /**
     * Index 삭제
     * 
     * @param indexName
     * @return
     * @throws IOException 
     */
    public boolean deleteIndex(String indexName){
        log.debug("deleteIndex start");
        if(!existsIndex(indexName)) return false;
        try{
            //인덱스 삭제 요청 객체
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        }catch (Exception e) {
            throw new ElasticSearchException("인덱스 삭제 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        } finally {
            log.debug("deleteIndex end");
        }
    }
    
    /**
     * index refresh 
     * 인덱스 생성, 데이터 저장 후 처리 
     * @param indexName
     * @throws IOException
     */
    public void refresh(String indexName){
        log.debug("refresh start.");
        Assert.notNull(indexName, "인덱스명은 필수입니다");
        try {
            restHighLevelClient.indices().refresh(new RefreshRequest(indexName), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticSearchException("인덱스 리프레쉬 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        } finally {
            log.debug("refresh end.");
        }
    }

    /**
     * 인덱스 세팅 정보 조회 
     * @param indexName
     * @return
     */
    public Settings getSettingsFromIndex(String indexName){
        log.debug("getSettings start");
        try {
            GetSettingsResponse getSettingsResponse = restHighLevelClient.indices().getSettings(new GetSettingsRequest().indices(indexName), RequestOptions.DEFAULT);
            return getSettingsResponse.getIndexToSettings().get(indexName);
        } catch (Exception e) {
            throw new ElasticSearchException("인덱스 Settings 정보를 가져오는 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        } finally {
            log.debug("getSettings end.");
        }
    }

    /**
     * Map에 있는 세팅 정보 컨버팅 
     * @param indexName
     * @param settingMap
     * @return
     */
    public Settings convertSettingsWithMap(Map<String, Object> settingMap){
        return Settings.builder()
                .loadFromMap(settingMap)
                .build();
    }

    /**
     * Json String 컨버트 
     * @param indexName
     * @param jsongString
     * @return
     */
    public Settings convertSettingsWithJson(String jsongString){
        return Settings.builder()
                .loadFromSource(jsongString, XContentType.JSON)
                .build();
    }

    /**
     * Settings 업데이트 
     * @param indexName
     * @param settings
     * @return
     */
    public boolean updateSettings(String indexName, Settings settings){
        log.debug("updateSettings start");
        try {
            UpdateSettingsRequest request = new UpdateSettingsRequest(indexName);
            request.settings(settings); 
            AcknowledgedResponse updateSettingsResponse = restHighLevelClient.indices().putSettings(request, RequestOptions.DEFAULT);
            return updateSettingsResponse.isAcknowledged(); 
        } catch (Exception e) {
            throw new ElasticSearchException("인덱스 Settings 정보를 업데이트 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        }finally {
            log.debug("updateSettings end");
        }
    }
    
    /**
     * Settings 업데이트  
     * @param indexName
     * @param number_of_replicas
     * @param refresh_interval
     * @param max_merge_at_once
     * @param segments_per_tier
     * @return
     */
    public boolean updateSettings(String indexName, int numberOfReplicas, String refreshInterval, int maxMergeAtOnce, int segmentsPerTier){
        Settings settings = Settings.builder()
        .put("index.number_of_replicas", numberOfReplicas)
        .put("index.refresh_interval", refreshInterval)
        .put("index.merge.policy.max_merge_at_once", maxMergeAtOnce)
        .put("index.merge.policy.segments_per_tier", segmentsPerTier)
        .build();
        return updateSettings(indexName, settings);
    }
    
    public void replaceAlias(String aliasNm, String curIndexNm, String newIndexNm) {
        log.debug("replaceAlias start");
        IndicesAliasesRequest request = new IndicesAliasesRequest(); 
        AliasActions addAction =
                new AliasActions(AliasActions.Type.ADD)
                .index(newIndexNm)
                .alias(aliasNm); 
        request.addAliasAction(addAction);
        
        AliasActions removeAction =
                new AliasActions(AliasActions.Type.REMOVE)
                .index(curIndexNm)
                .alias(aliasNm);
        request.addAliasAction(removeAction);
        try {
            restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticSearchException("알리아스 업데이트 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        }finally {
            log.debug("replaceAlias end");
        }
    }
    
    public void addAlias(String aliasNm, String newIndexNm) {
        log.debug("replaceAlias start");
        IndicesAliasesRequest request = new IndicesAliasesRequest(); 
        AliasActions addAction =
                new AliasActions(AliasActions.Type.ADD)
                .index(newIndexNm)
                .alias(aliasNm); 
        request.addAliasAction(addAction);
        try {
            restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticSearchException("알리아스 생성 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        }finally {
            log.debug("replaceAlias end");
        }
    }

    /**
     * 알리아스의 Index 조회
     * 
     * @param indexNm
     * @return
     * @throws IOException
     */
    public boolean existIndexFromAlias(String aliasNm, String indexNm){
        log.debug("existIndexFromAlias start");
        try {
            Map<String, Set<AliasMetadata>> aliases = getAliasInfo(aliasNm);
            if(aliases.containsKey(indexNm)) return true;
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticSearchException("알리아스의 인덱스 확인 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        }finally {
            log.debug("existIndexFromAlias end");
        }
    }

    /**
     * 알리아스 정보 조회 
     * @param aliasNm
     * @return
     */
    public Map<String, Set<AliasMetadata>> getAliasInfo(String aliasNm) {
        log.debug("getAliasInfo end");
        try {
            GetAliasesRequest request = new GetAliasesRequest(aliasNm);
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            GetAliasesResponse response = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
            return response.getAliases();
        } catch (Exception e) {
            throw new ElasticSearchException("알리아스 조회 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        }finally {
            log.debug("getAliasInfo end");
        }
    }

    /**
     * 알리아스의 인덱스명 가져오기, 첫번째 인덱스명  
     * @param aliasNm
     * @return
     */
    public List<String> getIndiceFromAlias(String alias) {
        log.debug("getIndiceFromAlias start");
        try {
            Map<String, Set<AliasMetadata>> aliaseInfo = getAliasInfo(alias);
            final List<String> allIndices = new ArrayList<>();
            aliaseInfo.keySet().stream().forEach(allIndices::add);
            return allIndices;
        } catch (Exception e) {
            throw new ElasticSearchException("알리아스의 인덱스명 조회 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        }finally {
            log.debug("getIndiceFromAlias end");
        }
    }
    
    /**
     * 색인 전체 건수
     * @param indiceNm
     * @return
     */
    public long getIndexCount(String indiceNm) {
        log.debug("getIndexCount start");
        try{
            CountResponse countResponse = restHighLevelClient.count(new CountRequest(indiceNm), RequestOptions.DEFAULT);
            return countResponse.getCount();
        } catch (Exception e) {
            throw new ElasticSearchException("인덱스 카운트 조회 중 오류가 발생하였습니다", e.getLocalizedMessage(), e);
        }finally {
            log.debug("getIndexCount end");
        }
    }

}
