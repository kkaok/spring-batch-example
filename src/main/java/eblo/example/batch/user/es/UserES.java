package eblo.example.batch.user.es;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Document( indexName = "idx_users")
@Document(indexName="alias_users")
public class UserES {

    @Id
    private Long id;
    
    @Field(type = FieldType.Keyword, name = "userId")
    private String userId;
    
    @Field(type = FieldType.Text, name = "userName")
    private String userName;
    
    @Field(type = FieldType.Integer, name = "age")
    private Integer age;
    
    @Field(type = FieldType.Date, name = "created")
    private Date created;
    
    @Field(type = FieldType.Date, name = "updated")
    private Date updated;
    
    //@Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    //@Field(type = FieldType.String, index = FieldIndex.analyzed, indexAnalyzer = "index_analyzer", searchAnalyzer = "search_analyzer")

}
