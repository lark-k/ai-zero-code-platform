package com.lk.aizerocodeplatform.rpc;

import com.lk.aizerocodeplatform.model.dto.user.AddUserDTO;
import com.lk.aizerocodeplatform.model.dto.user.DeleteUserDTO;
import com.lk.aizerocodeplatform.model.dto.user.QueryUserDTO;
import com.lk.aizerocodeplatform.model.dto.user.UpdateUserDTO;
import com.lk.aizerocodeplatform.model.dto.user.UserRegisterDTO;
import com.lk.aizerocodeplatform.model.entity.User;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.model.vo.user.UserVO;
import com.mybatisflex.core.paginate.Page;

import java.util.Collection;
import java.util.List;

public interface UserDubboService {

    Long userRegister(UserRegisterDTO userRegisterDTO);

    Long saveUser(AddUserDTO addUserDTO);

    Boolean updateUser(UpdateUserDTO updateUserDTO);

    Boolean deleteUser(DeleteUserDTO deleteUserDTO);

    Page<UserVO> pageQuery(QueryUserDTO queryUserDTO);

    User getById(Long id);

    List<User> listByIds(Collection<Long> ids);

    UserLoginVO desensitizeUserInfo(User user);

    UserVO getUserVoByUser(User user);
}
