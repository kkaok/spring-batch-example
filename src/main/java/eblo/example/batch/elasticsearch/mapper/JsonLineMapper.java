package eblo.example.batch.elasticsearch.mapper;

import org.springframework.batch.item.file.LineMapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eblo.example.batch.databind.CustomObjectMapper;
import eblo.example.batch.elasticsearch.model.JsonDataSet;

public class JsonLineMapper implements LineMapper<JsonDataSet> {

    private static int MINIMUM_LENGTH = 3;

    private Class<? extends JsonDataSet> targetType;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    public JsonLineMapper(Class<? extends JsonDataSet> targetType) {
        this.targetType = targetType;
    }

    @Override
    public JsonDataSet mapLine(String line, int lineNumber) {
        if(line == null || line.length() < MINIMUM_LENGTH) return null;
        
        String nline = line.trim();
        if(line.endsWith(",")) nline = nline.substring(0, line.length()-1);

        try {
            JsonDataSet jds = objectMapper.readValue(line, targetType);
            jds.setJsonData(nline);
            return jds;
        }catch(JsonParseException e) {
            return null;
        }catch(Exception e) {
            //throw e;
            e.printStackTrace();
            return null;
        }
    }
}