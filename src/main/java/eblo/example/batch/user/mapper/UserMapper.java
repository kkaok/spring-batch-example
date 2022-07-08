package eblo.example.batch.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import eblo.example.batch.user.model.User;

@Mapper
public interface UserMapper {

    List<User> findAll();
    
}
