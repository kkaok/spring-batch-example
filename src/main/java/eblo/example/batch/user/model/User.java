package eblo.example.batch.user.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {

    private Long id;
    private String userId;
    private String userName;
    private Integer age;
    private Date created;
    private Date updated;
    
}
