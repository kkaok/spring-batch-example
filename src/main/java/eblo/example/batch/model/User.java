package eblo.example.batch.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private Long id;
    private String userId;
    private String userName;
    private Integer age;
    private Date created;
    private Date updated;
    
}
