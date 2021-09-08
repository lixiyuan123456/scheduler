package com.naixue.nxdp.web;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.annotation.Admin;
import com.naixue.nxdp.model.Resignation;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.ResignationService;
import com.naixue.nxdp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/resignation")
public class ResignationController extends BaseController {

    @Autowired
    private ResignationService resignationService;

    @Autowired
    private UserService userService;

    @RequestMapping("")
    public Object console(HttpServletRequest request) {
        return "resignation/console";
    }

    @Admin
    @RequestMapping("/admin")
    public Object overview(HttpServletRequest request) {
        return "resignation/overview";
    }

    @RequestMapping("/resign.do")
    @ResponseBody
    public Object resign(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        resignationService.resign(currentUser.getId());
        return success();
    }

    @RequestMapping("/list.do")
    @ResponseBody
    public Object list(
            HttpServletRequest request,
            DataTableRequest<Resignation> dataTable,
            @RequestParam(required = false) boolean isAdmin) {
        Resignation condition = new Resignation();
        if (!isAdmin) {
            User currentUser = getCurrentUser(request);
            condition.setHolder(currentUser.getId());
        } else {
            String searchValue = dataTable.getSearch().get(DataTableRequest.Search.value.toString());
            User searchUser = userService.getUserByUserName(searchValue);
            if (searchUser != null) {
                searchValue = searchUser.getId();
            }
            condition.setHolder(searchValue);
        }
        Page<Resignation> page =
                resignationService.page(
                        condition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<Resignation>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @RequestMapping("/transferJobs.do")
    @ResponseBody
    public Object transferJobs(HttpServletRequest request, @RequestBody Resignation[] resignations) {
        User currentUser = getCurrentUser(request);
        resignationService.transferJobs(currentUser, Arrays.asList(resignations));
        return success();
    }
}
