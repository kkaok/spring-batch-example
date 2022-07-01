package eblo.example.batch.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import eblo.example.batch.model.User;

@Mapper
public interface UserMapper {

    List<User> findAll();
    
}
