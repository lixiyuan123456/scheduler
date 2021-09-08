package com.naixue.nxdp.web;

import com.naixue.nxdp.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

    @Autowired
    private AccountService accountService;

    @Scheduled(cron = "0 0 0 * * *")
    @RequestMapping("/sync-accounts.do")
    @ResponseBody
    public Object syncAccounts() throws Exception {
        // accountService.syncAllDepts();
        accountService.syncAccounts();
        // accountService.syncDefaultAccount2User();
        return success();
    }
}
