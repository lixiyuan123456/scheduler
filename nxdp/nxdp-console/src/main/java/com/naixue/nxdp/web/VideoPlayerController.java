package com.naixue.nxdp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/video-player")
public class VideoPlayerController {

    @RequestMapping("")
    public String videoPlayer() {
        return "video-player";
    }
}
