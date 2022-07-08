package eblo.example.batch.elasticsearch.mapper;

import org.junit.jupiter.api.Test;

import eblo.example.batch.elasticsearch.model.JsonDataSet;
import eblo.example.batch.user.model.UserJsonDataSet;

class JsonLineMapperTest {

    @Test
    void testJsonMapping() throws Exception {
        String jsonLine = "{\"id\":1,\"userId\":\"test1\",\"userName\":\"테스트1\",\"age\":21,\"created\":1656569783000,\"updated\":1656569783000},";
        //String jsonLine = "]";
        JsonLineMapper lineMapper = new JsonLineMapper(UserJsonDataSet.class);
        JsonDataSet user = lineMapper.mapLine(jsonLine, 1);
        System.out.println(user.toString());
    }
}
