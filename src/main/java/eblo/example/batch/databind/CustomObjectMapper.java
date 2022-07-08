package eblo.example.batch.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomObjectMapper extends ObjectMapper{

    private static final long serialVersionUID = 2374060561649093334L;

    public CustomObjectMapper() {
        super();
        super.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // unknown field 무시 
        // 매핑시 필드만 사용. 
        super.setVisibility(super.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        super.setSerializationInclusion(Include.NON_EMPTY); // 빈 값 제거 
    }

}
