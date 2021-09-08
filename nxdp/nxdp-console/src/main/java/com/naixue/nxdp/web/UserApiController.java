package com.naixue.nxdp.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.annotation.Admin;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naixue.nxdp.dao.UserRepository;

@RestController
@RequestMapping("/user/api")
public class UserApiController extends BaseController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @RequestMapping("/list-user")
    public Object queryUserList() {
        // 获取用户列表
        List<User> userList = userRepository.findAll();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", "ok");
        resultMap.put("userList", userList);
        return resultMap;
    }

    @Admin
    @RequestMapping("/search-users")
    public Object listUsers(DataTableRequest dataTable) {
        Page<User> page =
                userService.listUsers(dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<User>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @Admin
    @RequestMapping("/delete-user")
    public Object deleteUserByUserId(HttpServletRequest request, String userId) {
        userService.deleteUserByUserId(userId);
        return success();
    }

    @RequestMapping("/getRealTimeUser.do")
    public Object getUserInRealTime(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        User user = userService.getUserByUserId(currentUser.getId(), false);
        return success(user);
    }
}
