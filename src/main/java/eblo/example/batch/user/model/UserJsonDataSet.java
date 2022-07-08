package eblo.example.batch.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eblo.example.batch.elasticsearch.model.JsonDataSet;

public class UserJsonDataSet implements JsonDataSet{

    private String userId;

    @JsonIgnore
    private String jsonData;
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public String getJsonData() {
        return this.jsonData;
    }

    @Override
    public String getId() {
        return this.userId;
    }

    @Override
    public String toString() {
        return "UserJsonDataSet [id=" + getId() + ", jsonData=" + jsonData + "]";
    }

}
